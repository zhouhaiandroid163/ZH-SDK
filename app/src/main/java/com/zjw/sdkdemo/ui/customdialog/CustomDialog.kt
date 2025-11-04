package com.zjw.sdkdemo.ui.customdialog

import android.app.Activity
import android.content.Context

/**
 * <pre>
 * dialog构造提供类
 * </pre>
 */
object CustomDialog {

    @JvmStatic
    fun builder(context: Context?) = getBuilder(context)

    @JvmStatic
    fun builder(activity: Activity?) = getBuilder(activity)

    private fun getBuilder(context: Context?): AbsDialogBuilder {
        if (context == null) {
            throw NullPointerException("context cannot be NULL")
        }
        return DialogBuilder(context).builder()
    }

}