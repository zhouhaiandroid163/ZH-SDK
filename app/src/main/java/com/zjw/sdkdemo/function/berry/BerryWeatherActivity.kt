package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.weather.BerryForecastWeatherBean
import com.zhapp.ble.bean.berry.weather.BerryLatestWeatherBean
import com.zhapp.ble.bean.berry.weather.BerryWeatherIdBean
import com.zhapp.ble.bean.berry.weather.BerryWeatherKeyValueBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.WeatherCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryWeatherBinding
import kotlin.random.Random

class BerryWeatherActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryWeatherBinding.inflate(layoutInflater) }
    private val tag: String = BerryWeatherActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_weather_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnLatestWeather) {
            addLogI("btnLatestWeather")
            val bean = BerryLatestWeatherBean()
            bean.id = BerryWeatherIdBean(System.currentTimeMillis() / 1000, "BeiJing", "BeiJing", "BeiJing", false)
            bean.weather = 800
            bean.temperature = BerryWeatherKeyValueBean("temperature", 25)
            bean.humidity = BerryWeatherKeyValueBean("humidity", 24)
            bean.windSpeed = BerryWeatherKeyValueBean("windSpeed", 12)
            bean.windDeg = BerryWeatherKeyValueBean("windDeg", 20)
            bean.uvindex = BerryWeatherKeyValueBean("uvindex", 22)
            bean.aqi = BerryWeatherKeyValueBean("aqi", 21)
            val alertsList = mutableListOf<BerryLatestWeatherBean.WeatherAlertsListBean>()
            for (i in 0..10) {
                alertsList.add(BerryLatestWeatherBean.WeatherAlertsListBean("id$i", "type$i", "level$i", "title$i", "detail$i"))
            }
            bean.alertsList = alertsList
            bean.pressure = 20f
            addLogBean("sendBerryLatestWeather", bean)
            ControlBleTools.getInstance().sendBerryLatestWeather(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendBerryLatestWeather state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnForecastWeatherByDay) {
            addLogI("btnForecastWeatherByDay")
            val bean = BerryForecastWeatherBean()
            bean.id = BerryWeatherIdBean(System.currentTimeMillis() / 1000, "BeiJing", "BeiJing", "BeiJing", false)
            val dataList = mutableListOf<BerryForecastWeatherBean.WeatherData>()
            for (i in 0..3) {
                dataList.add(
                    BerryForecastWeatherBean.WeatherData(
                        BerryWeatherKeyValueBean("api", 30 + i),
                        BerryForecastWeatherBean.BerryWeatherRangeValueBean(28 + i, 29 + i),
                        BerryForecastWeatherBean.BerryWeatherRangeValueBean(26 + i, 27 + i),
                        "℃ ",
                        BerryForecastWeatherBean.BerryWeatherSunRiseSetBean((System.currentTimeMillis() - 480000 + i) / 1000, System.currentTimeMillis() / 1000),
                        BerryWeatherKeyValueBean("wind_speed", 12),
                        BerryWeatherKeyValueBean("wind_deg", 24 + i)
                    )
                )
            }
            bean.data = dataList
            addLogBean("sendBerryDailyForecastWeather", bean)
            ControlBleTools.getInstance().sendBerryDailyForecastWeather(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendBerryDailyForecastWeather state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnForecastWeatherByHour) {
            addLogI("btnForecastWeatherByHour")
            val bean = BerryForecastWeatherBean()
            bean.id = BerryWeatherIdBean(System.currentTimeMillis() / 1000, "BeiJing", "BeiJing", "BeiJing", false)
            val dataList = mutableListOf<BerryForecastWeatherBean.WeatherData>()
            for (i in 0..24) {
                dataList.add(
                    BerryForecastWeatherBean.WeatherData(
                        BerryWeatherKeyValueBean("api", 30 + (i % 10)),
                        BerryForecastWeatherBean.BerryWeatherRangeValueBean(28 + (i % 10), 29 + (i % 10)),
                        BerryForecastWeatherBean.BerryWeatherRangeValueBean(26 + (i % 10), 27 + (i % 10)),
                        "℃",
                        BerryForecastWeatherBean.BerryWeatherSunRiseSetBean((System.currentTimeMillis() - 480000 + (i % 10)) / 1000, System.currentTimeMillis() / 1000),
                        BerryWeatherKeyValueBean("wind_speed", 24 + (i % 10)),
                        BerryWeatherKeyValueBean("wind_deg", 23 + (i % 10))
                    )
                )
            }
            bean.data = dataList
            addLogBean("sendBerryHourlyForecastWeather", bean)
            ControlBleTools.getInstance().sendBerryHourlyForecastWeather(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendBerryHourlyForecastWeather state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendPressure) {
            addLogI("btnSendPressure")
            val value = Random.nextFloat() * (2000 - 1) + 2000
            addLogI("sendBerryPressureByWeather value=$value")
            ControlBleTools.getInstance().sendBerryPressureByWeather(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendBerryPressureByWeather state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSendSwitch){
            addLogI("btnSendSwitch")
            ControlBleTools.getInstance().setAppWeatherSwitch(binding.cbWeatherSwitch.isChecked, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setAppWeatherSwitch state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.weatherCallBack = WeatherCallBack {
            addLogI("weatherCallBack")
            binding.btnLatestWeather.callOnClick()
            binding.btnForecastWeatherByDay.callOnClick()
        }
    }
}