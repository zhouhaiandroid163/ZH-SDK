package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import androidx.lifecycle.Observer
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DialStyleBean
import com.zhapp.ble.bean.DoNotDisturbModeBean
import com.zhapp.ble.bean.FindWearSettingsBean
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.ScreenDisplayBean
import com.zhapp.ble.bean.ScreenSettingBean
import com.zhapp.ble.bean.WristScreenBean
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityOtherSetBinding
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.utils.DialogUtils

class SetOtherActivity : BaseActivity() {
    val binding by lazy { ActivityOtherSetBinding.inflate(layoutInflater) }
    private val tag: String = SetOtherActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_other)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutWristScreen.cbTop, binding.layoutWristScreen.llBottom, binding.layoutWristScreen.ivHelp)
        setMyCheckBox(binding.layoutScreenSetting.cbTop, binding.layoutScreenSetting.llBottom, binding.layoutScreenSetting.ivHelp)
        setMyCheckBox(binding.layoutOverlayScreen.cbTop, binding.layoutOverlayScreen.llBottom, binding.layoutOverlayScreen.ivHelp)
        setMyCheckBox(binding.layoutScreenDisplay.cbTop, binding.layoutScreenDisplay.llBottom, binding.layoutScreenDisplay.ivHelp)
        setMyCheckBox(binding.layoutVibrationIntensity.cbTop, binding.layoutVibrationIntensity.llBottom, binding.layoutVibrationIntensity.ivHelp)
        setMyCheckBox(binding.layoutVibrationTime.cbTop, binding.layoutVibrationTime.llBottom, binding.layoutVibrationTime.ivHelp)
        setMyCheckBox(binding.layoutDoNotDisturbMode.cbTop, binding.layoutDoNotDisturbMode.llBottom, binding.layoutDoNotDisturbMode.ivHelp)
        setMyCheckBox(binding.layoutPowerSaving.cbTop, binding.layoutPowerSaving.llBottom, binding.layoutPowerSaving.ivHelp)
        setMyCheckBox(binding.layoutFindWearSettings.cbTop, binding.layoutFindWearSettings.llBottom, binding.layoutFindWearSettings.ivHelp)
        setMyCheckBox(binding.layoutCallCustomizeSettings.cbTop, binding.layoutCallCustomizeSettings.llBottom, binding.layoutCallCustomizeSettings.ivHelp)

        selectSettingTime(binding.layoutWristScreen.tvStartTime)
        selectSettingTime(binding.layoutWristScreen.tvEndTime)

        selectSettingTime(binding.layoutScreenDisplay.tvStartTime)
        selectSettingTime(binding.layoutScreenDisplay.tvEndTime)

        selectSettingTime(binding.layoutDoNotDisturbMode.tvStartTime)
        selectSettingTime(binding.layoutDoNotDisturbMode.tvEndTime)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutWristScreen.btnGet) {
            addLogI("layoutWristScreen.btnGet")
            addLogI("getWristScreen")
            ControlBleTools.getInstance().getWristScreen(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getWristScreen state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutWristScreen.btnGet) {
            addLogI("layoutWristScreen.btnGet")
            val bean = WristScreenBean()
            val mode = binding.layoutWristScreen.etMode.text.toString().trim().toInt()
            val sensitivity = binding.layoutWristScreen.etSensitivity.text.toString().trim().toInt()
            bean.timingMode = mode
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutWristScreen.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutWristScreen.tvEndTime)
            bean.sensitivityMode = sensitivity
            addLogBean("setWristScreen", bean)
            ControlBleTools.getInstance().setWristScreen(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setWristScreen state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutScreenSetting.btnGet) {
            addLogI("layoutScreenSetting.btnGet")
            addLogI("getScreenSetting")
            ControlBleTools.getInstance().getScreenSetting(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getScreenSetting state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutScreenSetting.btnSet) {
            addLogI("layoutScreenSetting.btnSet")
            val screenSetting = ScreenSettingBean()
            val level = binding.layoutScreenSetting.etScreenLevel.text.toString().trim().toInt()
            val duration = binding.layoutScreenSetting.etScreenTime.text.toString().trim().toInt()
            screenSetting.level = level
            screenSetting.duration = duration
            screenSetting.isSwitch = binding.layoutScreenSetting.cbSwitch.isChecked
            screenSetting.doubleClick = binding.layoutScreenSetting.cbDoubleClickScreenSwitch.isChecked
            addLogBean("setScreenSetting", screenSetting)
            ControlBleTools.getInstance().setScreenSetting(screenSetting, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setScreenSetting state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutOverlayScreen.btnGet) {
            addLogI("layoutOverlayScreen.btnGet")
            addLogI("getOverlayScreen")
            ControlBleTools.getInstance().getOverlayScreen(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getOverlayScreen state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutOverlayScreen.btnOpen) {
            addLogI("layoutOverlayScreen.btnOpen")
            val isTrue = true
            addLogI("setScreenSetting isTrue=$isTrue")
            ControlBleTools.getInstance().setOverlayScreen(isTrue, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setOverlayScreen state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutOverlayScreen.btnClose) {
            addLogI(".layoutOverlayScreen.btnClose")
            val isTrue = false
            addLogI("setOverlayScreen isTrue=$isTrue")
            ControlBleTools.getInstance().setOverlayScreen(isTrue, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setOverlayScreen state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutScreenDisplay.btnGet) {
            addLogI("layoutScreenDisplay.btnGet")
            addLogI("getScreenDisplay")
            ControlBleTools.getInstance().getScreenDisplay(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getScreenDisplay state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutScreenDisplay.btnSet) {
            addLogI("layoutScreenDisplay.btnSet")
            val bean = ScreenDisplayBean()
            val mode = binding.layoutScreenDisplay.etMode.text.toString().trim().toInt()
            val style = binding.layoutScreenDisplay.etStyle.text.toString().trim().toInt()
            bean.timingMode = mode
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutScreenDisplay.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutScreenDisplay.tvEndTime)
            bean.dialStyle = DialStyleBean(style)
            addLogBean("setScreenDisplay", bean)
            ControlBleTools.getInstance().setScreenDisplay(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setScreenDisplay state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutVibrationIntensity.btnGet) {
            addLogI("layoutVibrationIntensity.btnGet")
            addLogI("getDeviceVibrationIntensity")
            ControlBleTools.getInstance().getDeviceVibrationIntensity(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getDeviceVibrationIntensity state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutVibrationIntensity.btnSet) {
            addLogI("layoutVibrationIntensity.btnSet")
            val value = binding.layoutVibrationIntensity.etVibrationIntensity.text.toString().trim().toInt()
            addLogI("setDeviceVibrationIntensity value=$value")
            ControlBleTools.getInstance().setDeviceVibrationIntensity(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setDeviceVibrationIntensity state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutVibrationTime.btnGet) {
            addLogI("layoutVibrationTime.btnGet")
            addLogI("getDeviceVibrationDuration")
            ControlBleTools.getInstance().getDeviceVibrationDuration(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getDeviceVibrationDuration state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutVibrationTime.btnSet) {
            addLogI("layoutVibrationTime.btnSet")
            val value = binding.layoutVibrationTime.etVibrationTime.text.toString().trim { it <= ' ' }.toInt()
            addLogI("setDeviceVibrationDuration value=$value")
            ControlBleTools.getInstance().setDeviceVibrationDuration(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setDeviceVibrationDuration state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutDoNotDisturbMode.btnGet) {
            addLogI("layoutDoNotDisturbMode.btnGet")
            addLogI("getDoNotDisturbMode")
            ControlBleTools.getInstance().getDoNotDisturbMode(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getDoNotDisturbMode state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutDoNotDisturbMode.btnSet) {
            addLogI("layoutDoNotDisturbMode.btnSet")
            val bean = DoNotDisturbModeBean()
            bean.isSwitch = binding.layoutDoNotDisturbMode.cbDoNotDisturbTimerSwitch.isChecked
            bean.isSmartSwitch = binding.layoutDoNotDisturbMode.cbDoNotDisturbSmartMode.isChecked
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutDoNotDisturbMode.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutDoNotDisturbMode.tvEndTime)
            addLogBean("setDoNotDisturbMode",bean)
            ControlBleTools.getInstance().setDoNotDisturbMode(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setDoNotDisturbMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutPowerSaving.btnGet) {
            addLogI("layoutPowerSaving.btnGet")
            addLogI("getPressureMode")
            ControlBleTools.getInstance().getPressureMode(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getPressureMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutPowerSaving.btnSet) {
            addLogI("layoutPowerSaving.btnSet")
            val bean = PressureModeBean()
            bean.pressureMode = binding.layoutPowerSaving.cbPressureMode.isChecked
            bean.relaxationReminder = binding.layoutPowerSaving.cbRelaxationReminder.isChecked
            addLogBean("setPressureMode",bean)
            ControlBleTools.getInstance().setPressureMode(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setPressureMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutFindWearSettings.btnGet) {
            addLogI("layoutFindWearSettings.layoutFindWearSettings")
            addLogI("getFindWearSettings")
            ControlBleTools.getInstance().getFindWearSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getFindWearSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutFindWearSettings.btnSet) {
            addLogI("layoutFindWearSettings.btnSet")
            val bean = FindWearSettingsBean()
            bean.vibrationMode = binding.layoutFindWearSettings.etVibrationMode.text.toString().trim().toInt()
            bean.ringMode = binding.layoutFindWearSettings.etRingMode.text.toString().trim().toInt()
            addLogBean("setFindWearSettings",bean)
            ControlBleTools.getInstance().setFindWearSettings(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setFindWearSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutCallCustomizeSettings.btnGet) {
            addLogI("layoutCallCustomizeSettings.btnGet")
            addLogI("getCustomizeSet")
            ControlBleTools.getInstance().getCustomizeSet(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getCustomizeSet state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutCallCustomizeSettings.btnSet) {
            addLogI("layoutCallCustomizeSettings.btnSet")
            val value = binding.layoutCallCustomizeSettings.etCustomizeSet.text.toString().trim().toInt()
            addLogI("setCustomizeSet value=$value")
            ControlBleTools.getInstance().setCustomizeSet(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setCustomizeSet state=$state")
                }
            })
        }

    }

    private fun initCallBack() {
        MySettingMenuCallBack.onWristScreenResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onWristScreenResult", bean!!)
        })

        MySettingMenuCallBack.onPowerSavingResult.observe(this, Observer { value ->
            addLogI("MySettingMenuCallBack.onPowerSavingResult value=$value")
        })

        MySettingMenuCallBack.onOverlayScreenResult.observe(this, Observer { value ->
            addLogI("MySettingMenuCallBack.onOverlayScreenResult value=$value")
        })

        MySettingMenuCallBack.onVibrationResult.observe(this, Observer { value ->
            addLogI("MySettingMenuCallBack.onVibrationResult value=$value")
        })

        MySettingMenuCallBack.onVibrationDurationResult.observe(this, Observer { value ->
            addLogI("MySettingMenuCallBack.onVibrationDurationResult value=$value")
        })

        MySettingMenuCallBack.onScreenDisplayResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onScreenDisplayResult", bean!!)
        })

        MySettingMenuCallBack.onDoNotDisturbModeResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onDoNotDisturbModeResult", bean!!)
        })

        MySettingMenuCallBack.onNotificationSetting.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onNotificationSetting", bean!!)
        })

        MySettingMenuCallBack.onFindWearSettings.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onFindWearSettings", bean!!)
        })

        MySettingMenuCallBack.onScreenSettingResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onScreenSettingResult", bean!!)
        })

        MySettingMenuCallBack.onCustomizeLeftClickSettings.observe(this, Observer { value ->
            addLogI("MySettingMenuCallBack.onCustomizeLeftClickSettings value=$value")
        })
    }
}