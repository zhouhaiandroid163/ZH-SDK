package com.zjw.sdkdemo.function.apricot

import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.lifecycle.Observer
import com.zh.ble.wear.protobuf.MusicProtos
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.BodyTemperatureSettingBean
import com.zhapp.ble.bean.ContinuousBloodOxygenSettingsBean
import com.zhapp.ble.bean.HeartRateMonitorBean
import com.zhapp.ble.bean.MusicInfoBean
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.RealTimeHeartRateConfigBean
import com.zhapp.ble.bean.SWBRMonitorBean
import com.zhapp.ble.bean.SWHRMonitorBean
import com.zhapp.ble.bean.SWHRVMonitorBean
import com.zhapp.ble.bean.SWSPO2MonitorBean
import com.zhapp.ble.bean.SleepModeBean
import com.zhapp.ble.bean.StockInfoBean
import com.zhapp.ble.bean.StockSymbolBean
import com.zhapp.ble.bean.WorldClockBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.MusicCallBack
import com.zhapp.ble.callback.RealTimeHeartRateCallback
import com.zhapp.ble.callback.SettingMenuCallBack
import com.zhapp.ble.callback.StockCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityFunSetBinding
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.utils.DialogUtils
import com.zjw.sdkdemo.utils.MusicUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SetFunActivity : BaseActivity() {
    val binding by lazy { ActivityFunSetBinding.inflate(layoutInflater) }
    private val tag: String = SetFunActivity::class.java.simpleName

    var stockInfos: MutableList<StockInfoBean?> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_fun)
        initLogSet(
            tag,
            binding.layoutLog.llLog,
            binding.layoutLog.cxLog,
            binding.layoutLog.llLogContent,
            binding.layoutLog.btnClear,
            binding.layoutLog.btnSet,
            binding.layoutLog.btnSendLog
        )
        initView()
        initListener()
        initCallback()
        initData()
    }

    private fun initData() {
        for (i in 0..9) {
            val symbol = "symbol_$i"
            val market = "market_$i"
            val name = "name$i"
            val latestPrice = 110.0f
            val preClose = 100.0f
            val halted = 11
            val timestamp = (System.currentTimeMillis() / 1000).toInt()
            val delay = 22
            val stockInfoBean = StockInfoBean(symbol, market, name, latestPrice, preClose, halted, timestamp, delay)
            stockInfos.add(stockInfoBean)
        }
    }

    private fun initView() {
        setMyCheckBox(binding.layoutWorldClock.cbTop, binding.layoutWorldClock.llBottom, binding.layoutWorldClock.ivHelp)
        setMyCheckBox(binding.layoutMusic.cbTop, binding.layoutMusic.llBottom, binding.layoutMusic.ivHelp)
        setMyCheckBox(binding.layoutStock.cbTop, binding.layoutStock.llBottom, binding.layoutStock.ivHelp)
        setMyCheckBox(binding.layoutRem.cbTop, binding.layoutRem.llBottom, binding.layoutRem.ivHelp)
        setMyCheckBox(binding.layoutContinuousHeart.cbTop, binding.layoutContinuousHeart.llBottom, binding.layoutContinuousHeart.ivHelp)
        setMyCheckBox(binding.layoutHeartRealTime.cbTop, binding.layoutHeartRealTime.llBottom, binding.layoutHeartRealTime.ivHelp)
        setMyCheckBox(binding.layoutContinuousSpo2.cbTop, binding.layoutContinuousSpo2.llBottom, binding.layoutContinuousSpo2.ivHelp)
        setMyCheckBox(binding.layoutContinuousTemperature.cbTop, binding.layoutContinuousTemperature.llBottom, binding.layoutContinuousTemperature.ivHelp)
        setMyCheckBox(binding.layoutPressureMode.cbTop, binding.layoutPressureMode.llBottom, binding.layoutPressureMode.ivHelp)
        setMyCheckBox(binding.layoutSleepMode.cbTop, binding.layoutSleepMode.llBottom, binding.layoutSleepMode.ivHelp)
        setMyCheckBox(binding.layoutSWSPO2Monitor.cbTop, binding.layoutSWSPO2Monitor.llBottom, binding.layoutSWSPO2Monitor.ivHelp)
        setMyCheckBox(binding.layoutSWHRMonitor.cbTop, binding.layoutSWHRMonitor.llBottom, binding.layoutSWHRMonitor.ivHelp)
        setMyCheckBox(binding.layoutSWHRVMonitor.cbTop, binding.layoutSWHRVMonitor.llBottom, binding.layoutSWHRVMonitor.ivHelp)
        setMyCheckBox(binding.layoutSWBRMonitor.cbTop, binding.layoutSWBRMonitor.llBottom, binding.layoutSWBRMonitor.ivHelp)

        selectSettingTime(binding.layoutContinuousSpo2.tvStartTime)
        selectSettingTime(binding.layoutContinuousSpo2.tvEndTime)

        selectSettingTime(binding.layoutContinuousTemperature.tvStartTime)
        selectSettingTime(binding.layoutContinuousTemperature.tvEndTime)


        selectSettingTime(binding.layoutSleepMode.tvStartTime)
        selectSettingTime(binding.layoutSleepMode.tvEndTime)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutWorldClock.btnGet) {
            addLogI("layoutWorldClock.btnGet")
            addLogI("getWorldClockList")
            ControlBleTools.getInstance().getWorldClockList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getWorldClockList state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutWorldClock.btnSet) {
            addLogI("layoutWorldClock.btnSet")
            val list: MutableList<WorldClockBean?> = ArrayList()

            val stringArray = getResources().getStringArray(R.array.world_clock_zone_name)
            for (i in 0..4) {
                val arr = stringArray[i]
                val split: Array<String?> = arr.split("\\*".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val bean = WorldClockBean()
                bean.cityName = split[1]
                bean.offset = toTimezoneInt(split[0]!!) / 15
                list.add(bean)
            }

            val worldClockBean = WorldClockBean()
            worldClockBean.cityName = "Beijing"
            worldClockBean.offset = TimeZone.getDefault().rawOffset / 60 / 1000 / 15
            addLogI("offset=${worldClockBean.offset}")
            list.add(worldClockBean)

            addLogBean("setWorldClockList", list)
            ControlBleTools.getInstance().setWorldClockList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setWorldClockList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutMusic.btnSync) {
            addLogI("layoutMusic.btnSync")
            syncMusic()
        }

        clickCheckConnect(binding.layoutStock.btnSync) {
            addLogI("layoutStock.btnSync")
            addLogBean("syncStockInfoList", stockInfos)
            ControlBleTools.getInstance().syncStockInfoList(stockInfos, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("syncStockInfoList state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutStock.btnGet) {
            addLogI("layoutStock.btnGet")
            addLogI("getStockSymbolList")
            ControlBleTools.getInstance().getStockSymbolList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getStockSymbolList state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutStock.btnSet) {
            addLogI("layoutStock.btnSet")
            val list: MutableList<StockSymbolBean?> = ArrayList()
            for (i in stockInfos.indices) {
                val symbolBean = StockSymbolBean()
                symbolBean.symbol = stockInfos[i]!!.symbol
                symbolBean.isWidget = false
                symbolBean.order = i
                list.add(symbolBean)
            }
            addLogBean("setStockSymbolOrder", list)
            ControlBleTools.getInstance().setStockSymbolOrder(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setStockSymbolOrder state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutStock.btnDelete) {
            addLogI(".layoutStock.btnDelete")
            val value = stockInfos[0]!!.symbol
            addLogI(".deleteStockBySymbol value=$value")
            ControlBleTools.getInstance().deleteStockBySymbol(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("deleteStockBySymbol state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutContinuousHeart.btnGet) {
            addLogI("layoutContinuousHeart.btnGet")
            addLogI("getHeartRateMonitor")
            ControlBleTools.getInstance().getHeartRateMonitor(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getHeartRateMonitor state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutContinuousHeart.btnSet) {
            addLogI("layoutContinuousHeart.btnSet")
            val bean = HeartRateMonitorBean()
            val warningValue = binding.layoutContinuousHeart.etHeartWarningValue.text.toString().trim().toInt()
            val sportWarningValue = binding.layoutContinuousHeart.etSportHeartWarningValue.text.toString().trim().toInt()
            val lowWarningValue = binding.layoutContinuousHeart.etHeartLow.text.toString().trim().toInt()
            bean.mode = if (binding.layoutContinuousHeart.cbSwitch.isChecked) SettingMenuCallBack.HeartRateMode.AUTO.mode else SettingMenuCallBack.HeartRateMode.OFF.mode
            bean.isWarning = binding.layoutContinuousHeart.cbHeartWarningSwitch.isChecked
            bean.warningValue = warningValue
            bean.isSportWarning = binding.layoutContinuousHeart.cbSportHeartWarningSwitch.isChecked
            bean.sportWarningValue = sportWarningValue
            bean.lowWarningValue = lowWarningValue
            bean.continuousHeartRateMode =
                if (binding.layoutContinuousHeart.cbSmartHeartSwitch.isChecked) SettingMenuCallBack.ContinuousHeartRateMode.INTELLIGENT_HEART_RATE.mode else SettingMenuCallBack.ContinuousHeartRateMode.ALL_DAY_HEART_RATE.mode
            addLogBean(".setHeartRateMonitor", bean)
            ControlBleTools.getInstance().setHeartRateMonitor(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setHeartRateMonitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutHeartRealTime.btnGet) {
            addLogI("layoutHeartRealTime.btnGet")
            addLogI("getRealTimeHeartRateConfig")
            ControlBleTools.getInstance().getRealTimeHeartRateConfig(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRealTimeHeartRateConfig state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutHeartRealTime.btnOpen) {
            addLogI("layoutHeartRealTime.btnOpen")
            val bean = RealTimeHeartRateConfigBean(true, 3, 30)
            addLogBean(".setRealTimeHeartRateConfig", bean)
            ControlBleTools.getInstance().setRealTimeHeartRateConfig(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setRealTimeHeartRateConfig state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutHeartRealTime.btnClose) {
            addLogI("layoutHeartRealTime.btnClose")
            val bean = RealTimeHeartRateConfigBean()
            bean.status = false
            bean.frequency = 3
            bean.overtime = 30
            addLogBean(".setRealTimeHeartRateConfig", bean)
            ControlBleTools.getInstance().setRealTimeHeartRateConfig(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("RealTimeHeartRateConfigBean state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutContinuousSpo2.btnGet) {
            addLogI("layoutContinuousSpo2.btnGet")
            addLogI("getContinuousBloodOxygenSettings")
            ControlBleTools.getInstance().getContinuousBloodOxygenSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getContinuousBloodOxygenSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutContinuousSpo2.btnSet) {
            addLogI("layoutContinuousSpo2.btnSet")
            val bean = ContinuousBloodOxygenSettingsBean()
            bean.mode =
                if (binding.layoutContinuousSpo2.cbSwitch.isChecked) SettingMenuCallBack.ContinuousBloodOxygenMode.AUTO.mode else SettingMenuCallBack.ContinuousBloodOxygenMode.OFF.mode
            bean.frequency = binding.layoutContinuousSpo2.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutContinuousSpo2.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutContinuousSpo2.tvEndTime)
            addLogBean(".setContinuousBloodOxygenSettings", bean)
            ControlBleTools.getInstance().setContinuousBloodOxygenSettings(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setContinuousBloodOxygenSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutContinuousTemperature.btnGet) {
            addLogI("layoutContinuousTemperature.btnGet")
            addLogI("getContinuousTemperature")
            ControlBleTools.getInstance().getContinuousTemperature(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getContinuousTemperature state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutContinuousTemperature.btnSet) {
            addLogI("layoutContinuousTemperature.btnSet")
            val bean = BodyTemperatureSettingBean()
            bean.mode =
                if (binding.layoutContinuousTemperature.cbSwitch.isChecked) SettingMenuCallBack.BodyTemperatureMode.AUTO.mode else SettingMenuCallBack.BodyTemperatureMode.OFF.mode
            bean.frequency = binding.layoutContinuousTemperature.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutContinuousTemperature.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutContinuousTemperature.tvEndTime)
            addLogBean(".setContinuousTemperature", bean)
            ControlBleTools.getInstance().setContinuousTemperature(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setContinuousTemperature state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutPressureMode.btnGet) {
            addLogI("layoutPressureMode.btnGet")
            addLogI("getPressureMode")
            ControlBleTools.getInstance().getPressureMode(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getPressureMode state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutPressureMode.btnSet) {
            addLogI("layoutPressureMode.btnSet")
            val bean = PressureModeBean()
            bean.pressureMode = binding.layoutPressureMode.cbPressureModeSwitch.isChecked
            bean.relaxationReminder = binding.layoutPressureMode.cbRelaxationReminderSwitch.isChecked
            addLogBean(".setPressureMode", bean)
            ControlBleTools.getInstance().setPressureMode(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setPressureMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutRem.btnRapidEyeMoveGet) {
            addLogI("btnRapidEyeMoveGet")
            addLogI("getRapidEyeMovement")
            ControlBleTools.getInstance().getRapidEyeMovement(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getRapidEyeMovement state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutRem.btnOpen) {
            addLogI("layoutRem.btnOpen")
            val isTrue = true
            addLogI(".setRapidEyeMovement isTrue=$isTrue")
            ControlBleTools.getInstance().setRapidEyeMovement(isTrue, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setRapidEyeMovement state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutRem.btnClose) {
            addLogI("layoutRem.btnClose")
            val isTrue = false
            addLogI("setRapidEyeMovement isTrue=$isTrue")
            ControlBleTools.getInstance().setRapidEyeMovement(isTrue, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setRapidEyeMovement state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSleepMode.btnGet) {
            addLogI("layoutSleepMode.btnGet")
            addLogI("getSleepMode")
            ControlBleTools.getInstance().getSleepMode(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSleepMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSleepMode.btnSet) {
            addLogI("layoutSleepMode.btnSet")
            val bean = SleepModeBean()
            bean.sleepModeSwitch = binding.layoutSleepMode.cbSwitch.isChecked
            bean.rapidEyeMovement = binding.layoutSleepMode.cbRemSwitch.isChecked
            bean.smartSwitch = binding.layoutSleepMode.cbSmartDoNotDisturbSwitch.isChecked
            bean.minimizeScreen = binding.layoutSleepMode.cbAutoDarkScreenSwitch.isChecked
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutSleepMode.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutSleepMode.tvEndTime)
            addLogBean("setSleepMode", bean)
            ControlBleTools.getInstance().setSleepMode(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSleepMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWSPO2Monitor.btnGet) {
            addLogI("layoutSWSPO2Monitor.btnGet")
            addLogI("getSWSPO2Monitor")
            ControlBleTools.getInstance().getSWSPO2Monitor(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSWSPO2Monitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWSPO2Monitor.btnSet) {
            addLogI("layoutSWSPO2Monitor.btnSet")
            val bean = SWSPO2MonitorBean()
            bean.isWarning = binding.layoutSWSPO2Monitor.cbSpo2WarningSwitch.isChecked
            bean.warningValue = binding.layoutSWSPO2Monitor.etSpo2WarningValue.text.toString().trim().toInt()
            addLogBean("setSWSPO2Monitor", bean)
            ControlBleTools.getInstance().setSWSPO2Monitor(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSWSPO2Monitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWHRMonitor.btnGet) {
            addLogI("layoutSWHRMonitor.btnGet")
            addLogI("getSWHRMonitor")
            ControlBleTools.getInstance().getSWHRMonitor(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSWHRMonitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWHRMonitor.btnSet) {
            addLogI("layoutSWHRMonitor.btnSet")
            val bean = SWHRMonitorBean()
            bean.isLowWarning = binding.layoutSWHRMonitor.cbHrLowWarningSwitch.isChecked
            bean.lowWarningValue = binding.layoutSWHRMonitor.etHrLowWarningValue.text.toString().trim().toInt()
            bean.isHeightWarning = binding.layoutSWHRMonitor.cbHrHeightWarningSwitch.isChecked
            bean.heightWarningValue = binding.layoutSWHRMonitor.etHrHeightWarningValue.text.toString().trim().toInt()
            addLogBean("setSWHRMonitor", bean)
            ControlBleTools.getInstance().setSWHRMonitor(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSWHRMonitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWHRVMonitor.btnGet){
            addLogI("layoutSWHRVMonitor.btnGet")
            addLogI("getSWHRVMonitor")
            ControlBleTools.getInstance().getSWHRVMonitor(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSWHRVMonitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWHRVMonitor.btnSet){
            addLogI("layoutSWHRVMonitor.btnSet")
            val bean = SWHRVMonitorBean()
            bean.isWarning = binding.layoutSWHRVMonitor.cbHrvWarningSwitch.isChecked
            bean.warningValue = binding.layoutSWHRVMonitor.etHrvWarningValue.text.toString().trim().toInt()
            addLogBean("setSWHRVMonitor", bean)
            ControlBleTools.getInstance().setSWHRVMonitor(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSWHRVMonitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWBRMonitor.btnGet){
            addLogI("layoutSWBRMonitor.btnGet")
            addLogI("getSWBRMonitor")
            ControlBleTools.getInstance().getSWBRMonitor(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSWBRMonitor state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSWBRMonitor.btnSet){
            addLogI("layoutSWBRMonitor.btnSet")
            val bean = SWBRMonitorBean()
            bean.isWarning = binding.layoutSWBRMonitor.cbBrWarningSwitch.isChecked
            bean.warningValue = binding.layoutSWBRMonitor.etBrWarningValue.text.toString().trim().toInt()
            addLogBean("setSWBRMonitor", bean)
            ControlBleTools.getInstance().setSWBRMonitor(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSWBRMonitor state=$state")
                }
            })
        }

    }

    private fun initCallback() {
        MySettingMenuCallBack.onWorldClockResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onWorldClockResult", bean!!)
        })

        MySettingMenuCallBack.onHeartRateMonitorResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onHeartRateMonitorResult", bean!!)
        })

        MySettingMenuCallBack.onContinuousBloodOxygenSetting.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onContinuousBloodOxygenSetting", bean!!)
        })

        MySettingMenuCallBack.onBodyTemperatureSettingResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onBodyTemperatureSettingResult", bean!!)
        })

        MySettingMenuCallBack.onPressureModeResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onPressureModeResult", bean!!)
        })

        MySettingMenuCallBack.onSleepModeResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSleepModeResult", bean!!)
        })

        MySettingMenuCallBack.onSleepModeResult.observe(this, Observer { value ->
            addLogI("MySettingMenuCallBack.rapidEyeMovement value=$value")
        })

        MySettingMenuCallBack.onSWSPO2Monitor.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSWSPO2Monitor", bean!!)
        })

        MySettingMenuCallBack.onSWHRMonitor.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSWHRMonitor", bean!!)
        })

        MySettingMenuCallBack.onSWBRMonitor.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSWBRMonitor", bean!!)
        })

        MySettingMenuCallBack.onSWHRVMonitor.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSWHRVMonitor", bean!!)
        })

        CallBackUtils.musicCallBack = object : MusicCallBack {
            override fun onRequestMusic() {
                addLogI("musicCallBack onRequestMusic")
                syncMusic()
            }

            override fun onSyncMusic(errorCode: Int) {
                addLogI("musicCallBack onSyncMusic errorCode=$errorCode")
            }

            override fun onQuitMusic() {
                addLogI("musicCallBack onQuitMusic")
            }

            override fun onSendMusicCmd(command: Int) {
                addLogI("musicCallBack onSyncMusic command=$command")

                when (command) {
                    MusicProtos.SEPlayerControlCommand.PLAYING_VALUE, MusicProtos.SEPlayerControlCommand.PAUSE_VALUE -> {
                        MusicUtils.controlMusic(this@SetFunActivity, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                    }

                    MusicProtos.SEPlayerControlCommand.PREV_VALUE -> {
                        MusicUtils.controlMusic(this@SetFunActivity, KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                    }

                    MusicProtos.SEPlayerControlCommand.NEXT_VALUE -> {
                        MusicUtils.controlMusic(this@SetFunActivity, KeyEvent.KEYCODE_MEDIA_NEXT)
                    }

                    MusicProtos.SEPlayerControlCommand.ADJUST_VOLUME_UP_VALUE -> {
                        if (audioManager != null) {
                            audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
                        }
                        syncMusic()
                    }

                    MusicProtos.SEPlayerControlCommand.ADJUST_VOLUME_DOWN_VALUE -> {
                        if (audioManager != null) {
                            audioManager!!.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND)
                        }
                        syncMusic()
                    }
                }
            }
        }

        CallBackUtils.stockCallBack = object : StockCallBack {
            override fun onStockInfoList(list: MutableList<StockSymbolBean?>) {
                addLogBean("stockCallBack onStockInfoList", list)
            }

            override fun onWearRequestStock() {
                addLogI("StockCallBack onWearRequestStock")
            }
        }

        CallBackUtils.realTimeHeartRateCallback = object : RealTimeHeartRateCallback {
            override fun onConfigResult(bean: RealTimeHeartRateConfigBean) {
                addLogBean("RealTimeHeartRateCallback onConfigResult", bean)
            }

            override fun onDataResult(timeMillis: Long, hrValue: Int) {
                addLogI("RealTimeHeartRateCallback onDataResult timeMillis=$timeMillis hrValue=$hrValue")
            }
        }
    }

    private var audioManager: AudioManager? = null
    private fun syncMusic() {
        if (audioManager == null) {
            audioManager = this.getSystemService(AUDIO_SERVICE) as AudioManager?
        }
        val mMusicTitle = binding.layoutMusic.etMusicTitle.getText().toString().trim()
        val mMusicState = binding.layoutMusic.etMusicState.getText().toString().trim().toInt()

        var mCurrent = 0
        audioManager?.let { mCurrent = it.getStreamVolume(AudioManager.STREAM_MUSIC) }

        var mMaxVolume = 0
        audioManager?.let { mMaxVolume = it.getStreamMaxVolume(AudioManager.STREAM_MUSIC) }

        val isNewPermissionType = binding.layoutMusic.cbIsNewPermission.isChecked
        val bean = MusicInfoBean(mMusicState, mMusicTitle, mCurrent, mMaxVolume, isNewPermissionType)
        addLogBean("syncMusicInfo", bean)
        ControlBleTools.getInstance().syncMusicInfo(bean, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                addLogI("syncMusicInfo state=$state")
            }
        })
    }

    private fun toTimezoneInt(s: String): Int {
        var value = 0
        if (s.contains("+")) {
            val replace = s.replace("+", "")
            val date = str2Date(replace, "HH:mm")
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                value = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            }
        } else if (s.contains("-")) {
            val replace = s.replace("-", "")
            val date = str2Date(replace, "HH:mm")
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                value = -(calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE))
            }
        }
        return value
    }

    fun str2Date(time: String?, pattern: String): Date? {
        val mFormatter = SimpleDateFormat(pattern, Locale.ENGLISH)
        try {
            return time?.takeIf { it.isNotEmpty() }?.let { mFormatter.parse(it) }
        } catch (e: ParseException) {
            e.printStackTrace()
            return null
        }
    }
}