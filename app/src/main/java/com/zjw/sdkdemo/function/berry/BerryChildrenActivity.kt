package com.zjw.sdkdemo.function.berry

import android.os.Bundle
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ThreadUtils
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.berry.children.AddParentInfoBean
import com.zhapp.ble.bean.berry.children.ChallengeInfoBean
import com.zhapp.ble.bean.berry.children.ChildrenInfoBean
import com.zhapp.ble.bean.berry.children.EarningsPriceBean
import com.zhapp.ble.bean.berry.children.FlashCardIdsBean
import com.zhapp.ble.bean.berry.children.FlashCardThemeBean
import com.zhapp.ble.bean.berry.children.MedalInfoBean
import com.zhapp.ble.bean.berry.children.ParentInfoBean
import com.zhapp.ble.callback.BerryBigFileResultCallBack
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.ChildrenCallBack
import com.zhapp.ble.callback.UploadBigDataListener
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBerryChildrenBinding
import com.zjw.sdkdemo.function.MainActivity.GlobalData
import com.zjw.sdkdemo.livedata.MyChildrenCallBack
import com.zjw.sdkdemo.utils.AssetUtils
import com.zjw.sdkdemo.utils.AssetUtils.getAssetBitmap
import com.zjw.sdkdemo.utils.DialogUtils
import java.io.File

class BerryChildrenActivity : BaseActivity() {
    private val binding by lazy { ActivityBerryChildrenBinding.inflate(layoutInflater) }
    private val tag = BerryChildrenActivity::class.java.simpleName

    private var parentInfos: ParentInfoBean? = null
    private var flashCardInfos: FlashCardIdsBean? = null
    private var challengeInfoBean: ChallengeInfoBean? = null

    private val parentHeadFilePath = AssetUtils.ASS_BERRY_FC_RESOURCE
    private lateinit var parentHeadFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_child_berry)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
        initData()
    }

    private fun initData() {
        FileUtils.createOrExistsDir(parentHeadFilePath)
        binding.layoutBerryChildParent.layoutSelectFile.tvTip.text = getString(R.string.select_file_attention, parentHeadFilePath)
    }

    private fun initView() {
        setMyCheckBox(binding.layoutBerryChildChild.cbTop, binding.layoutBerryChildChild.llBottom, binding.layoutBerryChildChild.ivHelp)
        setMyCheckBox(binding.layoutBerryChildParent.cbTop, binding.layoutBerryChildParent.llBottom, binding.layoutBerryChildParent.ivHelp)
        setMyCheckBox(binding.layoutBerryChildChallenge.cbTop, binding.layoutBerryChildChallenge.llBottom, binding.layoutBerryChildChallenge.ivHelp)
        setMyCheckBox(binding.layoutBerryChildFc.cbTop, binding.layoutBerryChildFc.llBottom, binding.layoutBerryChildFc.ivHelp)
        setMyCheckBox(binding.layoutBerryChildrenRevenue.cbTop, binding.layoutBerryChildrenRevenue.llBottom, binding.layoutBerryChildrenRevenue.ivHelp)
        setMyCheckBox(binding.layoutBerryChildrenMedal.cbTop, binding.layoutBerryChildrenMedal.llBottom, binding.layoutBerryChildrenMedal.ivHelp)
        binding.layoutBerryChildFc.cbFc1.setOnCheckedChangeListener(MyFlashCardCheckListener())
        binding.layoutBerryChildFc.cbFc2.setOnCheckedChangeListener(MyFlashCardCheckListener())
        binding.layoutBerryChildFc.cbFc3.setOnCheckedChangeListener(MyFlashCardCheckListener())
        binding.layoutBerryChildFc.cbFc4.setOnCheckedChangeListener(MyFlashCardCheckListener())
        binding.layoutBerryChildFc.cbFc5.setOnCheckedChangeListener(MyFlashCardCheckListener())
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutBerryChildChild.btnGet) {
            addLogI("layoutBerryChildChild.btnGet")
            addLogI("getChildrenInfoByBerry")
            ControlBleTools.getInstance().getChildrenInfoByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getChildrenInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildChild.btnGet) {
            addLogI("layoutBerryChildChild.btnGet")
            val bean = ChildrenInfoBean()
            bean.name = binding.layoutBerryChildChild.etName.text.toString()
            bean.bloodGroup = binding.layoutBerryChildChild.etBloodType.text.toString()
            bean.allergy = binding.layoutBerryChildChild.etAllergy.text.toString()
            bean.medication = binding.layoutBerryChildChild.etMedication.text.toString()
            bean.address = binding.layoutBerryChildChild.etAddress.text.toString()
            addLogBean("setChildrenInfoByBerry", bean)
            ControlBleTools.getInstance().setChildrenInfoByBerry(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setChildrenInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildParent.btnGet) {
            addLogI("layoutBerryChildParent.btnGet")
            addLogI("getParentInfoByBerry")
            ControlBleTools.getInstance().getParentInfoByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getParentInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildParent.layoutSelectFile.btnSelectFile) {
            addLogI("btnSelectFile")
            DialogUtils.showSelectImgDialog(this, parentHeadFilePath) { selectedFile ->
                parentHeadFile = selectedFile
                binding.layoutBerryChildParent.layoutSelectFile.tvFileName.text = selectedFile.name
            }
        }

        clickCheckConnect(binding.layoutBerryChildParent.btnAdd) {
            addLogI("layoutBerryChildParent.btnAdd")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            if (parentInfos == null || parentInfos!!.parentBaseInfos == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            if (parentInfos!!.headImgWidth == 0 || parentInfos!!.headImgHeight == 0 || parentInfos!!.headImgRadius == 0) {
                addLogI("Error ! headImgWidth == ${parentInfos!!.headImgWidth},headImgWidth == ${parentInfos!!.headImgHeight},headImgWidth == ${parentInfos!!.headImgRadius} ")
                return@clickCheckConnect
            }
            val bean = AddParentInfoBean()
            bean.id = binding.layoutBerryChildParent.etId.text.toString().trim().toInt()
            bean.name = binding.layoutBerryChildParent.etName.text.toString()
            bean.number = binding.layoutBerryChildParent.etPhone.text.toString()
            if (::parentHeadFile.isInitialized) {
                var headImg = ConvertUtils.bytes2Bitmap(FileIOUtils.readFile2BytesByStream(parentHeadFile))
                headImg = ImageUtils.scale(headImg, parentInfos!!.headImgWidth, parentInfos!!.headImgHeight)
                headImg = ImageUtils.toRoundCorner(headImg, parentInfos!!.headImgRadius * 1.0f)
                bean.avatarImg = headImg
            } else {
                var headImg = getAssetBitmap(this, parentHeadFilePath + "p_head.png")
                headImg = ImageUtils.scale(headImg, parentInfos!!.headImgWidth, parentInfos!!.headImgHeight)
                headImg = ImageUtils.toRoundCorner(headImg, parentInfos!!.headImgRadius * 1.0f)
                bean.avatarImg = headImg
            }

            val equipmentNumber = GlobalData.deviceInfo!!.equipmentNumber
            addLogBean("addParentInfoByBerry equipmentNumber=$equipmentNumber", bean)
            ControlBleTools.getInstance().addParentInfoByBerry(bean, equipmentNumber, object : UploadBigDataListener {
                override fun onSuccess() {
                    addLogI("addParentInfoByBerry onSuccess")
                    binding.layoutBerryChildParent.btnGet.callOnClick()
                }

                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                    val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                    addLogI("addParentInfoByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                }

                override fun onTimeout(msg: String?) {
                    addLogE("addParentInfoByBerry onTimeout msg=$msg")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildParent.btnDelete) {
            addLogI("layoutBerryChildParent.btnDelete")
            if (parentInfos == null || parentInfos!!.parentBaseInfos == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            val list = mutableListOf<Int>()
            for (item in parentInfos!!.parentBaseInfos) {
                list.add(item.id)
            }
            addLogBean("delParentInfoByBerry", list)
            ControlBleTools.getInstance().delParentInfoByBerry(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("delParentInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildFc.btnGet) {
            addLogI("layoutBerryChildFc.btnGet")
            addLogI("getFlashCardIdsByBerry")
            ControlBleTools.getInstance().getFlashCardIdsByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getFlashCardIdsByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildFc.btnSet) {
            addLogI("layoutBerryChildFc.btnSet")
            if (GlobalData.deviceInfo == null) {
                addLogI("deviceInfo is null")
                return@clickCheckConnect
            }
            if (flashCardInfos == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }

            val bean = FlashCardThemeBean().apply {
                themes = mutableListOf<FlashCardThemeBean.Theme>().apply {
                    if (fcTheme1 != null) {
                        //每种typeId仅支持存在一个
                        if (themes == null || themes.firstOrNull { it.typeId == fcTheme1!!.typeId } == null) add(fcTheme1!!)
                    }
                    if (fcTheme2 != null) {
                        //每种typeId仅支持存在一个
                        if (themes == null || themes.firstOrNull { it.typeId == fcTheme2!!.typeId } == null) add(fcTheme2!!)
                    }
                    if (fcTheme3 != null) {
                        //每种typeId仅支持存在一个
                        if (themes == null || themes.firstOrNull { it.typeId == fcTheme3!!.typeId } == null) add(fcTheme3!!)
                    }
                    if (fcTheme4 != null) {
                        //每种typeId仅支持存在一个
                        if (themes == null || themes.firstOrNull { it.typeId == fcTheme4!!.typeId } == null) add(fcTheme4!!)
                    }
                    if (fcTheme5 != null) {
                        //每种typeId仅支持存在一个
                        if (themes == null || themes.firstOrNull { it.typeId == fcTheme5!!.typeId } == null) add(fcTheme5!!)
                    }
                }
                //最多支持数量
                while (themes.size > flashCardInfos!!.maxCount) {
                    themes.removeAt(themes.size - 1)
                }
            }
            if (bean.themes.isEmpty()) {
                addLogI(getString(R.string.get_data_err1))
                return@clickCheckConnect
            }

            //已有的闪卡为空
            if (flashCardInfos!!.list.isNullOrEmpty()) {
                //直接发送

                val equipmentNumber = GlobalData.deviceInfo!!.equipmentNumber
                addLogBean("setupFlashCardByBerry equipmentNumber=$equipmentNumber", bean)
                ControlBleTools.getInstance().setupFlashCardByBerry(bean, equipmentNumber, object : UploadBigDataListener {
                    override fun onSuccess() {
                        addLogI("setupFlashCardByBerry onSuccess")
                        binding.layoutBerryChildFc.btnGet.callOnClick()
                    }

                    override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                        val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                        addLogI("setupFlashCardByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                    }

                    override fun onTimeout(msg: String?) {
                        addLogE("setupFlashCardByBerry onTimeout msg=$msg")
                    }

                })
            } else {
                //需要删除旧的
                //删除已有数据
                val list = flashCardInfos!!.list
                addLogBean("delFlashCardByBerry", list)
                ControlBleTools.getInstance().delFlashCardByBerry(list, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("delFlashCardByBerry state=$state")
                        if (state == SendCmdState.SUCCEED) {

                            val equipmentNumber = GlobalData.deviceInfo!!.equipmentNumber
                            addLogBean("setupFlashCardByBerry equipmentNumber=$equipmentNumber", bean)
                            ControlBleTools.getInstance().setupFlashCardByBerry(bean, equipmentNumber, object : UploadBigDataListener {
                                override fun onSuccess() {
                                    addLogI("setupFlashCardByBerry onSuccess")
                                    binding.layoutBerryChildFc.btnGet.callOnClick()
                                }

                                override fun onProgress(curPiece: Int, dataPackTotalPieceLength: Int) {
                                    val percentage = (curPiece * 100 / dataPackTotalPieceLength)
                                    addLogI("setupFlashCardByBerry onProgress curPiece=$curPiece dataPackTotalPieceLength=$dataPackTotalPieceLength  percentage=$percentage")
                                }

                                override fun onTimeout(msg: String?) {
                                    addLogE("setupFlashCardByBerry onTimeout msg=$msg")
                                }
                            })
                        }
                    }
                })
            }
        }

        clickCheckConnect(binding.layoutBerryChildFc.btnDelete) {
            addLogI("layoutBerryChildFc.btnDelete")
            if (flashCardInfos == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            val list = flashCardInfos!!.list
            addLogBean("delFlashCardByBerry", list)
            ControlBleTools.getInstance().delFlashCardByBerry(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("delFlashCardByBerry state=$state")
                }
            })

            ThreadUtils.runOnUiThreadDelayed({
                binding.layoutBerryChildFc.btnGet.callOnClick()
            }, 1000)
        }

        clickCheckConnect(binding.layoutBerryChildChallenge.btnGet) {
            addLogI("layoutBerryChildChallenge.btnGet")
            addLogI("getChallengeInfoByBerry")
            ControlBleTools.getInstance().getChallengeInfoByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getChallengeInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildChallenge.btnSet) {
            addLogI("layoutBerryChildChallenge.btnSet")
            if (challengeInfoBean == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }
            for (item in challengeInfoBean!!.detailList) {
                for (i in 0..<item.content.size) {
                    item.content[i] = "new ${item.content[i]}"
                }
            }
            val bean = challengeInfoBean!!
            addLogBean("modifyChallengeInfoByBerry", bean)
            ControlBleTools.getInstance().modifyChallengeInfoByBerry(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("modifyChallengeInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildrenRevenue.btnGet) {
            addLogI("layoutBerryChildrenRevenue.btnGet")
            addLogI("getEarningsInfoByBerry")
            ControlBleTools.getInstance().getEarningsInfoByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getEarningsInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildrenRevenue.btnSet) {
            addLogI("layoutBerryChildrenRevenue.btnSet")
            val bean = EarningsPriceBean()
            bean.stepsGoal = binding.layoutBerryChildrenRevenue.etTargetPriceStep.text.toString().trim().toInt()
            bean.sleepGoal = binding.layoutBerryChildrenRevenue.etTargetPriceSleep.text.toString().trim().toInt()
            bean.calGoal = binding.layoutBerryChildrenRevenue.etTargetPriceCalorie.text.toString().trim().toInt()
            bean.distanceGoal = binding.layoutBerryChildrenRevenue.etTargetPriceDistance.text.toString().trim().toInt()
            bean.flashCard = binding.layoutBerryChildrenRevenue.etTargetPriceFc.text.toString().trim().toInt()
            bean.task = binding.layoutBerryChildrenRevenue.etTargetPriceTask.text.toString().trim().toInt()
            bean.challenge = binding.layoutBerryChildrenRevenue.etTargetPriceChallenge.text.toString().trim().toInt()
            bean.medals = binding.layoutBerryChildrenRevenue.etTargetPriceMedals.text.toString().trim().toInt()
            addLogBean("setEarningsPriceByBerry",bean)
            ControlBleTools.getInstance().setEarningsPriceByBerry(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEarningsPriceByBerry state=$state")
                }
            })
        }


        clickCheckConnect(binding.layoutBerryChildrenMedal.btnGet) {
            addLogI("layoutBerryChildrenMedal.btnGet")
            addLogI("getMedalInfoByBerry")
            ControlBleTools.getInstance().getMedalInfoByBerry(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getMedalInfoByBerry state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBerryChildrenMedal.btnSet) {
            addLogI("layoutBerryChildrenMedal.btnSet")
            val modalStep = binding.layoutBerryChildrenMedal.etStep.text.toString().toInt()
            val modalChallenge = binding.layoutBerryChildrenMedal.etChallenge.text.toString().toInt()
            val modalFcCard = binding.layoutBerryChildrenMedal.etFcCard.text.toString().toInt()
            val modalScheduler = binding.layoutBerryChildrenMedal.etScheduler.text.toString().toInt()
            val list = arrayListOf<MedalInfoBean>()
            list.apply {
                add(MedalInfoBean().apply {
                    medalId = ChildrenCallBack.MedalId.STEPS_ID.id
                    completedProgress = modalStep
                })
                add(MedalInfoBean().apply {
                    medalId = ChildrenCallBack.MedalId.CHALLENGE_ID.id
                    completedProgress = modalChallenge
                })
                add(MedalInfoBean().apply {
                    medalId = ChildrenCallBack.MedalId.FLASH_CARD_ID.id
                    completedProgress = modalFcCard
                })
                add(MedalInfoBean().apply {
                    medalId = ChildrenCallBack.MedalId.SCHEDULER_ID.id
                    completedProgress = modalScheduler
                })
            }
            addLogBean("setMedalInfoByBerry",list)
            ControlBleTools.getInstance().setMedalInfoByBerry(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setMedalInfoByBerry state=$state")
                }
            })
        }
    }


    private var fcTheme1: FlashCardThemeBean.Theme? = null
    private var fcTheme2: FlashCardThemeBean.Theme? = null
    private var fcTheme3: FlashCardThemeBean.Theme? = null
    private var fcTheme4: FlashCardThemeBean.Theme? = null
    private var fcTheme5: FlashCardThemeBean.Theme? = null
    private var fcDataIndex = 0

    inner class MyFlashCardCheckListener : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
            if (isChecked) {
                fcDataIndex += 1
            }
            when (buttonView?.id) {
                R.id.cbFc1 -> {
                    if (isChecked) {
                        fcTheme1 = FlashCardThemeBean.Theme().apply {
                            themeId = 1
                            typeId = 1
                            topicId = 1
                            topicTitle = "$fcDataIndex 1111111111"
                            promptContent = "$fcDataIndex 111"
                            topics = mutableListOf<FlashCardThemeBean.BaseTopicType>().apply {
                                //getMaxNum() == 10 //--------------Up to 10 can be set, and there must be 10, otherwise it is invalid--------------
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex because"
                                    questionTextByHI = "$fcDataIndex क्योंकि"
                                    answerTextByEN = "$fcDataIndex Gives a reason"
                                    answerTextByHI = "$fcDataIndex कारण बताता है"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex people"
                                    questionTextByHI = "$fcDataIndex लोग"
                                    answerTextByEN = "$fcDataIndex Human beings"
                                    answerTextByHI = "$fcDataIndex मानव समुदाय"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex country"
                                    questionTextByHI = "$fcDataIndex देश"
                                    answerTextByEN = "$fcDataIndex Nation/land"
                                    answerTextByHI = "$fcDataIndex राष्ट्र/भूमि"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex morning"
                                    questionTextByHI = "$fcDataIndex सुबह"
                                    answerTextByEN = "$fcDataIndex Early part of day"
                                    answerTextByHI = "$fcDataIndex दिन का आरंभिक भाग"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex evening"
                                    questionTextByHI = "$fcDataIndex शाम"
                                    answerTextByEN = "$fcDataIndex Late part of day"
                                    answerTextByHI = "$fcDataIndex दिन का अंतिम भाग"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex because"
                                    questionTextByHI = "$fcDataIndex क्योंकि"
                                    answerTextByEN = "$fcDataIndex Gives a reason"
                                    answerTextByHI = "$fcDataIndex कारण बताता है"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex people"
                                    questionTextByHI = "$fcDataIndex लोग"
                                    answerTextByEN = "$fcDataIndex Human beings"
                                    answerTextByHI = "$fcDataIndex मानव समुदाय"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex country"
                                    questionTextByHI = "$fcDataIndex देश"
                                    answerTextByEN = "$fcDataIndex Nation/land"
                                    answerTextByHI = "$fcDataIndex राष्ट्र/भूमि"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex morning"
                                    questionTextByHI = "$fcDataIndex सुबह"
                                    answerTextByEN = "$fcDataIndex Early part of day"
                                    answerTextByHI = "$fcDataIndex दिन का आरंभिक भाग"
                                })
                                add(FlashCardThemeBean.TopicType1().apply {
                                    questionTextByEN = "$fcDataIndex evening"
                                    questionTextByHI = "$fcDataIndex शाम"
                                    answerTextByEN = "$fcDataIndex Late part of day"
                                    answerTextByHI = "$fcDataIndex दिन का अंतिम भाग"
                                })
                            }
                        }
                        addLogI("fcTheme1=${formatObject(fcTheme1!!)}")
                    } else {
                        fcTheme1 = null
                    }
                }

                R.id.cbFc2 -> {
                    if (isChecked) {
                        fcTheme2 = FlashCardThemeBean.Theme().apply {
                            themeId = 2
                            typeId = 2
                            topicId = 2
                            topicTitle = "$fcDataIndex 2222222222"
                            promptContent = "$fcDataIndex 222"
                            topics = mutableListOf<FlashCardThemeBean.BaseTopicType>().apply {
                                //getMaxNum() == 10 //--------------Up to 10 can be set, and there must be 10, otherwise it is invalid--------------
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q1.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a1.png")
                                    answerText = "$fcDataIndex Bahrain"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q2.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a2.png")
                                    answerText = "$fcDataIndex Bangladesh"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q3.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a3.png")
                                    answerText = "$fcDataIndex Bhutan"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q4.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a4.png")
                                    answerText = "$fcDataIndex Hong Kong"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q5.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a5.png")
                                    answerText = "$fcDataIndex South Africa"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q6.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a6.png")
                                    answerText = "$fcDataIndex Switzerland"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q7.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a7.png")
                                    answerText = "$fcDataIndex Turkey"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q8.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a8.png")
                                    answerText = "$fcDataIndex Ukraine"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q9.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a9.png")
                                    answerText = "$fcDataIndex Ukraine2"
                                })
                                add(FlashCardThemeBean.TopicType2().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_q10.png")
                                    answerImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t2_a10.png")
                                    answerText = "$fcDataIndex Ukraine3"
                                })
                            }
                        }
                        addLogI("fcTheme2=${formatObject(fcTheme2!!)}")
                    } else {
                        fcTheme2 = null
                    }
                }

                R.id.cbFc3 -> {
                    if (isChecked) {
                        fcTheme3 = FlashCardThemeBean.Theme().apply {
                            themeId = 3
                            typeId = 3
                            topicId = 3
                            topicTitle = "$fcDataIndex 3333333333"
                            promptContent = "$fcDataIndex 333"
                            topics = mutableListOf<FlashCardThemeBean.BaseTopicType>().apply {
                                //getMaxNum() == 10 //--------------Up to 10 can be set, and there must be 10, otherwise it is invalid --------------
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 1 "
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 1"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 1"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 2"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 3"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 2"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 2"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 2"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 2"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 2"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 3"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 3"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 3"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 3"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 3"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 4"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 4"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 4"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 4"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 4"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 5"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 5"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 5"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 5"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 5"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 6"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 6"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 6"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 6"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 6"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 7"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 7"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 7"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 7"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 7"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 8"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 8"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 8"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 8"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 8"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 9"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 9"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 9"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 9"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 9"
                                })
                                add(FlashCardThemeBean.TopicType3().apply {
                                    questionText = "$fcDataIndex Guess the macros of the food 0"
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t3.png")
                                    answerText1 = "$fcDataIndex Protein: 11g 0"
                                    answerText2 = "$fcDataIndex Carbs: 22.8g 0"
                                    answerText3 = "$fcDataIndex Fats: 0.3g 0"
                                    answerText4 = "$fcDataIndex Fibre: 2.6g 0"
                                })
                            }
                        }
                        addLogI("fcTheme3=${formatObject(fcTheme3!!)}")
                    } else {
                        fcTheme3 = null
                    }
                }

                R.id.cbFc4 -> {
                    if (isChecked) {
                        fcTheme4 = FlashCardThemeBean.Theme().apply {
                            themeId = 4
                            typeId = 4
                            topicId = 4
                            topicTitle = "$fcDataIndex 4444444444"
                            promptContent = "$fcDataIndex 444"
                            topics = mutableListOf<FlashCardThemeBean.BaseTopicType>().apply {
                                //getMaxNum() == 10 //--------------Up to 10 can be set, and there must be 10, otherwise it is invalid --------------
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 1
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 2
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 3
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 4
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 1
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 2
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 3
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 4
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 1
                                })
                                add(FlashCardThemeBean.TopicType4().apply {
                                    questionImg = getAssetBitmap(this@BerryChildrenActivity, parentHeadFilePath + "fc_t4.png")
                                    answerNumber = 2
                                })
                            }
                        }
                        addLogI("fcTheme4=${formatObject(fcTheme4!!)}")
                    } else {
                        fcTheme4 = null
                    }
                }

                R.id.cbFc5 -> {
                    if (isChecked) {
                        fcTheme5 = FlashCardThemeBean.Theme().apply {
                            themeId = 5
                            typeId = 5
                            topicId = 5
                            topicTitle = "$fcDataIndex 5555555555"
                            promptContent = "$fcDataIndex 555"
                            topics = mutableListOf<FlashCardThemeBean.BaseTopicType>().apply {
                                //getMaxNum() == 10 //--------------Up to 10 can be set, and there must be 10, otherwise it is invalid --------------
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which planet is closest to the Sun?"
                                    answerNumber = 4
                                    answerText1 = "$fcDataIndex Earth"
                                    answerText2 = "$fcDataIndex Venus"
                                    answerText3 = "$fcDataIndex Mars"
                                    answerText4 = "$fcDataIndex Mercury"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex What is the capital of India?"
                                    answerNumber = 3
                                    answerText1 = "$fcDataIndex Mumbai"
                                    answerText2 = "$fcDataIndex Chennai"
                                    answerText3 = "$fcDataIndex New Delhi"
                                    answerText4 = "$fcDataIndex Kolkata"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which animal is known as the \"King of the Jungle\"?"
                                    answerNumber = 3
                                    answerText1 = "$fcDataIndex Elephant"
                                    answerText2 = "$fcDataIndex Tiger"
                                    answerText3 = "$fcDataIndex Lion"
                                    answerText4 = "$fcDataIndex Zebra"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which country is famous for the Eiffel Tower?"
                                    answerNumber = 2
                                    answerText1 = "$fcDataIndex Italy"
                                    answerText2 = "$fcDataIndex France"
                                    answerText3 = "$fcDataIndex Spain"
                                    answerText4 = "$fcDataIndex Germany"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which fruit is known for having many seeds and is red or pink in color?"
                                    answerNumber = 2
                                    answerText1 = "$fcDataIndex Apple"
                                    answerText2 = "$fcDataIndex Watermelon"
                                    answerText3 = "$fcDataIndex Mango"
                                    answerText4 = "$fcDataIndex Pear"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which planet is closest to the Sun?"
                                    answerNumber = 4
                                    answerText1 = "$fcDataIndex Earth"
                                    answerText2 = "$fcDataIndex Venus"
                                    answerText3 = "$fcDataIndex Mars"
                                    answerText4 = "$fcDataIndex Mercury"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex What is the capital of India?"
                                    answerNumber = 3
                                    answerText1 = "$fcDataIndex Mumbai"
                                    answerText2 = "$fcDataIndex Chennai"
                                    answerText3 = "$fcDataIndex New Delhi"
                                    answerText4 = "$fcDataIndex Kolkata"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which animal is known as the \"King of the Jungle\"?"
                                    answerNumber = 3
                                    answerText1 = "$fcDataIndex Elephant"
                                    answerText2 = "$fcDataIndex Tiger"
                                    answerText3 = "$fcDataIndex Lion"
                                    answerText4 = "$fcDataIndex Zebra"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which country is famous for the Eiffel Tower?"
                                    answerNumber = 2
                                    answerText1 = "$fcDataIndex Italy"
                                    answerText2 = "$fcDataIndex France"
                                    answerText3 = "$fcDataIndex Spain"
                                    answerText4 = "$fcDataIndex Germany"
                                })
                                add(FlashCardThemeBean.TopicType5().apply {
                                    questionText = "$fcDataIndex Which fruit is known for having many seeds and is red or pink in color?"
                                    answerNumber = 2
                                    answerText1 = "$fcDataIndex Apple"
                                    answerText2 = "$fcDataIndex Watermelon"
                                    answerText3 = "$fcDataIndex Mango"
                                    answerText4 = "$fcDataIndex Pear"
                                })
                            }
                        }
                        addLogI("fcTheme5=${formatObject(fcTheme5!!)}")
                    } else {
                        fcTheme5 = null
                    }
                }
            }
        }
    }

    private fun initCallBack() {

        MyChildrenCallBack.onChildrenInfo.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onChildrenInfo", bean!!)
        })

        MyChildrenCallBack.onParentInfos.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onChildrenInfo", bean!!)
            parentInfos = bean
        })

        MyChildrenCallBack.onFlashCardInfos.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onFlashCardInfos", bean!!)
            flashCardInfos = bean
        })

        MyChildrenCallBack.onFlashCardProgress.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onFlashCardProgressv", bean!!)
        })

        MyChildrenCallBack.onChallengeInfos.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onChallengeInfos", bean!!)
            challengeInfoBean = bean
        })

        MyChildrenCallBack.onChallengeResult.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onChallengeResult", bean!!)
        })

        MyChildrenCallBack.onEarningsInfo.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onEarningsInfo", bean!!)
        })

        MyChildrenCallBack.onChangePocketMoney.observe(this, Observer { bean ->
            addLogBean("MyChildrenCallBack.onChangePocketMoney", bean!!)
        })


        MyChildrenCallBack.onMedalInfo.observe(this, Observer { list ->
            addLogBean("MyChildrenCallBack.onMedalInfo", list!!)
        })

        CallBackUtils.berryBigFileResultCallBack = BerryBigFileResultCallBack { bean ->
            addLogBean("berryBigFileResultCallBack", bean)
        }
    }
}