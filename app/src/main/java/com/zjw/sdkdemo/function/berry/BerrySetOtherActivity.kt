package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.MorningPostBean
import com.zhapp.ble.bean.berry.RecordingCmdBean
import com.zhapp.ble.bean.berry.VaultInfoBean
import com.zhapp.ble.bean.berry.VaultSimpleBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.MorningPostCallBack
import com.zhapp.ble.callback.RecordingCallBack
import com.zhapp.ble.callback.VaultCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerrySetOtherBinding

class BerrySetOtherActivity : BaseActivity() {
    private val binding by lazy { ActivityBerrySetOtherBinding.inflate(layoutInflater) }
    private val tag: String = BerrySetOtherActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_other_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutBerrySetOtherMorningNews.cbTop, binding.layoutBerrySetOtherMorningNews.llBottom, binding.layoutBerrySetOtherMorningNews.ivHelp)
        setMyCheckBox(binding.layoutBerrySetOtherVault.cbTop, binding.layoutBerrySetOtherVault.llBottom, binding.layoutBerrySetOtherVault.ivHelp)
        setMyCheckBox(binding.layoutBerrySetOtherRecording.cbTop, binding.layoutBerrySetOtherRecording.llBottom, binding.layoutBerrySetOtherRecording.ivHelp)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutBerrySetOtherMorningNews.btnSet) {
            addLogI("layoutBerrySetOtherMorningNews.btnSet")
            val title = binding.layoutBerrySetOtherMorningNews.etTitle.text.toString().trim()
            val content = binding.layoutBerrySetOtherMorningNews.etContent.text.toString().trim()
            val bean = MorningPostBean().apply {
                this.title = title
                this.content = content
            }
            addLogBean("setMorningPost", bean)
            ControlBleTools.getInstance().setMorningPost(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setMorningPost state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherVault.btnVaultAdd) {
            addLogI("btnVaultAdd")
            val cardId = binding.layoutBerrySetOtherVault.etCardId01.text.toString().trim()
            val sort = binding.layoutBerrySetOtherVault.etCategory.text.toString().trim().toInt()
            val appNum = binding.layoutBerrySetOtherVault.etPayAppType.text.toString().trim().toInt()
            val alwaysOn = binding.layoutBerrySetOtherVault.etIsAlways.text.toString().trim().toInt()
            val deleteDays = binding.layoutBerrySetOtherVault.etDeleteDays.text.toString().trim().toInt()
            val password = binding.layoutBerrySetOtherVault.etPassword.text.toString().trim()
            val vaultStringList = ArrayList<String>()
            val strValue = binding.layoutBerrySetOtherVault.etCardData.text.toString()
            if (strValue.contains(",")) {
                val split = strValue.split(",")
                vaultStringList.addAll(split)
            } else {
                vaultStringList.add(strValue)
            }
            val bean = VaultInfoBean().apply {
                this.cardId = cardId
                this.sort = sort
                this.appNum = appNum
                this.alwaysOn = alwaysOn
                this.deleteDays = deleteDays
                this.password = password
                this.vaultStringList = vaultStringList
            }
            addLogBean("setVaultInfo", bean)
            ControlBleTools.getInstance().setVaultInfo(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setVaultInfo state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherVault.btnVaultGetSimpleList) {
            addLogI("btnVaultGetSimpleList")
            addLogI("getSimpleVaultInfoList")
            ControlBleTools.getInstance().getSimpleVaultInfoList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getSimpleVaultInfoList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherVault.btnVaultIdGet) {
            addLogI("btnVaultIdGet")
            val id = binding.layoutBerrySetOtherVault.etCardId02.text.toString().trim()
            addLogI("getVaultInfo id=$id")
            ControlBleTools.getInstance().getVaultInfo(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getVaultInfo state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutBerrySetOtherVault.btnVaultIdDelete) {
            addLogI("btnVaultIdDelete")
            val list = arrayListOf<String>()
            val ids = binding.layoutBerrySetOtherVault.etCardIds.text.toString().trim()
            if (ids.contains(",")) {
                val split = ids.split(",")
                list.addAll(split)
            } else {
                list.add(ids)
            }
            addLogBean("delVaultInfoList", list)
            ControlBleTools.getInstance().delVaultInfoList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("delVaultInfoList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherRecording.btnSend) {
            addLogI("layoutBerrySetOtherRecording.btnSend")
            val value = binding.layoutBerrySetOtherRecording.etCmd.text.toString().trim().toInt()
            addLogI("sendRecordingCmd value=$value")
            ControlBleTools.getInstance().sendRecordingCmd(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("sendRecordingCmd state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.morningPostCallBack = MorningPostCallBack {
            addLogI("morningPostCallBack")
            binding.layoutBerrySetOtherMorningNews.btnSet.callOnClick()
        }

        CallBackUtils.vaultCallBack = object : VaultCallBack {
            override fun onSimpleVaultInfoList(list: List<VaultSimpleBean?>) {
                addLogBean("vaultCallBack onSimpleVaultInfoList", list)
            }

            override fun onVaultInfo(bean: VaultInfoBean) {
                addLogBean("vaultCallBack onVaultInfo", bean)
            }

            override fun onDevRequestVaultInfo(list: List<String>) {
                addLogBean("vaultCallBack onDevRequestVaultInfo", list)
            }
        }

        CallBackUtils.recordingCallBack = object : RecordingCallBack {
            override fun onRecordingCmd(bean: RecordingCmdBean) {
                addLogBean("recordingCallBack recordingCallBack", bean)
            }

            override fun onRecordingData(data: ByteArray) {
                addLogI("recordingCallBack onRecordingData data.size=${data.size}")
            }
        }
    }
}