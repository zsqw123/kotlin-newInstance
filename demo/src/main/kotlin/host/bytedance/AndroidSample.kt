package host.bytedance

import zsu.ni.newInstance
import kotlin.properties.ReadOnlyProperty

abstract class ViewModel
abstract class Activity
abstract class Fragment

class FooViewModel : ViewModel()

class FooFragment : Fragment()

inline fun <reified T : ViewModel> viewModel(): ReadOnlyProperty<Activity, T> {
    val factory = { newInstance<T>() }
    return ReadOnlyProperty { _, _ -> factory() }
}

inline fun <reified T : Fragment> fragment(): ReadOnlyProperty<Activity, T> {
    val factory = { newInstance<T>() }
    return ReadOnlyProperty { _, _ -> factory() }
}

class MyActivity : Activity() {
    val fooViewModel: FooViewModel by viewModel()
    val fooFragment: FooFragment by fragment()
}

fun main() {
    val activity = MyActivity()
    println(activity.fooViewModel)
    println(activity.fooFragment)
}
