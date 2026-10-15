package com.zjw.sdkdemo.function.factory

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceSNCallBack
import com.zhapp.ble.callback.PhilipsKeyCallBack
import com.zhapp.ble.utils.BleUtils
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityFactoryBinding
import com.zjw.sdkdemo.livedata.DeviceLiveData.initCallBack
import com.zjw.sdkdemo.utils.ToastUtils

class FactoryActivity : BaseActivity() {
    private val binding by lazy { ActivityFactoryBinding.inflate(layoutInflater) }
    private val tag: String = FactoryActivity::class.java.simpleName

    private var snBytes: ByteArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_factory)
        initLogSet(
            tag,
            binding.layoutLog.llLog,
            binding.layoutLog.cxLog,
            binding.layoutLog.llLogContent,
            binding.layoutLog.btnClear,
            binding.layoutLog.btnSet,
            binding.layoutLog.btnSendLog
        )
        initListener()
        initCallBack()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnRestart) {
            addLogI("btnRestart")
            ControlBleTools.getInstance().restartByProduction()
        }
        clickCheckConnect(binding.btReset) {
            addLogI("btReset")
            ControlBleTools.getInstance().resetByProduction()
        }
        clickCheckConnect(binding.btSoftReset) {
            addLogI("btSoftReset")
            ControlBleTools.getInstance().softResetByProduction()
        }
        clickCheckConnect(binding.btHardReset) {
            addLogI("btHardReset")
            ControlBleTools.getInstance().hardResetByProduction()
        }
        clickCheckConnect(binding.btHeartLightLeakTest) {
            addLogI("btHeartLightLeakTest")
            ControlBleTools.getInstance().heartLightLeakTestByProduction()
        }

        clickCheckConnect(binding.btVerifyPhilipsKey){
            addLogI("btVerifyPhilipsKey")
            ControlBleTools.getInstance().verifyPhilipsKeyByProduction()
        }

        clickCheckConnect(binding.btGetDeviceUUID) {
            addLogI("btGetDeviceUUID")
            ControlBleTools.getInstance().getDeviceUUIDByProduction()
        }

        clickCheckConnect(binding.btWritePhilipsKey){
            addLogI("btWritePhilipsKey")
            val key = binding.etKey.text.toString()
            ControlBleTools.getInstance().writePhilipsKeyByProduction(key)
        }

        clickCheckConnect(binding.btnGetSN){
            addLogI("btnGetSN")
            ControlBleTools.getInstance().getDeviceSNByProduction()
        }

        clickCheckConnect(binding.btnReviseSN){
            addLogI("btnReviseSN")
            if(snBytes == null){
                ToastUtils.showToast(R.string.get_data_tip1)
                return@clickCheckConnect
            }
            ControlBleTools.getInstance().reviseDeviceSNByProduction(snBytes)
        }
    }

    private fun initCallBack() {
        CallBackUtils.philipsKeyCallBack = object : PhilipsKeyCallBack{
            override fun onKeyValid(isValid: Boolean) {
                addLogI("onKeyValid $isValid")
            }

            override fun onDeviceUUID(uuid: String) {
                addLogI("onDeviceUUID $uuid")
            }
        }

        CallBackUtils.deviceSNCallBack = object : DeviceSNCallBack{
            override fun onSN(snBytes: ByteArray?, sn: String?, isNeedRevise: Boolean) {
                this@FactoryActivity.snBytes = snBytes
                addLogI("onSN snBytes: ${BleUtils.bytes2HexString(snBytes)} sn: $sn isNeedRevise: $isNeedRevise")
            }

            override fun onSNReviseResult(isSuccess: Boolean, msg: String?) {
                addLogI("onSNReviseResult isSuccess: $isSuccess msg: $msg")
            }
        }
    }
}