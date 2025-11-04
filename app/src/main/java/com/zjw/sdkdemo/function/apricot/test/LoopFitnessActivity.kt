package com.zjw.sdkdemo.function.apricot.test

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.PathUtils
import com.blankj.utilcode.util.TimeUtils
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ActivityDurationBean
import com.zhapp.ble.bean.AutoActiveSportBean
import com.zhapp.ble.bean.ContinuousBloodOxygenBean
import com.zhapp.ble.bean.ContinuousHeartRateBean
import com.zhapp.ble.bean.ContinuousPressureBean
import com.zhapp.ble.bean.ContinuousTemperatureBean
import com.zhapp.ble.bean.DailyBean
import com.zhapp.ble.bean.EffectiveStandingBean
import com.zhapp.ble.bean.ExaminationBean
import com.zhapp.ble.bean.OffEcgDataBean
import com.zhapp.ble.bean.OfflineBloodOxygenBean
import com.zhapp.ble.bean.OfflineHeartRateBean
import com.zhapp.ble.bean.OfflinePressureDataBean
import com.zhapp.ble.bean.OfflineTemperatureDataBean
import com.zhapp.ble.bean.OverallDayMovementData
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
import com.zhapp.ble.callback.FitnessDataCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityLoopFitnessBinding
import com.zjw.sdkdemo.livedata.BleConnectState
import com.zjw.sdkdemo.utils.DescriptionUtils
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.MyFileUtils.saveLog
import com.zjw.sdkdemo.utils.ToastUtils

@SuppressLint("SetTextI18n")
class LoopFitnessActivity : BaseActivity() {
    private val binding: ActivityLoopFitnessBinding by lazy { ActivityLoopFitnessBinding.inflate(layoutInflater) }


    private var mCount = 0
    private var mInterval = 0L

    //成功次数
    private var sucNum = 0

    //失败次数
    private var fillNum = 0

    private var recordFilePath = ""

    var isStartLoop = false
    var mTimerHandler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_loop_sync)
        initData()
        initListener()
        initCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        ControlBleTools.getInstance().realTimeDataSwitch(false, null)
    }

    private fun initData() {
        recordFilePath = PathUtils.getAppDataPathExternalFirst() + "/ring/同步数据行为日志" + TimeUtils.getNowString(TimeUtils.getSafeDateFormat("yyyyMMdd_HH:mm:ss")) + ".txt"
        binding.tvLog.text = getString(R.string.input_log_info) + "\n" + recordFilePath
        ControlBleTools.getInstance().realTimeDataSwitch(true, null)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnStart) {
            mCount = binding.etCount.text?.trim().toString().toInt()
            mInterval = binding.etInterval.text?.trim().toString().toLong()

            if (mCount < 1) {
                ToastUtils.showToast("参数异常，重新填写")
                return@clickCheckConnect
            }
            mInterval *= (1 + (Math.random() * 10).toInt())
            if (mInterval < 1000) {
                ToastUtils.showToast("参数异常，重新填写")
                return@clickCheckConnect
            }
            if (!isStartLoop) {
                //开启循环
                isStartLoop = true
                binding.btnStart.text = "结束循环"
                binding.tvCount.text = "循环总次数：0"
                binding.tvSucNum.text = "成功次数：$sucNum"
                binding.tvFailNum.text = "失败次数：$fillNum"
                loop()
            } else {
                //结束循环
                isStartLoop = false
                binding.btnStart.text = "开始循环"
                mCount = 0
                mInterval = 0
                sucNum = 0
                fillNum = 0
            }

        }

    }


    fun initCallback() {

        BleConnectState.observe(this) { state ->
            binding.tvStatus.text = DescriptionUtils.getConnectStateStr(this@LoopFitnessActivity, state!!)
            saveLog(recordFilePath, binding.tvStatus.text.toString())

            when (state) {
                BleCommonAttributes.STATE_CONNECTED -> {
                    loop()
                }
                BleCommonAttributes.STATE_TIME_OUT -> {
                    binding.tvStatus.text = getString(R.string.input_connect_state) + getString(R.string.connect_state_4)
                    if (sucNum + fillNum < mCount) {
                        saveLog(recordFilePath, binding.tvStatus.text.toString())
                        binding.tvFailNum.text = "失败次数：${++fillNum}"
                        binding.tvCount.text = "循环总次数：${sucNum + fillNum}"
                        saveLog(recordFilePath, "总次数：$mCount 成功次数：$sucNum 失败次数：$fillNum")
                        loop()
                    } else {
                        binding.btnStart.text = "循环已完成"
                        ControlBleTools.getInstance().disconnect()
                    }
                }
            }
        }

        //日常数据回调
        CallBackUtils.fitnessDataCallBack = object : FitnessDataCallBack {
            override fun onProgress(progress: Int, total: Int) {
                mTimerHandler.removeCallbacksAndMessages(null)

                binding.tvDailyProgress.text = "同步进度 :progress  $progress  total $total"
                if (progress == total) {
                    binding.tvDailyProgress.text = "同步进度 :同步完成"
                    binding.tvSucNum.text = "成功次数：${++sucNum}"
                    binding.tvCount.text = "循环总次数：${sucNum + fillNum}"
                    saveLog(recordFilePath, "总次数：$mCount 成功次数：$sucNum 失败次数：$fillNum")
                    Handler(Looper.myLooper()!!).postDelayed({
                        loop()
                    }, mInterval)
                } else {
                    mTimerHandler.postDelayed({
                        binding.tvDailyProgress.text = "同步进度 :同步失败"
                        //超时失败
                        binding.tvFailNum.text = "失败次数：${++fillNum}"
                        binding.tvCount.text = "循环总次数：${sucNum + fillNum}"
                        saveLog(recordFilePath, "总次数：$mCount 成功次数：$sucNum 失败次数：$fillNum")
                        loop()
                    }, 3 * 11 * 1000L)
                }
                saveLog(recordFilePath, binding.tvDailyProgress.text.toString())

            }

            override fun onDailyData(data: DailyBean?) {
            }

            override fun onSleepData(data: SleepBean?) {
            }

            override fun onContinuousHeartRateData(data: ContinuousHeartRateBean?) {
            }

            override fun onOfflineHeartRateData(data: OfflineHeartRateBean?) {
            }

            override fun onContinuousBloodOxygenData(data: ContinuousBloodOxygenBean?) {
            }

            override fun onOfflineBloodOxygenData(data: OfflineBloodOxygenBean?) {
            }

            override fun onContinuousPressureData(data: ContinuousPressureBean?) {
            }

            override fun onOfflinePressureData(data: OfflinePressureDataBean?) {
            }

            override fun onContinuousTemperatureData(data: ContinuousTemperatureBean?) {
            }

            override fun onOfflineTemperatureData(data: OfflineTemperatureDataBean?) {
            }

            override fun onEffectiveStandingData(data: EffectiveStandingBean?) {
            }

            override fun onActivityDurationData(data: ActivityDurationBean?) {
            }

            override fun onOffEcgData(data: OffEcgDataBean?) {
            }

            override fun onExaminationData(data: ExaminationBean?) {
            }

            override fun onRingTodayActiveTypeData(bean: TodayActiveTypeData?) {
            }

            override fun onRingOverallDayMovementData(bean: OverallDayMovementData?) {
            }

            override fun onRingTodayRespiratoryRateData(bean: TodayRespiratoryRateData?) {
            }

            override fun onRingHealthScore(bean: RingHealthScoreBean?) {
            }

            override fun onRingSleepResult(bean: RingSleepResultBean?) {
            }

            override fun onRingBatteryData(bean: RingBatteryBean?) {
            }

            override fun onDrinkWaterData(bean: DrinkWaterBean?) {

            }

            override fun onRingSleepNAP(list: MutableList<RingSleepNapBean>?) {
            }

            override fun onRingAutoActiveSportData(data: AutoActiveSportBean?) {
            }

            override fun onRingBodyBatteryData(data: RingBodyBatteryBean?) {
            }

            override fun onRingStressDetectionData(data: RingStressDetectionBean?) {
            }
        }
    }


    private fun loop() {
        if (isStartLoop) {
            if (sucNum + fillNum < mCount) {
                if (ControlBleTools.getInstance().isConnect) {
                    val num = 1 + (Math.random() * 6).toInt()
                    when (num) {
                        1 -> {
                            ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
                            ControlBleTools.getInstance().getDailyHistoryData(null)
                        }

                        2 -> {
                            ControlBleTools.getInstance().setTimeFormat(true, null)
                            ControlBleTools.getInstance().getDailyHistoryData(null)
                        }

                        3 -> {
                            ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
                            ControlBleTools.getInstance().getAutoSportData(null)
                            ControlBleTools.getInstance().getDailyHistoryData(null)
                        }

                        4 -> {
                            ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
                            ControlBleTools.getInstance().getFitnessSportIdsData(null)
                            ControlBleTools.getInstance().getDailyHistoryData(null)
                        }

                        5 -> {
                            ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
                            ControlBleTools.getInstance().getDeviceBattery(null)
                            ControlBleTools.getInstance().getDailyHistoryData(null)
                        }

                        6 -> {
                            ControlBleTools.getInstance().setTime(System.currentTimeMillis(), null)
                            ControlBleTools.getInstance().getDailyHistoryData(null)
                            //ControlBleTools.getInstance().getFirmwareLog(null)
                        }
                    }


                } else {
                    ControlBleTools.getInstance().connect(MyConstants.deviceName, MyConstants.deviceAddress, MyConstants.deviceProtocol)
                }
            } else {
                binding.btnStart.text = "循环已完成"
            }
        }
    }


}