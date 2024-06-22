package zsu.ni.kcp.backend

import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression

internal class NewInstancePreProcessedValue(
    val originFunctionCall: IrFunctionAccessExpression,
)
