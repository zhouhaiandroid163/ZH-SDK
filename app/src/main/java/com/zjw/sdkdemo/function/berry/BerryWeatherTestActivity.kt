package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.weather.BerryForecastWeatherBean
import com.zhapp.ble.bean.berry.weather.BerryLatestWeatherBean
import com.zhapp.ble.bean.berry.weather.BerryWeatherIdBean
import com.zhapp.ble.bean.berry.weather.BerryWeatherKeyValueBean
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryWeatherTestBinding

class BerryWeatherTestActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryWeatherTestBinding.inflate(layoutInflater) }
    private val tag: String = BerryWeatherTestActivity::class.java.simpleName

    private var weatherType = 200
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_weather_id_test_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
    }

    private fun initView() {
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val texts = resources.getStringArray(R.array.weatherTypeValue)
                weatherType = texts[position].trim().toInt()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun initListener() {
        clickCheckConnect(binding.btnSendWeather) {
            addLogI("btnSendWeather")
            val newTemp = binding.etCurTemp.text.toString().trim().toInt()
            sendWeather(newTemp, weatherType)
        }

        clickCheckConnect(binding.btnSendT006) {
            addLogI("btnSendT006")
            val newTemp = binding.etT006Temp.text.toString().trim().toInt()
            val newType = binding.etT006id.text.toString().trim().toInt()
            sendWeather(newTemp, newType)
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

    fun sendWeather(temp: Int, weatherType: Int) {
        addLogI("sendWeather")
        val latestWeatherBean = BerryLatestWeatherBean()
        latestWeatherBean.id = BerryWeatherIdBean(System.currentTimeMillis() / 1000, "BeiJing", "BeiJing", "BeiJing", false)
        latestWeatherBean.weather = weatherType
        latestWeatherBean.temperature = BerryWeatherKeyValueBean("temperature", temp + 5)
        latestWeatherBean.humidity = BerryWeatherKeyValueBean("humidity", temp + 4)
        latestWeatherBean.windSpeed = BerryWeatherKeyValueBean("windSpeed", temp + 3)
        latestWeatherBean.windDeg = BerryWeatherKeyValueBean("windDeg", temp + 0)
        latestWeatherBean.uvindex = BerryWeatherKeyValueBean("uvindex", temp + 2)
        latestWeatherBean.aqi = BerryWeatherKeyValueBean("aqi", temp + 1)
        val alertsList = mutableListOf<BerryLatestWeatherBean.WeatherAlertsListBean>()
        for (i in 0..10) {
            alertsList.add(BerryLatestWeatherBean.WeatherAlertsListBean("id$i", "type$i", "level$i", "title$i", "detail$i"))
        }
        latestWeatherBean.alertsList = alertsList
        latestWeatherBean.pressure = temp * 1.0F

        addLogBean("sendBerryLatestWeather", latestWeatherBean)
        ControlBleTools.getInstance().sendBerryLatestWeather(latestWeatherBean, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                addLogI("sendBerryLatestWeather state=$state")
            }
        })

        val berryForecastWeatherBean = BerryForecastWeatherBean()
        berryForecastWeatherBean.id = BerryWeatherIdBean(System.currentTimeMillis() / 1000, "BeiJing", "BeiJing", "BeiJing", false)
        val forecastDataList = mutableListOf<BerryForecastWeatherBean.WeatherData>()
        for (i in 0..3) {
            forecastDataList.add(
                BerryForecastWeatherBean.WeatherData(
                    BerryWeatherKeyValueBean("api", temp + i),
                    BerryForecastWeatherBean.BerryWeatherRangeValueBean(temp + 2 + i, temp + 3 + i),
                    BerryForecastWeatherBean.BerryWeatherRangeValueBean(temp + i, temp + 1 + i),
                    "℃ ",
                    BerryForecastWeatherBean.BerryWeatherSunRiseSetBean((System.currentTimeMillis() - 480000 + i) / 1000, System.currentTimeMillis() / 1000),
                    BerryWeatherKeyValueBean("wind_speed", temp + 4 + (i % 10)),
                    BerryWeatherKeyValueBean("wind_deg", temp + 5 + (i % 10))
                )
            )
        }
        berryForecastWeatherBean.data = forecastDataList

        addLogBean("sendBerryDailyForecastWeather", berryForecastWeatherBean)
        ControlBleTools.getInstance().sendBerryDailyForecastWeather(berryForecastWeatherBean, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                addLogI("sendBerryDailyForecastWeather state=$state")
            }
        })

        val bean = BerryForecastWeatherBean()
        bean.id = BerryWeatherIdBean(System.currentTimeMillis() / 1000, "BeiJing", "BeiJing", "BeiJing", false)
        val dataList = mutableListOf<BerryForecastWeatherBean.WeatherData>()
        for (i in 0..24) {
            dataList.add(
                BerryForecastWeatherBean.WeatherData(
                    BerryWeatherKeyValueBean("api", 30 + (i % 10)),
                    BerryForecastWeatherBean.BerryWeatherRangeValueBean(temp + 8 + (i % 10), temp + 9 + (i % 10)),
                    BerryForecastWeatherBean.BerryWeatherRangeValueBean(temp + 6 + (i % 10), temp + 7 + (i % 10)),
                    "℃",
                    BerryForecastWeatherBean.BerryWeatherSunRiseSetBean((System.currentTimeMillis() - 480000 + (i % 10)) / 1000, System.currentTimeMillis() / 1000),
                    BerryWeatherKeyValueBean("wind_speed", temp + 5 + (i % 10)),
                    BerryWeatherKeyValueBean("wind_deg", temp + 6 + (i % 10))
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
}