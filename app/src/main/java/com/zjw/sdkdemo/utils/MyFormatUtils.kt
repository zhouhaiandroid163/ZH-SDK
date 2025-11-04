package com.zjw.sdkdemo.utils

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.TimeUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object MyFormatUtils {
    @JvmStatic
    fun formatJson(unformattedJson: String): String? {
        try {
            if (unformattedJson.trim { it <= ' ' }.startsWith("{")) {
                val jsonObject = JSONObject(unformattedJson)
                return jsonObject.toString(4)
            } else if (unformattedJson.trim { it <= ' ' }.startsWith("[")) {
                val jsonArray = JSONArray(unformattedJson)
                return jsonArray.toString(4)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return unformattedJson
    }

    @JvmStatic
    fun format(mObject: Any): String? {
        return formatJson(GsonUtils.toJson(mObject))
    }

    fun getTime():String{
        return TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyy-MM-dd HH:mm:ss.SSS"))
    }
}