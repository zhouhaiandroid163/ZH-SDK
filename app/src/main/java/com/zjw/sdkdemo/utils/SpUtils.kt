package com.zjw.sdkdemo.utils

import android.content.Context
import androidx.core.content.edit
import com.zjw.sdkdemo.base.BaseApplication

object SpUtils {
    private const val SHARED_PREFERENCES = "demo_zh_bracelet_device_tools"

    @JvmStatic
    fun setValue(key: String, value: String) {
        val sharedPreferences = BaseApplication.mContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit { putString(key, value) }
    }

    @JvmStatic
    fun getValue(key: String, defValue: String): String {
        val sharedPreferences = BaseApplication.mContext.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE)
        return sharedPreferences?.getString(key, defValue).toString()
    }

    @JvmStatic
    fun setSearchName(str: String) {
        setValue("search_name", str)
    }

    @JvmStatic
    fun getSearchName(): String = getValue("search_name", "")


    @JvmStatic
    fun setBrDeviceName(str: String) {
        setValue("device_br_name", str)
    }

    @JvmStatic
    fun getBrDeviceName(): String = getValue("device_br_name", "")

    @JvmStatic
    fun setBrDeviceMac(str: String) {
        setValue("device_br_mac", str)
    }

    @JvmStatic
    fun getBrDeviceMac(): String = getValue("device_br_mac", "")

    @JvmStatic
    fun setLogUserID(str: String) {
        setValue("log_user_id", str)
    }

    @JvmStatic
    fun getLogUserID(): String = getValue("log_user_id", "")
//    fun getLogUserID(): String = "e8aa4cf6"

}
