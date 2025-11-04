package com.zjw.sdkdemo.livedata

import android.util.Log
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.WidgetBean
import com.zhapp.ble.callback.BerryDevReqContactCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.MicroCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.utils.FindPhoneUtils

object MyMicroCallBack {
    private val tag: String = MicroCallBack::class.java.simpleName

    // 设备应用列表
    val onApplicationList = UnFlawedLiveData<MutableList<WidgetBean>>()

    // 设备运动排序列表
    val onSportWidgetSortList = UnFlawedLiveData<MutableList<WidgetBean>>()

    // 设备直达卡片列表
    val onWidgetList = UnFlawedLiveData<MutableList<WidgetBean>>()

    // 戒指NFC睡眠错误信息
    val onNfcSleepErr = UnFlawedLiveData<Int>()
    
    // 戒指佩戴状态
    val onRingWearingStatus = UnFlawedLiveData<Int>()

    fun initMyMicroCallBack() {
        CallBackUtils.microCallBack = object : MicroCallBack {
            override fun onWearSendFindPhone(mode: Int) {
                Log.w(tag, "onWearSendFindPhone mode=$mode")
                FindPhoneUtils.findPhone(mode)
            }

            override fun onPhotograph(status: Int) {
                Log.i(tag, "onPhotograph status=$status")
            }

            override fun onWidgetList(list: MutableList<WidgetBean>) {
                onWidgetList.postValue(list)
            }

            override fun onApplicationList(list: MutableList<WidgetBean>) {
                onApplicationList.postValue(list)
            }

            override fun onSportTypeIconList(list: MutableList<WidgetBean?>?) {
            }

            override fun onSportTypeOtherList(list: MutableList<WidgetBean?>?) {
            }

            override fun onQuickWidgetList(list: MutableList<WidgetBean?>?) {
            }

            override fun onSportWidgetSortList(list: MutableList<WidgetBean>) {
                onSportWidgetSortList.postValue(list)
            }

            override fun onNfcSleepErr(error: Int) {
                onNfcSleepErr.postValue(error)
            }

            override fun onRingWearingStatus(status: Int) {
                onRingWearingStatus.postValue(status)
            }
        }

        CallBackUtils.berryDevReqContactCallBack = BerryDevReqContactCallBack { phoneNumber ->
            val contactName = "test name"
            ControlBleTools.getInstance().updateBerryContactInfo(contactName, phoneNumber, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    Log.i(tag, "updateBerryContactInfo state=$state")
                }
            })
        }

    }

}