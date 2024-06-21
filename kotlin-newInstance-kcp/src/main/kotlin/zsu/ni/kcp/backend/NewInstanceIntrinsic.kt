package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.jvm.JvmFileFacadeClass
import org.jetbrains.kotlin.backend.jvm.codegen.JvmIrIntrinsicExtension
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.packageFqName
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.InsnList

object NewInstanceIntrinsic: JvmIrIntrinsicExtension {
    override fun getIntrinsic(symbol: IrFunctionSymbol): IntrinsicMethod? {
        @OptIn(UnsafeDuringIrConstructionAPI::class)
        val originFunction = symbol.owner
        val name = originFunction.name.asString()
        if (name != "newInstance") return null
        val packageName = originFunction.parent as? IrClass ?: return null
        if (packageName.packageFqName?.asString() != "zsu.ni") return null
        return NewInstance
    }

    override fun rewritePluginDefinedOperationMarker(
        v: InstructionAdapter,
        reifiedInsn: AbstractInsnNode,
        instructions: InsnList,
        type: IrType
    ): Boolean {

        return false
    }
}