package zsu.ni.kcp.backend

import org.jetbrains.kotlin.backend.jvm.codegen.BlockInfo
import org.jetbrains.kotlin.backend.jvm.codegen.ExpressionCodegen
import org.jetbrains.kotlin.backend.jvm.codegen.PromisedValue
import org.jetbrains.kotlin.backend.jvm.intrinsics.IntrinsicMethod
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression


object NewInstance : IntrinsicMethod() {
    override fun invoke(
        expression: IrFunctionAccessExpression, codegen: ExpressionCodegen, data: BlockInfo,
    ): PromisedValue? {
        val type = expression.getTypeArgument(0)!!

        return super.invoke(expression, codegen, data)
    }
}