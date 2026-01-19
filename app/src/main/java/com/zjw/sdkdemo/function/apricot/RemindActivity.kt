package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ClassicBluetoothStateBean
import com.zhapp.ble.bean.ClockInfoBean
import com.zhapp.ble.bean.ClockInfoBean.DataBean
import com.zhapp.ble.bean.CommonReminderBean
import com.zhapp.ble.bean.ContactBean
import com.zhapp.ble.bean.ContactLotBean
import com.zhapp.ble.bean.EmergencyContactBean
import com.zhapp.ble.bean.EventInfoBean
import com.zhapp.ble.bean.NotificationSettingsBean
import com.zhapp.ble.bean.PhysiologicalCycleBean
import com.zhapp.ble.bean.SleepReminder
import com.zhapp.ble.bean.TimeBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.CallReminderModeCallback
import com.zhapp.ble.callback.CallStateCallBack
import com.zhapp.ble.callback.ContactCallBack
import com.zhapp.ble.callback.ContactLotCallBack
import com.zhapp.ble.callback.EmergencyContactsCallBack
import com.zhapp.ble.callback.PhysiologicalCycleCallBack
import com.zhapp.ble.callback.QuickReplyCallBack
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityRemindBinding
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.utils.DialogUtils

class RemindActivity : BaseActivity() {
    val binding by lazy { ActivityRemindBinding.inflate(layoutInflater) }
    private val tag: String = RemindActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_reminder)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.llLogContent, binding.layoutLog.btnClear, binding.layoutLog.btnSet, binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallback()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutNoticeSystem.cbTop, binding.layoutNoticeSystem.llBottom, binding.layoutNoticeSystem.ivHelp)
        setMyCheckBox(binding.layoutNoticeOther.cbTop, binding.layoutNoticeOther.llBottom, binding.layoutNoticeOther.ivHelp)
        setMyCheckBox(binding.layoutQuitReply.cbTop, binding.layoutQuitReply.llBottom, binding.layoutQuitReply.ivHelp)
        setMyCheckBox(binding.layoutContactsList.cbTop, binding.layoutContactsList.llBottom, binding.layoutContactsList.ivHelp)
        setMyCheckBox(binding.layoutCallSet.cbTop, binding.layoutCallSet.llBottom, binding.layoutCallSet.ivHelp)
        setMyCheckBox(binding.layoutSosContacts.cbTop, binding.layoutSosContacts.llBottom, binding.layoutSosContacts.ivHelp)
        setMyCheckBox(binding.layoutFavoriteContacts.cbTop, binding.layoutFavoriteContacts.llBottom, binding.layoutFavoriteContacts.ivHelp)
        setMyCheckBox(binding.layoutEvent.cbTop, binding.layoutEvent.llBottom, binding.layoutEvent.ivHelp)
        setMyCheckBox(binding.layoutClock.cbTop, binding.layoutClock.llBottom, binding.layoutClock.ivHelp)
        setMyCheckBox(binding.layoutSedentary.cbTop, binding.layoutSedentary.llBottom, binding.layoutSedentary.ivHelp)
        setMyCheckBox(binding.layoutMedication.cbTop, binding.layoutMedication.llBottom, binding.layoutMedication.ivHelp)
        setMyCheckBox(binding.layoutDrinkWater.cbTop, binding.layoutDrinkWater.llBottom, binding.layoutDrinkWater.ivHelp)
        setMyCheckBox(binding.layoutMeals.cbTop, binding.layoutMeals.llBottom, binding.layoutMeals.ivHelp)
        setMyCheckBox(binding.layoutWashHand.cbTop, binding.layoutWashHand.llBottom, binding.layoutWashHand.ivHelp)
        setMyCheckBox(binding.layoutPhysiologicalCycle.cbTop, binding.layoutPhysiologicalCycle.llBottom, binding.layoutPhysiologicalCycle.ivHelp)
        setMyCheckBox(binding.layoutNoiceSet.cbTop, binding.layoutNoiceSet.llBottom, binding.layoutNoiceSet.ivHelp)
        setMyCheckBox(binding.layoutBtCallSwitch.cbTop, binding.layoutBtCallSwitch.llBottom, binding.layoutBtCallSwitch.ivHelp)
        setMyCheckBox(binding.layoutLargeContactsList.cbTop, binding.layoutLargeContactsList.llBottom, binding.layoutLargeContactsList.ivHelp)
        setMyCheckBox(binding.layoutSleepReminder.cbTop, binding.layoutSleepReminder.llBottom, binding.layoutSleepReminder.ivHelp)
        binding.layoutMedication.layoutReminder.cbDoNotDisturbSwitch.visibility = View.GONE
        binding.layoutMeals.layoutReminder.cbDoNotDisturbSwitch.visibility = View.GONE
        binding.layoutWashHand.layoutReminder.cbDoNotDisturbSwitch.visibility = View.GONE

        selectTime(binding.layoutEvent.tvStartTime)
        selectDate(binding.layoutPhysiologicalCycle.tvStartDate)
        selectSettingTime(binding.layoutClock.tvStartTime)
        selectSettingTime(binding.layoutSleepReminder.tvStartTime)

        selectSettingTime(binding.layoutSedentary.layoutReminder.tvStartTime)
        selectSettingTime(binding.layoutSedentary.layoutReminder.tvEndTime)

        selectSettingTime(binding.layoutMedication.layoutReminder.tvStartTime)
        selectSettingTime(binding.layoutMedication.layoutReminder.tvEndTime)

        selectSettingTime(binding.layoutDrinkWater.layoutReminder.tvStartTime)
        selectSettingTime(binding.layoutDrinkWater.layoutReminder.tvEndTime)

        selectSettingTime(binding.layoutMeals.layoutReminder.tvStartTime)
        selectSettingTime(binding.layoutMeals.layoutReminder.tvEndTime)

        selectSettingTime(binding.layoutWashHand.layoutReminder.tvStartTime)
        selectSettingTime(binding.layoutWashHand.layoutReminder.tvEndTime)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutNoticeSystem.btnCallReminder) {
            addLogI("btnCallReminder")
            sendSysNotice(0)
        }

        clickCheckConnect(binding.layoutNoticeSystem.btnMissedCall) {
            addLogI("btnMissedCall")
            sendSysNotice(1)
        }

        clickCheckConnect(binding.layoutNoticeSystem.btnSmsReminder) {
            addLogI("btnSmsReminder")
            sendSysNotice(2)
        }

        clickCheckConnect(binding.layoutNoticeSystem.btnHangUpPhone) {
            addLogI("btnHangUpPhone")
            val value = 1
            addLogI("sendCallState value=$value")
            ControlBleTools.getInstance().sendCallState(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendCallState state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutNoticeSystem.btnAnswerThePhone) {
            addLogI("btnAnswerThePhone")
            val value = 0
            addLogI("sendCallState value=$value")
            ControlBleTools.getInstance().sendCallState(value, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("sendCallState state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutNoticeOther.btnSelectOtherApp) {
            addLogI("btnSelectOtherApp")
            DialogUtils.showAppDialog(this, binding.layoutNoticeOther.etAppName, binding.layoutNoticeOther.etPackName)
        }

        clickCheckConnect(binding.layoutNoticeOther.btnSend) {
            addLogI("layoutNoticeOther.btnSend")
            val appPackName = binding.layoutNoticeOther.etPackName.text.toString().trim()
            val appName = binding.layoutNoticeOther.etAppName.text.toString().trim()
            if (!TextUtils.isEmpty(appPackName) && !TextUtils.isEmpty(appName)) {
                val title = binding.layoutNoticeOther.etTitle.text.toString().trim()
                val text = binding.layoutNoticeOther.etContext.text.toString().trim()
                val ticker = binding.layoutNoticeOther.etTicker.text.toString().trim()
                addLogI("sendAppNotification appName=$appName appPackName=$appPackName title=$title text=$text ticker=$ticker")
                ControlBleTools.getInstance().sendAppNotification(appName, appPackName, title, text, ticker, object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState?) {
                        addLogI("sendAppNotification state=$state")
                    }
                })
            } else {
                addLogI(getString(R.string.please_select_app))
            }
        }

        clickCheckConnect(binding.layoutCallSet.btnGet) {
            addLogI("layoutCallSet.btnGet")
            addLogI("getCallModeSettings")
            ControlBleTools.getInstance().getCallModeSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("getCallModeSettings state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutCallSet.btnSet) {
            addLogI("layoutCallSet.btnSet")
            val mode = binding.layoutCallSet.etMode.text.toString().trim().toInt()
            val volume = binding.layoutCallSet.etVolume.text.toString().trim().toInt()
            addLogI("setCallModeSettings mode=$mode volume=$volume")
            ControlBleTools.getInstance().setCallModeSettings(mode, volume, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState?) {
                    addLogI("setCallModeSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutQuitReply.btnGet) {
            addLogI("layoutQuitReply.btnGet")
            addLogI("getDevShortReplyData")
            ControlBleTools.getInstance().getDevShortReplyData(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getDevShortReplyData state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutQuitReply.btnSet) {
            addLogI("layoutQuitReply.btnSet")
            val list = ArrayList<String?>()
            val reply = binding.layoutQuitReply.etContent.text.toString().trim()
            if (reply.contains(",")) {
                val rs: Array<String?> = reply.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                list.addAll(listOf(*rs))
            } else {
                list.add(reply)
            }
            addLogBean("setDevShortReplyData", list)
            ControlBleTools.getInstance().setDevShortReplyData(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setDevShortReplyData state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutContactsList.btnGet) {
            addLogI("layoutContactsList.btnGet")
            addLogI("getContactList")
            ControlBleTools.getInstance().getContactList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getContactList state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutContactsList.btnSet) {
            addLogI("layoutContactsList.btnSet")
            val list: MutableList<ContactBean?> = java.util.ArrayList<ContactBean?>()
            for (i in 0..9) {
                val contactBean = ContactBean()
                contactBean.contacts_name = "name_$i"
                contactBean.contacts_number = "1234567890$i"
                list.add(contactBean)
            }
            addLogBean("setContactList", list)
            ControlBleTools.getInstance().setContactList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setContactList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSosContacts.btnGet) {
            addLogI("layoutSosContacts.btnGet")
            addLogI("getEmergencyContacts")
            ControlBleTools.getInstance().getEmergencyContacts(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getEmergencyContacts state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutSosContacts.btnSet) {
            addLogI("layoutSosContacts.btnSet")
            val bean = EmergencyContactBean()
            val sosList: MutableList<ContactBean?> = java.util.ArrayList<ContactBean?>()
            val contactBean = ContactBean()
            val name = binding.layoutSosContacts.etName.getText().toString().trim()
            val phone = binding.layoutSosContacts.etPhone.getText().toString().trim()
            contactBean.contacts_name = name
            contactBean.contacts_number = phone
            contactBean.contacts_sequence = 1
            sosList.add(contactBean)
            bean.contactList = sosList
            bean.sosSwitch = true
            bean.max = 1
            addLogBean("setEmergencyContacts", bean)
            ControlBleTools.getInstance().setEmergencyContacts(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEmergencyContacts state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutFavoriteContacts.btnGet) {
            addLogI("layoutFavoriteContacts.btnGet")
            addLogI("getCollectContactList")
            ControlBleTools.getInstance().getCollectContactList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getCollectContactList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutFavoriteContacts.btnSet) {
            addLogI("layoutFavoriteContacts.btnSet")
            val bean = EmergencyContactBean()
            val list: MutableList<ContactBean?> = java.util.ArrayList<ContactBean?>()
            val contactBean = ContactBean()
            val name = binding.layoutFavoriteContacts.etName.getText().toString().trim()
            val phone = binding.layoutFavoriteContacts.etPhone.getText().toString().trim()
            contactBean.contacts_name = name
            contactBean.contacts_number = phone
            contactBean.contacts_sequence = 1
            list.add(contactBean)
            bean.contactList = list
            bean.sosSwitch = true
            bean.max = 1
            addLogBean("setCollectContactList", list)
            ControlBleTools.getInstance().setCollectContactList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEmergencyContacts state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutEvent.btnGet) {
            addLogI("layoutEvent.btnGet")
            addLogI("getEventInfoList")
            ControlBleTools.getInstance().getEventInfoList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getEventInfoList state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutEvent.btnSet) {
            addLogI("layoutEvent.btnSet")
            val list = ArrayList<EventInfoBean?>()
            for (i in 0..4) {
                val infoBean = EventInfoBean()
                val description = binding.layoutEvent.etDescription.text.toString().trim() + "" + i
                val timeBean: TimeBean = DialogUtils.getTimeBean(binding.layoutEvent.tvStartTime)
                timeBean.minute = if (timeBean.minute < 55) {
                    timeBean.minute + i
                } else {
                    timeBean.minute - i
                }
                infoBean.time = timeBean
                infoBean.description = description
                infoBean.isFinish = binding.layoutEvent.cbIsEnd.isChecked
                list.add(infoBean)
            }
            addLogBean("setEventInfoList", list)
            ControlBleTools.getInstance().setEventInfoList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setEventInfoList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutClock.btnGet) {
            addLogI("layoutClock.btnGet")
            addLogI("getClockInfoList")
            ControlBleTools.getInstance().getClockInfoList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getClockInfoList state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutClock.btnSet) {
            addLogI("layoutClock.btnSet")
            val list = ArrayList<ClockInfoBean?>()
            val clockInfo = ClockInfoBean()
            val bean = DataBean()
            bean.time = DialogUtils.getSettingTimeBean(binding.layoutClock.tvStartTime)
            bean.clockName = binding.layoutClock.etName.text.toString().trim()
            bean.isEnable = binding.layoutClock.cbSwitch.isChecked
            bean.isMonday = binding.layoutClock.cbWeek1.isChecked
            bean.isTuesday = binding.layoutClock.cbWeek2.isChecked
            bean.isWednesday = binding.layoutClock.cbWeek3.isChecked
            bean.isThursday = binding.layoutClock.cbWeek4.isChecked
            bean.isFriday = binding.layoutClock.cbWeek5.isChecked
            bean.isSaturday = binding.layoutClock.cbWeek6.isChecked
            bean.isSunday = binding.layoutClock.cbWeek7.isChecked
            bean.calculateWeekDays()
            clockInfo.id = 0
            clockInfo.data = bean
            list.add(clockInfo)
            list.add(clockInfo)
            addLogBean("setClockInfoList", list)
            ControlBleTools.getInstance().setClockInfoList(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setClockInfoList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSedentary.btnGet) {
            addLogI("layoutSedentary.btnGet")
            addLogI("getSedentaryReminder")
            ControlBleTools.getInstance().getSedentaryReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSedentaryReminder state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutSedentary.btnSet) {
            addLogI("layoutSedentary.btnSet")
            val bean = CommonReminderBean()
            bean.isOn = binding.layoutSedentary.layoutReminder.cbSwitch.isChecked
            bean.noDisturbInLaunch = binding.layoutSedentary.layoutReminder.cbDoNotDisturbSwitch.isChecked
            val frequency = binding.layoutSedentary.layoutReminder.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutSedentary.layoutReminder.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutSedentary.layoutReminder.tvEndTime)
            bean.frequency = frequency
            addLogBean("setSedentaryReminder", bean)
            ControlBleTools.getInstance().setSedentaryReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSedentaryReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutMedication.btnGet) {
            addLogI("layoutMedication.btnGet")
            addLogI("getMedicationReminder")
            ControlBleTools.getInstance().getMedicationReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getMedicationReminder state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutMedication.btnSet) {
            addLogI("layoutMedication.btnSet")
            val bean = CommonReminderBean()
            bean.isOn = binding.layoutMedication.layoutReminder.cbSwitch.isChecked
            val frequency = binding.layoutMedication.layoutReminder.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutMedication.layoutReminder.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutMedication.layoutReminder.tvEndTime)
            bean.frequency = frequency
            addLogBean("setMedicationReminder", bean)
            ControlBleTools.getInstance().setMedicationReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setMedicationReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutDrinkWater.btnGet) {
            addLogI("layoutMeals.btnGet")
            addLogI("getDrinkWaterReminder")
            ControlBleTools.getInstance().getDrinkWaterReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getScreenDisplay state=$state")

                }
            })
        }
        clickCheckConnect(binding.layoutDrinkWater.btnSet) {
            addLogI("layoutMeals.btnSet")
            val bean = CommonReminderBean()
            bean.isOn = binding.layoutDrinkWater.layoutReminder.cbSwitch.isChecked
            bean.noDisturbInLaunch = binding.layoutDrinkWater.layoutReminder.cbDoNotDisturbSwitch.isChecked
            val frequency = binding.layoutDrinkWater.layoutReminder.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutDrinkWater.layoutReminder.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutDrinkWater.layoutReminder.tvEndTime)
            bean.frequency = frequency
            addLogBean("setDrinkWaterReminder", bean)
            ControlBleTools.getInstance().setDrinkWaterReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setDrinkWaterReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutMeals.btnGet) {
            addLogI("layoutMeals.btnGet")
            addLogI("getHaveMealsReminder")
            ControlBleTools.getInstance().getHaveMealsReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getHaveMealsReminder state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutMeals.btnSet) {
            addLogI("layoutMeals.btnSet")
            val bean = CommonReminderBean()
            bean.isOn = binding.layoutMeals.layoutReminder.cbSwitch.isChecked
            val frequency = binding.layoutMeals.layoutReminder.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutMeals.layoutReminder.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutMeals.layoutReminder.tvEndTime)
            bean.frequency = frequency
            addLogBean("setHaveMealsWaterReminder", bean)
            ControlBleTools.getInstance().setHaveMealsWaterReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setHaveMealsWaterReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutWashHand.btnGet) {
            addLogI("layoutWashHand.btnGet")
            addLogI("getWashHandReminder")
            ControlBleTools.getInstance().getWashHandReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getWashHandReminder state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutWashHand.btnSet) {
            addLogI("layoutWashHand.btnSet")
            val bean = CommonReminderBean()
            bean.isOn = binding.layoutWashHand.layoutReminder.cbSwitch.isChecked
            val frequency = binding.layoutWashHand.layoutReminder.etInterval.text.toString().trim().toInt()
            bean.startTime = DialogUtils.getSettingTimeBean(binding.layoutWashHand.layoutReminder.tvStartTime)
            bean.endTime = DialogUtils.getSettingTimeBean(binding.layoutWashHand.layoutReminder.tvEndTime)
            bean.frequency = frequency
            addLogBean("setWashHandReminder", bean)
            ControlBleTools.getInstance().setWashHandReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setWashHandReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutPhysiologicalCycle.btnGet) {
            addLogI("layoutPhysiologicalCycle.btnGet")
            addLogI("getPhysiologicalCycle")
            ControlBleTools.getInstance().getPhysiologicalCycle(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getPhysiologicalCycle state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutPhysiologicalCycle.btnSet) {
            addLogI("layoutPhysiologicalCycle.btnSet")
            val bean = PhysiologicalCycleBean()
            val tip = binding.layoutPhysiologicalCycle.etTip.getText().toString().trim().toInt()
            val allDay = binding.layoutPhysiologicalCycle.etAllDay.getText().toString().trim().toInt()
            val day = binding.layoutPhysiologicalCycle.etDay.getText().toString().trim().toInt()
            bean.remindSwitch = binding.layoutPhysiologicalCycle.cbSwitch.isChecked
            bean.advanceDay = tip
            bean.totalCycleDay = allDay
            bean.physiologicalCycleDay = day
            bean.physiologicalStartDate = DialogUtils.getDateBean(binding.layoutPhysiologicalCycle.tvStartDate)
            bean.physiologicalCycleSwitch = binding.layoutPhysiologicalCycle.cbPeriodSwitch.isChecked
            addLogBean("setPhysiologicalCycle", bean)
            ControlBleTools.getInstance().setPhysiologicalCycle(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setPhysiologicalCycle state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutNoiceSet.btnGet) {
            addLogI("layoutNoiceSet.btnGet")
            addLogI("getNotificationSettings")
            ControlBleTools.getInstance().getNotificationSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getNotificationSettings state=$state")
                }
            })
        }
        clickCheckConnect(binding.layoutNoiceSet.btnSet) {
            addLogI("layoutNoiceSet.btnSet")
            val bean = NotificationSettingsBean()
            bean.noticeNotLightUp = binding.layoutNoiceSet.cbIsScreen.isChecked
            bean.delayReminderSwitch = binding.layoutNoiceSet.cbIsDelayReminder.isChecked
            val vMode = binding.layoutNoiceSet.etVibration.text.toString().trim().toInt()
            val rMode = binding.layoutNoiceSet.etRing.text.toString().trim().toInt()
            bean.phoneRemindVibrationMode = vMode
            bean.phoneRemindRingMode = rMode
            addLogBean("setNotificationSettings", bean)
            ControlBleTools.getInstance().setNotificationSettings(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setNotificationSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBtCallSwitch.btnGet) {
            addLogI("layoutBtCallSwitch.btnGet")
            addLogI("getClassicBluetoothState")
            ControlBleTools.getInstance().getClassicBluetoothState(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getClassicBluetoothState state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutBtCallSwitch.btnSet) {
            addLogI("layoutBtCallSwitch.btnSet")
            val switch = binding.layoutBtCallSwitch.cbSwitch.isChecked
            val disconnectRemind = binding.layoutBtCallSwitch.cbDisconnectRemind.isChecked
            val bean = ClassicBluetoothStateBean(switch, disconnectRemind)
            addLogBean("setClassicBluetoothState", bean)
            ControlBleTools.getInstance().setClassicBluetoothState(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setClassicBluetoothState state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutLargeContactsList.btnGet) {
            addLogI("layoutLargeContactsList.btnGet")
            addLogI("getContactLotList")
            ControlBleTools.getInstance().getContactLotList(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getContactLotList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutLargeContactsList.btnSet) {
            addLogI("layoutLargeContactsList.btnSet")
            val list: MutableList<ContactBean?> = ArrayList()
            val name = binding.layoutLargeContactsList.etName.text.toString().trim()
            val phone = binding.layoutLargeContactsList.etPhone.text.toString().trim()
            for (i in 0..49) {
                val contactBean = ContactBean()
                contactBean.contacts_name = i.toString() + name
                contactBean.contacts_number = phone + i
                list.add(contactBean)
            }
            val bean = ContactLotBean()
            bean.allCount = list.size
            bean.data = list
            addLogBean("setContactLotList", bean)
            ControlBleTools.getInstance().setContactLotList(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setContactLotList state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSleepReminder.btnGet) {
            addLogI("layoutSleepReminder.btnGet")
            addLogI("getSleepReminder")
            ControlBleTools.getInstance().getSleepReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSleepReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutSleepReminder.btnSet) {
            addLogI("layoutSleepReminder.btnSet")
            val bean = SleepReminder()
            bean.isOn = binding.layoutSleepReminder.cbSwitch.isChecked
            bean.reminderTime = DialogUtils.getSettingTimeBean(binding.layoutSleepReminder.tvStartTime)
            addLogBean("setSleepReminder", bean)
            ControlBleTools.getInstance().setSleepReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSleepReminder state=$state")
                }
            })
        }
    }

    private fun initCallback() {
        MySettingMenuCallBack.onSedentaryReminderResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSedentaryReminderResult", bean!!)
        })

        MySettingMenuCallBack.onDrinkWaterReminderResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onDrinkWaterReminderResult", bean!!)
        })

        MySettingMenuCallBack.onMedicationReminderResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onMedicationReminderResult", bean!!)
        })

        MySettingMenuCallBack.onHaveMealsReminderResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onHaveMealsReminderResult", bean!!)
        })

        MySettingMenuCallBack.onWashHandReminderResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onWashHandReminderResult", bean!!)
        })


        MySettingMenuCallBack.onEventInfoResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onEventInfoResult", bean!!)
        })

        MySettingMenuCallBack.onClockInfoResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onClockInfoResult", bean!!)
        })


        MySettingMenuCallBack.onClassicBleStateSetting.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onClassicBleStateSetting", bean!!)
        })

        MySettingMenuCallBack.onSleepReminder.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSleepReminder", bean!!)
        })

        CallBackUtils.callReminderModeCallback = CallReminderModeCallback { mode, volume ->
            addLogI("callReminderModeCallback onResult mode=$mode volume=$volume")
        }

        CallBackUtils.quickReplyCallBack = object : QuickReplyCallBack {
            override fun onQuickReplyResult(list: ArrayList<String>) {
                addLogBean("quickReplyCallBack onQuickReplyResult", list)
            }

            override fun onMessage(phoneNumber: String?, text: String?) {
                addLogI("quickReplyCallBack onMessage phoneNumber=$phoneNumber text$text")
            }
        }

        CallBackUtils.physiologicalCycleCallBack = PhysiologicalCycleCallBack { bean ->
            addLogBean("physiologicalCycleCallBack", bean)
        }

        CallBackUtils.contactLotCallBack = ContactLotCallBack { bean ->
            addLogBean("contactLotCallBack", bean)
        }

        CallBackUtils.emergencyContactsCallBack = EmergencyContactsCallBack { bean ->
            addLogBean("emergencyContactsCallBack", bean)
        }


        CallBackUtils.collectContactCallBack = ContactCallBack { bean ->
            addLogBean("collectContactCallBack", bean)
        }

        CallBackUtils.contactCallBack = ContactCallBack { bean ->
            addLogBean("contactCallBack", bean)
        }

        CallBackUtils.callStateCallBack = CallStateCallBack { state ->
            addLogI("callStateCallBack state=$state")
        }

    }

    private fun sendSysNotice(type: Int) {
        val phone = binding.layoutNoticeSystem.etPhone.text.toString().trim()
        val content = binding.layoutNoticeSystem.etContacts.text.toString().trim()
        val msg = binding.layoutNoticeSystem.etMsg.text.toString().trim()
        addLogI("sendSystemNotification type=$type phone=$phone content=$content msg=$msg")
        ControlBleTools.getInstance().sendSystemNotification(type, phone, content, msg, object : SendCmdStateListener() {
            override fun onState(state: SendCmdState) {
                addLogI("sendSystemNotification state=$state")
            }
        })
    }
}




