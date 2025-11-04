package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.WeatherDayBean
import com.zhapp.ble.bean.WeatherPerHourBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.WeatherCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityWeatherBinding
import java.util.Calendar

class WeatherActivity : BaseActivity() {
    private val binding by lazy { ActivityWeatherBinding.inflate(layoutInflater) }
    private val tag: String = WeatherActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_weather)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnOpen) {
            addLogI("btnOpen")
            val isSwitch = true
            addLogI("setAppWeatherSwitch isSwitch=$isSwitch")
            ControlBleTools.getInstance().setAppWeatherSwitch(isSwitch, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setAppWeatherSwitch state=$state")
                }
            })
        }
        clickCheckConnect(binding.btnClose) {
            addLogI("btnClose")
            val isSwitch = false
            addLogI("setAppWeatherSwitch isSwitch=$isSwitch")
            ControlBleTools.getInstance().setAppWeatherSwitch(isSwitch, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setAppWeatherSwitch state=$state")
                }
            })
        }
        clickCheckConnect(binding.btnWeatherSendDay) {
            addLogI("btnWeatherSendDay")
            val bean = WeatherDayBean()
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            bean.year = cal.get(Calendar.YEAR)
            bean.month = cal.get(Calendar.MONTH) + 1
            bean.day = cal.get(Calendar.DAY_OF_MONTH)
            bean.hour = cal.get(Calendar.HOUR_OF_DAY)
            bean.minute = cal.get(Calendar.MINUTE)
            bean.second = cal.get(Calendar.SECOND)
            bean.cityName = "BeiJing"
            bean.locationName = "CN"
            for (i in 0..3) {
                val listBean = WeatherDayBean.Data()
                listBean.aqi = 80 + i
                listBean.now_temperature = 30 + i
                listBean.low_temperature = 20 + i
                listBean.high_temperature = 30 + i
                listBean.humidity = 70 + i
                listBean.weather_id = 804
                listBean.weather_name = "Clouds"
                listBean.Wind_speed = 2 + i
                listBean.wind_info = 252 + i
                listBean.Probability_of_rainfall = 4 + i
                listBean.sun_rise = (1568958164 + i).toString() + ""
                listBean.sun_set = (1569002733 + i).toString() + ""
                listBean.wind_power = 10
                listBean.visibility = 10
                bean.list.add(listBean)
            }
            addLogBean("sendWeatherDailyForecast",bean)
            ControlBleTools.getInstance().sendWeatherDailyForecast(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendWeatherDailyForecast state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnWeatherSendHour) {
            addLogI("btnWeatherSendHour")
            val bean = WeatherPerHourBean()
            val cal = Calendar.getInstance()
            cal.timeInMillis = System.currentTimeMillis()
            bean.year = cal.get(Calendar.YEAR)
            bean.month = cal.get(Calendar.MONTH) + 1
            bean.day = cal.get(Calendar.DAY_OF_MONTH)
            bean.hour = cal.get(Calendar.HOUR_OF_DAY)
            bean.minute = cal.get(Calendar.MINUTE)
            bean.second = cal.get(Calendar.SECOND)
            bean.cityName = "BeiJing"
            bean.locationName = "CN"
            for (i in 0..<4 * 24) {
                val listBean = WeatherPerHourBean.Data()
                listBean.now_temperature = 10 + i
                listBean.humidity = 60 + i
                listBean.weather_id = 804
                listBean.Wind_speed = 2 + i
                listBean.wind_info = 152 + i
                listBean.Probability_of_rainfall = 4 + i
                listBean.wind_power = 10
                listBean.visibility = 10
                bean.list.add(listBean)
            }
            addLogBean("sendWeatherPreHour",bean)
            ControlBleTools.getInstance().sendWeatherPreHour(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendWeatherPreHour state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.weatherCallBack = WeatherCallBack {
            addLogI("weatherCallBack")
        }
    }
}