package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.OfflineMapResBean
import com.zhapp.ble.bean.berry.BerryOfflineMapBean
import com.zhapp.ble.callback.BerryOfflineMapCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryOffLineMapBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.utils.AssetUtils

class BerryOfflineMapActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryOffLineMapBinding.inflate(layoutInflater) }
    private val tag: String = BerryOfflineMapActivity::class.java.simpleName

    private val mResource = AssetUtils.ASS_BERRY_OFFLINE_MAP_RESOURCE + "map_resource/"
    private var offlineMapBean: BerryOfflineMapBean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_offline_map_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initListener()
        initCallBack()
    }

    private fun initListener() {
        clickCheckConnect(binding.btnGet) {
            addLogI("btnGet")
            addLogI("getOfflineMapDataByBerry")
            ControlBleTools.getInstance().getOfflineMapDataByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getOfflineMapDataByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnDelete) {
            addLogI("btnDelete")
            if (offlineMapBean == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            if (offlineMapBean?.maps.isNullOrEmpty()) {
                addLogI(getString(R.string.no_data))
                return@clickCheckConnect
            }
            val value = offlineMapBean?.maps?.get(0)
            addLogI("deleteOfflineMapDataByBerry value=$value")
            ControlBleTools.getInstance().deleteOfflineMapDataByBerry(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("deleteOfflineMapDataByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.btnSend) {
            addLogI("btnSend")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            val offlineMapResBean = OfflineMapResBean()
            offlineMapResBean.mapName = "testMap111"
            offlineMapResBean.mapFiles = mutableListOf<OfflineMapResBean.MapFile?>().apply {
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "landuse.db"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "landuse.db")
                })
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "landuse.idx"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "landuse.idx")
                })
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "landuse.mlp"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "landuse.mlp")
                })
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "roads.idx"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "roads.idx")
                })
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "roads.mlp"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "roads.mlp")
                })
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "water.idx"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "water.idx")
                })
                add(OfflineMapResBean.MapFile().apply {
                    fileName = "water.mlp"
                    fileBytes = AssetUtils.getAssetBytes(this@BerryOfflineMapActivity, mResource + "water.mlp")
                })
            }
            val deviceType = GlobalData.deviceInfo?.equipmentNumber
            addLogBean("setOfflineMapDataByBerry deviceType=$deviceType", offlineMapResBean)
            ControlBleTools.getInstance().setOfflineMapDataByBerry(offlineMapResBean, deviceType, object : UploadBigDataListener {
                override fun onSuccess() {
                    addLogI("setOfflineMapDataByBerry onSuccess")
                }

                override fun onProgress(setOfflineMapDataByBerry: Int, dataPackTotalPieceLength: Int) {
                    addLogI("setOfflineMapDataByBerry onProgress setOfflineMapDataByBerry=$setOfflineMapDataByBerry dataPackTotalPieceLength=$dataPackTotalPieceLength")
                }

                override fun onTimeout(msg: String?) {
                    addLogE("setOfflineMapDataByBerry onTimeout msg=$msg")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.berryOfflineMapCallBack = BerryOfflineMapCallBack { bean ->
            addLogBean("berryOfflineMapCallBack", bean)
            offlineMapBean = bean
        }
    }

}