package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.codegen.ClassCodegen
import org.jetbrains.kotlin.backend.jvm.codegen.JvmIrIntrinsicExtension
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.codegen.inline.newMethodNodeWithCorrectStackSize
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isSubtypeOf
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode.Companion.DEFAULT_UAST
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.InsnList
import org.jetbrains.org.objectweb.asm.tree.LdcInsnNode
import zsu.ni.kcp.TAG
import zsu.ni.kcp.log

@OptIn(UnsafeDuringIrConstructionAPI::class)
class NewInstanceIntrinsic(
    private val backendContext: JvmBackendContext,
) : JvmIrIntrinsicExtension {
    private val typeSystem = backendContext.typeSystem

    /** maybe one day we can get [ClassCodegen]'s mapper is better :) */
    private val typeMapper = backendContext.defaultTypeMapper

    override fun getIntrinsic(symbol: IrFunctionSymbol): IntrinsicMethod? {
        val originFunction = symbol.owner
        val name = originFunction.name.asString()
        if (name != "newInstance") return null
        val packageName = originFunction.parent as? IrClass ?: return null
        if (packageName.packageFqName?.asString() != "zsu.ni") return null
        return NewInstance
    }

    /**
     * ----------- payload part
     * iconst(6) // operationType=6 typeOf
     * aconst(T) // typeParamName=P
     * invokestatic(kotlin/jvm/internal/Intrinsics.reifiedOperationMarker)
     * aconst(null) <----- will insert real payload type here
     * aconst [NewInstance.MAGIC_PAYLOAD_LDC]
     * call plugin magic marker
     * ----------- ni part
     * iconst(6) // operationType=6 typeOf
     * aconst(T) // typeParamName=T
     * invokestatic(kotlin/jvm/internal/Intrinsics.reifiedOperationMarker)
     * aconst(null)
     * aconst [NewInstance.MAGIC_NI_LDC]
     * call plugin magic marker
     */
    override fun rewritePluginDefinedOperationMarker(
        v: InstructionAdapter, reifiedInsn: AbstractInsnNode, instructions: InsnList, type: IrType
    ): Boolean {
        val isPayload = reifiedInsn.isPayload()
        val isNewInstance = reifiedInsn.isNewInstance()
        if (!isPayload && !isNewInstance) return false

        val ldcMagicConstNode = reifiedInsn.next
        val magicCallNode = ldcMagicConstNode.next

        if (isPayload) {
            val payloadTypeLdc = LdcInsnNode(type)
            instructions.insert(reifiedInsn, payloadTypeLdc)
        } else { // isNewInstance
            val realPayloadType = reifiedInsn.popPayloadType(instructions)
            val constructorDescriptor = findProperConstructor(type, realPayloadType)
            val newMethod = writeNewInstanceCodeIntoNewMethod(type, constructorDescriptor, realPayloadType)
            instructions.insert(reifiedInsn, newMethod.instructions)
        }

        // remove reifiedInsn & ldc & magic call
        instructions.remove(reifiedInsn)
        instructions.remove(ldcMagicConstNode)
        instructions.remove(magicCallNode)
        return true
    }


    private fun writeNewInstanceCodeIntoNewMethod(
        type: IrType, constructorDescriptor: String, payloadType: IrType?,
    ) = newMethodNodeWithCorrectStackSize { mv ->
        val mapper = backendContext.defaultTypeMapper
        val asmType = mapper.mapType(type, DEFAULT_UAST)
        mv.anew(asmType)
        mv.dup()
        if (payloadType != null) {
            val payloadAsmType = mapper.mapType(payloadType, DEFAULT_UAST)
            mv.load(0, payloadAsmType)
        }
        mv.invokespecial(asmType.internalName, "<init>", constructorDescriptor, false)
    }

    private fun findProperConstructor(
        type: IrType, realPayloadType: IrType?,
    ): String {
        val expectedSize = if (realPayloadType == null) 0 else 1

        val classDeclaration = requireNotNull(type.classOrNull?.owner) {
            "$TAG Cannot find correspond class for ${type.log()}"
        }
        val constructors = classDeclaration.constructors

        // basic matching logic, didn't support conflict overload currently.
        fun IrConstructor.match(): Boolean {
            val constructorValueParameters = valueParameters
            val valueParameterSize = constructorValueParameters.size
            if (valueParameterSize != expectedSize) return false
            // no argument will not get conflicted, early returns it.
            if (realPayloadType == null) return true

            val constructorParameterType = constructorValueParameters.first().type
            if (realPayloadType == constructorParameterType) return true
            if (realPayloadType.isSubtypeOf(constructorParameterType, typeSystem)) return true
            return false
        }

        val matchedConstructors = constructors.filter(IrConstructor::match).toList()

        require(matchedConstructors.isNotEmpty()) {
            val inputTypeLog = if (realPayloadType == null) "" else
                "Input parameter types: ${realPayloadType.log()}"
            "$TAG Cannot find matched constructor for ${classDeclaration.name}. " + inputTypeLog
        }
        require(matchedConstructors.size == 1) {
            val inputTypeLog = if (realPayloadType == null) "" else
                "Input parameter types: ${realPayloadType.log()}\n"
            val matchedTypes = matchedConstructors.map { it.valueParameters.first().type }
            val conflictMessage = matchedTypes.joinToString(
                prefix = "Conflict constructor payloads: [", postfix = "]"
            ) { it.log() }
            "$TAG Multiple conflict constructors are matched for creating ${classDeclaration.name} instance. \n" +
                    inputTypeLog + conflictMessage
        }

        return createDescriptorForConstructor(matchedConstructors.first())
    }

    private fun createDescriptorForConstructor(constructor: IrConstructor): String {
        val argumentTypes = constructor.valueParameters
            .map { typeMapper.mapType(it.type, DEFAULT_UAST) }.toTypedArray()
        return Type.getMethodDescriptor(Type.VOID_TYPE, *argumentTypes)
    }
}


/** @receiver reifiedInsn */
private fun AbstractInsnNode.isPayload(): Boolean {
    return (next as? LdcInsnNode)?.cst == NewInstance.MAGIC_PAYLOAD_LDC
}

/** @receiver reifiedInsn */
private fun AbstractInsnNode.isNewInstance(): Boolean {
    return (next as? LdcInsnNode)?.cst == NewInstance.MAGIC_NI_LDC
}

private fun AbstractInsnNode.popPayloadType(instructions: InsnList): IrType? {
    val payloadTypeNode = previous?.previous?.previous?.previous as? LdcInsnNode ?: return null
    val payloadType = payloadTypeNode.cst as? IrType ?: return null
    instructions.remove(payloadTypeNode) // pop payload node
    return payloadType
}

