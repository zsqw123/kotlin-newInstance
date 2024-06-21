package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.codegen.ClassCodegen
import org.jetbrains.kotlin.backend.jvm.codegen.JvmIrIntrinsicExtension
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.codegen.inline.newMethodNodeWithCorrectStackSize
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.InsnList
import org.jetbrains.org.objectweb.asm.tree.LdcInsnNode
import zsu.ni.kcp.TAG

@OptIn(UnsafeDuringIrConstructionAPI::class)
class NewInstanceIntrinsic(
    private val backendContext: JvmBackendContext,
) : JvmIrIntrinsicExtension {
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
        assertEmptyConstructor(type)

        val ldcNode = reifiedInsn.next
        val magicCallNode = ldcNode.next

        val newMethod = writeNewInstanceCodeIntoNewMethod(type)
        instructions.insert(reifiedInsn, newMethod.instructions)
        // remove reifiedInsn & ldc & magic call
        instructions.remove(reifiedInsn)
        instructions.remove(ldcNode)
        instructions.remove(magicCallNode)
        return true
    }

    /** @receiver reifiedInsn */
    private fun AbstractInsnNode.isNewInstancePlugin(): Boolean {
        return (next as? LdcInsnNode)?.cst == NewInstance.MAGIC_LDC
    }

    private fun assertEmptyConstructor(type: IrType) {
        /** maybe one day we can get [ClassCodegen]'s mapper is better :) */
        val classDeclaration = type.classOrNull?.owner ?: throw IllegalStateException(
            "$TAG Cannot find correspond class for $type"
        )
        val constructors = classDeclaration.constructors
        require(constructors.any { it.valueParameters.isEmpty() }) {
            "$TAG Cannot find 0 argument constructor for ${classDeclaration.name}"
        }
    }

    private fun writeNewInstanceCodeIntoNewMethod(
        type: IrType
    ) = newMethodNodeWithCorrectStackSize { mv ->
        val mapper = backendContext.defaultTypeMapper
        val asmType = mapper.mapType(type, TypeMappingMode.DEFAULT_UAST)
        mv.anew(asmType)
        mv.dup()
        mv.invokespecial(asmType.internalName, "<init>", "()V", false)
    }
}
