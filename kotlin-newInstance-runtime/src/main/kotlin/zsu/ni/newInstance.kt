package zsu.ni

inline fun <reified T> newInstance(): T {
    throw UnsupportedOperationException("This function is implemented as an intrinsic")
}

inline fun <reified T, reified P> newInstance(payload: P): T {
    throw UnsupportedOperationException("This function is implemented as an intrinsic")
}
