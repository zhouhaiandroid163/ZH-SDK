package com.zjw.sdkdemo.ui.customdialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class MyDialog : Dialog {

    private lateinit var mContext: Context

    constructor(context: Context) : super(context) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
    }

    override fun show() {
        if (mContext is Activity && !(this.mContext as Activity).isDestroyed) {
            super.show()
            mOnShowDismissListener?.onShow()
        }
    }

    override fun dismiss() {
        val view: View? = currentFocus
        val mInputMethodManager: InputMethodManager = mContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mInputMethodManager.hideSoftInputFromWindow(view?.windowToken, InputMethodManager.HIDE_IMPLICIT_ONLY)
        super.dismiss()
        mOnShowDismissListener?.onDismiss()
    }

    private var mOnShowDismissListener: OnShowDismissListener? = null

    fun setOnShowDismissListener(onShowListener: OnShowDismissListener) {
        this.mOnShowDismissListener = onShowListener
    }

    interface OnShowDismissListener {
        fun onShow()
        fun onDismiss()
    }

}