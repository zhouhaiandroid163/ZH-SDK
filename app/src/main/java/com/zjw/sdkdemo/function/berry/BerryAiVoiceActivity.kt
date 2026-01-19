package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import com.blankj.utilcode.util.TimeUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ClockInfoBean
import com.zhapp.ble.bean.ScreenSettingBean
import com.zhapp.ble.bean.SettingTimeBean
import com.zhapp.ble.bean.berry.AiActionBean
import com.zhapp.ble.bean.berry.AiHistoryUiBean
import com.zhapp.ble.bean.berry.AiOpenFunctionBean
import com.zhapp.ble.bean.berry.AiToggleBean
import com.zhapp.ble.bean.berry.AiViewUiBean
import com.zhapp.ble.bean.berry.AiVoiceCmdBean
import com.zhapp.ble.callback.AiFunctionCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryAiVoiceBinding
import com.zjw.sdkdemo.utils.AiVoiceUtils
import com.zjw.sdkdemo.utils.DialogUtils
import java.io.ByteArrayOutputStream
import java.util.Calendar

class BerryAiVoiceActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryAiVoiceBinding.inflate(layoutInflater) }
    private val tag: String = BerryAiVoiceActivity::class.java.simpleName

    private var isReceivingVoiceData = false
    private var voiceDataBuffer = ByteArrayOutputStream()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_ai_voice_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
    }

    private fun initView() {
        selectTime(binding.tvViewUiTime)
        selectTime(binding.tvAiActionTime)
        selectTime(binding.tvAiToggleTime)
        selectTime(binding.tvAiOpenFunTime)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnSendAiCmd) {
            addLogI("btnSendAiCmd")
            val value = binding.etAiCmd.text.toString().trim().toInt()
            addLogI("sendAiVoiceCmd value=$value")
            ControlBleTools.getInstance().sendAiVoiceCmd(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiVoiceCmd state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendAiError) {
            addLogI("btnSendAiError")
            val value = binding.etAiError.text.toString().trim().toInt()
            addLogI("sendAiErrorCode value=$value")
            ControlBleTools.getInstance().sendAiErrorCode(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiErrorCode state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendAiTranslatedText) {
            addLogI("btnSendAiTranslatedText")
            val value = binding.etAiTranslatedText.text.toString().trim()
            addLogI("sendAiTranslatedText value=$value")
            ControlBleTools.getInstance().sendAiTranslatedText(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiTranslatedText state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendAiAnswerText) {
            addLogI("btnSendAiAnswerText")
            val value = binding.etAiAnswerText.text.toString().trim()
            addLogI("sendAiAnswerText value=$value")
            ControlBleTools.getInstance().sendAiAnswerText(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiAnswerText state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendViewUi) {
            addLogI("btnSendViewUi")
            val title = binding.etAiVTitle.text.toString().trim()
            val value = binding.etAiVValue.text.toString().trim()
            val unit = binding.etAiVUnit.text.toString().trim()
            val footer = binding.etAiVFooter.text.toString().trim()
            val bean = AiViewUiBean().apply {
                this.title = title
                this.value = value
                this.unit = unit
                this.footer = footer
                this.actionTime = DialogUtils.getTimeBean(binding.tvViewUiTime)
            }
            addLogBean("sendAiViewUi",bean)
            ControlBleTools.getInstance().sendAiViewUi(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiViewUi state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendHistoryUi) {
            addLogI("btnSendHistoryUi")
            val title = binding.etAiHTitle.text.toString().trim()
            val period = binding.etAiHPeriod.text.toString().trim()
            val valueTitle = binding.etAiHValueTitle.text.toString().trim()
            val yMax = binding.etAiHYMax.text.toString().trim().toInt()
            val maxValue = binding.etAiHMaxValue.text.toString().trim().toInt()
            val minValue = binding.etAiHMinValue.text.toString().trim().toInt()
            val avgValue = binding.etAiHAvgValue.text.toString().trim().toInt()
            val unit = binding.etAiHUnit.text.toString().trim()
            val ydm = binding.etAiHDate.text.toString().trim()
            val time = TimeUtils.string2Date(ydm, "yyyy-MM-dd")
            val chartValue = binding.etAiHChartValue.text.toString().trim().toInt()
            val category = binding.etAiHCategory.text.toString().trim()

            val calender = Calendar.getInstance()
            calender.time = time
            val bean = AiHistoryUiBean().apply {
                this.title = title
                this.period = period
                this.valueTitle = valueTitle
                this.setyMax(yMax)
                this.summary = AiHistoryUiBean.Summary().apply {
                    this.maxValue = maxValue
                    this.minValue = minValue
                    this.avgValue = avgValue
                    this.unit = unit
                }
                this.chartData = mutableListOf<AiHistoryUiBean.ChartData>().apply {
                    repeat(6) {
                        add(AiHistoryUiBean.ChartData().apply {
                            this.year = calender.get(Calendar.YEAR)
                            this.month = calender.get(Calendar.MONTH) + 1
                            this.day = calender.get(Calendar.DAY_OF_MONTH)
                            this.week = calender.get(Calendar.DAY_OF_WEEK) - 1
                            this.chartValue = chartValue + it
                            this.category = category
                        })
                        calender.add(Calendar.DAY_OF_MONTH, 1)
                    }
                }
            }
            addLogBean("sendAiHistoryUi",bean)
            ControlBleTools.getInstance().sendAiHistoryUi(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiHistoryUi state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendAiAction) {
            addLogI("btnSendAiAction")
            val scenario = binding.etAiAScenario.text.toString().trim().toInt()
            val thresholdValue = binding.etAiAThresholdValue.text.toString().trim().toInt()
            val unit = binding.etAiAUnit.text.toString().trim()
            val ydmTime = TimeUtils.string2Date(binding.etAiADate.text.toString().trim(), "yyyy-MM-dd")
            val ydmCalender = Calendar.getInstance()
            ydmCalender.time = ydmTime
            val bean = AiActionBean().apply {
                this.scenario = scenario
                this.thresholdValue = thresholdValue
                this.unit = unit
                this.year = ydmCalender.get(Calendar.YEAR)
                this.month = ydmCalender.get(Calendar.MONTH) + 1
                this.day = ydmCalender.get(Calendar.DAY_OF_MONTH)
                this.actionTime = DialogUtils.getTimeBean(binding.tvAiActionTime)
            }
            addLogBean("sendAiAction",bean)
            ControlBleTools.getInstance().sendAiAction(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiAction state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendAiToggle) {
            addLogI("btnSendAiToggle")
            val scenario = binding.etAiTScenario.text.toString().trim().toInt()
            val isOpen = binding.cbAiTToggleStatus.isChecked
            val bean = AiToggleBean().apply {
                this.scenario = scenario
                this.isToggleStatus = isOpen
                this.actionTime = DialogUtils.getTimeBean(binding.tvAiToggleTime)
            }
            addLogBean("sendAiToggle",bean)
            ControlBleTools.getInstance().sendAiToggle(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiToggle state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendAiOpenFun) {
            addLogI("btnSendAiOpenFun")
            val function = binding.etAiOFScenario.text.toString().trim().toInt()
            val contactsNumber = binding.etAiOFContactsNumber.text.toString().trim()
            val workoutType = binding.etAiOFWorkoutType.text.toString().trim().toInt()
            val duration = binding.etAiOFDuration.text.toString().trim().toInt()
            val musicCommand = binding.etAiOFMusicCommand.text.toString().trim().toInt()
            val bean = AiOpenFunctionBean().apply {
                this.function = function
                this.actionTime = DialogUtils.getTimeBean(binding.tvAiOpenFunTime)
                this.contactsNumber = contactsNumber
                this.workoutType = workoutType
                this.duration = duration
                this.musicCommand = musicCommand
                this.screenSetting = ScreenSettingBean().apply {
                    this.level = 1
                    this.isSwitch = true
                    this.duration = 5
                    this.doubleClick = true
                }
                this.clockInfoList = mutableListOf<ClockInfoBean>().apply {
                    add(ClockInfoBean().apply {
                        this.id = 0
                        this.data = ClockInfoBean.DataBean().apply {
                            this.time = SettingTimeBean(8, 0)
                            this.isMonday = true
                            this.clockName = "clock1"
                        }
                    })
                }
            }
            addLogBean("sendAiOpenFunction",bean)
            ControlBleTools.getInstance().sendAiOpenFunction(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendAiOpenFunction state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.aiFunctionCallBack = object : AiFunctionCallBack {
            override fun onDevAiVoiceCmd(bean: AiVoiceCmdBean) {
                addLogBean("aiFunctionCallBack onDevAiVoiceCmd",bean)

                when (bean.voiceState) {
                    1 -> {
                        isReceivingVoiceData = true
                        voiceDataBuffer.reset()
                    }

                    2 -> {
                        isReceivingVoiceData = false
                        val receivedData = voiceDataBuffer.toByteArray()
                        AiVoiceUtils.playVoice(receivedData)
                    }
                }
            }

            override fun onDevAiVoiceData(data: ByteArray) {
                addLogI("aiFunctionCallBack onDevAiVoiceData data.size=${data.size}")
                if (!isReceivingVoiceData) return
                voiceDataBuffer.write(data)
            }
        }
    }
}