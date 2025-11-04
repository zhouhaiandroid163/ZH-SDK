package com.zjw.sdkdemo.function

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.LogUtils
import com.zhapp.ble.BleCommonAttributes
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.DeviceInfoBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceInfoCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityMainBinding
import com.zjw.sdkdemo.function.apricot.BindUnBindActivity
import com.zjw.sdkdemo.function.apricot.BreathingLightActivity
import com.zjw.sdkdemo.function.apricot.ChildrenActivity
import com.zjw.sdkdemo.function.apricot.DataActivity
import com.zjw.sdkdemo.function.apricot.ESimActivity
import com.zjw.sdkdemo.function.apricot.EmojiActivity
import com.zjw.sdkdemo.function.apricot.EvActivity
import com.zjw.sdkdemo.function.apricot.FileActivity
import com.zjw.sdkdemo.function.apricot.MeasureActivity
import com.zjw.sdkdemo.function.apricot.MeasureTypeActivity
import com.zjw.sdkdemo.function.apricot.MicroActivity
import com.zjw.sdkdemo.function.apricot.RemindActivity
import com.zjw.sdkdemo.function.apricot.SetFunActivity
import com.zjw.sdkdemo.function.apricot.SetOtherActivity
import com.zjw.sdkdemo.function.apricot.SetUserActivity
import com.zjw.sdkdemo.function.apricot.SportAuxiliaryActivity
import com.zjw.sdkdemo.function.apricot.SportScreenActivity
import com.zjw.sdkdemo.function.apricot.WeatherActivity
import com.zjw.sdkdemo.function.apricot.dial.DialActivity
import com.zjw.sdkdemo.function.apricot.ring.RingActivity
import com.zjw.sdkdemo.function.apricot.sifli.SifliActivity
import com.zjw.sdkdemo.function.apricot.test.LoopBindActivity
import com.zjw.sdkdemo.function.apricot.test.LoopFitnessActivity
import com.zjw.sdkdemo.function.apricot.test.NotifySendTestActivity
import com.zjw.sdkdemo.function.apricot.test.SyncTestActivity
import com.zjw.sdkdemo.function.berry.BerryAiVoiceActivity
import com.zjw.sdkdemo.function.berry.BerryBindUnBindActivity
import com.zjw.sdkdemo.function.berry.BerryChildrenActivity
import com.zjw.sdkdemo.function.berry.BerryDialActivity
import com.zjw.sdkdemo.function.berry.BerryFileActivity
import com.zjw.sdkdemo.function.berry.BerryOfflineMapActivity
import com.zjw.sdkdemo.function.berry.BerryRemindActivity
import com.zjw.sdkdemo.function.berry.BerrySetOtherActivity
import com.zjw.sdkdemo.function.berry.BerrySocketActivity
import com.zjw.sdkdemo.function.berry.BerryWeatherActivity
import com.zjw.sdkdemo.function.berry.BerryWeatherTestActivity
import com.zjw.sdkdemo.function.factory.BerryDeviceRealtimeLogActivity
import com.zjw.sdkdemo.function.factory.FactoryActivity
import com.zjw.sdkdemo.function.factory.RingHeartRawGetActivity
import com.zjw.sdkdemo.livedata.BleConnectState
import com.zjw.sdkdemo.ui.adapter.MyExpandableAdapter
import com.zjw.sdkdemo.utils.DescriptionUtils
import com.zjw.sdkdemo.utils.MyConstants
import com.zjw.sdkdemo.utils.ToastUtils

class MainActivity : BaseActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val tag: String = MainActivity::class.java.simpleName

    object GlobalData {
        var deviceInfo: DeviceInfoBean? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.app_name)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initData()
        initCallback()
    }

    fun initData() {
        if (intent.getStringExtra(MyConstants.EXTRA_DEVICE_ADDRESS) != null && intent.getStringExtra(MyConstants.EXTRA_DEVICE_ADDRESS) != "") {
            MyConstants.deviceAddress = intent.getStringExtra(MyConstants.EXTRA_DEVICE_ADDRESS)
        }
        if (intent.getStringExtra(MyConstants.EXTRA_DEVICE_NAME) != null && intent.getStringExtra(MyConstants.EXTRA_DEVICE_NAME) != "") {
            MyConstants.deviceName = intent.getStringExtra(MyConstants.EXTRA_DEVICE_NAME)
        }
        if (intent.getStringExtra(MyConstants.EXTRA_DEVICE_PROTOCOL) != null && intent.getStringExtra(MyConstants.EXTRA_DEVICE_PROTOCOL) != "") {
            MyConstants.deviceProtocol = intent.getStringExtra(MyConstants.EXTRA_DEVICE_PROTOCOL)
        }

        MyConstants.isBind = intent.getBooleanExtra(MyConstants.EXTRA_DEVICE_IS_BIND, false)

        binding.tvDeviceName.text = MyConstants.deviceName
        binding.tvDeviceAddress.text = MyConstants.deviceAddress
    }

    fun initView() {
        val children = mapOf(
            R.string.gr_gather_function to listOf(
                R.string.ch_bind_unbind,
                R.string.ch_bind_unbind_berry,
                R.string.ch_data,
                R.string.ch_file,
                R.string.ch_file_berry,
                R.string.ch_dial,
                R.string.ch_dial_berry,
                R.string.ch_weather,
                R.string.ch_weather_berry,
                R.string.ch_weather_id_test_berry,
                R.string.ch_sport_auxiliary,
                R.string.ch_sport_screen,
                R.string.ch_measure,
                R.string.ch_ev,
                R.string.ch_emoji,
                R.string.ch_esim,
                R.string.ch_breathing_light,
                R.string.ch_ai_voice_berry,
                R.string.ch_offline_map_berry,
                R.string.ch_socket_berry,
                R.string.ch_micro_function,
            ),
            R.string.gr_set_menu to listOf(
                R.string.ch_set_user,
                R.string.ch_set_reminder,
                R.string.ch_set_reminder_berry,
                R.string.ch_set_fun,
                R.string.ch_set_child,
                R.string.ch_set_child_berry,
                R.string.ch_set_other,
                R.string.ch_set_other_berry,
            ),
            R.string.gr_other to listOf(
                R.string.ch_ring,
                R.string.ch_sifil,
            ),
            R.string.gr_test to listOf(
                R.string.ch_sync_send_cmd_test,
                R.string.ch_loop_bind,
                R.string.ch_loop_sync,
                R.string.ch_loop_notify,
            ),
            R.string.gr_factory to listOf(
                R.string.ch_factory,
                R.string.ch_ring_heart_raw_get,
                R.string.ch_get_device_real_log_berry,
            ),
        )
        val adapter = MyExpandableAdapter(this, children)
        binding.expandableListView.setAdapter(adapter)
        binding.expandableListView.setOnChildClickListener { _, _, groupPos, childPos, _ ->
            val groupNameId = adapter.getGroup(groupPos)
            val childNameId = adapter.getChild(groupPos, childPos)
            LogUtils.i("OnChildClickListener ${getStringResourceNameById(groupNameId)} - ${getStringResourceNameById(childNameId)}")
            val isConnect = ControlBleTools.getInstance().isConnect
            if (isConnect) {
                when (groupNameId) {
                    R.string.gr_gather_function ->
                        when (childNameId) {
                            R.string.ch_bind_unbind -> startActivity(Intent(this, BindUnBindActivity::class.java))
                            R.string.ch_bind_unbind_berry -> startActivity(Intent(this, BerryBindUnBindActivity::class.java))
                            R.string.ch_data -> startActivity(Intent(this, DataActivity::class.java))
                            R.string.ch_file -> startActivity(Intent(this, FileActivity::class.java))
                            R.string.ch_file_berry -> startActivity(Intent(this, BerryFileActivity::class.java))
                            R.string.ch_dial -> startActivity(Intent(this, DialActivity::class.java))
                            R.string.ch_dial_berry -> startActivity(Intent(this, BerryDialActivity::class.java))
                            R.string.ch_weather -> startActivity(Intent(this, WeatherActivity::class.java))
                            R.string.ch_weather_berry -> startActivity(Intent(this, BerryWeatherActivity::class.java))
                            R.string.ch_weather_id_test_berry -> startActivity(Intent(this, BerryWeatherTestActivity::class.java))
                            R.string.ch_sport_auxiliary -> startActivity(Intent(this, SportAuxiliaryActivity::class.java))
                            R.string.ch_sport_screen -> startActivity(Intent(this, SportScreenActivity::class.java))
                            R.string.ch_measure -> startActivity(Intent(this, MeasureTypeActivity::class.java).apply { putExtra(MeasureActivity.DEVICE_TYPE_TAG, "1") })
                            R.string.ch_ev -> startActivity(Intent(this, EvActivity::class.java))
                            R.string.ch_emoji -> startActivity(Intent(this, EmojiActivity::class.java))
                            R.string.ch_esim -> startActivity(Intent(this, ESimActivity::class.java))
                            R.string.ch_breathing_light -> startActivity(Intent(this, BreathingLightActivity::class.java))
                            R.string.ch_ai_voice_berry -> startActivity(Intent(this, BerryAiVoiceActivity::class.java))
                            R.string.ch_offline_map_berry -> startActivity(Intent(this, BerryOfflineMapActivity::class.java))
                            R.string.ch_socket_berry -> startActivity(Intent(this, BerrySocketActivity::class.java))
                            R.string.ch_micro_function -> startActivity(Intent(this, MicroActivity::class.java))
                        }

                    R.string.gr_set_menu ->
                        when (childNameId) {
                            R.string.ch_set_user -> startActivity(Intent(this, SetUserActivity::class.java))
                            R.string.ch_set_reminder -> startActivity(Intent(this, RemindActivity::class.java))
                            R.string.ch_set_reminder_berry -> startActivity(Intent(this, BerryRemindActivity::class.java))
                            R.string.ch_set_fun -> startActivity(Intent(this, SetFunActivity::class.java))
                            R.string.ch_set_child -> startActivity(Intent(this, ChildrenActivity::class.java))
                            R.string.ch_set_child_berry -> startActivity(Intent(this, BerryChildrenActivity::class.java))
                            R.string.ch_set_other -> startActivity(Intent(this, SetOtherActivity::class.java))
                            R.string.ch_set_other_berry -> startActivity(Intent(this, BerrySetOtherActivity::class.java))
                        }

                    R.string.gr_other ->
                        when (childNameId) {
                            R.string.ch_ring -> startActivity(Intent(this, RingActivity::class.java))
                            R.string.ch_sifil -> startActivity(Intent(this, SifliActivity::class.java))
                        }

                    R.string.gr_test ->
                        when (childNameId) {
                            R.string.ch_sync_send_cmd_test -> startActivity(Intent(this, SyncTestActivity::class.java))
                            R.string.ch_loop_bind -> startActivity(Intent(this, LoopBindActivity::class.java))
                            R.string.ch_loop_sync -> startActivity(Intent(this, LoopFitnessActivity::class.java))
                            R.string.ch_loop_notify -> startActivity(Intent(this, NotifySendTestActivity::class.java))
                        }

                    R.string.gr_factory ->
                        when (childNameId) {
                            R.string.ch_factory -> startActivity(Intent(this, FactoryActivity::class.java))
                            R.string.ch_ring_heart_raw_get -> startActivity(Intent(this, RingHeartRawGetActivity::class.java))
                            R.string.ch_get_device_real_log_berry -> startActivity(Intent(this, BerryDeviceRealtimeLogActivity::class.java))
                        }
                }
            } else {
                ToastUtils.showToast(R.string.device_no_connect)
            }
            true
        }

        setMyCheckBox(binding.cxConnect, binding.llConnect, binding.ivHelp)

        binding.cxFun.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.expandableListView.visibility = View.VISIBLE
            } else {
                binding.expandableListView.visibility = View.GONE
            }
        }
    }


    private fun initListener() {
        binding.btnConnectDevice.setOnClickListener {
            addLogI("btnConnectDevice()")
            ControlBleTools.getInstance().connect(MyConstants.deviceName, MyConstants.deviceAddress, MyConstants.deviceProtocol)
        }

        binding.btnDisConnectDevice.setOnClickListener {
            addLogI("btnDisConnectDevice()")
            ControlBleTools.getInstance().disconnect()
        }
    }


    private fun initCallback() {
        ControlBleTools.getInstance().setBleStateCallBack { state ->
            addLogI("setBleStateCallBack state=$state")
            BleConnectState.postValue(state)
            if (state == BleCommonAttributes.STATE_TIME_OUT) {
                if (MyConstants.deviceAddress != null) {
                    ControlBleTools.getInstance().connect(MyConstants.deviceName, MyConstants.deviceAddress, MyConstants.deviceProtocol)
                }
            }

            if (state == BleCommonAttributes.STATE_CONNECTED) {
                getDeviceInfo()
            }
        }

        CallBackUtils.deviceInfoCallBack = object : DeviceInfoCallBack {
            override fun onDeviceInfo(bean: DeviceInfoBean) {
                addLogBean("deviceInfoCallBack onDeviceInfo", bean)
                GlobalData.deviceInfo = bean
            }

            override fun onBatteryInfo(capacity: Int, chargeStatus: Int) {
                addLogI("deviceInfoCallBack onBatteryInfo capacity=$capacity chargeStatus=$chargeStatus")
            }
        }

        BleConnectState.observe(this) { state ->
            binding.tvConnectState.text = DescriptionUtils.getConnectStateStr(this, state!!)

        }
    }

    fun getStringResourceNameById(resId: Int): String? {
        return try {
            resources.getResourceEntryName(resId)
        } catch (_: Exception) {
            null
        }
    }

    private fun getDeviceInfo() {
        addLogI("getDeviceInfo")
        ControlBleTools.getInstance().getDeviceInfo(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                addLogI("getDeviceInfo state=$state")
            }
        })
    }
}