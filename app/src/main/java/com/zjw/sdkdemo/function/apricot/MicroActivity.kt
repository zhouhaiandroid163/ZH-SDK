package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import android.text.TextUtils
import androidx.lifecycle.Observer
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.WidgetBean
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityMicroBinding
import com.zjw.sdkdemo.livedata.MyMicroCallBack
import java.util.Collections

/**
 * 小功能综合
 * Small function integration
 */
class MicroActivity : BaseActivity() {
    private val binding by lazy { ActivityMicroBinding.inflate(layoutInflater) }
    private val tag: String = MicroActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_micro_function)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutTakePhoto.cbTop, binding.layoutTakePhoto.llBottom, binding.layoutTakePhoto.ivHelp)
        setMyCheckBox(binding.layoutWidgetList.cbTop, binding.layoutWidgetList.llBottom, binding.layoutWidgetList.ivHelp)
        setMyCheckBox(binding.layoutApplicationList.cbTop, binding.layoutApplicationList.llBottom, binding.layoutApplicationList.ivHelp)
        setMyCheckBox(binding.layoutSportsIconList.cbTop, binding.layoutSportsIconList.llBottom, binding.layoutSportsIconList.ivHelp)
    }

    private fun initListener() {
        clickCheckConnect(binding.btnMicroRestart) {
            addLogI("btnMicroRestart")
            addLogI("rebootDevice")
            ControlBleTools.getInstance().rebootDevice(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("rebootDevice state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnMicroShutdown) {
            addLogI("btnMicroShutdown")
            addLogI("shutdownDevice")
            ControlBleTools.getInstance().shutdownDevice(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("shutdownDevice state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnMicroFindWear) {
            addLogI("btnMicroFindWear")
            addLogI("sendFindWear")
            ControlBleTools.getInstance().sendFindWear(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendFindWear state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutTakePhoto.btnOpen) {
            addLogI("layoutMicroTakePhoto.btnOpen")
            val value = 0
            addLogI("sendPhonePhotogragh value=$value")
            ControlBleTools.getInstance().sendPhonePhotogragh(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendPhonePhotogragh state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutTakePhoto.btnClose) {
            addLogI("layoutMicroTakePhoto.btnClose")
            val value = 1
            addLogI("sendPhonePhotogragh value=$value")
            ControlBleTools.getInstance().sendPhonePhotogragh(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendPhonePhotogragh state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutWidgetList.btnGet) {
            addLogI("layoutWidgetList.btnGet")
            addLogI("getWidgetList")
            ControlBleTools.getInstance().getWidgetList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getWidgetList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutWidgetList.btnSet) {
            addLogI("layoutWidgetList.btnSet")
            var list = MyMicroCallBack.onWidgetList.value
            if (list == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }

            //重新排序直达卡片顺序
            val widget = binding.layoutWidgetList.etCardNumber.text.toString().trim()
            if (TextUtils.isEmpty(widget)) {
                //3,4交换
                if (!list[2].sortable || !list[3].sortable) {
                    addLogI(getString(R.string.get_data_tip2))
                    return@clickCheckConnect
                }
                Collections.swap(list, 2, 3)
                //重新赋值排序字段
                for (i in list.indices) {
                    list[i].order = i + 1
                }
            } else {
                if (widget.contains(",")) {
                    val ids: Array<String?> = widget.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    list = ArrayList()
                    for (i in ids.indices) {
                        val widgetBean = WidgetBean()
                        widgetBean.functionId = ids[i]!!.toInt()
                        widgetBean.order = i + 1
                        widgetBean.isEnable = i != ids.size - 1
                        widgetBean.haveHide = true
                        widgetBean.sortable = true
                        list.add(widgetBean)
                    }
                } else {
                    addLogE(getString(R.string.get_data_err1))
                }
            }
            addLogBean("setWidgetList",list)
            ControlBleTools.getInstance().setWidgetList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setWidgetList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutApplicationList.btnGet) {
            addLogI("layoutApplicationList.btnGet")
            addLogI("getApplicationList")
            ControlBleTools.getInstance().getApplicationList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getApplicationList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutApplicationList.btnSet) {
            addLogI("layoutApplicationList.btnSet")

            val list = MyMicroCallBack.onApplicationList.value

            if (list == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            //重新排序应用列表
            //3,4交换
            if (!list[2].sortable || !list[3].sortable) {
                addLogI(getString(R.string.get_data_tip2))
                return@clickCheckConnect
            }
            Collections.swap(list, 2, 3)
            //重新赋值排序字段
            for (i in list.indices) {
                list[i].order = i + 1
            }
            addLogBean("setApplicationList",list)
            ControlBleTools.getInstance().setApplicationList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setApplicationList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSportsIconList.btnGet) {
            addLogI("layoutSportsIconList.btnGet")
            addLogI("getSportWidgetSortList")
            ControlBleTools.getInstance().getSportWidgetSortList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSportWidgetSortList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSportsIconList.btnSet) {
            addLogI("layoutSportsIconList.btnSet")
            val list = MyMicroCallBack.onSportWidgetSortList.value
            if (list == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            //重新排序运动
            //3,4交换
            if (!list[2].sortable || !list[3].sortable) {
                addLogI(getString(R.string.get_data_tip2))
                return@clickCheckConnect
            }
            Collections.swap(list, 2, 3)
            //重新赋值排序字段
            for (i in list.indices) {
                list[i].order = i + 1
            }
            addLogBean("setSportWidgetSortList",list)
            ControlBleTools.getInstance().setSportWidgetSortList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSportWidgetSortList state=$state")
                }
            })
        }

    }

    private fun initCallback() {
        MyMicroCallBack.onWidgetList.observe(this, Observer { list ->
            addLogBean("MyMicroCallBack.onWidgetList", list!!)
        })

        MyMicroCallBack.onApplicationList.observe(this, Observer { list ->
            addLogBean("MyMicroCallBack.onApplicationList", list!!)
        })

        MyMicroCallBack.onSportWidgetSortList.observe(this, Observer { list ->
            addLogBean("MyMicroCallBack.onSportWidgetSortList", list!!)
        })
    }
}