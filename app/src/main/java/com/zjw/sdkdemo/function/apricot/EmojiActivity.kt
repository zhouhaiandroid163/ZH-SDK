package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.GlodFriendContactsBean
import com.zhapp.ble.bean.GlodFriendEmojiBean
import com.zhapp.ble.bean.TimeBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.GlodFriendContactsCallBack
import com.zhapp.ble.callback.GlodFriendEmojiCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityEmojiBinding
import java.util.Calendar

class EmojiActivity : BaseActivity() {
    val binding by lazy { ActivityEmojiBinding.inflate(layoutInflater) }
    private val tag: String = EmojiActivity::class.java.simpleName

    var glodFriendEmojiBean: GlodFriendEmojiBean? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_emoji)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallback()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnContactsGet) {
            addLogI("btnContactsGet")
            addLogI("getGlodFriendContactsList")
            ControlBleTools.getInstance().getGlodFriendContactsList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getGlodFriendContactsList state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnContactsSet) {
            addLogI("btnContactsSet")
            val array = binding.etContacts.text.toString().split(",")
            val list = mutableListOf<GlodFriendContactsBean>()
            for (a in array.indices step 2) {
                val bean = GlodFriendContactsBean()
                bean.callContactsId = array[a].toInt()
                bean.callContactsName = array[a + 1]
                list.add(bean)
            }
            addLogBean("setGlodFriendContactsList", list)
            ControlBleTools.getInstance().setGlodFriendContactsList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setGlodFriendContactsList state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSetEmoji) {
            addLogI("btnSetEmoji")
            val array = binding.etEmoji.text.toString().split(",")
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            val bean = GlodFriendEmojiBean(
                array[0].toInt(),
                array[1],
                array[2].toInt(),
                array[3].toInt(),
                TimeBean(cal[Calendar.YEAR], cal[Calendar.MONTH] + 1, cal[Calendar.DAY_OF_MONTH], cal[Calendar.HOUR_OF_DAY], cal[Calendar.MINUTE], cal[Calendar.SECOND])
            )
            addLogBean("setGlodFriendContactsList", bean)
            ControlBleTools.getInstance().setGlodFriendEmoji(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setGlodFriendEmoji state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSuccess) {
            addLogI("btnSuccess")
            if (glodFriendEmojiBean != null) {
                glodFriendEmojiBean?.friendEmojiState = 1
                val bean = glodFriendEmojiBean!!
                addLogBean("setGlodFriendEmojiRequest", bean)
                ControlBleTools.getInstance().setGlodFriendEmojiRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("setGlodFriendEmojiRequest state=$state")
                    }
                })
            }
        }

        clickCheckConnect(binding.btnFail) {
            addLogI("btnFail")
            if (glodFriendEmojiBean != null) {
                glodFriendEmojiBean?.friendEmojiState = 0
                val bean = glodFriendEmojiBean!!
                addLogBean("setGlodFriendEmojiRequest", bean)
                ControlBleTools.getInstance().setGlodFriendEmojiRequest(bean, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("setGlodFriendEmojiRequest state=$state")
                    }
                })
            }
        }
    }

    private fun initCallback() {
        CallBackUtils.glodFriendContactsCallBack = GlodFriendContactsCallBack { bean ->
            addLogBean("glodFriendContactsCallBack", bean)
            var text = ""
            bean.forEach {
                text += it.callContactsId.toString() + "," + it.callContactsName + ","
            }
            text = text.removeRange(text.length - 1, text.length)
            binding.etContacts.setText(text)
        }

        CallBackUtils.glodFriendEmojiCallBack = GlodFriendEmojiCallBack { bean ->
            addLogBean("glodFriendEmojiCallBack", bean)
            glodFriendEmojiBean = bean
            val emojiStr = "${bean.callContactsId},${bean.callContactsName},${bean.emoji},${bean.color}"
            binding.etEmoji.setText(emojiStr)
        }
    }
}