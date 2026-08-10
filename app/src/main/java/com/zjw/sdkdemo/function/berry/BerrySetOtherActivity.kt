package com.zjw.sdkdemo.function.berry

import android.graphics.Bitmap
import android.os.Bundle
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.MorningPostBean
import com.zhapp.ble.bean.berry.RecordingCmdBean
import com.zhapp.ble.bean.berry.TvDeviceBean
import com.zhapp.ble.bean.berry.TvIconBean
import com.zhapp.ble.bean.berry.TvKeyEventBean
import com.zhapp.ble.bean.berry.VaultInfoBean
import com.zhapp.ble.bean.berry.VaultSimpleBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.MorningPostCallBack
import com.zhapp.ble.callback.RecordingCallBack
import com.zhapp.ble.callback.TvRemoteCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.callback.VaultCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerrySetOtherBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.utils.DialogUtils
import com.zjw.sdkdemo.utils.ToastUtils
import java.io.File
import kotlin.math.min

class BerrySetOtherActivity : BaseActivity() {
    private val binding by lazy { ActivityBerrySetOtherBinding.inflate(layoutInflater) }
    private val tag: String = BerrySetOtherActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_other_berry)
        initLogSet(
            tag,
            binding.layoutLog.llLog,
            binding.layoutLog.cxLog,
            binding.layoutLog.llLogContent,
            binding.layoutLog.btnClear,
            binding.layoutLog.btnSet,
            binding.layoutLog.btnSendLog
        )
        initView()
        initListener()
        initCallBack()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutBerrySetOtherMorningNews.cbTop, binding.layoutBerrySetOtherMorningNews.llBottom, binding.layoutBerrySetOtherMorningNews.ivHelp)
        setMyCheckBox(binding.layoutBerrySetOtherVault.cbTop, binding.layoutBerrySetOtherVault.llBottom, binding.layoutBerrySetOtherVault.ivHelp)
        setMyCheckBox(binding.layoutBerrySetOtherRecording.cbTop, binding.layoutBerrySetOtherRecording.llBottom, binding.layoutBerrySetOtherRecording.ivHelp)
        setMyCheckBox(binding.layoutBerrySetOtherTv.cbTop, binding.layoutBerrySetOtherTv.llBottom, binding.layoutBerrySetOtherTv.ivHelp)
        setMyCheckBox(binding.layoutBerrySetOtherPassword.cbTop, binding.layoutBerrySetOtherPassword.llBottom, binding.layoutBerrySetOtherPassword.ivHelp)
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

        clickCheckConnect(binding.layoutBerrySetOtherTv.btnList) {
            addLogI("layoutBerrySetOtherTv.btnList")
            val activeId = binding.layoutBerrySetOtherTv.etActiveId.text.toString().trim()
            val deviceId = binding.layoutBerrySetOtherTv.etDeviceIdList.text.toString().trim()
            val name = binding.layoutBerrySetOtherTv.etNameList.text.toString().trim()
            val bean = TvDeviceBean()
            bean.deviceId = activeId
            val list = arrayListOf<TvDeviceBean.TvDeviceInfoBean>()
            if (deviceId.contains(",") || name.contains(",")) {
                val idSplit = deviceId.split(",")
                val nameSplit = name.split(",")
                val num = min(idSplit.size, nameSplit.size)
                for (i in 0 until num) {
                    val item = TvDeviceBean.TvDeviceInfoBean()
                    item.deviceId = idSplit[i]
                    item.deviceName = nameSplit[i]
                    list.add(item)
                }
            } else {
                list.add(TvDeviceBean.TvDeviceInfoBean().apply {
                    this.deviceId = deviceId
                    this.deviceName = name
                })
            }
            bean.deviceList = list
            ControlBleTools.getInstance().setTvDeviceList(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setTvDeviceList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherTv.btnError) {
            addLogI("layoutBerrySetOtherTv.btnError")
            val error = binding.layoutBerrySetOtherTv.etError.text.toString().trim().toInt()
            ControlBleTools.getInstance().setTvErrorStatus(error, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setTvErrorStatus state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherPassword.btnPassword) {
            addLogI("layoutBerrySetOtherPassword.btnPassword")
            val password = binding.layoutBerrySetOtherPassword.etPassword.text.toString().trim()
            ControlBleTools.getInstance().setBerryDevicePassword(password, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setBerryDevicePassword state=$state")
                }
            })
        }

        FileUtils.createOrExistsDir(iconFilePath)
        binding.layoutBerrySetOtherTv.iconFileSelect.tvTip.text = getString(R.string.select_file_attention, iconFilePath)
        clickCheckConnect(binding.layoutBerrySetOtherTv.btnIcons) {
            addLogI("layoutBerrySetOtherTv.btnIcons")
            val index = binding.layoutBerrySetOtherTv.etTvIndex.text.toString().trim().toInt()
            val name = binding.layoutBerrySetOtherTv.etTvName.text.toString().trim()
            if(iconFile==null){
                ToastUtils.showToast(R.string.select_file_tip)
                return@clickCheckConnect
            }
            val icons = arrayListOf<TvIconBean>()
            icons.add(TvIconBean().apply {
                this.index = index
                this.name = name+"2"
                this.icon = iconFile
            })
            icons.add(TvIconBean().apply {
                this.index = index+1
                this.name = name+"2"
                this.icon = iconFile
            })
            val equipmentNumber = GlobalData.deviceInfo!!.equipmentNumber
            ControlBleTools.getInstance().sendTvIconList(icons,equipmentNumber,object : UploadBigDataListener {
                override fun onSuccess() {
                    addLogI("setTvIconList onSuccess")
                }

                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                    val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                    addLogI("setTvIconList onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                }

                override fun onTimeout(msg: String?) {
                    addLogE("setTvIconList onTimeout msg=$msg")
                }
            })
        }

        clickCheckConnect(binding.layoutBerrySetOtherTv.iconFileSelect.btnSelectFile) {
            addLogI("layoutDialOrdinary.layoutSelectFile.btnSelectFile")
            DialogUtils.showSelectImgDialog(this, iconFilePath) { selectedFile ->
                val bgData = FileIOUtils.readFile2BytesByStream(selectedFile)
                iconFile = ConvertUtils.bytes2Bitmap(bgData)
                binding.layoutBerrySetOtherTv.iconFileSelect.tvFileName.text = selectedFile.name
            }
        }
    }

    private var iconFile: Bitmap? = null
    private val iconFilePath = PathUtils.getExternalAppCachePath() + "/tvIcon"


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

        CallBackUtils.tvRemoteCallBack = object : TvRemoteCallBack {
            override fun onTvSwitchDevice(deviceId: String?) {
                addLogI("tvRemoteCallBack onTvSwitchDevice deviceId:${deviceId}")
            }

            override fun onTVKeyEvent(event: TvKeyEventBean) {
                addLogBean("tvRemoteCallBack onTVKeyEvent", event)
            }

        }

        MySettingMenuCallBack.onBerryPasswordSync.observe(this, Observer { psw ->
            addLogI("MySettingMenuCallBack.onBerryPasswordSync = $psw")
        })
    }
}