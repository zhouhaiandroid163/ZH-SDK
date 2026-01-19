package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.SecondaryScreenWearDataBean
import com.zhapp.ble.bean.SportRequestBean
import com.zhapp.ble.bean.SportResponseBean
import com.zhapp.ble.bean.SportStatusBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.SecondaryScreenSportCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivitySportScreenBinding
import com.zjw.sdkdemo.livedata.BleConnectState
import com.zjw.sdkdemo.utils.DescriptionUtils
import com.zjw.sdkdemo.utils.DialogUtils

/**
 * 副屏运动
 * Secondary screen movement
 */
class SportScreenActivity : BaseActivity() {
    private val binding by lazy { ActivitySportScreenBinding.inflate(layoutInflater) }
    private val tag: String = SportScreenActivity::class.java.simpleName

    private var sportStartTime = 0L
    private var sportType = 0
    private var sportDuration = 0

    private val sportInit = -1
    private val sportRequestStart = -2
    private val sportErrorStopTip = -3
    private val sportStart = SecondaryScreenSportCallBack.SportState.START.state
    private val sportPause = SecondaryScreenSportCallBack.SportState.PAUSE.state
    private val sportResume = SecondaryScreenSportCallBack.SportState.RESUME.state
    private val sportEnd = SecondaryScreenSportCallBack.SportState.STOP.state
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_sport_screen)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        binding.tvConnectState.text = DescriptionUtils.getConnectStateStr(this, BleConnectState.value!!)
        updateSportState(sportInit)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnGetSportState) {
            addLogI("btnGetSportState")
            addLogI("getSportStatus")
            ControlBleTools.getInstance().getSportStatus(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getSportStatus state=$state")
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
            //结束原因运动时长小于1分钟
            if (sportDuration < 60) {
                sendSportState(sportErrorStopTip)
                showErrorStopTipDialog()
            }
            //正常结束运动
            else {
                sendSportState(sportEnd)
            }
        }
    }

    private fun initCallBack() {
        BleConnectState.observe(this) { state ->
            binding.tvConnectState.text = DescriptionUtils.getConnectStateStr(this, state!!)
            updateSportState(sportInit)
            //获取设备运动状态
            if (state == BleCommonAttributes.STATE_CONNECTED) {
                binding.btnGetSportState.callOnClick()
            }
        }

        CallBackUtils.secondaryScreenSportCallBack = object : SecondaryScreenSportCallBack {
            //APP获取运动状态，设备回复运动状态
            override fun onSportStatus(bean: SportStatusBean) {
                addLogBean("secondaryScreenSportCallBack onSportStatus", bean)
                //未开始
                if (bean.timestamp == 0L) {
                    updateSportState(sportInit)
                }
                //运动中
                else {
                    sportType = bean.sportType
                    sportStartTime = bean.timestamp
                    binding.etSportType.setText(bean.sportType.toString())
                    //已暂停
                    if (bean.isPaused) {
                        updateSportState(sportPause)
                    }
                    //运动中
                    else {
                        updateSportState(sportResume)
                    }
                }
            }

            //APP发送-运动开始，设备回复
            override fun onSecondaryScreenSportResponseBean(bean: SportResponseBean) {
                addLogBean("secondaryScreenSportCallBack onSecondaryScreenSportResponseBean", bean)
                //回复正常
                if (bean.code == SecondaryScreenSportCallBack.ResponseCode.OK.code) {
                    updateSportState(sportStart)
                }
                //回复异常
                else {
                    updateSportState(sportInit)
                }
            }

            //设备主动下发运动状态
            override fun onSecondaryScreenSportRequestBean(bean: SportRequestBean) {
                addLogBean("secondaryScreenSportCallBack onSecondaryScreenSportRequestBean", bean)
                replyRequest()
                val stop = SecondaryScreenSportCallBack.SportState.STOP.state
                val normalEnd = SecondaryScreenSportCallBack.StopErrorCode.NORMAL_END.code
                //运动已结束
                if (bean.state == stop && bean.stopErrorCode != normalEnd) {
                    showErrorStopTipDialog()
                } else {
                    updateSportState(bean.state)
                }
            }

            //副屏运动过程中数据
            override fun onSecondaryScreenWearData(bean: SecondaryScreenWearDataBean) {
                addLogBean("secondaryScreenSportCallBack onSecondaryScreenWearData", bean)
                binding.tvWearData.text = bean.toString()
                sportDuration = bean.sportTimestamp
            }
        }
    }


    /**
     * 结束原因运动时长小于1分钟弹窗提示
     */
    private fun showErrorStopTipDialog() {
        DialogUtils.showTwoButtonDialog(this, getString(R.string.btn_prompt), getString(R.string.sport_time_short_tip), getString(R.string.btn_confirm), getString(R.string.btn_cancel), {
            sendSportState(sportEnd)
        }, {
            sendSportState(sportResume)
        })
    }

    /**
     * 回复设备request
     */
    private fun replyRequest() {
        addLogI("replyRequest")
        val bean = SportResponseBean()
        bean.code = SecondaryScreenSportCallBack.ResponseCode.OK.code
        bean.gpsAccuracy = 1
        bean.selectVersion = 1
        addLogBean("replyDevSecondaryScreenSportRequest",bean)
        ControlBleTools.getInstance().replyDevSecondaryScreenSportRequest(bean, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                addLogI("replyDevSecondaryScreenSportRequest state=$state")
            }
        })
    }

    private fun sendSportState(sportState: Int) {
        addLogI("sendSportState sportState=$sportState")
        when (sportState) {
            //APP发送开始指令，等待设备回复
            sportRequestStart -> {
                sportType = binding.etSportType.text.toString().trim().toInt()
                sportStartTime = System.currentTimeMillis() / 1000L
                val bean = SportRequestBean.getStartPhoneSportRequest(sportType, sportStartTime)
                addLogBean("secondaryScreenSportRequest",bean)
                ControlBleTools.getInstance().secondaryScreenSportRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("secondaryScreenSportRequest state=$state")
                        updateSportState(sportRequestStart)
                    }
                })
            }
            //运动暂停
            sportPause -> {
                val bean = SportRequestBean.getPausePhoneSportRequest(sportType, sportStartTime)
                addLogBean("secondaryScreenSportRequest",bean)
                ControlBleTools.getInstance().secondaryScreenSportRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("secondaryScreenSportRequest state=$state")
                        updateSportState(sportPause)
                    }
                })
            }
            //运动继续
            sportResume -> {
                val bean = SportRequestBean.getResumePhoneSportRequest(sportType, sportStartTime)
                addLogBean("secondaryScreenSportRequest",bean)
                ControlBleTools.getInstance().secondaryScreenSportRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("secondaryScreenSportRequest state=$state")
                        updateSportState(sportResume)
                    }
                })
            }
            //运动结束-正常
            sportEnd -> {
                val bean = SportRequestBean.getStopPhoneSportRequest(sportType, sportStartTime)
                addLogBean("secondaryScreenSportRequest",bean)
                ControlBleTools.getInstance().secondaryScreenSportRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("secondaryScreenSportRequest state=$state")
                        updateSportState(sportEnd)
                    }
                })
            }
            //运动结束-异常
            sportErrorStopTip -> {
                val bean = SportRequestBean.getErrorStopPhoneSportRequest(sportType, sportStartTime, SecondaryScreenSportCallBack.StopErrorCode.SPORT_DURATION_NO_METTING.code)
                addLogBean("secondaryScreenSportRequest",bean)
                ControlBleTools.getInstance().secondaryScreenSportRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("secondaryScreenSportRequest state=$state")
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