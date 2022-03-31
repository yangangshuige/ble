package androidx.fragment.app

import android.widget.Toast
import com.example.test.widget.LoadingDialogHelper

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   4/25/21
 * Fragment扩展
 */
fun Fragment.showLoading(msg: String = "正在加载") {
    LoadingDialogHelper.showLoading(this.context, msg)
}

fun Fragment.hideLoading() {
    LoadingDialogHelper.hideLoading(this.context)
}

fun Fragment.showToast(msg: String) {
    Toast.makeText(requireContext(),
        msg,
        Toast.LENGTH_SHORT).show()
}

fun Fragment.showToast(msg: Int) {
    Toast.makeText(requireContext(),
        getString(msg),
        Toast.LENGTH_SHORT).show()
}
