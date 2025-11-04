package com.zjw.sdkdemo.utils

import android.content.Context
import android.text.TextUtils
import com.zhapp.ble.callback.DiyWatchFaceCallBack
import com.zjw.sdkdemo.R

object DiyUtils {

    /**
     * 获取位置昵称
     */
    fun getFunctionsLocationNameByType(context: Context, type: Int): String {
        return when (type) {
            DiyWatchFaceCallBack.DiyWatchFaceLocation.MIDDLE_UP.location -> context.getString(R.string.middle_up)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.LEFT_MIDDLE.location -> context.getString(R.string.left_middle)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.RIGHT_MIDDLE.location -> context.getString(R.string.right_middle)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.MIDDLE_DOWN.location -> context.getString(R.string.middle_down)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.LEFT_UP.location -> context.getString(R.string.left_up)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.RIGHT_UP.location -> context.getString(R.string.right_up)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.MIDDLE.location -> context.getString(R.string.middle)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.LEFT_DOWN.location -> context.getString(R.string.left_down)
            DiyWatchFaceCallBack.DiyWatchFaceLocation.RIGHT_DOWN.location -> context.getString(R.string.right_down)
            else -> ""
        }
    }

    /**
     * 获取复杂功能昵称
     */
    fun getFunctionsDetailNameByType(context: Context, type: Int): String {
        return when (type) {
            DiyWatchFaceCallBack.DiyWatchFaceFunction.STEP.function -> context.getString(R.string.step)
            DiyWatchFaceCallBack.DiyWatchFaceFunction.BATTERY.function -> context.getString(R.string.power)
            DiyWatchFaceCallBack.DiyWatchFaceFunction.CALORIE.function -> context.getString(R.string.calorie)
            DiyWatchFaceCallBack.DiyWatchFaceFunction.GENERAL_DATE.function -> context.getString(R.string.ordinary_date)
            DiyWatchFaceCallBack.DiyWatchFaceFunction.HEART_RATE.function -> context.getString(R.string.heart)
            DiyWatchFaceCallBack.DiyWatchFaceFunction.OFF.function -> context.getString(R.string.close)
            else -> ""
        }
    }

    /**
     * 根据复杂功能类型名获取类型
     */
    fun getDiyWatchFaceFunctionByTypeName(typeName: String): DiyWatchFaceCallBack.DiyWatchFaceFunction {
        var function = DiyWatchFaceCallBack.DiyWatchFaceFunction.BATTERY
        val fStr: String = typeName.uppercase()
        if (TextUtils.equals(fStr, "Kwh".uppercase())) {
            function = DiyWatchFaceCallBack.DiyWatchFaceFunction.BATTERY
        } else if (TextUtils.equals(fStr, "GeneralDate".uppercase())) {
            function = DiyWatchFaceCallBack.DiyWatchFaceFunction.GENERAL_DATE
        } else if (TextUtils.equals(fStr, "Step".uppercase())) {
            function = DiyWatchFaceCallBack.DiyWatchFaceFunction.STEP
        } else if (TextUtils.equals(fStr, "HeartRate".uppercase())) {
            function = DiyWatchFaceCallBack.DiyWatchFaceFunction.HEART_RATE
        } else if (TextUtils.equals(fStr, "Calorie".uppercase())) {
            function = DiyWatchFaceCallBack.DiyWatchFaceFunction.CALORIE
        }
        return function
    }
}