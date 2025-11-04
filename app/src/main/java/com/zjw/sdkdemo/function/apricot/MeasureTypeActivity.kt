package com.zjw.sdkdemo.function.apricot

import android.content.Intent
import android.os.Bundle
import com.zhapp.ble.callback.ActiveMeasureCallBack
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityMeasureTypeBinding

class MeasureTypeActivity : BaseActivity() {
    private val binding by lazy { ActivityMeasureTypeBinding.inflate(layoutInflater) }
    //0=戒指,1=手环 0=Ring, 1=Bracelet
    private var deviceType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_measure)
        initListener()
        initData()
    }

    private fun initData() {
        deviceType = intent.getIntExtra(MeasureActivity.DEVICE_TYPE_TAG, 0)
        if (deviceType == 0) {
            setTitle(R.string.ring_measure)
        } else {
            setTitle(R.string.ch_measure)
        }
    }

    private fun initListener() {
        clickCheckConnect(binding.toHeartRate) {
            startActivity(Intent(this, MeasureActivity::class.java).apply {
                putExtra(MeasureActivity.MEASURE_TYPE_TAG, ActiveMeasureCallBack.MeasureType.HEART_RATE.type)
                putExtra(MeasureActivity.DEVICE_TYPE_TAG, deviceType)
            })
        }

        clickCheckConnect(binding.toBloodOxygen) {
            startActivity(Intent(this, MeasureActivity::class.java).apply {
                putExtra(MeasureActivity.MEASURE_TYPE_TAG, ActiveMeasureCallBack.MeasureType.BLOOD_OXYGEN.type)
                putExtra(MeasureActivity.DEVICE_TYPE_TAG, deviceType)
            })
        }

        clickCheckConnect(binding.toStress) {
            startActivity(Intent(this, MeasureActivity::class.java).apply {
                putExtra(MeasureActivity.MEASURE_TYPE_TAG, ActiveMeasureCallBack.MeasureType.STRESS_HRV.type)
                putExtra(MeasureActivity.DEVICE_TYPE_TAG, deviceType)
            })
        }

        clickCheckConnect(binding.toTemperature) {
            startActivity(Intent(this, MeasureActivity::class.java).apply {
                putExtra(MeasureActivity.MEASURE_TYPE_TAG, ActiveMeasureCallBack.MeasureType.BODY_TEMPERATURE.type)
                putExtra(MeasureActivity.DEVICE_TYPE_TAG, deviceType)
            })
        }

        clickCheckConnect(binding.toGoMoreStress) {
            startActivity(Intent(this, MeasureActivity::class.java).apply {
                putExtra(MeasureActivity.MEASURE_TYPE_TAG, ActiveMeasureCallBack.MeasureType.STRESS.type)
                putExtra(MeasureActivity.DEVICE_TYPE_TAG, deviceType)
            })
        }
    }
}