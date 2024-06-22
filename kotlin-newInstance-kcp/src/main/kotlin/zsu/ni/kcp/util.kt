package zsu.ni.kcp

import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

internal const val TAG = "[NewInstancePlugin]"

internal fun IrType.log() = dumpKotlinLike()

internal fun List<IrType>.log() = map { it.dumpKotlinLike() }

internal fun InstructionAdapter.loadWithAutoUnboxing(
    index: Int, boxedType: Type, expectedType: Type,
) {
    when (expectedType.sort) {
        Type.OBJECT, Type.VOID, Type.ARRAY -> return
    }
    val primitiveType = PrimitiveType.fromSort(expectedType.sort)
    load(index, boxedType)
    invokevirtual(
        boxedType.internalName, primitiveType.unboxMethodName,
        primitiveType.unboxDescriptor, false
    )
}

private enum class PrimitiveType(
    val sort: Int, val unboxMethodName: String, val unboxDescriptor: String,
) {
    Z(Type.BOOLEAN, "booleanValue", "()Z"),
    C(Type.CHAR, "charValue", "()C"),
    B(Type.BYTE, "byteValue", "()B"),
    S(Type.SHORT, "shortValue", "()S"),
    I(Type.INT, "intValue", "()I"),
    F(Type.FLOAT, "floatValue", "()F"),
    J(Type.LONG, "longValue", "()J"),
    D(Type.DOUBLE, "doubleValue", "()D"),
    ;

    companion object {
        fun fromSort(sort: Int) = entries.first { it.sort == sort }
    }
}

private fun obj(internalName: String) = Type.getObjectType(internalName)

private val primitiveTypes = arrayOf(
    "V",
    "Z",
    "C",
    "B",
    "S",
    "I",
    "F",
    "J",
    "D",
)
