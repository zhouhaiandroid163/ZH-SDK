package com.zjw.sdkdemo.function.apricot

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.TimeUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ActivityDurationBean
import com.zhapp.ble.bean.AutoActiveSportBean
import com.zhapp.ble.bean.ContinuousBloodOxygenBean
import com.zhapp.ble.bean.ContinuousHeartRateBean
import com.zhapp.ble.bean.ContinuousPressureBean
import com.zhapp.ble.bean.ContinuousTemperatureBean
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.DeviceInfoBean
import com.zhapp.ble.bean.EffectiveStandingBean
import com.zhapp.ble.bean.ExaminationBean
import com.zhapp.ble.bean.MptPowerLogBean
import com.zhapp.ble.bean.OffEcgDataBean
import com.zhapp.ble.bean.OfflineBloodOxygenBean
import com.zhapp.ble.bean.OfflineHeartRateBean
import com.zhapp.ble.bean.OfflinePressureDataBean
import com.zhapp.ble.bean.OfflineTemperatureDataBean
import com.zhapp.ble.bean.OverallDayMovementData
import com.zhapp.ble.bean.RealTimeBean
import com.zhapp.ble.bean.RingBatteryBean
import com.zhapp.ble.bean.RingBodyBatteryBean
import com.zhapp.ble.bean.RingHealthScoreBean
import com.zhapp.ble.bean.RingSleepNapBean
import com.zhapp.ble.bean.RingSleepResultBean
import com.zhapp.ble.bean.RingStressDetectionBean
import com.zhapp.ble.bean.SleepBean
import com.zhapp.ble.bean.TodayActiveTypeData
import com.zhapp.ble.bean.TodayRespiratoryRateData
import com.zhapp.ble.bean.berry.DrinkWaterBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceInfoCallBack
import com.zhapp.ble.callback.DeviceMptPowerLogCallBack
import com.zhapp.ble.callback.FitnessDataCallBack
import com.zhapp.ble.callback.RealTimeDataCallBack
import com.zhapp.ble.callback.SportParsingProgressCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityDataBinding
import com.zjw.sdkdemo.function.berry.BerryDeviceHistoryLogActivity
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.livedata.MySportCallBack

class DataActivity : BaseActivity() {
    private val binding by lazy { ActivityDataBinding.inflate(layoutInflater) }
    private val tag: String = DataActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_data)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnDataGetSimpleSettingSummary) {
            addLogI("btnDataGetSimpleSettingSummary")
            addLogI("getSimpleSetting")
            ControlBleTools.getInstance().getSimpleSetting(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSimpleSetting state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetDevice) {
            addLogI("btnGetDevice")
            addLogI("getDeviceInfo")
            ControlBleTools.getInstance().getDeviceInfo(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDeviceInfo state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetBattery) {
            addLogI("btnGetBattery")
            addLogI("getDeviceBattery")
            ControlBleTools.getInstance().getDeviceBattery(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDeviceBattery state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetBatteryUseList) {
            addLogI("btnGetBatteryUseList")
            addLogI("getMptPowerLogList")
            ControlBleTools.getInstance().getMptPowerLogList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getMptPowerLogList state=$state")
                }
            })

        }

        clickCheckConnect(binding.btnRealDataSwitch) {
            addLogI("btnRealDataSwitch")
            if (binding.btnRealDataSwitch.tag == 1) {
                binding.btnRealDataSwitch.tag = 0
            } else {
                binding.btnRealDataSwitch.tag = 1
            }
            val value = binding.btnRealDataSwitch.tag == 1
            addLogI("realTimeDataSwitch value=$value")
            ControlBleTools.getInstance().realTimeDataSwitch(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("realTimeDataSwitch state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetFitness) {
            addLogI("btnGetFitness")
            addLogI("getDailyHistoryData")
            ControlBleTools.getInstance().getDailyHistoryData(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDailyHistoryData state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetSport) {
            addLogI("btnGetSport")
            addLogI("getFitnessSportIdsData")
            ControlBleTools.getInstance().getFitnessSportIdsData(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getFitnessSportIdsData state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnGetDeviceHistoryLog) {
            addLogI("btnGetDeviceHistoryLog")
            startActivity(Intent(this, DeviceHistoryLogActivity::class.java))
        }

        clickCheckConnect(binding.btnGetDeviceHistoryLogBerry) {
            addLogI("btnGetDeviceHistoryLogBerry")
            startActivity(Intent(this, BerryDeviceHistoryLogActivity::class.java))
        }
    }


    private fun initCallback() {
        MySettingMenuCallBack.onSimpleSettingResult.observe(this) { bean ->
            addLogBean("MySettingMenuCallBack.onSimpleSettingResult", bean!!)
        }

        MySportCallBack.onDevSportInfo.observe(this, Observer { bean ->
            addLogBean("MySportCallBack.onDevSportInfo", bean!!)
        })

        CallBackUtils.sportParsingProgressCallBack = SportParsingProgressCallBack { progress, total ->
            addLogI("sportParsingProgressCallBack progress=$progress total=$total")
        }

        CallBackUtils.deviceInfoCallBack = object : DeviceInfoCallBack {
            override fun onDeviceInfo(bean: DeviceInfoBean) {
                addLogBean("deviceInfoCallBack onDeviceInfo", bean)
            }

            override fun onBatteryInfo(capacity: Int, chargeStatus: Int) {
                addLogI("deviceInfoCallBack onBatteryInfo capacity=$capacity chargeStatus=$chargeStatus")
            }
        }

        CallBackUtils.realTimeDataCallback = object : RealTimeDataCallBack {
            override fun onResult(bean: RealTimeBean) {
                addLogBean("realTimeDataCallback onResult", bean)
            }

            override fun onFail() {
                addLogI("realTimeDataCallback onFail")
            }
        }


        CallBackUtils.fitnessDataCallBack = object : FitnessDataCallBack {
            override fun onProgress(progress: Int, total: Int) {
                addLogI("fitnessDataCallBack onProgress progress=$progress total=$total")
            }
            override fun onDailyData(bean: DailyBean) {
                addLogBean("fitnessDataCallBack onDailyData", bean)
            }

            override fun onSleepData(bean: SleepBean) {
                addLogBean("fitnessDataCallBack onSleepData", bean)
            }

            override fun onContinuousHeartRateData(bean: ContinuousHeartRateBean) {
                addLogBean("fitnessDataCallBack onContinuousHeartRateData", bean)
            }

            override fun onOfflineHeartRateData(bean: OfflineHeartRateBean) {
                addLogBean("fitnessDataCallBack onOfflineHeartRateData", bean)
            }

            override fun onContinuousBloodOxygenData(bean: ContinuousBloodOxygenBean) {
                addLogBean("fitnessDataCallBack onContinuousBloodOxygenData", bean)
            }

            override fun onOfflineBloodOxygenData(bean: OfflineBloodOxygenBean) {
                addLogBean("fitnessDataCallBack onOfflineBloodOxygenData", bean)
            }

            override fun onContinuousPressureData(bean: ContinuousPressureBean) {
                addLogBean("fitnessDataCallBack onContinuousPressureData", bean)
            }

            override fun onOfflinePressureData(bean: OfflinePressureDataBean) {
                addLogBean("fitnessDataCallBack onOfflinePressureData", bean)
            }

            override fun onContinuousTemperatureData(bean: ContinuousTemperatureBean) {
                addLogBean("fitnessDataCallBack onContinuousTemperatureData", bean)
            }

            override fun onOfflineTemperatureData(bean: OfflineTemperatureDataBean) {
                addLogBean("fitnessDataCallBack onOfflineTemperatureData", bean)
            }

            override fun onEffectiveStandingData(bean: EffectiveStandingBean) {
                addLogBean("fitnessDataCallBack onEffectiveStandingData", bean)
            }

            override fun onActivityDurationData(bean: ActivityDurationBean) {
                addLogBean("fitnessDataCallBack onActivityDurationData", bean)
            }

            override fun onOffEcgData(bean: OffEcgDataBean) {
                addLogBean("fitnessDataCallBack onOffEcgData", bean)
            }

            override fun onExaminationData(bean: ExaminationBean) {
                addLogBean("fitnessDataCallBack onExaminationData", bean)
            }

            override fun onRingTodayActiveTypeData(bean: TodayActiveTypeData) {
                addLogBean("fitnessDataCallBack onRingTodayActiveTypeData", bean)
            }

            override fun onRingOverallDayMovementData(bean: OverallDayMovementData) {
                addLogBean("fitnessDataCallBack onRingOverallDayMovementData", bean)
            }

            override fun onRingTodayRespiratoryRateData(bean: TodayRespiratoryRateData) {
                addLogBean("fitnessDataCallBack onRingTodayRespiratoryRateData", bean)
            }

            override fun onRingHealthScore(bean: RingHealthScoreBean) {
                addLogBean("fitnessDataCallBack onRingHealthScore", bean)
            }

            override fun onRingSleepResult(bean: RingSleepResultBean) {
                addLogBean("fitnessDataCallBack onRingSleepResult", bean)
            }

            override fun onRingBatteryData(bean: RingBatteryBean) {
                addLogBean("fitnessDataCallBack onRingBatteryData", bean)
            }

            override fun onDrinkWaterData(bean: DrinkWaterBean) {
                addLogBean("fitnessDataCallBack onDrinkWaterData", bean)
            }

            override fun onRingSleepNAP(list: MutableList<RingSleepNapBean>) {
                addLogBean("fitnessDataCallBack onRingSleepNAP", list)
            }

            override fun onRingAutoActiveSportData(bean: AutoActiveSportBean) {
                addLogBean("fitnessDataCallBack onRingAutoActiveSportData", bean)
            }

            override fun onRingBodyBatteryData(bean: RingBodyBatteryBean) {
                addLogBean("fitnessDataCallBack onRingBodyBatteryData", bean)
            }

            override fun onRingStressDetectionData(bean: RingStressDetectionBean) {
                addLogBean("fitnessDataCallBack onRingStressDetectionData", bean)
            }
        }

        CallBackUtils.deviceMptPowerLogCallBack = DeviceMptPowerLogCallBack { list ->
            addLogBean("deviceMptPowerLogCallBack", list)
            if (!list.isNullOrEmpty()) {
                showPowerLog(list[0])
            }
        }
    }


    data class EnergyData(val nameId: Int, val value: Float)

    private fun showPowerLog(bean: MptPowerLogBean) {
        addLogI("showPowerLog")
        addLogI(getString(R.string.record_data_start_time) + TimeUtils.millis2String(bean.startTimestamp * 1000L))
        addLogI(getString(R.string.record_data_end_time) + TimeUtils.millis2String(bean.endTimestamp * 1000L))
        addLogI(getString(R.string.record_power_value) + "${bean.powerPercent} %")

        val energyList: MutableList<EnergyData> = ArrayList()
        if (!bean.powerLogs.isNullOrEmpty()) {
            StringBuilder()
            for (powerLog in bean.powerLogs) {
                if (powerLog.id != 0) {
                    energyList.add(EnergyData(powerLog.id, powerLog.percent.toFloat()))
                }
            }
        }
    }
}