package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.codegen.ClassCodegen
import org.jetbrains.kotlin.backend.jvm.codegen.JvmIrIntrinsicExtension
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.codegen.inline.newMethodNodeWithCorrectStackSize
import org.jetbrains.kotlin.ir.backend.js.utils.valueArguments
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
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

    override fun rewritePluginDefinedOperationMarker(
        v: InstructionAdapter, reifiedInsn: AbstractInsnNode, instructions: InsnList, type: IrType
    ): Boolean {
        if (!reifiedInsn.isNewInstancePlugin()) return false
        val preProcessedValue = (reifiedInsn as LdcInsnNode).cst as NewInstancePreProcessedValue
        val originFunctionCall = preProcessedValue.originFunctionCall

        val ldcNode = reifiedInsn.next
        val magicCallNode = ldcNode.next

        val constructorDescriptor = findProperConstructor(type, originFunctionCall)
        val newMethod = writeNewInstanceCodeIntoNewMethod(type, constructorDescriptor)
        instructions.insert(reifiedInsn, newMethod.instructions)
        // remove reifiedInsn & ldc & magic call
        instructions.remove(reifiedInsn)
        instructions.remove(ldcNode)
        instructions.remove(magicCallNode)
        return true
    }

    /** @receiver reifiedInsn */
    private fun AbstractInsnNode.isNewInstancePlugin(): Boolean {
        return (next as? LdcInsnNode)?.cst == NewInstance.MAGIC_NI_LDC
    }

    private fun writeNewInstanceCodeIntoNewMethod(
        type: IrType, constructorDescriptor: String,
    ) = newMethodNodeWithCorrectStackSize { mv ->
        val mapper = backendContext.defaultTypeMapper
        val asmType = mapper.mapType(type, DEFAULT_UAST)
        mv.anew(asmType)
        mv.dup()
        mv.invokespecial(asmType.internalName, "<init>", constructorDescriptor, false)
    }

    private fun findProperConstructor(
        type: IrType, originFunctionCall: IrFunctionAccessExpression,
    ): String {
        val classDeclaration = requireNotNull(type.classOrNull?.owner) {
            "$TAG Cannot find correspond class for ${type.log()}"
        }
        val parameterTypes = originFunctionCall.valueArguments.mapIndexed { index, valueArgument ->
            requireNotNull(valueArgument) {
                val functionName = originFunctionCall.origin?.debugName
                "$TAG Value argument $index of function $functionName is null!"
            }
            valueArgument.type
        }
        val constructors = classDeclaration.constructors

        // basic matching logic, didn't support conflict overload currently.
        fun IrConstructor.match(): Boolean {
            val constructorValueParameters = valueParameters
            val valueParameterSize = constructorValueParameters.size
            if (valueParameterSize != parameterTypes.size) return false
            val constructorParameterTypes = constructorValueParameters.map { it.type }
            for ((i, constructorParameterType) in constructorParameterTypes.withIndex()) {
                val parameterType = parameterTypes[i]
                if (parameterType == constructorParameterType) return true
                if (parameterType.isSubtypeOf(constructorParameterType, typeSystem)) return true
            }
            return false
        }

        val matchedConstructors = constructors.filter(IrConstructor::match).toList()

        require(matchedConstructors.isNotEmpty()) {
            "$TAG Cannot find matched constructor for ${classDeclaration.name}. " +
                    "Input parameter types: ${parameterTypes.log()}"
        }
        require(matchedConstructors.size == 1) {
            "$TAG Multiple conflict constructors are matched for creating ${classDeclaration.name} instance. " +
                    "Input parameter types: ${parameterTypes.log()}"
        }

        return createDescriptorForConstructor(matchedConstructors.first())
    }

    private fun createDescriptorForConstructor(constructor: IrConstructor): String {
        val argumentTypes = constructor.valueParameters
            .map { typeMapper.mapType(it.type, DEFAULT_UAST) }.toTypedArray()
        return Type.getMethodDescriptor(Type.VOID_TYPE, *argumentTypes)
    }
}
