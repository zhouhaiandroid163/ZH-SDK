package com.zjw.sdkdemo.base

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ProcessUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.utils.SaveLog
import com.zjw.sdkdemo.livedata.DeviceLiveData

@SuppressLint("StaticFieldLeak")
class BaseApplication : Application() {
    companion object {

        lateinit var application: BaseApplication
        @JvmStatic
        lateinit var mContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        val name = ProcessUtils.getCurrentProcessName()
        if (packageName == name) {
            application = this
            mContext = applicationContext
        }
        initCallBack()
        initData()
    }

    private fun initCallBack() {
        ControlBleTools.getInstance().setInitStatusCallBack {
            LogUtils.d("sdk init complete")
        }
    }
    private fun initData() {
        ControlBleTools.getInstance().init(this)
        DeviceLiveData.initCallBack()
        SaveLog.init(this)
    }

}