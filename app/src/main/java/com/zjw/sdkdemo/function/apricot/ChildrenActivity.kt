package com.zjw.sdkdemo.function.apricot

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.ExamSettingsBean
import com.zhapp.ble.bean.SchedulerBean.AlertBean
import com.zhapp.ble.bean.SchedulerBean.HabitBean
import com.zhapp.ble.bean.SchedulerBean.ReminderBean
import com.zhapp.ble.bean.SchoolBean
import com.zhapp.ble.bean.SettingTimeBean
import com.zhapp.ble.bean.TimeBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.ExamModeDetailCallback
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityChildrenBinding
import com.zjw.sdkdemo.livedata.MyChildrenCallBack
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.utils.DialogUtils

class ChildrenActivity : BaseActivity() {
    private val binding by lazy { ActivityChildrenBinding.inflate(layoutInflater) }
    private val tag = ChildrenActivity::class.java.simpleName

    private var testSchoolModeTimeValue = 0
    private var examValue = 0
    private var examSupportCount = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_set_child)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
    }

    private fun initView() {
        setMyCheckBox(binding.layoutChildScheduler.cbTop, binding.layoutChildScheduler.llBottom, binding.layoutChildScheduler.ivHelp)
        setMyCheckBox(binding.layoutChildSchoolMode.cbTop, binding.layoutChildSchoolMode.llBottom, binding.layoutChildSchoolMode.ivHelp)
        setMyCheckBox(binding.layoutChildExamMode.cbTop, binding.layoutChildExamMode.llBottom, binding.layoutChildExamMode.ivHelp)

        binding.layoutChildScheduler.layoutAlert.tvEndTime.visibility = View.GONE
        binding.layoutChildScheduler.layoutHid.tvEndTime.visibility = View.GONE

        selectSettingTime(binding.layoutChildScheduler.layoutAlert.tvStartTime)
        selectSettingTime(binding.layoutChildScheduler.layoutHid.tvStartTime)
        selectSettingTime(binding.layoutChildScheduler.layoutRid.tvStartTime)
        selectSettingTime(binding.layoutChildScheduler.layoutRid.tvEndTime)
    }

    private fun initListener() {
        clickCheckConnect(binding.layoutChildScheduler.btnGet) {
            addLogI("layoutChildScheduler.btnGet")
            addLogI("getScheduleReminder")
            ControlBleTools.getInstance().getScheduleReminder(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getScheduleReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutChildScheduler.btnSet) {
            addLogI("layoutChildScheduler.btnSet")
            if (MySettingMenuCallBack.onSchedulerResult.value == null) {
                addLogI(getString(R.string.get_data_tip1))
                return@clickCheckConnect
            }

            val bean = MySettingMenuCallBack.onSchedulerResult.value!!
            bean.alertList = arrayListOf<AlertBean>().apply {
                add(AlertBean().apply {
                    alertBerryId = binding.layoutChildScheduler.layoutAlert.etId.text.toString().trim().toInt()
                    alertName = binding.layoutChildScheduler.layoutAlert.etName.text.toString()
                    notifyText = binding.layoutChildScheduler.layoutAlert.etContent.text.toString()
                    alertTime = DialogUtils.getSettingTimeBean(binding.layoutChildScheduler.layoutAlert.tvStartTime)
                    isMonday = binding.layoutChildScheduler.layoutAlert.cbWeek1.isChecked
                    isTuesday = binding.layoutChildScheduler.layoutAlert.cbWeek2.isChecked
                    isWednesday = binding.layoutChildScheduler.layoutAlert.cbWeek3.isChecked
                    isThursday = binding.layoutChildScheduler.layoutAlert.cbWeek4.isChecked
                    isFriday = binding.layoutChildScheduler.layoutAlert.cbWeek5.isChecked
                    isSaturday = binding.layoutChildScheduler.layoutAlert.cbWeek6.isChecked
                    isSunday = binding.layoutChildScheduler.layoutAlert.cbWeek7.isChecked
                })
            }
            bean.habitBeanList = arrayListOf<HabitBean>().apply {
                add(HabitBean().apply {
                    habitBerryId = binding.layoutChildScheduler.layoutHid.etId.text.toString().trim().toInt()
                    habitName = binding.layoutChildScheduler.layoutHid.etName.text.toString()
                    habitType = binding.layoutChildScheduler.layoutHid.etContent.text.toString().trim().toInt()
                    habitTimeList = arrayListOf<SettingTimeBean>().apply {
                        add(DialogUtils.getSettingTimeBean(binding.layoutChildScheduler.layoutHid.tvStartTime))
                    }
                    isMonday = binding.layoutChildScheduler.layoutHid.cbWeek1.isChecked
                    isTuesday = binding.layoutChildScheduler.layoutHid.cbWeek2.isChecked
                    isWednesday = binding.layoutChildScheduler.layoutHid.cbWeek3.isChecked
                    isThursday = binding.layoutChildScheduler.layoutHid.cbWeek4.isChecked
                    isFriday = binding.layoutChildScheduler.layoutHid.cbWeek5.isChecked
                    isSaturday = binding.layoutChildScheduler.layoutHid.cbWeek6.isChecked
                    isSunday = binding.layoutChildScheduler.layoutHid.cbWeek7.isChecked
                })
            }
            bean.reminderBeanList = arrayListOf<ReminderBean>().apply {
                add(ReminderBean().apply {
                    reminderBerryId = binding.layoutChildScheduler.layoutRid.etId.text.toString().trim().toInt()
                    reminderName = binding.layoutChildScheduler.layoutRid.etName.text.toString()
                    reminderType = binding.layoutChildScheduler.layoutRid.etContent.text.toString().trim().toInt()
                    startTime = DialogUtils.getSettingTimeBean(binding.layoutChildScheduler.layoutRid.tvStartTime)
                    endTime = DialogUtils.getSettingTimeBean(binding.layoutChildScheduler.layoutRid.tvEndTime)
                    isMonday = binding.layoutChildScheduler.layoutHid.cbWeek1.isChecked
                    isTuesday = binding.layoutChildScheduler.layoutHid.cbWeek2.isChecked
                    isWednesday = binding.layoutChildScheduler.layoutHid.cbWeek3.isChecked
                    isThursday = binding.layoutChildScheduler.layoutHid.cbWeek4.isChecked
                    isFriday = binding.layoutChildScheduler.layoutHid.cbWeek5.isChecked
                    isSaturday = binding.layoutChildScheduler.layoutHid.cbWeek6.isChecked
                    isSunday = binding.layoutChildScheduler.layoutHid.cbWeek7.isChecked
                })
            }
            addLogBean("setScheduleReminder",bean)
            ControlBleTools.getInstance().setScheduleReminder(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setScheduleReminder state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutChildSchoolMode.btnGet) {
            addLogI("layoutChildSchoolMode.btnGet")
            addLogI("getSchoolMode")
            ControlBleTools.getInstance().getSchoolMode(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getSchoolMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutChildSchoolMode.btnSet) {
            addLogI("layoutChildSchoolMode.btnSet")
            testSchoolModeTimeValue++
            if (testSchoolModeTimeValue + 10 > 24) {
                testSchoolModeTimeValue = 0
            }
            val bean = SchoolBean(
                true,
                SettingTimeBean(testSchoolModeTimeValue, 0),
                SettingTimeBean(testSchoolModeTimeValue + 10, 30),
                true,
                true,
                true,
                true,
                true,
                true,
                true,
                testSchoolModeTimeValue,
                true,
                true
            )
            addLogBean("setSchoolMode",bean)
            ControlBleTools.getInstance().setSchoolMode(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setSchoolMode state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutChildExamMode.btnGet) {
            addLogI("layoutChildExamMode.btnGet")
            addLogI("getExamReminderSettings")
            ControlBleTools.getInstance().getExamReminderSettings(object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("getExamReminderSettings state=$state")
                }
            })
        }

        clickCheckConnect(binding.layoutChildExamMode.btnSet) {
            addLogI("layoutChildExamMode.btnSet")
            examValue++
            if (examValue + 10 > 24) {
                examValue = 0
            }
            if (examSupportCount == 0) {
                return@clickCheckConnect
            }
            val list: MutableList<ExamSettingsBean> = mutableListOf()
            repeat(examSupportCount) {
                list.add(ExamSettingsBean().apply {
                    name = "name $examValue"
                    duration = 120 + examValue
                    nudges = 60 + examValue
                    time = TimeBean(2025, 4, 1, examValue, examValue + 30, examValue + 40)
                    status = (examValue + 1) % 2 == 0
                })
            }
            addLogBean("setExamReminderSettings",list)
            ControlBleTools.getInstance().setExamReminderSettings(list, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setExamReminderSettings state=$state")
                }
            })
        }
    }

    private fun initCallBack() {
        MyChildrenCallBack.onSchedulerResult.observe(this, Observer { list ->
            addLogBean("MyChildrenCallBack.onSchedulerResult", list!!)
        })

        MySettingMenuCallBack.onSchedulerResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSchedulerResult", bean!!)
        })

        MySettingMenuCallBack.onSchoolModeResult.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onSchoolModeResult", bean!!)
        })

        CallBackUtils.examModeDetailCallback = ExamModeDetailCallback { bean, supportCount ->
            examSupportCount = supportCount
            addLogBean("examModeDetailCallback supportCount=$supportCount", bean)
        }
    }
}