package androidx.fragment.app

import android.content.Context
import androidx.annotation.MainThread
import androidx.lifecycle.*
import com.example.test.biz.viewmodel.ProcessViewModel

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   4/1/21
 * App整个生命周期可用ViewModelStore实现，可以通过此获取生命周期中唯一的ViewModel
 * <p>
 *     ProcessViewModelStore
 */
class ProcessViewModelStoreOwner private constructor(): ViewModelStoreOwner {
    private val viewModelStore = ViewModelStore()
    override fun getViewModelStore(): ViewModelStore {
        return viewModelStore
    }

    companion object{
        private val instance = ProcessViewModelStoreOwner()

        /**
         * 获取此ViewModelStore实例
         */
        @JvmStatic
        fun get(): ProcessViewModelStoreOwner = instance

        /**
         * Java 使用获取进程唯一ViewModel
         */
        @JvmStatic
        fun <VM: ProcessViewModel> getProcessViewModel(vmClazz: Class<VM>): VM {
            val factory = ViewModelProvider.NewInstanceFactory()

            val store = instance.viewModelStore

            return ViewModelProvider(store, factory).get(vmClazz)
        }
    }
}

/**
 * 返回一个Lazy代理的进程级别唯一 [ViewModel],
 *
 * ```
 * class MyFragment : Fragment() {
 *     val viewmodel: MyViewModel by processViewModels()
 * }
 * ```
 *
 * 属性仅在Fragment attached后可以使用
 * [Fragment.onAttach()], 在此之前使用会抛出 IllegalArgumentException异常。
 */
@MainThread
inline fun <reified VM : ViewModel> Fragment.processViewModels()
        = createViewModelLazy(VM::class, { ProcessViewModelStoreOwner.get().viewModelStore })

/**
 * 返回一个Lazy代理的进程级别唯一 [ViewModel],
 *
 * ```
 * class MyActivity : FragmentActivity() {
 *     val viewmodel: MyViewModel by processViewModels()
 * }
 * ```
 *
 * 属性仅在Fragment attached后可以使用
 * [Fragment.onAttach()], 在此之前使用会抛出 IllegalArgumentException异常。
 */
@MainThread
inline fun <reified VM : ViewModel> FragmentActivity.processViewModels(): Lazy<VM> {
    val factoryPromise = {
        defaultViewModelProviderFactory
    }
        return ViewModelLazy(VM::class,
            {ProcessViewModelStoreOwner.get().viewModelStore}, factoryPromise)
}

/**
 * 返回一个Lazy代理的进程级别唯一 [ViewModel],
 *
 * ```
 *     val viewmodel: SomeProcessViewModel by context.processViewModels()
 * ```
 */
@MainThread
inline fun <reified VM : ViewModel> Context.processViewModels(): Lazy<VM> {
    val factoryPromise = {
        ViewModelProvider.NewInstanceFactory()
    }

    return ViewModelLazy(VM::class,
        {ProcessViewModelStoreOwner.get().viewModelStore}, factoryPromise)
}
