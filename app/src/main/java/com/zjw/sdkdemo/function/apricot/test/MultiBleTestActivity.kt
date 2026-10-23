package com.zjw.sdkdemo.function.apricot.test

import android.os.Bundle
import com.blankj.utilcode.util.GsonUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ActivityDurationBean
import com.zhapp.ble.bean.AutoActiveSportBean
import com.zhapp.ble.bean.ContinuousBloodOxygenBean
import com.zhapp.ble.bean.ContinuousHeartRateBean
import com.zhapp.ble.bean.ContinuousHeartRateSportFiveMinAfterBean
import com.zhapp.ble.bean.ContinuousPressureBean
import com.zhapp.ble.bean.ContinuousRRIDBean
import com.zhapp.ble.bean.ContinuousTemperatureBean
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.DevSportInfoBean
import com.zhapp.ble.bean.EffectiveStandingBean
import com.zhapp.ble.bean.ExaminationBean
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
import com.zhapp.ble.bean.SleepHRVBean
import com.zhapp.ble.bean.SleepRRIBean
import com.zhapp.ble.bean.SportRequestBean
import com.zhapp.ble.bean.SportStatusBean
import com.zhapp.ble.bean.TodayActiveTypeData
import com.zhapp.ble.bean.TodayRespiratoryRateData
import com.zhapp.ble.bean.berry.DrinkWaterBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.FitnessDataCallBack
import com.zhapp.ble.callback.RealTimeDataCallBack
import com.zhapp.ble.callback.SportCallBack
import com.zhapp.ble.callback.SportParsingProgressCallBack
import com.zhapp.ble.parsing.ParsingStateManager
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityMultiBleTestBinding
import com.zjw.sdkdemo.function.apricot.EvActivity

/**
 * Created by Android on 2026/10/19.
 */
class MultiBleTestActivity : BaseActivity() {
    private val binding by lazy { ActivityMultiBleTestBinding.inflate(layoutInflater) }
    private val tag: String = EvActivity::class.java.simpleName

    private var connectDevMacs = mutableListOf<String>()

    private var isRealTimeData = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_multi_ble)
        initLogSet(
            tag,
            binding.layoutLog.llLog,
            binding.layoutLog.cxLog,
            binding.layoutLog.llLogContent,
            binding.layoutLog.btnClear,
            binding.layoutLog.btnSet,
            binding.layoutLog.btnSendLog
        )
        initEvent()
        initCallback()
    }

    private fun initEvent() {
        clickCheckConnect(binding.btnGetConnectedDevices) {
            addLogI("btnGetConnectedDevices")
            val connectedDevices = ControlBleTools.getInstance().connectedDevices
            connectDevMacs.clear()
            connectDevMacs.addAll(connectedDevices)
            addLogI("connectedDevices=${GsonUtils.toJson(connectedDevices)}")
        }

        clickCheckConnect(binding.btnGetCurrentDevice) {
            addLogI("btnGetCurrentDevice")
            val currentDevice = ControlBleTools.getInstance().currentDeviceName
            val currentDeviceAddress = ControlBleTools.getInstance().currentDeviceMac
            val currentProtocol = ControlBleTools.getInstance().currentProtocol
            addLogI("currentDeviceName=$currentDevice,currentDeviceAddress=$currentDeviceAddress,currentProtocol=$currentProtocol")
        }

        clickCheckConnect(binding.btnGetDailyData) {
            addLogI("btnGetDailyData")
            connectDevMacs.forEach { mac ->
                ControlBleTools.getInstance().runForDevice(mac, { tools ->
                    tools.getDailyHistoryData(object : ParsingStateManager.SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("getDailyData state=$state,mac=$mac")
                        }
                    })
                })
            }
        }

        clickCheckConnect(binding.btnGetSportData){
            addLogI("btnGetSportData")
            connectDevMacs.forEach { mac ->
                ControlBleTools.getInstance().runForDevice(mac, { tools ->
                    tools.getFitnessSportIdsData(object : ParsingStateManager.SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("getFitnessSportIdsData state=$state,mac=$mac")
                        }
                    })
                })
            }
        }

        clickCheckConnect(binding.btnGetRealTimeData){
            addLogI("btnGetRealTimeData")
            isRealTimeData = !isRealTimeData
            connectDevMacs.forEach { mac ->
                ControlBleTools.getInstance().runForDevice(mac, { tools ->
                    tools.realTimeDataSwitch(isRealTimeData,object : ParsingStateManager.SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("getRealTimeData state=$state,mac=$mac")
                        }
                    })
                })
            }
        }
    }

    private fun initCallback() {
        CallBackUtils.fitnessDataCallBack = object : FitnessDataCallBack{
            override fun onProgress(progress: Int, total: Int) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onProgress mac=$mac,progress=$progress,total=$total")
            }

            override fun onDailyData(bean: DailyBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onDailyData mac=$mac,bean=$bean")
            }

            override fun onSleepData(bean: SleepBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onSleepData mac=$mac,bean=$bean")
            }

            override fun onContinuousHeartRateData(bean: ContinuousHeartRateBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onContinuousHeartRateData mac=$mac,bean=$bean")
            }

            override fun onOfflineHeartRateData(bean: OfflineHeartRateBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onOfflineHeartRateData mac=$mac,bean=$bean")
            }

            override fun onContinuousBloodOxygenData(bean: ContinuousBloodOxygenBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onContinuousBloodOxygenData mac=$mac,bean=$bean")
            }

            override fun onOfflineBloodOxygenData(bean: OfflineBloodOxygenBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onOfflineBloodOxygenData mac=$mac,bean=$bean")
            }

            override fun onContinuousPressureData(bean: ContinuousPressureBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onContinuousPressureData mac=$mac,bean=$bean")
            }

            override fun onOfflinePressureData(bean: OfflinePressureDataBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onOfflinePressureData mac=$mac,bean=$bean")
            }

            override fun onContinuousTemperatureData(bean: ContinuousTemperatureBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onContinuousTemperatureData mac=$mac,bean=$bean")
            }

            override fun onOfflineTemperatureData(bean: OfflineTemperatureDataBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onOfflineTemperatureData mac=$mac,bean=$bean")
            }

            override fun onEffectiveStandingData(bean: EffectiveStandingBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onEffectiveStandingData mac=$mac,bean=$bean")
            }

            override fun onActivityDurationData(bean: ActivityDurationBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onActivityDurationData mac=$mac,bean=$bean")
            }

            override fun onOffEcgData(bean: OffEcgDataBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onOffEcgData mac=$mac,bean=$bean")
            }

            override fun onExaminationData(bean: ExaminationBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onExaminationData mac=$mac,bean=$bean")
            }

            override fun onRingTodayActiveTypeData(bean: TodayActiveTypeData?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingTodayActiveTypeData mac=$mac,bean=$bean")
            }

            override fun onRingOverallDayMovementData(bean: OverallDayMovementData?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingOverallDayMovementData mac=$mac,bean=$bean")
            }

            override fun onRingTodayRespiratoryRateData(bean: TodayRespiratoryRateData?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingTodayRespiratoryRateData mac=$mac,bean=$bean")
            }

            override fun onRingHealthScore(bean: RingHealthScoreBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingHealthScore mac=$mac,bean=$bean")
            }

            override fun onRingSleepResult(bean: RingSleepResultBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingSleepResult mac=$mac,bean=$bean")
            }

            override fun onRingSleepNAP(list: List<RingSleepNapBean?>?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingSleepNAP mac=$mac,list=$list")
            }

            override fun onRingAutoActiveSportData(bean: AutoActiveSportBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingAutoActiveSportData mac=$mac,bean=$bean")
            }

            override fun onRingBodyBatteryData(bean: RingBodyBatteryBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingBodyBatteryData mac=$mac,bean=$bean")
            }

            override fun onRingStressDetectionData(bean: RingStressDetectionBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingStressDetectionData mac=$mac,bean=$bean")
            }

            override fun onRingBatteryData(bean: RingBatteryBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onRingBatteryData mac=$mac,bean=$bean")
            }

            override fun onDrinkWaterData(bean: DrinkWaterBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onDrinkWaterData mac=$mac,bean=$bean")
            }

            override fun onSleepRRIData(bean: SleepRRIBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onSleepRRIData mac=$mac,bean=$bean")
            }

            override fun onSleepHRVData(bean: SleepHRVBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onSleepHRVData mac=$mac,bean=$bean")
            }

            override fun onContinuousHeartRateSportFiveMinAfter(bean: ContinuousHeartRateSportFiveMinAfterBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onContinuousHeartRateSportFiveMinAfter mac=$mac,bean=$bean")
            }

            override fun onContinuousRRIData(bean: ContinuousRRIDBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onContinuousRRIData mac=$mac,bean=$bean")
            }
        }

        CallBackUtils.sportParsingProgressCallBack = object : SportParsingProgressCallBack{
            override fun onProgress(progress: Int, total: Int) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onProgress mac=$mac,progress=$progress,total=$total")
            }
        }

        CallBackUtils.sportCallBack = object : SportCallBack{
            override fun onDevSportInfo(data: DevSportInfoBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onDevSportInfo mac=$mac,data=$data")
            }

            override fun onSportStatus(statusBean: SportStatusBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onSportStatus mac=$mac,statusBean=$statusBean")
            }

            override fun onSportRequest(requestBean: SportRequestBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onSportRequest mac=$mac")
            }
        }

        CallBackUtils.realTimeDataCallback = object : RealTimeDataCallBack{
            override fun onResult(bean: RealTimeBean?) {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onResult mac=$mac,bean=$bean")
            }

            override fun onFail() {
                val mac = CallBackUtils.getCallbackDeviceMac()
                addLogI("onFail mac=$mac")
            }
        }
    }
}