package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ActiveMeasureParamsBean
import com.zhapp.ble.bean.ActiveMeasureResultBean
import com.zhapp.ble.bean.ActiveMeasureStatusBean
import com.zhapp.ble.bean.ActiveMeasuringBean
import com.zhapp.ble.callback.ActiveMeasureCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.WearActiveMeasureCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityActiveMeasureBinding

class MeasureActivity : BaseActivity() {
    private val binding by lazy { ActivityActiveMeasureBinding.inflate(layoutInflater) }
    private val tag: String = MeasureActivity::class.java.simpleName

    companion object {
        const val DEVICE_TYPE_TAG = "DEVICE_TYPE_TAG"
        const val MEASURE_TYPE_TAG = "MEASURE_TYPE_TAG"
    }

    //0=戒指,1=手环 0=Ring, 1=Bracelet
    private var deviceType = 0

    private var measureType = 0
    private lateinit var timeoutHeader: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_measure)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initData()
        initListener()
        initCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        executeCheckConnect {
            stopMeasure()
        }
    }

    private fun initData() {
        timeoutHeader = Handler(Looper.getMainLooper())
        deviceType = intent.getIntExtra(DEVICE_TYPE_TAG, 0)
        measureType = intent.getIntExtra(MEASURE_TYPE_TAG, 1)
        if (deviceType == 0) {
            setTitle(R.string.ring_measure)
        } else {
            setTitle(R.string.ch_measure)
        }
        when (measureType) {
            ActiveMeasureCallBack.MeasureType.HEART_RATE.type -> {
                setTitle(R.string.heart)
            }

            ActiveMeasureCallBack.MeasureType.BLOOD_OXYGEN.type -> {
                setTitle(R.string.spo2)
            }

            ActiveMeasureCallBack.MeasureType.STRESS_HRV.type -> {
                setTitle(R.string.hrv_pressure)
            }

            ActiveMeasureCallBack.MeasureType.BODY_TEMPERATURE.type -> {
                setTitle(R.string.temperature)
            }

            ActiveMeasureCallBack.MeasureType.STRESS.type -> {
                setTitle(R.string.pressure)
            }
        }
        binding.btnStop.isEnabled = false
    }

    private fun initListener() {
        clickCheckConnect(binding.btnStart) {
            addLogI("btnStart")
            startMeasure()
        }
        clickCheckConnect(binding.btnStop) {
            addLogI("btnStop")
            stopMeasure()
        }
    }

    private fun initCallback() {
        if (deviceType == 0) {
            CallBackUtils.activeMeasureCallBack = object : ActiveMeasureCallBack {
                override fun onMeasureStatus(bean: ActiveMeasureStatusBean) {
                    addLogBean("activeMeasureCallBack onMeasureStatus", bean)

                    binding.tvState.text = bean.toString()
                    if (!bean.isSuccess) {
                        binding.btnStart.isEnabled = true
                        binding.btnStop.isEnabled = false
                    }
                    if (bean.measureTime != 0) {
                        timeoutHeader.postDelayed({
                            binding.btnStart.isEnabled = true
                            binding.btnStop.isEnabled = false
                        }, bean.measureTime * 1000L)
                    }
                }

                override fun onMeasuring(bean: ActiveMeasuringBean) {
                    addLogBean("activeMeasureCallBack onMeasuring", bean)
                    binding.tvData.append(bean.toString() + "\n")
                }

                override fun onMeasureResult(bean: ActiveMeasureResultBean) {
                    addLogBean("activeMeasureCallBack onMeasureResult", bean)
                    binding.btnStart.isEnabled = true
                    binding.btnStop.isEnabled = false
                    binding.tvResult.text = bean.toString()
                }
            }
        } else {
            CallBackUtils.wearActiveMeasureCallBack = object : WearActiveMeasureCallBack {
                override fun onMeasureStatus(bean: ActiveMeasureStatusBean) {
                    addLogBean("wearActiveMeasureCallBack onMeasureStatus", bean)
                    binding.tvState.text = bean.toString()
                    if (!bean.isSuccess) {
                        binding.btnStart.isEnabled = true
                        binding.btnStop.isEnabled = false
                    }
                    if (bean.measureTime != 0) {
                        timeoutHeader.postDelayed({
                            binding.btnStart.isEnabled = true
                            binding.btnStop.isEnabled = false
                        }, bean.measureTime * 1000L)
                    }
                }

                override fun onMeasuring(bean: ActiveMeasuringBean) {
                    addLogBean("wearActiveMeasureCallBack onMeasuring", bean)
                    binding.tvData.append(bean.toString() + "\n")
                }

                override fun onMeasureResult(bean: ActiveMeasureResultBean) {
                    addLogBean("wearActiveMeasureCallBack onMeasureResult", bean)
                    binding.btnStart.isEnabled = true
                    binding.btnStop.isEnabled = false
                    binding.tvResult.text = bean.toString()
                }
            }
        }
    }

    /**
     * 开始测量
     */
    fun startMeasure() {
        binding.btnStart.isEnabled = false
        binding.btnStop.isEnabled = true
        binding.tvState.text = ""
        binding.tvData.text = ""
        binding.tvResult.text = ""

        val bean = ActiveMeasureParamsBean()
        bean.measureType = measureType
        bean.isSwitchMeasure = true
        if (deviceType == 0) {
            addLogBean("activeMeasurementStart", bean)
            ControlBleTools.getInstance().activeMeasurementStart(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("activeMeasurementStart state=$state")
                    if (state != SendCmdState.SUCCEED) {
                        binding.btnStart.isEnabled = true
                        binding.btnStop.isEnabled = false
                    }

                }
            })
        } else {
            addLogBean("wearActiveMeasurementStart", bean)
            ControlBleTools.getInstance().wearActiveMeasurementStart(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("wearActiveMeasurementStart state=$state")
                    if (state != SendCmdState.SUCCEED) {
                        binding.btnStart.isEnabled = true
                        binding.btnStop.isEnabled = false
                    }
                }
            })
        }
    }

    /**
     * 结束测量
     */
    fun stopMeasure() {
        binding.btnStart.isEnabled = true
        binding.btnStop.isEnabled = false
        val bean = ActiveMeasureParamsBean()
        bean.measureType = measureType
        bean.isSwitchMeasure = false
        if (deviceType == 0) {
            addLogBean("activeMeasurementStop",bean)
            ControlBleTools.getInstance().activeMeasurementStop(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("activeMeasurementStop state=$state")
                }
            })
        } else {
            addLogBean("wearActiveMeasurementStop",bean)
            ControlBleTools.getInstance().wearActiveMeasurementStop(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("wearActiveMeasurementStop state=$state")
                }
            })
        }
    }
}