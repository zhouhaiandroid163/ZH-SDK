package com.zjw.sdkdemo.function.factory

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityFactoryBinding

class FactoryActivity : BaseActivity() {
    private val binding by lazy { ActivityFactoryBinding.inflate(layoutInflater) }
    private val tag: String = FactoryActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_factory)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
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
        clickCheckConnect(binding.btHeartLightLeakTest) {
            addLogI("btHeartLightLeakTest")
            ControlBleTools.getInstance().heartLightLeakTestByProduction()
        }
    }
}