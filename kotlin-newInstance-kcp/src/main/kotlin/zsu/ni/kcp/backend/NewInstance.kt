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
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import zsu.ni.kcp.TAG
import zsu.ni.kcp.log

object NewInstance : IntrinsicMethod() {
    override fun invoke(
        expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo,
    ): PromisedValue {
        val type = expression.getTypeArgument(0)!!
        val payloadType = expression.getTypeArgument(1)
        if (payloadType != null) {
            insertInlineMarker(codegen, payloadType, MAGIC_PAYLOAD_LDC)
        }
        insertInlineMarker(codegen, type, MAGIC_NI_LDC)
        return with(codegen) { expression.onStack }
    }

    private fun insertInlineMarker(
        codegen: ExpressionCodegen, parameterType: IrType, magic: String
    ) {
        val mv = codegen.mv
        val reifiedArgument = with(codegen.typeSystem) {
            val typeParameterClassifier = parameterType.typeConstructor().getTypeParameterClassifier()
            val typeParameterName = requireNotNull(typeParameterClassifier?.getName()) {
                "$TAG cannot get parameter classifier name for ${parameterType.log()}"
            }
            ReificationArgument(typeParameterName.asString(), parameterType.isMarkedNullable(), 0)
        }
        // fake to kotlin typeOf operation
        ReifiedTypeInliner.putReifiedOperationMarker(
            ReifiedTypeInliner.OperationKind.TYPE_OF, reifiedArgument, codegen.mv
        )
        mv.aconst(null)
        mv.markPluginGenerated(magic)
    }

    // we must use this operation to mark this reified operation will be proceeded by our plugin.
    // Reference from kotlinx-serialization's `serializer` inline
    // https://github.com/JetBrains/kotlin/blob/master/plugins/kotlinx-serialization/kotlinx-serialization.backend/src/org/jetbrains/kotlinx/serialization/compiler/backend/ir/SerializationJvmIrIntrinsicSupport.kt
    private fun InstructionAdapter.markPluginGenerated(magic: String) {
        aconst(magic)
        invokestatic(
            pluginIntrinsicsMarkerOwner,
            pluginIntrinsicsMarkerMethod,
            pluginIntrinsicsMarkerSignature,
            false
        )
    }

    internal const val MAGIC_NI_LDC = "zsu.ni"
    internal const val MAGIC_PAYLOAD_LDC = "zsu.ni.payload"
}

