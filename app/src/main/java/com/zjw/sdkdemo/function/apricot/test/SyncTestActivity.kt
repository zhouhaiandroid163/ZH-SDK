package com.zjw.sdkdemo.function.apricot.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.blankj.utilcode.util.GsonUtils
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
import com.zhapp.ble.bean.UserInfo
import com.zhapp.ble.bean.berry.DrinkWaterBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceInfoCallBack
import com.zhapp.ble.callback.FitnessDataCallBack
import com.zhapp.ble.callback.RealTimeDataCallBack
import com.zhapp.ble.callback.VerifyUserIdCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityTestSyncBinding

@SuppressLint("SetTextI18n")
class SyncTestActivity : BaseActivity() {
    private val binding by lazy { ActivityTestSyncBinding.inflate(layoutInflater) }
    private val tag = SyncTestActivity::class.java.simpleName

    // 是否同步数据中 Whether the data is being synchronized
    private var isSyncing = false

    // 日常数据
    private var dailyResult = StringBuffer()

    private var mHandler: Handler? = null

    // 同步中延时其他指令时长
    private val deTime = 3 * 1000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_sync_send_cmd_test)
        initData()
        initListener()
        initCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler?.removeCallbacksAndMessages(null)
    }

    private fun initData() {
        mHandler = Handler(Looper.getMainLooper())
    }

    private fun initListener() {

        binding.btnClearLog.setOnClickListener {
            executeCheckConnect {
                binding.tvDailyResult.text = ""
                binding.tvDailyProgress.text = ""
                binding.tvOtherData.text = ""
            }
        }


        binding.btnGetDailyHistoryData.setOnClickListener {
            executeCheckConnect {
                //同步开始  Start syncing
                isSyncing = true
                ControlBleTools.getInstance().getDailyHistoryData(object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        Log.i(tag, "getDailyHistoryData state=$state")
                        if (state != SendCmdState.SUCCEED) {
                            isSyncing = false
                        }
                    }
                })
            }
        }

        binding.btnRealTimeDataSwitch.setOnClickListener {
            executeCheckConnect {
                if (isSyncing) {
                    mHandler?.removeCallbacks(runnableByRealTimeDataSwitch)
                    mHandler?.postDelayed(runnableByRealTimeDataSwitch, deTime)
                } else {
                    realTimeDataSwitch()
                }
            }
        }

        binding.btnGetDeviceInfo.setOnClickListener {
            executeCheckConnect {
                if (isSyncing) {
                    mHandler?.removeCallbacks(runnableByGetDeviceInfo)
                    mHandler?.postDelayed(runnableByGetDeviceInfo, deTime)
                } else {
                    getDeviceInfo()
                }
            }
        }

        binding.btnSetUserProfile.setOnClickListener {
            executeCheckConnect {
                if (isSyncing) {
                    mHandler?.removeCallbacks(runnableBySetUserProfile)
                    mHandler?.postDelayed(runnableBySetUserProfile, deTime)
                } else {
                    getDeviceInfo()
                }
            }
        }

        binding.btnSetUserInformation.setOnClickListener {
            executeCheckConnect {
                if (isSyncing) {
                    mHandler?.removeCallbacks(runnableByUserInformation)
                    mHandler?.postDelayed(runnableByUserInformation, deTime)
                } else {
                    getDeviceInfo()
                }
            }
        }

        binding.btnVerifyUserId.setOnClickListener {
            executeCheckConnect {
                if (isSyncing) {
                    mHandler?.removeCallbacks(runnableByVerifyUserId)
                    mHandler?.postDelayed(runnableByVerifyUserId, deTime)
                } else {
                    getDeviceInfo()
                }
            }
        }
    }

    private fun initCallback() {

        CallBackUtils.fitnessDataCallBack = object : FitnessDataCallBack {
            override fun onProgress(progress: Int, total: Int) {
                Log.e(tag, "onProgress : progress $progress  total $total")
                if (progress == 0) {
                    //同步开始  Start syncing
                    isSyncing = true

                    dailyResult = StringBuffer()
                    binding.tvDailyResult.text = ""
                }
                binding.tvDailyProgress.text = "onProgress :progress  $progress  total $total"
                if (progress == total) {
                    //同步完成 Synchronization completed
                    isSyncing = false

                    binding.tvDailyResult.text = dailyResult.toString()
                    binding.tvDailyProgress.text = ""

                }
            }

            override fun onDailyData(data: DailyBean) {
                Log.e(tag, "onDailyData : $data")
                dailyResult.append("\n\nonDailyData : $data")
            }

            override fun onSleepData(data: SleepBean) {
                Log.e(tag, "onSleepData : $data")
                dailyResult.append("\n\nSleepBean : $data")
            }

            override fun onContinuousHeartRateData(data: ContinuousHeartRateBean) {
                Log.e(tag, "onContinuousHeartRateData : $data")
                dailyResult.append("\n\nContinuousHeartRateBean : $data")
            }

            override fun onOfflineHeartRateData(data: OfflineHeartRateBean) {
                Log.e(tag, "onOfflineHeartRateData : $data")
                dailyResult.append("\n\nOfflineHeartRateBean : $data")
            }

            override fun onContinuousBloodOxygenData(data: ContinuousBloodOxygenBean) {
                Log.e(tag, "onContinuousBloodOxygenData : $data")
                dailyResult.append("\n\nContinuousBloodOxygenBean : $data")
            }

            override fun onOfflineBloodOxygenData(data: OfflineBloodOxygenBean) {
                Log.e(tag, "onOfflineBloodOxygenData : $data")
                dailyResult.append("\n\nOfflineBloodOxygenBean : $data")
            }

            override fun onContinuousPressureData(data: ContinuousPressureBean) {
                Log.e(tag, "onContinuousPressureData : $data")
                dailyResult.append("\n\nContinuousPressureBean : $data")
            }

            override fun onOfflinePressureData(data: OfflinePressureDataBean) {
                Log.e(tag, "onOfflinePressureData : $data")
                dailyResult.append("\n\nOfflinePressureDataBean : $data")
            }

            override fun onContinuousTemperatureData(data: ContinuousTemperatureBean) {
                Log.e(tag, "onContinuousTemperatureData : $data")
                dailyResult.append("\n\nContinuousTemperatureBean : $data")
            }

            override fun onOfflineTemperatureData(data: OfflineTemperatureDataBean) {
                Log.e(tag, "onOfflineTemperatureData : $data")
                dailyResult.append("\n\nOfflineTemperatureDataBean : $data")
            }

            override fun onEffectiveStandingData(data: EffectiveStandingBean) {
                Log.e(tag, "onEffectiveStandingData : $data")
                dailyResult.append("\n\nEffectiveStandingBean : $data")
            }

            override fun onActivityDurationData(data: ActivityDurationBean) {
                dailyResult.append("\n\nActivityDurationBean : $data")
            }

            override fun onOffEcgData(data: OffEcgDataBean) {
                dailyResult.append("\n\nOffEcgDataBean : $data")
            }

            override fun onExaminationData(data: ExaminationBean) {
                Log.e(tag, "ExaminationBean : " + GsonUtils.toJson(data))
                dailyResult.append("\n\nExaminationBean : ${GsonUtils.toJson(data)}")
            }

            override fun onRingTodayActiveTypeData(bean: TodayActiveTypeData) {
                Log.e(tag, "TodayActivityIndicatorsBean : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nTodayActivityIndicatorsBean : ${GsonUtils.toJson(bean)}")

            }

            override fun onRingOverallDayMovementData(bean: OverallDayMovementData) {
                Log.e(tag, "onRingOverallDayMovementData : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nTodayActivityIndicatorsBean : ${GsonUtils.toJson(bean)}")
            }

            override fun onRingTodayRespiratoryRateData(bean: TodayRespiratoryRateData) {
                Log.e(tag, "onRingTodayRespiratoryRateData : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nonRingTodayRespiratoryRateData : ${GsonUtils.toJson(bean)}")
            }

            override fun onRingHealthScore(bean: RingHealthScoreBean) {
                Log.e(tag, "RingHealthScoreBean : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nRingHealthScoreBean : ${GsonUtils.toJson(bean)}")
            }

            override fun onRingSleepResult(bean: RingSleepResultBean) {
                Log.e(tag, "RingSleepResultBean : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nRingSleepResultBean : ${GsonUtils.toJson(bean)}")
            }

            override fun onRingBatteryData(bean: RingBatteryBean?) {
                Log.e(tag, "RingSleepResultBean : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nRingSleepResultBean : ${GsonUtils.toJson(bean)}")
            }

            override fun onDrinkWaterData(bean: DrinkWaterBean?) {
                Log.e(tag, "DrinkWaterBean : " + GsonUtils.toJson(bean))
                dailyResult.append("\n\nDrinkWaterBean : ${GsonUtils.toJson(bean)}")
            }

            override fun onRingSleepNAP(list: List<RingSleepNapBean>) {
                Log.e(tag, "RingSleepNapBean : " + GsonUtils.toJson(list))
                dailyResult.append("\n\nRingSleepNapBean : ${GsonUtils.toJson(list)}")
            }

            override fun onRingAutoActiveSportData(data: AutoActiveSportBean) {
                Log.e(tag, "AutoActiveSportData : " + GsonUtils.toJson(data))
                dailyResult.append("\n\nAutoActiveSportData : ${GsonUtils.toJson(data)}")
            }

            override fun onRingBodyBatteryData(data: RingBodyBatteryBean?) {
                Log.e(tag, "onRingBodyBatteryData : " + GsonUtils.toJson(data))
                dailyResult.append("\n\nRingBodyBatteryBean : ${GsonUtils.toJson(data)}")
            }

            override fun onRingStressDetectionData(data: RingStressDetectionBean?) {
                Log.e(tag, "onRingStressDetectionData : " + GsonUtils.toJson(data))
                dailyResult.append("\n\nRingStressDetectionBean : ${GsonUtils.toJson(data)}")
            }
        }

        CallBackUtils.realTimeDataCallback = object : RealTimeDataCallBack {
            override fun onResult(bean: RealTimeBean) {
                binding.tvOtherData.text = bean.toString() + "\n\n" + binding.tvOtherData.text.toString()
            }

            override fun onFail() {}
        }

        CallBackUtils.deviceInfoCallBack = object : DeviceInfoCallBack {
            override fun onDeviceInfo(deviceInfoBean: DeviceInfoBean) {
                val tmp = getString(R.string.broadcast_device_version_name) + deviceInfoBean.firmwareVersion + " \n" + getString(R.string.broadcast_device_type) + deviceInfoBean.equipmentNumber +
                        " \n" + getString(R.string.mac_address) + deviceInfoBean.mac + " \n" + getString(R.string.ns_number) + deviceInfoBean.serialNumber
                binding.tvOtherData.text = tmp + "\n\n" + binding.tvOtherData.text.toString()
            }

            override fun onBatteryInfo(capacity: Int, chargeStatus: Int) {
                Log.i(tag, "onBatteryInfo [0=未知，1=充电中，2=未充电，3=充满了]")
                Log.i(tag, "capacity=$capacity chargeStatus=$chargeStatus")
                val tmp = "capacity=$capacity chargeStatus=$chargeStatus"
                binding.tvOtherData.text = tmp + "\n\n" + binding.tvOtherData.text.toString()
            }
        }

        CallBackUtils.verifyUserIdCallBack = VerifyUserIdCallBack { state -> binding.tvOtherData.text = "onVerifyState:" + state + "\n\n" + binding.tvOtherData.text.toString() }

    }


    //region BUTTON A

    val runnableByRealTimeDataSwitch: Runnable = Runnable {
        realTimeDataSwitch()
    }


    private fun realTimeDataSwitch() {
        if (ControlBleTools.getInstance().isConnect) {
            ControlBleTools.getInstance().realTimeDataSwitch(true, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    Log.i(tag, "realTimeDataSwitch state=$state")
                }
            })
        }
    }
    //endregion

    //region BUTTON B

    val runnableByGetDeviceInfo: Runnable = Runnable {
        getDeviceInfo()
    }

    private fun getDeviceInfo() {
        if (ControlBleTools.getInstance().isConnect) {
            ControlBleTools.getInstance().getDeviceInfo(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    Log.i(tag, "getDeviceInfo state=$state")
                }
            })
        }
    }
    //endregion

    //region BUTTON C

    val runnableBySetUserProfile: Runnable = Runnable {
        setUserProfile()
    }


    private fun setUserProfile() {
        if (ControlBleTools.getInstance().isConnect) {
            val bean = UserInfo()
            ControlBleTools.getInstance().setUserProfile(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    Log.i(tag, "setUserProfile state=$state")
                }
            })
        }
    }
    //endregion

    //region BUTTON D

    val runnableByUserInformation: Runnable = Runnable {
        setUserInformation()
    }


    private fun setUserInformation() {
        if (ControlBleTools.getInstance().isConnect) {
            val userInfo = UserInfo()
            ControlBleTools.getInstance().setUserInformation(0, 0, userInfo, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    Log.i(tag, "setUserInformation state=$state")
                }
            })
        }
    }
    //endregion


    //region BUTTON E

    val runnableByVerifyUserId: Runnable = Runnable {
        verifyUserId()
    }


    private fun verifyUserId() {
        if (ControlBleTools.getInstance().isConnect) {
            ControlBleTools.getInstance().verifyUserId("", object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    Log.i(tag, "verifyUserId state=$state")
                }
            })
        }
    }
    //endregion


}