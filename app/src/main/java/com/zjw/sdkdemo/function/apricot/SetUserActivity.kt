package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import android.widget.ArrayAdapter
import com.blankj.utilcode.util.ConvertUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.LanguageListBean
import com.zhapp.ble.bean.UserInfo
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.LanguageCallBack
import com.zhapp.ble.callback.TimeFormatCallBack
import com.zhapp.ble.callback.UserInfoCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityUserSetBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SetUserActivity : BaseActivity() {
    val binding by lazy { ActivityUserSetBinding.inflate(layoutInflater) }
    private val tag: String = SetUserActivity::class.java.simpleName

    private var languageArr = arrayOf(
        "阿尔巴尼亚语", "阿拉伯语", "阿姆哈拉语", "爱尔兰语", "奥利亚语",
        "巴斯克语", "白俄罗斯语", "保加利亚语", "波兰语", "波斯语",
        "布尔语", "丹麦语", "德语", "俄语", "法语",
        "菲律宾语", "芬兰语", "高棉语", "格鲁吉亚语", "古吉拉特语",
        "哈萨克语", "海地克里奥尔语", "韩语", "荷兰语", "加利西亚语",
        "加泰罗尼亚语", "捷克语", "卡纳达语", "克罗地亚语", "库尔德语",
        "拉丁语", "老挝语", "卢旺达语", "罗马尼亚语", "马尔加什语",
        "马拉地语", "马拉雅拉姆语", "马来语", "蒙古语", "孟加拉语",
        "缅甸语", "苗语", "南非祖鲁语", "尼泊尔语", "挪威语",
        "葡萄牙语", "日语", "瑞典语", "塞尔维亚语", "僧伽罗语",
        "斯洛伐克语", "索马里语", "塔吉克语", "泰卢固语", "泰米尔语",
        "泰语", "土耳其语", "乌尔都语", "乌克兰语", "乌兹别克语",
        "西班牙语", "希腊语", "匈牙利语", "伊博语", "意大利语",
        "印地语", "印尼语", "英语", "越南语", "繁体中文",
        "简体中文", "希伯来语"
    )

    private var languageList: MutableList<String> = mutableListOf()
    private var devList: MutableList<String> = mutableListOf()
    private var languageListBean: LanguageListBean? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_user)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initData()
        initListener()
        initCallback()
    }

    private fun initData() {
        languageArr.forEach {
            languageList.add(it)
        }
    }

    private fun initView() {
        setMyCheckBox(binding.layoutUserSyncTime.cbTop, binding.layoutUserSyncTime.llBottom, binding.layoutUserSyncTime.ivHelp)
        setMyCheckBox(binding.layoutUserLanguage.cbTop, binding.layoutUserLanguage.llBottom, binding.layoutUserLanguage.ivHelp)
        setMyCheckBox(binding.layoutUserUserConfig.cbTop, binding.layoutUserUserConfig.llBottom, binding.layoutUserUserConfig.ivHelp)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutUserSyncTime.btnSetSystemTime) {
            addLogI("layoutUserSyncTime.btnSetSystemTime")
            val time = System.currentTimeMillis()
            addLogI("setTime time=$time")
            ControlBleTools.getInstance().setTime(time, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setTime state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserSyncTime.btnGeTimeType) {
            addLogI("layoutUserSyncTime.btnGeTimeType")
            addLogI("getTimeFormat")
            ControlBleTools.getInstance().getTimeFormat(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getTimeFormat state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserSyncTime.btnSeTimeType) {
            addLogI("layoutUserSyncTime.btnSeTimeType")
            val value = binding.layoutUserSyncTime.cbTimeType.isChecked
            addLogI("setTimeFormat value=$value")
            ControlBleTools.getInstance().setTimeFormat(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setTimeFormat state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserSyncTime.btnSetSystemTimeOrTimeType) {
            addLogI("layoutUserSyncTime.btnSetSystemTimeOrTimeType")
            val time = System.currentTimeMillis()
            val value = binding.layoutUserSyncTime.cbTimeType.isChecked
            addLogI("setTime time=$time value=$value")
            ControlBleTools.getInstance().setTime(time, value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setTime state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserLanguage.btnGet) {
            addLogI("layoutUserLanguage.btnGet")
            addLogI("getLanguageList")
            ControlBleTools.getInstance().getLanguageList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getLanguageList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserLanguage.btnSet) {
            addLogI("layoutUserLanguage.btnSet")
            if (languageListBean == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            val id = languageListBean!!.languageList[binding.layoutUserLanguage.spLan.selectedItemPosition]
            addLogI("setLanguage id=$id")
            ControlBleTools.getInstance().setLanguage(id, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setLanguage state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserUserConfig.btnSetUserInfo) {
            addLogI("layoutUserUserConfig.btnSetUserInfo")
            val bean = getTestUserInfo()
            addLogBean("setUserProfile", bean)
            ControlBleTools.getInstance().setUserProfile(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setUserProfile state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserUserConfig.btnSetUnitType) {
            addLogI("layoutUserUserConfig.btnSetUnitType")
            val value = if (binding.layoutUserUserConfig.cbUnitType.isChecked) {
                1
            } else {
                0
            }
            addLogI("setDistanceUnit value=$value")
            ControlBleTools.getInstance().setDistanceUnit(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setDistanceUnit state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserUserConfig.btnSetTemperatureType) {
            addLogI("layoutUserUserConfig.btnSetTemperatureType")
            val value = if (binding.layoutUserUserConfig.cbTemperatureType.isChecked) {
                1
            } else {
                0
            }
            addLogI("setTemperatureUnit value=$value")
            ControlBleTools.getInstance().setTemperatureUnit(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setTemperatureUnit state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserUserConfig.btnSetUserInfoOrUnit) {
            addLogI("layoutUserUserConfig.btnSetUserInfoOrUnit")
            val bean = getTestUserInfo()
            val unitType = if (binding.layoutUserUserConfig.cbUnitType.isChecked) {
                1
            } else {
                0
            }
            val temperatureType = if (binding.layoutUserUserConfig.cbTemperatureType.isChecked) {
                1
            } else {
                0
            }
            addLogBean("setUserInformation unitType=$unitType temperatureType=$temperatureType", bean)
            ControlBleTools.getInstance().setUserInformation(unitType, temperatureType, bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setUserInformation state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutUserUserConfig.btnGetUserProfile) {
            addLogI("layoutUserUserConfig.btnGetUserProfile")
            addLogI("getUserProfile")
            ControlBleTools.getInstance().getUserProfile(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getUserProfile state=$state")
                }
            })
        }
    }

    private fun initCallback() {
        CallBackUtils.languageCallback = LanguageCallBack { bean ->
            addLogBean("LanguageCallBack", bean)
            loadData(bean)
        }


        CallBackUtils.userInfoCallBack = object : UserInfoCallBack {
            override fun onUserInfo(bean: UserInfo) {
                addLogBean("userInfoCallBack onUserInfo", bean)
            }

            override fun onDayTimeSleep(isDayTime: Boolean) {
                addLogI("setUserInfoCallBack isDayTime=$isDayTime")
            }

            override fun onAppWeatherSwitch(isSwitch: Boolean) {
                addLogI("setUserInfoCallBack isSwitch=$isSwitch")
            }
        }

        CallBackUtils.timeFormatCallBack = object : TimeFormatCallBack{
            override fun onTimeFormat(is12: Boolean) {
                addLogI("timeFormatCallBack is12=$is12")
            }
        }
    }

    private fun loadData(bean: LanguageListBean) {
        try {
            languageListBean = bean
            val sIndex = ConvertUtils.int2HexString(languageListBean!!.selectLanguageId).toInt()
            val sValue = languageList[sIndex - 1]
            if (!languageListBean!!.languageList.isNullOrEmpty()) {
                devList.clear()
                for (i in languageListBean!!.languageList) {
                    val index = ConvertUtils.int2HexString(i).toInt()
                    val value = languageList[index - 1]
                    devList.add(value)
                }
            }
            val adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devList)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.layoutUserLanguage.spLan.adapter = adapter
            binding.layoutUserLanguage.spLan.setSelection(devList.indexOf(sValue))
        } catch (_: Exception) {
            addLogI("loadData Exception")
        }
    }

    private fun getTestUserInfo(): UserInfo {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val date = simpleDateFormat.parse("2021-10-01")!!
        val bean = UserInfo()
        bean.userName = "test_user_name"
        bean.age = 18
        bean.height = 170
        bean.weight = 60.0f
        bean.birthday = date.time.toInt()
        bean.sex = 2
        bean.maxHr = 80
        bean.calGoal = 180
        bean.stepGoal = 18000
        bean.distanceGoal = 18
        bean.standingTimesGoal = 18
        bean.appWeatherSwitch = false
        return bean
    }

}