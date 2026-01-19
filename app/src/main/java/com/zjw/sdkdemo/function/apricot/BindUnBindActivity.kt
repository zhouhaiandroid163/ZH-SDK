package com.zjw.sdkdemo.function.apricot

import android.content.Intent
import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.BindDeviceStateCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.RequestDeviceBindStateCallBack
import com.zhapp.ble.callback.UnbindDeviceCallBack
import com.zhapp.ble.callback.VerifyUserIdCallBack
import com.zhapp.ble.manager.BleBCManager
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBindUnbindBinding
import com.zjw.sdkdemo.function.ScanDeviceActivity
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.SpUtils

class BindUnBindActivity : BaseActivity() {
    private val binding by lazy { ActivityBindUnbindBinding.inflate(layoutInflater) }
    private val tag: String = BindUnBindActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_bind_unbind)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnBindDevice) {
            addLogI("btnBindDevice")
            addLogI("requestDeviceBindState")
            ControlBleTools.getInstance().requestDeviceBindState(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("requestDeviceBindState state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnVerifyUserId) {
            addLogI("btnVerifyUserId")
            val userId = binding.etUserId02.text.toString().trim()
            addLogI("verifyUserId userId=$userId")
            ControlBleTools.getInstance().verifyUserId(userId, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("verifyUserId state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnUnBindDevice) {
            addLogI("btnUnBindDevice")
            addLogI("unbindDevice")
            ControlBleTools.getInstance().unbindDevice(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("unbindDevice state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnDeviceConfirmUnbind) {
            addLogI("btnDeviceConfirmUnbind")
            addLogI("unbindDeviceWaitConfirmation")
            ControlBleTools.getInstance().unbindDeviceWaitConfirmation(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("unbindDeviceWaitConfirmation onState state=$state")
                    if (state == SendCmdState.SUCCEED) {
                        unBindDevice()
                    }
                }
            })
        }

        clickCheckConnect(binding.btnDeviceBtPar) {
            addLogI("btnDeviceBtPar")
            if (MyConstants.isBind == true) {
                devicePairing()
            }
        }
    }

    private fun initCallback() {
        CallBackUtils.requestDeviceBindStateCallBack = RequestDeviceBindStateCallBack { state ->
            addLogI("requestDeviceBindStateCallBack state=$state")
            if (!state) {
                addLogI("bindDevice")
                ControlBleTools.getInstance().bindDevice(object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("bindDevice state=$state")
                    }
                })
            }
        }

        CallBackUtils.bindDeviceStateCallBack = BindDeviceStateCallBack { bean ->
            addLogBean("bindDeviceStateCallBack", bean)
            if (bean.deviceVerify) {
                addLogI(getString(R.string.device_bind_success))
                MyConstants.isBind = true

                val userId = binding.etUserId01.text.toString().trim()
                addLogI("sendAppBindResult userId=$userId")
                ControlBleTools.getInstance().sendAppBindResult(userId, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("sendAppBindResult state=$state")
                    }
                })
            } else {
                addLogI(getString(R.string.device_bind_fail))
            }
        }

        CallBackUtils.verifyUserIdCallBack = VerifyUserIdCallBack { state ->
            addLogI("VerifyUserIdCallBack state=$state")
        }

        CallBackUtils.unbindDeviceCallBack = UnbindDeviceCallBack {
            addLogI("unbindDeviceCallBack")
            unBindDevice()
        }
    }

    private fun unBindDevice() {
        MyConstants.isBind = false
        val name = SpUtils.getBrDeviceName()
        val mac = SpUtils.getBrDeviceMac()

        if (name.isNotEmpty() && mac.isNotEmpty()) {
            BleBCManager.getInstance().removeBond(mac)
            SpUtils.setBrDeviceName("")
            SpUtils.setBrDeviceMac("")
        }

        addLogI("disconnect")
        ControlBleTools.getInstance().disconnect()
        startActivity(Intent(this, ScanDeviceActivity::class.java))
        this.finish()
    }

    private fun devicePairing() {
        val name = SpUtils.getBrDeviceName()
        val mac = SpUtils.getBrDeviceMac()
        if (name.isNotEmpty() && mac.isNotEmpty()) {
            if (!BleBCManager.getInstance().checkBondByMac(mac)) {
                BleBCManager.getInstance().createBond(mac, SearchHeadsetBondListener(mac, name))
            } else {
                BleBCManager.getInstance().connectHeadsetBluetoothDevice(mac, null)
            }
        }
    }

    private inner class SearchHeadsetBondListener(var mac: String?, var name: String?) : BleBCManager.BondListener {
        override fun onWaiting() {
            addLogI("SearchHeadsetBondListener Waiting")
        }

        override fun onBondError(e: Exception) {
            addLogE("SearchHeadsetBondListener Exception=${e.message}")
        }

        override fun onBonding() {
            addLogI("SearchHeadsetBondListener onBonding")
        }

        override fun onBondFailed() {
            addLogI("SearchHeadsetBondListener onBondFailed")
            addLogI(getString(R.string.device_pair_tip1, name))
        }

        override fun onBondSucceeded() {
            addLogI("SearchHeadsetBondListener onBondSucceeded")
            BleBCManager.getInstance().connectHeadsetBluetoothDevice(mac, null)
        }
    }
}