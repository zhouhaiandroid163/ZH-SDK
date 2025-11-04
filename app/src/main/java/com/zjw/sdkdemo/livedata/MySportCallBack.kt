package com.zjw.sdkdemo.livedata

import com.zhapp.ble.bean.DevSportInfoBean
import com.zhapp.ble.bean.SportRequestBean
import com.zhapp.ble.bean.SportStatusBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.SportCallBack

object MySportCallBack {
    //运动数据
    val onDevSportInfo = UnFlawedLiveData<DevSportInfoBean>()

    //运动状态
    val onSportStatus = UnFlawedLiveData<SportStatusBean>()

    //运动状态上报
    val onSportRequest = UnFlawedLiveData<SportRequestBean>()

    fun initMySportCallBack() {

        CallBackUtils.sportCallBack = object : SportCallBack {
            override fun onDevSportInfo(bean: DevSportInfoBean) {
                onDevSportInfo.postValue(bean)
            }

            override fun onSportStatus(bean: SportStatusBean) {
                onSportStatus.postValue(bean)
            }

            override fun onSportRequest(bean: SportRequestBean) {
                onSportRequest.postValue(bean)
            }
        }
    }
}