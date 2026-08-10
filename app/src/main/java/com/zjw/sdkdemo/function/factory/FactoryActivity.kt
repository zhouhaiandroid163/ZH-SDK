package com.zjw.sdkdemo.function.factory

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.PhilipsKeyCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityFactoryBinding
import com.zjw.sdkdemo.livedata.DeviceLiveData.initCallBack

class FactoryActivity : BaseActivity() {
    private val binding by lazy { ActivityFactoryBinding.inflate(layoutInflater) }
    private val tag: String = FactoryActivity::class.java.simpleName

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
    }
}