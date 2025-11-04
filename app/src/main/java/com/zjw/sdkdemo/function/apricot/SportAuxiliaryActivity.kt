package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.PhoneSportDataBean
import com.zhapp.ble.bean.SportRequestBean
import com.zhapp.ble.bean.SportResponseBean
import com.zhapp.ble.bean.SportStatusBean
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivitySportAuxiliaryBinding
import com.zjw.sdkdemo.livedata.MySportCallBack

class SportAuxiliaryActivity : BaseActivity() {
    private val binding by lazy { ActivitySportAuxiliaryBinding.inflate(layoutInflater) }
    private val tag: String = SportAuxiliaryActivity::class.java.simpleName

    //是否运动中
    private var isSporting: Boolean = false

    //运动是否暂停
    private var isPause: Boolean = false

    //上次发送的时间戳
    private var lastSendTime: Long = 0L

    //上次发送的经纬度
    private var lastSendLat: Double = 0.0
    private var lastSendLon: Double = 0.0

    //region TODO 定位
    private var locationThread: Thread? = null

    private val latData: DoubleArray = doubleArrayOf(
        22.631818, 22.631812, 22.631823, 22.631834, 22.631845,
        22.631856, 22.631867, 22.631878, 22.631889, 22.631890, 22.631818
    )
    private val lonData: DoubleArray = doubleArrayOf(
        113.833136, 113.833112, 113.833123, 113.833134, 113.833145,
        113.833156, 113.833167, 113.833178, 113.833189, 113.833190, 113.833136
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_sport_auxiliary)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
    }

    private fun initView() {
        binding.layoutTip.tvTip.text = getString(R.string.port_auxiliary_tip)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnGetDevSportStatus) {
            addLogI("btnGetDevSportStatus")
            addLogI("getSportStatus")
            ControlBleTools.getInstance().getSportStatus(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getSportStatus state=$state")
                }
            })
        }
    }

    private fun initCallback() {
        MySportCallBack.onSportStatus.observe(this, Observer { bean ->
            addLogBean("MySportCallBack.onSportStatus", bean!!)
            handSportStatus(bean)
        })

        MySportCallBack.onSportRequest.observe(this, Observer { bean ->
            addLogBean("MySportCallBack.sportRequest", bean!!)
            handleSportRequest(bean)
        })
    }

    /**
     * 处理设备运动状态
     *
     */
    private fun handSportStatus(statusBean: SportStatusBean) {
        //设备独立运动，不需要APP提供定位数据
        if (statusBean.isStandalone) {
            return
        }
        if (statusBean.duration != 0L && statusBean.sportType != 0 && statusBean.timestamp != 0L) {
            updateSportState(true, statusBean.isPaused)
        } else {
            updateSportState(false, statusBean.isPaused)
        }

    }

    /**
     * 处理设备运动状态变化
     *
     */
    private fun handleSportRequest(devSportRequest: SportRequestBean) {

        //GPS开启定位，进行预定位
        when (devSportRequest.state) {
            //预定位
            0 -> {
                val bean = SportResponseBean()
                if (isSporting) {
                    bean.code = 2
                } else {
                    bean.code = 0
                }
                bean.gpsAccuracy = 1
                addLogBean("replyDevSportRequest",bean)
                ControlBleTools.getInstance().replyDevSportRequest(bean, object : SendCmdStateListener(null) {
                    override fun onState(state: SendCmdState?) {
                        addLogI("replyDevSportRequest state=$state")
                    }
                })
            }

            //运动开始
            1 -> {
                initLocationSet()
                updateSportState(isSporting = true, isPause = false)
            }

            //运动暂停
            2 -> {
                updateSportState(isSporting = true, isPause = true)
            }

            //运动继续
            3 -> {
                updateSportState(isSporting = true, isPause = false)
            }

            //运动结束
            4 -> {
                updateSportState(isSporting = false, isPause = false)
            }
        }
    }


    /**
     * 发送手机定位数据
     */
    private fun handleLocationData(lat: Double, lon: Double) {
        //运动中，且不暂停
        if (isSporting && !isPause) {
            //首次定位
            if (lastSendTime == 0L || lastSendLat == 0.0 || lastSendLon == 0.0) {
                sendLocationData(lat, lon)
            }
            //位置发生变化
            else if (lat != lastSendLat || lon != lastSendLon) {
                sendLocationData(lat, lon)
            }
            //距离上次发送超过5秒
            else if (System.currentTimeMillis() - lastSendTime >= 5000) {
                sendLocationData(lat, lon)
            }
        }
    }

    /**
     * 发送定位
     */
    private fun sendLocationData(lat: Double, lon: Double) {
        val sendTime = System.currentTimeMillis()

        lastSendTime = sendTime
        lastSendLat = lat
        lastSendLon = lon

        val bean = PhoneSportDataBean()
        bean.gpsAccuracy = 1
        bean.timestamp = (sendTime / 1000).toInt()
        bean.latitude = lat
        bean.longitude = lon
        addLogBean("sendPhoneSportData",bean)
        ControlBleTools.getInstance().sendPhoneSportData(bean, object : SendCmdStateListener(null) {
            override fun onState(state: SendCmdState?) {
                Log.i(tag, "sendPhoneSportData state=$state")
            }
        })
    }

    /**
     * 开始定位 TODO 换真实定位数据
     */
    private fun startLocation() {
        if (locationThread == null) {
            val random: java.util.Random = java.util.Random()
            locationThread = Thread {
                try {
                    while (true) {
                        Thread.sleep(1000)
                        val lat = latData[random.nextInt(latData.size - 1)]
                        val lon = lonData[random.nextInt(lonData.size - 1)]
                        handleLocationData(lat, lon)
                    }
                } catch (e: InterruptedException) {
                    addLogE("startLocation e=${e.message}")
                }
            }
            locationThread?.start()
        }
    }

    /**
     * 结束定位 TODO 结束定位
     */
    private fun stopLocation() {
        if (locationThread != null) {
            locationThread?.interrupt()
            locationThread = null
        }
    }

    private fun initLocationSet() {
        lastSendTime = 0L
        lastSendLat = 0.0
        lastSendLon = 0.0
    }

    private fun updateSportState(isSporting: Boolean, isPause: Boolean) {
        addLogI("updateSportState isSporting=$isSporting isPause=$isPause")
        this.isSporting = isSporting
        this.isPause = isPause
        if (isSporting && !isPause) {
            startLocation()
        } else {
            stopLocation()
        }
    }

}