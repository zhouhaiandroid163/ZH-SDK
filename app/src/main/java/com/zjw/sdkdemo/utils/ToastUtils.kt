package com.zjw.sdkdemo.utils

import android.widget.Toast
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.ThreadUtils
import com.zjw.sdkdemo.base.BaseApplication
import java.lang.ref.WeakReference
import com.blankj.utilcode.util.ToastUtils as BlankjToastUtils

object ToastUtils {
    private val contextRef = WeakReference(BaseApplication.mContext.applicationContext)

    @JvmStatic
    fun showToast(resId: Int) {
        ThreadUtils.runOnUiThread {
            contextRef.get()?.let {
                showToast(it.getText(resId), Toast.LENGTH_SHORT)
            }
        }
    }

    @JvmStatic
    fun showToast(msg: String) {
        ThreadUtils.runOnUiThread {
            showToast(msg, Toast.LENGTH_SHORT)
        }
    }

    @JvmStatic
    fun showToast(strCharSequence: CharSequence, duration: Int) {
        // 后台不弹toast
        if (!AppUtils.isAppForeground()) return
        contextRef.get()?.let { context ->
            if (duration == Toast.LENGTH_SHORT) {
                BlankjToastUtils.showShort(strCharSequence)
            } else {
                BlankjToastUtils.showLong(strCharSequence)
            }
        }
    }
}
