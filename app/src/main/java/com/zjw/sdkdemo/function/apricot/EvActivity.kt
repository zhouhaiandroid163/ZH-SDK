package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.EvDataInfoBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.EVCarReqCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityEvBinding
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.utils.DialogUtils

class EvActivity : BaseActivity() {
    private val binding by lazy { ActivityEvBinding.inflate(layoutInflater) }
    private val tag: String = EvActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_ev)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
    }

    private fun initView() {
        selectTime(binding.tvTime)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnEvDataSet) {
            addLogI("btnEvDataSet")
            val bean = getEvDataInfoBean()
            addLogBean("setEvDataInfo", bean)
            ControlBleTools.getInstance().setEvDataInfo(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEvDataInfo state=$state")
                }
            })
        }
        clickCheckConnect(binding.btnEvRemindSet) {
            addLogI("btnEvRemindSet")
            val remind = binding.etEvRemind.text.toString().trim().toInt()
            addLogI("setEvRemindStatus remind=$remind")
            ControlBleTools.getInstance().setEvRemindStatus(remind, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEvRemindStatus state=$state")
                }
            })
        }
    }

    private fun initCallback() {
        CallBackUtils.evCarReqCallBack = EVCarReqCallBack {
            addLogI("evCarReqCallBack")
        }

        MySettingMenuCallBack.onEvDataInfo.observe(this) { bean ->
            addLogBean("MySettingMenuCallBack.onEvDataInfo", bean!!)
        }

        MySettingMenuCallBack.onEvRemindType.observe(this) { bean ->
            addLogBean("MySettingMenuCallBack.onEvRemindType", bean!!)
        }
    }

    private fun getEvDataInfoBean(): EvDataInfoBean {
        val evDataInfo = EvDataInfoBean()
        evDataInfo.refreshInterval = 10
        evDataInfo.dataConnectType = binding.etDataConnectType.text.toString().trim().toInt()

        val failStatus = binding.etFailStatus.text.toString().trim()
        if (failStatus.isNotEmpty()) {
            evDataInfo.dataFailType = failStatus.toInt()
        }
        val app = EvDataInfoBean.ApplicationData()
        app.time = DialogUtils.getTimeBean(binding.tvTime)
        app.carStatus = binding.etCarStatus.text.toString().trim().toInt()
        val chargeStatus = EvDataInfoBean.ApplicationData.ChargeStatus()

        chargeStatus.status = binding.etChargeStatus.text.toString().trim().toInt()
        chargeStatus.currentCharge = binding.etCurrentCharge.text.toString().trim().toInt()
        chargeStatus.fullChargeTime = binding.etFullChargeTime.text.toString().trim().toInt()
        val remainingMile = EvDataInfoBean.ApplicationData.RemainingMile()
        remainingMile.ecoMode = binding.etEcoMode.text.toString().trim().toInt()
        remainingMile.sportMode = binding.etSportMode.text.toString().trim().toInt()
        remainingMile.powerMode = binding.etPowerMode.text.toString().trim().toInt()
        val tireGauge = EvDataInfoBean.ApplicationData.TireGauge()
        tireGauge.frontWheel = binding.etFrontWheel.text.toString().trim().toInt()
        tireGauge.rearWheel = binding.etRearWheel.text.toString().trim().toInt()
        val display = EvDataInfoBean.ApplicationData.Display()
        display.totalDistance = binding.etTotalDistance.text.toString().trim().toInt()
        display.totalCarbonEmission = binding.etTotalCarbonEmission.text.toString().trim().toDouble()
        display.curDayTotalDistance = binding.etCurDayTotalDistance.text.toString().trim().toInt()
        display.curDayAvgSpeed = binding.etCurDayAvgSpeed.text.toString().trim().toDouble()
        display.curDayTotalDrivingTime = binding.etCurDayTotalDrivingTime.text.toString().trim().toInt()
        app.chargeStatus = chargeStatus
        app.remainingMile = remainingMile
        app.tireGauge = tireGauge
        app.display = display
        evDataInfo.applicationData = app
        return evDataInfo
    }
}