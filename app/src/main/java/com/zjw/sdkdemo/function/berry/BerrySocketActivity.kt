package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.blankj.utilcode.util.NetworkUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.WearSocketConfig
import com.zhapp.ble.bean.berry.WearSocketMessageData
import com.zhapp.ble.bean.berry.WearSocketResp
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.WearSocketCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerrySocketBinding
import com.zjw.sdkdemo.manager.BerrySocketManager
import java.io.ByteArrayOutputStream

class BerrySocketActivity : BaseActivity() {
    private val binding by lazy { ActivityBerrySocketBinding.inflate(layoutInflater) }
    private val tag: String = BerrySocketActivity::class.java.simpleName

    private val dataBuffer = ByteArrayOutputStream()
    private var lastDataReceivedTime: Long = 0
    private val dataTimeout: Long = 200
    private val alipayDataHandler = Handler(Looper.getMainLooper())
    private val processDataRunnable = Runnable {
        processTimedOutData()
    }

    private var serverData: ByteArray? = null
    private var sentDataIndex: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_socket_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
    }

    override fun onDestroy() {
        super.onDestroy()
        alipayDataHandler.removeCallbacks(processDataRunnable)
        clearDataBuffer()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnSocketNetworkNormal) {
            addLogI("sendSocketNetworkEnable")
            val enable = true
            addLogI("sendSocketNetworkEnable enable=$enable")
            ControlBleTools.getInstance().sendSocketNetworkEnable(enable, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendSocketNetworkEnable state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSocketNetworkAbnormal) {
            addLogI("btnSocketNetworkAbnormal")
            val enable = false
            addLogI("sendSocketNetworkEnable enable=$enable")
            ControlBleTools.getInstance().sendSocketNetworkEnable(enable, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendSocketNetworkEnable state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.wearSocketCallBack = object : WearSocketCallBack {
            override fun onSocketConfig(bean: WearSocketConfig) {
                addLogBean("wearSocketCallBack onSocketConfig", bean)

                NetworkUtils.isAvailableAsync { it ->
                    if (it) {
                        bean.let {
                            BerrySocketManager.connect(it)
                        }
                    } else {
                        val enable = false
                        addLogI("sendSocketNetworkEnable enable=$enable")
                        ControlBleTools.getInstance().sendSocketNetworkEnable(enable, object : SendCmdStateListener() {
                            override fun onState(state: SendCmdState) {
                                addLogI("sendSocketNetworkEnable state=$state")
                            }
                        })
                    }
                }
            }

            override fun onWearSendSocketMessage(bean: WearSocketMessageData) {
                addLogBean("wearSocketCallBack onWearSendSocketMessage", bean)

                bean.let {
                    val bean = WearSocketMessageData().apply {
                        this.uuid = bean.uuid
                        this.messageData = bean.messageData
                    }
                    addLogBean("replyWearSocketMessage", bean)
                    ControlBleTools.getInstance().replyWearSocketMessage(bean, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("replyWearSocketMessage state=$state")
                        }
                    })
                    if (bean.messageData != null) {
                        BerrySocketManager.sendData(bean.messageData) { isSuc, msg ->
                            addLogI("sendData isSuc=$isSuc msg=$msg")
                        }
                    }
                }
            }

            override fun onWearRequestSocketMessage(bean: WearSocketMessageData) {
                addLogBean("wearSocketCallBack onWearRequestSocketMessage", bean)
                if (serverData != null) {
                    val dataSize = serverData!!.size
                    val startIndex = sentDataIndex
                    val endIndex = (startIndex + 1024).coerceAtMost(dataSize)
                    val chunk = serverData!!.sliceArray(startIndex until endIndex)

                    val bean = WearSocketMessageData().apply {
                        this.uuid = bean.uuid
                        this.messageData = chunk
                    }
                    addLogBean("sendServerSocketMessage", bean)
                    ControlBleTools.getInstance().sendServerSocketMessage(bean, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("SendCmdStateListener state=$state")
                        }
                    })
                    sentDataIndex = endIndex
                    if (sentDataIndex >= dataSize) {
                        sentDataIndex = 0
                    }
                }
            }

            override fun onSocketDisconnect(bean: WearSocketResp) {
                addLogBean("wearSocketCallBack onSocketDisconnect", bean)

                BerrySocketManager.destroy()
                alipayDataHandler.removeCallbacks(processDataRunnable)
                clearDataBuffer()
                val bean = WearSocketResp().apply {
                    this.uuid = uuid
                    this.respType = 1
                }
                addLogBean("replyWearSocketDisconnect", bean)
                ControlBleTools.getInstance().replyWearSocketDisconnect(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("replyWearSocketDisconnect state=$state")
                    }
                })
                sentDataIndex = 0
            }

            override fun onRequestNetworkStatus() {
                addLogI("wearSocketCallBack onRequestNetworkStatus")
                NetworkUtils.isAvailableAsync {
                    addLogBean("replyWearSocketNetworkEnable", it)
                    ControlBleTools.getInstance().replyWearSocketNetworkEnable(it, object : SendCmdStateListener() {
                        override fun onState(state: SendCmdState) {
                            addLogI("replyWearSocketNetworkEnable state=$state")
                        }
                    })
                }
            }
        }

        addLogI("setSocketListener")
        BerrySocketManager.setSocketListener(object : BerrySocketManager.BerrySocketListener {
            override fun onConnected(sessionId: Int) {
                val enable = true
                addLogI("sendSocketNetworkEnable enable=$enable")
                ControlBleTools.getInstance().sendSocketNetworkEnable(enable, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("sendSocketNetworkEnable state=$state")
                    }
                })
                clearDataBuffer()
                val bean = WearSocketResp().apply {
                    this.uuid = sessionId
                    this.respType = 1
                }
                addLogBean("replyWearSocketResp", bean)
                ControlBleTools.getInstance().replyWearSocketResp(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("replyWearSocketResp state=$state")
                    }
                })
            }

            override fun onConnectTimeout() {
                addLogI("setSocketListener onConnectTimeout")
                clearDataBuffer()
                val bean = WearSocketResp().apply {
                    this.uuid = 0
                    this.respType = 3
                }
                addLogBean("replyWearSocketResp", bean)
                ControlBleTools.getInstance().replyWearSocketResp(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("SendCmdStateListener state=$state")
                    }
                })
            }

            override fun onDisconnected(sessionId: Int) {
                addLogI("setSocketListener onDisconnected sessionId=$sessionId")
                clearDataBuffer()
                val bean = WearSocketResp().apply {
                    this.uuid = sessionId
                    this.respType = 2
                }
                addLogBean("replyWearSocketResp", bean)
                ControlBleTools.getInstance().replyWearSocketResp(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("SendCmdStateListener state=$state")
                    }
                })
                sentDataIndex = 0
            }

            override fun onDataReceived(data: ByteArray) {
                addLogI("setSocketListener onDisconnected data.size=${data.size}")
                dataBuffer.write(data)
                lastDataReceivedTime = System.currentTimeMillis()
                alipayDataHandler.removeCallbacks(processDataRunnable)
                alipayDataHandler.postDelayed(processDataRunnable, dataTimeout)
            }
        })
    }

    private fun processTimedOutData() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDataReceivedTime >= dataTimeout) {
            val completeData = dataBuffer.toByteArray()
            if (completeData.isNotEmpty()) {
                serverData = completeData
                dataBuffer.reset()
            }
        }
    }

    private fun clearDataBuffer() {
        dataBuffer.reset()
        alipayDataHandler.removeCallbacks(processDataRunnable)
        lastDataReceivedTime = 0
    }
}
