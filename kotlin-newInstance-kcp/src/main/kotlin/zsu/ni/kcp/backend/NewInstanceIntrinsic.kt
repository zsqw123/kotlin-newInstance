package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.common.extensions.IrIntrinsicExtension
import org.jetbrains.kotlin.backend.jvm.codegen.JvmIrIntrinsicExtension
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.InsnList

object NewInstanceIntrinsic: JvmIrIntrinsicExtension {
    override fun getIntrinsic(symbol: IrFunctionSymbol): IntrinsicMethod? {
        TODO("Not yet implemented")
    }

    override fun rewritePluginDefinedOperationMarker(
        v: InstructionAdapter,
        reifiedInsn: AbstractInsnNode,
        instructions: InsnList,
        type: IrType
    ): Boolean {
        TODO("Not yet implemented")
    }
}