package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.ThreadUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.BerryDeviceInfoBean
import com.zhapp.ble.callback.BerryBindCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.UnbindDeviceCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryBindUnbindBinding

class BerryBindUnBindActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryBindUnbindBinding.inflate(layoutInflater) }
    private val tag: String = BerryBindUnBindActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_bind_unbind_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnSendUserId) {
            addLogI("btnSendUserId")
            val userId = binding.etUserId.text.toString().trim()
            val phoneName = DeviceUtils.getModel()
            val systemVersion = DeviceUtils.getSDKVersionName()
            addLogI("setUserIdByBerryProtocol userId=$userId phoneName=$phoneName systemVersion=$systemVersion")
            ControlBleTools.getInstance().setUserIdByBerryProtocol(userId, phoneName, systemVersion, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setUserIdByBerryProtocol state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnBindDevice) {
            addLogI("btnBindDevice")
            addLogI("bindDevice")
            ControlBleTools.getInstance().bindDevice(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("bindDevice state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnReplyBindSuccess) {
            addLogI("btnReplyBindSuccess")
            val value = true
            addLogI("bindDeviceSucByBerryProtocol value=$value")
            ControlBleTools.getInstance().bindDeviceSucByBerryProtocol(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("bindDeviceSucByBerryProtocol state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnOneKeyBind) {
            addLogI("btnOneKeyBind")
            val userId = binding.etUserId.text.toString().trim()
            val phoneName = DeviceUtils.getModel()
            val systemVersion = DeviceUtils.getSDKVersionName()
            addLogI("setUserIdByBerryProtocol userId=$userId phoneName=$phoneName systemVersion=$systemVersion")
            ControlBleTools.getInstance().setUserIdByBerryProtocol(userId, phoneName, systemVersion, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setUserIdByBerryProtocol state=$state")

                    if (state == SendCmdState.SUCCEED) {
                        addLogI("bindDevice")
                        ControlBleTools.getInstance().bindDevice(object : SendCmdStateListener() {
                            override fun onState(state: SendCmdState?) {
                                addLogI("bindDevice state=$state")

                                if (state == SendCmdState.SUCCEED) {
                                    ThreadUtils.runOnUiThreadDelayed({

                                        val value = true
                                        addLogI("bindDeviceSucByBerryProtocol value=$value")
                                        ControlBleTools.getInstance().bindDeviceSucByBerryProtocol(value, object : SendCmdStateListener() {
                                            override fun onState(state: SendCmdState?) {
                                                addLogI("bindDeviceSucByBerryProtocol state=$state")
                                            }
                                        })

                                    }, 1000)
                                }
                            }
                        })
                    }
                }
            })
        }

        clickCheckConnect(binding.btnUnbindDevice) {
            addLogI("btnUnbindDevice")
            addLogI("unbindDevice")
            ControlBleTools.getInstance().unbindDevice(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("unbindDevice state=$state")
                }
            })
        }
    }

    private fun initCallBack() {

        CallBackUtils.berryBindCallBack = object : BerryBindCallBack {
            override fun onUserIdResult(bean: BerryDeviceInfoBean) {
                addLogBean("berryBindCallBack onUserIdResult", bean)
            }

            override fun onBindStatus(state: Int) {
                addLogI("berryBindCallBack onBindStatus state=$state")
            }

            override fun onBindSuccess(state: Int) {
                addLogI("berryBindCallBack onBindSuccess state=$state")
            }
        }


        CallBackUtils.unbindDeviceCallBack = UnbindDeviceCallBack {
            addLogI("unbindDeviceCallBack")
            addLogI("disconnect")
            ControlBleTools.getInstance().disconnect()
            AppUtils.relaunchApp(true)
        }
    }
}