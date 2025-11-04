package com.zjw.sdkdemo.function.apricot.ring

import android.os.Bundle
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.RingSportDataBean
import com.zhapp.ble.bean.RingSportStatusBean
import com.zhapp.ble.bean.SendRingSportStatusBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.RingSportCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityRingSportScreenBinding
import com.zjw.sdkdemo.livedata.BleConnectState
import com.zjw.sdkdemo.utils.DescriptionUtils

class RingSportScreenActivity : BaseActivity() {
    private val binding by lazy { ActivityRingSportScreenBinding.inflate(layoutInflater) }
    private val tag: String = RingSportScreenActivity::class.java.simpleName

    private var sportStartTime = 0L
    private var sportType = 0

    private val sportInit = -1
    private val sportRequestStart = -2

    private val sportNone = RingSportCallBack.RingSportStatus.SPORT_STATUS_NONE.status
    private val sportStart = RingSportCallBack.RingSportStatus.SPORT_STATUS_START.status
    private val sportPause = RingSportCallBack.RingSportStatus.SPORT_STATUS_PAUSE.status
    private val sportResume = RingSportCallBack.RingSportStatus.SPORT_STATUS_RESUME.status
    private val sportEnd = RingSportCallBack.RingSportStatus.SPORT_STATUS_END.status

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ring_sport_screen)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
        initData()
    }

    private fun initData() {
        binding.tvConnectState.text = DescriptionUtils.getConnectStateStr(this, BleConnectState.value!!)
        updateSportState(sportInit)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnGetSportState) {
            addLogI("btnGetSportState")
            addLogI("getRingSportStatus")
            ControlBleTools.getInstance().getRingSportStatus(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getRingSportStatus state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnStart) {
            addLogI("btnStart")
            sendSportState(sportRequestStart)
        }

        clickCheckConnect(binding.btnPause) {
            addLogI("btnPause")
            sendSportState(sportPause)
        }

        clickCheckConnect(binding.btnResume) {
            addLogI("btnResume")
            sendSportState(sportResume)
        }

        clickCheckConnect(binding.btnEnd) {
            addLogI("btnEnd")
            sendSportState(sportEnd)
        }
    }

    private fun initCallback() {
        BleConnectState.observe(this) { state ->
            binding.tvConnectState.text = DescriptionUtils.getConnectStateStr(this, state!!)
            updateSportState(sportInit)
            if (state == BleCommonAttributes.STATE_CONNECTED) {
                binding.btnGetSportState.callOnClick()
            }
        }

        CallBackUtils.ringSportCallBack = object : RingSportCallBack {
            override fun onRingSportStatus(bean: RingSportStatusBean) {
                addLogBean("ringSportCallBack onRingSportStatus", bean)


                //请求开始，回复异常
                if (bean.startResult != RingSportCallBack.RingSportStartResult.SPORT_START_RESULT_NONE.result) {
                    addLogBean("ringSportCallBack onRingSportStatus startResult", bean.startResult)
                }

                //运动结束
                if (bean.sportStatus == sportEnd) {
                    //异常结束
                    if (bean.endReason != RingSportCallBack.RingSportEndReason.SPORT_END_REASON_NONE.reason) {
                        addLogBean("ringSportCallBack onRingSportStatus endReason", bean.endReason)
                    }
                    //正常结束，有运动数据需要同步
                    if (bean.isSportNoSync) {
                        addLogI("ringSportCallBack onRingSportStatus sportNoSync")
                    }
                }

                //运动中
                if (bean.sportStatus == sportStart || bean.sportStatus == sportPause || bean.sportStatus == sportResume) {
                    sportType = bean.sportType
                    binding.etSportType.setText(bean.sportType.toString())
                    sportStartTime = bean.startTime
                }

                updateSportState(bean.sportStatus)

            }

            //运动中数据  Data in Sporting
            override fun onRingSportData(bean: RingSportDataBean) {
                addLogBean("ringSportCallBack onRingSportData", bean)
                binding.tvWearData.text = bean.toString()
            }
        }
    }

    private fun sendSportState(sportState: Int) {
        addLogI("sendSportState sportState=$sportState")
        when (sportState) {
            //APP发送开始指令，等待设备回复
            sportRequestStart -> {
                sportType = binding.etSportType.text.toString().trim().toInt()
                sportStartTime = System.currentTimeMillis() / 1000
                val bean = SendRingSportStatusBean(sportType, RingSportCallBack.RingSportStatus.SPORT_STATUS_START.status, sportStartTime)
                addLogBean("sendRingSportStatus",bean)
                ControlBleTools.getInstance().sendRingSportStatus(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("sendRingSportStatus state=$state")
                        updateSportState(sportRequestStart)
                    }
                })
            }
            //运动暂停
            sportPause -> {
                val bean = SendRingSportStatusBean(sportType, RingSportCallBack.RingSportStatus.SPORT_STATUS_PAUSE.status, sportStartTime)
                addLogBean("sendRingSportStatus",bean)
                ControlBleTools.getInstance().sendRingSportStatus(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("sendRingSportStatus state=$state")
                    }
                })
            }
            //运动继续
            sportResume -> {
                val bean = SendRingSportStatusBean(sportType, RingSportCallBack.RingSportStatus.SPORT_STATUS_RESUME.status, sportStartTime)
                addLogBean("sendRingSportStatus",bean)
                ControlBleTools.getInstance().sendRingSportStatus(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("sendRingSportStatus state=$state")
                    }
                })
            }
            //运动结束-正常
            sportEnd -> {
                val bean = SendRingSportStatusBean(sportType, RingSportCallBack.RingSportStatus.SPORT_STATUS_END.status, System.currentTimeMillis() / 1000)
                addLogBean("sendRingSportStatus",bean)
                ControlBleTools.getInstance().sendRingSportStatus(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("sendRingSportStatus state=$state")
                    }
                })
            }
        }
    }

    private fun updateSportState(sportState: Int) {
        addLogI("updateSportState sportState=$sportState")
        when (sportState) {
            //初始状态
            sportInit -> {
                sportType = 0
                sportStartTime = 0L
                binding.tvWearData.text = ""
                binding.etSportType.setText("")
                binding.tvSportState.text = getString(R.string.state_not_exercising)
                binding.btnStart.isEnabled = true
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = false
                binding.btnEnd.isEnabled = false
            }
            //APP发送开始指令，等待设备回复
            sportRequestStart -> {
                binding.tvSportState.text = getString(R.string.state_waiting_reply)
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = false
                binding.btnEnd.isEnabled = false
            }
            //未运动
            sportNone -> {
                sportType = 0
                sportStartTime = 0L
                binding.tvWearData.text = ""
                binding.etSportType.setText("")
                binding.tvSportState.text = getString(R.string.state_not_exercising)
                binding.btnStart.isEnabled = true
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = false
                binding.btnEnd.isEnabled = false
            }
            //运动开始
            sportStart -> {
                binding.tvSportState.text = getString(R.string.state_in_motion)
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = true
                binding.btnResume.isEnabled = false
                binding.btnEnd.isEnabled = false
            }
            //运动暂停
            sportPause -> {
                binding.tvSportState.text = getString(R.string.state_sport_pause)
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = true
                binding.btnEnd.isEnabled = true
            }
            //运动继续
            sportResume -> {
                binding.tvSportState.text = getString(R.string.state_in_motion)
                binding.btnStart.isEnabled = false
                binding.btnPause.isEnabled = true
                binding.btnResume.isEnabled = false
                binding.btnEnd.isEnabled = false
            }
            //运动结束
            sportEnd -> {
                sportType = 0
                sportStartTime = 0L
                binding.tvWearData.text = ""
                binding.etSportType.setText("")
                binding.tvSportState.text = getString(R.string.state_sport_over)
                binding.btnStart.isEnabled = true
                binding.btnPause.isEnabled = false
                binding.btnResume.isEnabled = false
                binding.btnEnd.isEnabled = false
            }
        }
    }
}