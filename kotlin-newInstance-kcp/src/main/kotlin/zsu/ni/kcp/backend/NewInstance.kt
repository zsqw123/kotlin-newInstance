package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.jvm.codegen.BlockInfo
import org.jetbrains.kotlin.backend.jvm.codegen.ExpressionCodegen
import org.jetbrains.kotlin.backend.jvm.codegen.PromisedValue
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.codegen.inline.ReificationArgument
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner.Companion.pluginIntrinsicsMarkerMethod
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner.Companion.pluginIntrinsicsMarkerOwner
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner.Companion.pluginIntrinsicsMarkerSignature
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

object NewInstance : IntrinsicMethod() {
    override fun invoke(
        expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo,
    ): PromisedValue? {
        val type = expression.getTypeArgument(0)!!
        val mv = codegen.mv
        val reifiedArgument = with(codegen.typeSystem) {
            val typeParameter = type.typeConstructor().getTypeParameterClassifier()
                ?: return super.invoke(expression, codegen, data)
            ReificationArgument(typeParameter.getName().asString(), type.isMarkedNullable(), 0)
        }
        // fake to kotlin typeOf operation
        ReifiedTypeInliner.putReifiedOperationMarker(
            ReifiedTypeInliner.OperationKind.TYPE_OF, reifiedArgument, mv
        )
        mv.aconst(null)
        mv.markPluginGenerated()
        return with(codegen) {
            expression.onStack
        }
    }

    // we must use this operation to mark this reified operation will be proceeded by our plugin.
    // Reference from kotlinx-serialization's `serializer` inline
    // https://github.com/JetBrains/kotlin/blob/master/plugins/kotlinx-serialization/kotlinx-serialization.backend/src/org/jetbrains/kotlinx/serialization/compiler/backend/ir/SerializationJvmIrIntrinsicSupport.kt
    private fun InstructionAdapter.markPluginGenerated() {
        aconst("zsu.ni.NewInstance")
        invokestatic(
            pluginIntrinsicsMarkerOwner,
            pluginIntrinsicsMarkerMethod,
            pluginIntrinsicsMarkerSignature,
            false
        )
    }

    internal const val MAGIC_LDC = "zsu.ni.NewInstance"
}

