package com.zjw.sdkdemo.function.apricot.ring

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.RingAutoActiveSportConfigBean
import com.zhapp.ble.bean.RingSleepConfigBean
import com.zhapp.ble.callback.AutoSportDataCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceBatteryReportingCallBack
import com.zhapp.ble.callback.RingAllDaySleepConfigCallBack
import com.zhapp.ble.callback.RingAutoActiveSportConfigCallBack
import com.zhapp.ble.callback.RingChargingCaseInfoCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityRingBinding
import com.zjw.sdkdemo.function.apricot.MeasureActivity
import com.zjw.sdkdemo.function.apricot.MeasureTypeActivity
import com.zjw.sdkdemo.livedata.MyMicroCallBack
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack

class RingActivity : BaseActivity() {
    private val binding by lazy { ActivityRingBinding.inflate(layoutInflater) }
    private val tag: String = RingActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_ring)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutAllSleepSwitch.cbTop, binding.layoutAllSleepSwitch.llBottom,binding.layoutAllSleepSwitch.ivHelp)
        setMyCheckBox(binding.layoutAutoActivityDetection.cbTop, binding.layoutAutoActivityDetection.llBottom,binding.layoutAutoActivityDetection.ivHelp)
        setMyCheckBox(binding.layoutAutoMotionRecognize.cbTop, binding.layoutAutoMotionRecognize.llBottom,binding.layoutAutoMotionRecognize.ivHelp)
    }

    fun initListener() {
        clickCheckConnect(binding.btnRingSportScreen) {
            addLogI("btnRingSportScreen")
            startActivity(Intent(this, RingSportScreenActivity::class.java))
        }

        clickCheckConnect(binding.btnRingAirplaneMode) {
            addLogI("btnRingAirplaneMode")
            addLogI("setRingAirplaneMode")
            ControlBleTools.getInstance().setRingAirplaneMode(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setRingAirplaneMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnRingGetChargingCaseState) {
            addLogI("btnRingGetChargingCaseState")
            addLogI("getRingChargingCaseInfo")
            ControlBleTools.getInstance().getRingChargingCaseInfo(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRingChargingCaseInfo state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnRingMeasure) {
            addLogI("btnRingMeasure")
            startActivity(Intent(this, MeasureTypeActivity::class.java).apply { putExtra(MeasureActivity.DEVICE_TYPE_TAG, "0") })
        }

        clickCheckConnect(binding.btnRingGetAutoSport) {
            addLogI("btnRingGetAutoSport")
            addLogI("getAutoSportData")
            ControlBleTools.getInstance().getAutoSportData(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getAutoSportData state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnRingGetNfcSleepError) {
            addLogI("btnRingGetNfcSleepError")
            addLogI("getRingNFCSleepErr")
            ControlBleTools.getInstance().getRingNFCSleepErr(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRingNFCSleepErr state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnRingGetWearingStatus){
            addLogI("btnRingGetWearingStatus")
            addLogI("getRingWearingStatus")
            ControlBleTools.getInstance().getRingWearingStatus(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRingWearingStatus state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutAllSleepSwitch.btnGet) {
            addLogI("layoutAllSleepSwitch.btnGet")
            addLogI("getRingAllDaySleepConfig")
            ControlBleTools.getInstance().getRingAllDaySleepConfig(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRingAllDaySleepConfig state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutAllSleepSwitch.btnSet) {
            if (binding.layoutAllSleepSwitch.btnSet.tag == 1) {
                binding.layoutAllSleepSwitch.btnSet.tag = 0
            } else {
                binding.layoutAllSleepSwitch.btnSet.tag = 1
            }
            val isTrue = binding.layoutAllSleepSwitch.btnSet.tag == 1
            addLogI("layoutAllSleepSwitch.btnSet isTrue=$isTrue")
            val bean = RingSleepConfigBean(isTrue)
            addLogBean("setRingAllDaySleepConfig",bean)
            ControlBleTools.getInstance().setRingAllDaySleepConfig(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setRingAllDaySleepConfig state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutAutoActivityDetection.btnGet) {
            addLogI("layoutAutoActivityDetection.btnGet")
            addLogI("getRingAutoActiveSportConfig")
            ControlBleTools.getInstance().getRingAutoActiveSportConfig(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRingAutoActiveSportConfig state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutAutoActivityDetection.btnSet) {
            addLogI("layoutAutoActivityDetection.btnSet")
            val isTrue = binding.layoutAutoActivityDetection.cbSwitch.isChecked
            val timeValue: Int = binding.layoutAutoActivityDetection.etTime.text.toString().trim().toInt()
            val bean = RingAutoActiveSportConfigBean(isTrue)
            bean.autoActiveSportActivetime = timeValue
            addLogBean("setRingAutoActiveSportConfig",bean)
            ControlBleTools.getInstance().setRingAutoActiveSportConfig(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setRingAutoActiveSportConfig state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutAutoMotionRecognize.btnGet) {
            addLogI("layoutAutoMotionRecognize.btnGet")
            addLogI("getMotionRecognition")
            ControlBleTools.getInstance().getMotionRecognition(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getMotionRecognition state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutAutoMotionRecognize.btnSet) {
            addLogI("layoutAutoMotionRecognize.btnSet")
            val autoSportSwitch = binding.layoutAutoMotionRecognize.cbSwitch.isChecked
            val autoSportStopSwitch = binding.layoutAutoMotionRecognize.cbAutoSportStopSwitch.isChecked
            addLogI("setMotionRecognition autoSportSwitch=$autoSportSwitch autoSportStopSwitch=$autoSportStopSwitch")
            ControlBleTools.getInstance().setMotionRecognition(autoSportSwitch, autoSportStopSwitch, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setMotionRecognition state=$state")
                }
            })
        }
    }

    fun initCallback() {
        MyMicroCallBack.onNfcSleepErr.observe(this, object : Observer<Int?> {
            override fun onChanged(value: Int?) {
                addLogI("ringNFCSleepError value=$value")
            }
        })

        MyMicroCallBack.onRingWearingStatus.observe(this, object : Observer<Int?> {
            override fun onChanged(value: Int?) {
                addLogI("onRingWearingStatus value=$value")
            }
        })

        MySettingMenuCallBack.onMotionRecognitionResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onMotionRecognitionResult", bean!!)
        })

        CallBackUtils.ringChargingCaseInfoCallBack = RingChargingCaseInfoCallBack { bean ->
            addLogBean( "ringChargingCaseInfoCallBack",bean)
        }

        CallBackUtils.autoSportDataCallBack = AutoSportDataCallBack { bean ->
            addLogBean( "autoSportDataCallBack",bean)
        }

        CallBackUtils.ringAllDaySleepConfigCallBack = RingAllDaySleepConfigCallBack { bean ->
            addLogBean( "ringAllDaySleepConfigCallBack",bean)
        }

        CallBackUtils.ringAutoActiveSportConfigCallBack = RingAutoActiveSportConfigCallBack { bean ->
            addLogBean( "ringAutoActiveSportConfigCallBack",bean)
        }

        CallBackUtils.deviceBatteryReportingCallBack = DeviceBatteryReportingCallBack { bean ->
            addLogBean( "deviceBatteryReportingCallBack",bean)
        }
    }
}