package com.zjw.sdkdemo.livedata

import android.text.TextUtils
import android.util.Log
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.BehaviorLogCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceLogCallBack
import com.zjw.sdkdemo.utils.MySaveLog

object DeviceLiveData {

    fun initCallBack() {

        // 设置相关回调 Set relevant callbacks
        MySettingMenuCallBack.initMySettingMenuCallBack()

        // 小功能综合相关回调 Comprehensive callback related to minor functions
        MyMicroCallBack.initMyMicroCallBack()

        // 设备多运动相关回调 Device motion-related callbacks
        MySportCallBack.initMySportCallBack()

        // 儿童相关功能回调 Callback for child-related functions
        MyChildrenCallBack.initMyChildrenCallBack()


        // 设备日志 Device log
        ControlBleTools.getInstance().deviceLogCallBack = object : DeviceLogCallBack {
            override fun onLogI(mode: String?, tag: String, msg: String) {
                if (TextUtils.isEmpty(mode)) {
                    Log.i(tag, msg)
                    msg.let { tag.let { it1 -> MySaveLog.writeFile("I",it1, it) } }
                } else {
                    val str = StringBuilder()
                        .append("<").append(mode).append(">").append("  ").append(msg)
                        .toString()
                    Log.i(tag, str)
                    msg.let { tag.let { it1 -> MySaveLog.writeFile("I",it1, str) } }
                }
            }

            override fun onLogV(mode: String, tag: String?, msg: String) {
                if (TextUtils.isEmpty(mode)) {
                    Log.v(tag, msg)
                    msg.let { tag?.let { it1 -> MySaveLog.writeFile("V",it1, it) } }
                } else {
                    val str = StringBuilder()
                        .append("<").append(mode).append(">").append("  ").append(msg)
                        .toString()
                    Log.v(tag, str)
                    msg.let { tag?.let { it1 -> MySaveLog.writeFile("V",it1, str) } }
                }
            }

            override fun onLogE(mode: String, tag: String?, msg: String) {
                if (TextUtils.isEmpty(mode)) {
                    Log.e(tag, msg)
                    msg.let { tag?.let { it1 -> MySaveLog.writeFile("E",it1, it) } }
                } else {
                    val str = StringBuilder()
                        .append("<").append(mode).append(">").append("  ").append(msg)
                        .toString()
                    Log.e(tag, str)
                    msg.let { tag?.let { it1 -> MySaveLog.writeFile("E",it1, str) } }
                }
            }

            override fun onLogD(mode: String?, tag: String, msg: String) {
                if (TextUtils.isEmpty(mode)) {
                    Log.d(tag, msg)
                    msg.let { tag.let { it1 -> MySaveLog.writeFile("D",it1, it) } }
                } else {
                    val str = StringBuilder()
                        .append("<").append(mode).append(">").append("  ").append(msg)
                        .toString()
                    Log.d(tag, str)
                    msg.let { tag.let { it1 -> MySaveLog.writeFile("D",it1, str) } }
                }
            }

            override fun onLogW(mode: String?, tag: String?, msg: String) {
                if (TextUtils.isEmpty(mode)) {
                    Log.w(tag, msg)
                    msg.let { tag?.let { it1 -> MySaveLog.writeFile("W",it1, it) } }
                } else {
                    val str = StringBuilder()
                        .append("<").append(mode).append(">").append("  ").append(msg)
                        .toString()
                    Log.w(tag, str)
                    msg.let { tag?.let { it1 -> MySaveLog.writeFile("W",it1, str) } }
                }
            }
        }

        //行为日志 behavior log
        CallBackUtils.behaviorLogCallBack = BehaviorLogCallBack { module, tag, msg -> Log.d(module, "$tag ---> $msg") }

    }
}