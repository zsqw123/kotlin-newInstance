package zsu.ni.kcp

import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike

internal const val TAG = "[NewInstancePlugin]"

internal fun IrType.log() = dumpKotlinLike()

internal fun List<IrType>.log() = map { it.dumpKotlinLike() }
