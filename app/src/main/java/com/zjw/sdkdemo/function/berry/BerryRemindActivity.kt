package com.zjw.sdkdemo.function.berry

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.PathUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.SuperNotificationBean
import com.zhapp.ble.bean.berry.AddFavoriteContactBean
import com.zhapp.ble.bean.berry.FavoriteContactsBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.DeviceOpenNotifyAppCallBack
import com.zhapp.ble.callback.FavoriteContactsCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.callback.WhatsAppQuickReplyCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.base.BaseApplication
import com.zjw.sdkdemo.databinding.ActivityBerryRemindBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.utils.AssetUtils.getAssetBitmap
import com.zjw.sdkdemo.utils.DialogUtils
import java.io.File

class BerryRemindActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryRemindBinding.inflate(layoutInflater) }
    private val tag: String = BerryRemindActivity::class.java.simpleName

    private var favoriteContactsBean: FavoriteContactsBean? = null

    private val parentHeadFilePath = PathUtils.getExternalAppCachePath() + "/berry_favorite_head"
    private lateinit var parentHeadFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_reminder_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        FileUtils.createOrExistsDir(parentHeadFilePath)
        binding.layoutBerryRemindFavoriteHeadContacts.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, parentHeadFilePath)
        getDeviceInfo()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutBerryRemindSyncCallSwitch.cbTop, binding.layoutBerryRemindSyncCallSwitch.llBottom, binding.layoutBerryRemindSyncCallSwitch.ivHelp)
        setMyCheckBox(binding.layoutBerryRemindWhatsAppQuickReply.cbTop, binding.layoutBerryRemindWhatsAppQuickReply.llBottom, binding.layoutBerryRemindWhatsAppQuickReply.ivHelp)
        setMyCheckBox(binding.layoutBerryRemindSuperNotice.cbTop, binding.layoutBerryRemindSuperNotice.llBottom, binding.layoutBerryRemindSuperNotice.ivHelp)
        setMyCheckBox(binding.layoutBerryRemindFavoriteHeadContacts.cbTop, binding.layoutBerryRemindFavoriteHeadContacts.llBottom, binding.layoutBerryRemindFavoriteHeadContacts.ivHelp)

        binding.layoutBerryRemindSyncCallSwitch.cbCallNoticeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            addLogI("setBerryIncomingCallNotificationSwitch isChecked=$isChecked")
            ControlBleTools.getInstance().setBerryIncomingCallNotificationSwitch(isChecked, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setBerryIncomingCallNotificationSwitch state=$state")
                }
            })
        }

        binding.layoutBerryRemindSyncCallSwitch.cbMissNoticeSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            addLogI("setBerryMissCallNotificationSwitch isChecked=$isChecked")
            ControlBleTools.getInstance().setBerryMissCallNotificationSwitch(isChecked, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setBerryMissCallNotificationSwitch state=$state")
                }
            })
        }

        binding.layoutBerryRemindSuperNotice.etType.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                try {
                    val typeStr = binding.layoutBerryRemindSuperNotice.etType.text.toString().trim()
                    if (typeStr.isNotEmpty()) {
                        val type = binding.layoutBerryRemindSuperNotice.etType.text.toString().trim().toInt()
                        when (type) {
                            //type 0 叫车应用对象:(int)订单状态枚举{0(预约)1(即将到达)2(到达)}，(str)0TP字符串，(str)车牌号字符串,(str)提示文案
                            //type 1 送餐应用对象:(int)订单状态枚举{0(正在派送)1(送达)}，(str)预计时间
                            //type 2 电子商务应用对象:(int)订单状态枚举{0(正在派送),1(送达),2(恢复库存)}，(str)预计送达时间，(str)商品名称，(str)放置地点
                            //type 3 OTT和娱乐应用:(str)状态字符串，(str)名称
                            //type 4 日历和会议:(str)标题字符串，(str)时间字符串
                            //type 5 健康与健身应用:(int)状态枚举{0(锻炼完成),1(目标完成)}，(str)状态字符串，(str)内容字符串，(int)完成进度(0-100)
                            //type 6 Messaging :(str)0TP内容
                            //type 7 Communication:(NULL)直接显示点赞
                            //type 8 金融:(str)状态字符串，(str)付款人，(str)金额
                            //type 9 Dating apps:(int)状态枚举{0(新配对)1(超级喜欢)}，(str)状态字符串 (str)姓名
                            SuperNotificationBean.NOTIFICATION_TYPE_TAXI -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_TAKEOUT -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_PARCEL -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_OTT -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_CALENDAR -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_HEALTH -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_MESSAGING -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_COMMUNICATION -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_FINANCE -> {}
                            SuperNotificationBean.NOTIFICATION_TYPE_DATING -> {}
                        }
                    }
                } catch (e: Exception) {
                    addLogE("etType Exception e=${e.message}")
                    e.printStackTrace()
                }
            }
        })
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutBerryRemindSyncCallSwitch.btnSimultaneouslySetWitch) {
            addLogI("btnSimultaneouslySetWitch")
            val switchCall = binding.layoutBerryRemindSyncCallSwitch.cbCallNoticeSwitch.isChecked
            val switchMiss = binding.layoutBerryRemindSyncCallSwitch.cbMissNoticeSwitch.isChecked
            addLogI("setBerryCallNotificationSwitch switchCall=$switchCall switchMiss=$switchMiss")
            ControlBleTools.getInstance().setBerryCallNotificationSwitch(switchCall, switchMiss, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setBerryCallNotificationSwitch state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryRemindWhatsAppQuickReply.btnGet) {
            addLogI("layoutBerryRemindWhatsAppQuickReply.btnGet")
            addLogI("getDevWhatsAppShortReplyData")
            ControlBleTools.getInstance().getDevWhatsAppShortReplyData(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getDevWhatsAppShortReplyData state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutBerryRemindWhatsAppQuickReply.btnSet) {
            addLogI("layoutBerryRemindWhatsAppQuickReply.btnSet")
            val list = ArrayList<String>()
            val reply: String = binding.layoutBerryRemindWhatsAppQuickReply.etContent.getText().toString().trim { it <= ' ' }
            if (reply.contains(",")) {
                val rs = reply.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                list.addAll(listOf(*rs))
            } else {
                list.add(reply)
            }
            addLogBean("setDevWhatsAppShortReplyData", list)
            ControlBleTools.getInstance().setDevWhatsAppShortReplyData(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setDevWhatsAppShortReplyData state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryRemindSuperNotice.btnSelectOtherAPP) {
            addLogI("btnSelectOtherAPP")
            DialogUtils.showAppDialog(this, binding.layoutBerryRemindSuperNotice.etAppName, binding.layoutBerryRemindSuperNotice.etPackName)
        }

        clickCheckConnect(binding.layoutBerryRemindSuperNotice.btnSend) {
            addLogI("layoutBerryRemindSuperNotice.btnSend")
            val bean = SuperNotificationBean()
            bean.appName = binding.layoutBerryRemindSuperNotice.etAppName.text.toString().trim()
            bean.packageName = binding.layoutBerryRemindSuperNotice.etPackName.text.toString().trim()
            val type = binding.layoutBerryRemindSuperNotice.etType.text.toString().trim().toInt()
            val content = binding.layoutBerryRemindSuperNotice.etContent.text.toString().trim().split(",")
            bean.notificationType = type
            if(!content.isEmpty()){
                //type 0 叫车应用对象:(int)订单状态枚举{0(预约)1(即将到达)2(到达)}，(str)0TP字符串，(str)车牌号字符串,(str)提示文案
                //type 1 送餐应用对象:(int)订单状态枚举{0(正在派送)1(送达)}，(str)预计时间
                //type 2 电子商务应用对象:(int)订单状态枚举{0(正在派送),1(送达),2(恢复库存)}，(str)预计送达时间，(str)商品名称，(str)放置地点
                //type 3 OTT和娱乐应用:(str)状态字符串，(str)名称
                //type 4 日历和会议:(str)标题字符串，(str)时间字符串
                //type 5 健康与健身应用:(int)状态枚举{0(锻炼完成),1(目标完成)}，(str)状态字符串，(str)内容字符串，(int)完成进度(0-100)
                //type 6 Messaging :(str)0TP内容
                //type 7 Communication:(NULL)直接显示点赞
                //type 8 金融:(str)状态字符串，(str)付款人，(str)金额
                //type 9 Dating apps:(int)状态枚举{0(新配对)1(超级喜欢)}，(str)状态字符串 (str)姓名
                val list = mutableListOf<String>()
                for (t in content){
                    list.add(t)
                }
                bean.contentList = list
            }
            /*when (type) {
                SuperNotificationBean.NOTIFICATION_TYPE_TAXI -> {
                    bean.notificationContent = SuperNotificationBean.TaxiNotificationContent().apply {
                        orderStatus = content[0].toInt()
                        otp = content[1]
                        plateNumber = content[2]
                        hintText = content[3]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_TAKEOUT -> {
                    bean.notificationContent = SuperNotificationBean.TakeoutNotificationContent().apply {
                        orderStatus = content[0].toInt()
                        expectedTime = content[1]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_PARCEL -> {
                    bean.notificationContent = SuperNotificationBean.ParcelNotificationContent().apply {
                        orderStatus = content[0].toInt()
                        expectedTime = content[1]
                        productName = content[2]
                        productPlace = content[3]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_OTT -> {
                    bean.notificationContent = SuperNotificationBean.OTTNotificationContent().apply {
                        status = content[0].toInt()
                        name = content[1]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_CALENDAR -> {
                    bean.notificationContent = SuperNotificationBean.CalendarNotificationContent().apply {
                        title = content[0]
                        time = content[1]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_HEALTH -> {
                    bean.notificationContent = SuperNotificationBean.HealthNotificationContent().apply {
                        status = content[0].toInt()
                        statusStr = content[1]
                        content = content[2]
                        progress = content[3].toInt()
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_MESSAGING -> {
                    bean.notificationContent = SuperNotificationBean.MessagingNotificationContent().apply {
                        otp = content[0]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_COMMUNICATION -> {
                    bean.notificationContent = SuperNotificationBean.CommunicationNotificationContent().apply {
                        likeCount = content[0]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_FINANCE -> {
                    bean.notificationContent = SuperNotificationBean.FinanceNotificationContent().apply {
                        status = content[0].toInt()
                        payer = content[1]
                        amount = content[2]
                    }
                }

                SuperNotificationBean.NOTIFICATION_TYPE_DATING -> {
                    bean.notificationContent = SuperNotificationBean.DatingNotificationContent().apply {
                        status = content[0].toInt()
                        statusStr = content[1]
                        name = content[2]
                    }
                }
            }*/
            addLogBean("sendSuperNotification", bean)
            ControlBleTools.getInstance().sendSuperNotification(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("sendSuperNotification state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryRemindFavoriteHeadContacts.layoutSelectFile.btnSelectFile) {
            addLogI("btnSelectFile")
            DialogUtils.showSelectImgDialog(this, parentHeadFilePath) { selectedFile ->
                this.parentHeadFile = selectedFile
                binding.layoutBerryRemindFavoriteHeadContacts.layoutSelectFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.layoutBerryRemindFavoriteHeadContacts.btnGet) {
            addLogI("getFavoriteContacts.btnGet")
            ControlBleTools.getInstance().getFavoriteContacts(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getFavoriteContacts state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryRemindFavoriteHeadContacts.btnSet) {
            addLogI("layoutBerryRemindFavoriteHeadContacts.btnSet")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            if (favoriteContactsBean == null) {
                addLogE("favoriteContactsBean is null")
                return@clickCheckConnect
            }
            val list = mutableListOf<AddFavoriteContactBean>()
            for (i in 0..favoriteContactsBean!!.supportMax - 1) {
                list.add(
                    AddFavoriteContactBean().apply {
                        val fName = binding.layoutBerryRemindFavoriteHeadContacts.etName.text.toString().trim()
                        name = "$fName $i"
                        val fPhone = binding.layoutBerryRemindFavoriteHeadContacts.ePhone.text.toString().trim()
                        phoneNumber = "$fPhone$i"
                        if (::parentHeadFile.isInitialized) {
                            val headImg = ConvertUtils.bytes2Bitmap(FileIOUtils.readFile2BytesByStream(parentHeadFile))
                            avatarImg = headImg
                        } else {
                            when(i){
                                0 -> {
                                    var headImg = getAssetBitmap(this@BerryRemindActivity, "berry/fc_resource" + File.separator + "p_head.png")
                                    avatarImg = headImg
                                }
                                1 -> {
                                    var headImg = getAssetBitmap(this@BerryRemindActivity, "berry/fc_resource" + File.separator + "fc_t4.png")
                                    avatarImg = headImg
                                }
                                2 -> {
                                    var headImg = getAssetBitmap(this@BerryRemindActivity, "berry/fc_resource" + File.separator + "fc_t3.png")
                                    avatarImg = headImg
                                }
                                3 -> {
                                    var headImg = getAssetBitmap(this@BerryRemindActivity, "berry/fc_resource" + File.separator + "fc_t2_q10.png")
                                    avatarImg = headImg
                                }
                            }
                        }
                    })
            }

            val supportMax = favoriteContactsBean!!.supportMax
            val equipmentNumber = GlobalData.deviceInfo!!.equipmentNumber
            addLogBean("setFavoriteContacts supportMax=$supportMax equipmentNumber=$equipmentNumber",list)
            ControlBleTools.getInstance().setFavoriteContacts(supportMax, list, equipmentNumber, object : UploadBigDataListener {
                override fun onSuccess() {
                    addLogI("setFavoriteContacts onSuccess")
                    binding.layoutBerryRemindFavoriteHeadContacts.btnGet.callOnClick()
                }

                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                    val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                    addLogI("setFavoriteContacts onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                }

                override fun onTimeout(msg: String?) {
                    addLogE("setFavoriteContacts onTimeout msg=$msg")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryRemindFavoriteHeadContacts.btnDelete) {
            addLogI("layoutBerryRemindFavoriteHeadContacts.btnDelete")
            if (favoriteContactsBean == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            val list = favoriteContactsBean!!.list
            addLogBean("deleteFavoriteContacts",list)
            ControlBleTools.getInstance().deleteFavoriteContacts(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("deleteFavoriteContacts state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        CallBackUtils.whatsAppQuickReplyCallBack = object : WhatsAppQuickReplyCallBack {
            override fun onQuickReplyResult(list: ArrayList<String>) {
                addLogBean("whatsAppQuickReplyCallBack onQuickReplyResult", list)
            }

            override fun onMessage(phoneNumber: String?, text: String?) {
                addLogI("whatsAppQuickReplyCallBack onMessage phoneNumber=$phoneNumber text=$text")
            }
        }

        CallBackUtils.favoriteContactsCallBack = FavoriteContactsCallBack { bean ->
            addLogBean("favoriteContactsCallBack", bean)
            favoriteContactsBean = bean
        }

        CallBackUtils.deviceOpenNotifyAppCallBack = DeviceOpenNotifyAppCallBack { packageName ->
            addLogI("deviceOpenNotifyAppCallBack packageName=$packageName")
            val packageManager = BaseApplication.mContext.packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                BaseApplication.mContext.startActivity(launchIntent)
            } else {
                addLogI("deviceOpenNotifyAppCallBack launchIntent=null")
            }
        }
    }

    private fun getDeviceInfo() {
        addLogI("getDeviceInfo")
        ControlBleTools.getInstance().getDeviceInfo(object : SendCmdStateListener() {
            override fun onState(state: SendCmdState?) {
                addLogI("getDeviceInfo state=$state")
            }
        })
    }
}