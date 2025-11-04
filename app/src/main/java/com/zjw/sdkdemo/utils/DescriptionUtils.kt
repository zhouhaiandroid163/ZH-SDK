package com.zjw.sdkdemo.utils

import android.content.Context
import com.zhapp.ble.BleCommonAttributes
import com.zjw.sdkdemo.R

object DescriptionUtils {
    /**
     * 获取连接状态字符串
     * Obtain the connection status string
     */
    @JvmStatic
    fun getConnectStateStr(context: Context, state: Int): String {
        return when (state) {
            BleCommonAttributes.STATE_DISCONNECTED -> context.getString(R.string.connect_state_0)
            BleCommonAttributes.STATE_CONNECTING-> context.getString(R.string.connect_state_1)
            BleCommonAttributes.STATE_CONNECTED -> context.getString(R.string.connect_state_2)
            BleCommonAttributes.STATE_TIME_OUT -> context.getString(R.string.connect_state_4)
            else -> context.getString(R.string.unknown)
        }
    }
}