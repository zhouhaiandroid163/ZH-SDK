package com.zjw.sdkdemo.livedata

import com.zhapp.ble.bean.BodyTemperatureSettingBean
import com.zhapp.ble.bean.BreathingLightSettingsBean
import com.zhapp.ble.bean.ClassicBluetoothStateBean
import com.zhapp.ble.bean.ClockInfoBean
import com.zhapp.ble.bean.CommonReminderBean
import com.zhapp.ble.bean.ContinuousBloodOxygenSettingsBean
import com.zhapp.ble.bean.DoNotDisturbModeBean
import com.zhapp.ble.bean.EvDataInfoBean
import com.zhapp.ble.bean.EventInfoBean
import com.zhapp.ble.bean.FindWearSettingsBean
import com.zhapp.ble.bean.HeartRateMonitorBean
import com.zhapp.ble.bean.NotificationSettingsBean
import com.zhapp.ble.bean.PressureModeBean
import com.zhapp.ble.bean.SWBRMonitorBean
import com.zhapp.ble.bean.SWHRMonitorBean
import com.zhapp.ble.bean.SWHRVMonitorBean
import com.zhapp.ble.bean.SWSPO2MonitorBean
import com.zhapp.ble.bean.SchedulerBean
import com.zhapp.ble.bean.SchoolBean
import com.zhapp.ble.bean.ScreenDisplayBean
import com.zhapp.ble.bean.ScreenSettingBean
import com.zhapp.ble.bean.SimpleSettingSummaryBean
import com.zhapp.ble.bean.SleepModeBean
import com.zhapp.ble.bean.SleepReminder
import com.zhapp.ble.bean.WorldClockBean
import com.zhapp.ble.bean.WristScreenBean
import com.zhapp.ble.callback.CallBackUtils
import com.zhapp.ble.callback.SettingMenuCallBack

object MySettingMenuCallBack {

    // 震动强度
    val onVibrationResult = UnFlawedLiveData<Int>()

    // 震动时长
    val onVibrationDurationResult = UnFlawedLiveData<Int>()

    // 省电设置
    val onPowerSavingResult = UnFlawedLiveData<Boolean>()

    // 覆盖息屏
    val onOverlayScreenResult = UnFlawedLiveData<Boolean>()

    // 快速眼动
    val onRapidEyeMovementResult = UnFlawedLiveData<Boolean>()

    // 抬腕亮屏
    val onWristScreenResult = UnFlawedLiveData<WristScreenBean>()

    // 勿扰模式
    val onDoNotDisturbModeResult = UnFlawedLiveData<DoNotDisturbModeBean>()

    // 心率检测
    val onHeartRateMonitorResult = UnFlawedLiveData<HeartRateMonitorBean>()

    // 息屏设置
    val onScreenDisplayResult = UnFlawedLiveData<ScreenDisplayBean>()

    // 屏幕设置
    val onScreenSettingResult = UnFlawedLiveData<ScreenSettingBean>()

    // 学校模式
    val onSchoolModeResult = UnFlawedLiveData<SchoolBean>()

    // 通话蓝牙状态
    val onClassicBleStateSetting = UnFlawedLiveData<ClassicBluetoothStateBean>()

    // 调度器
    val onSchedulerResult = UnFlawedLiveData<SchedulerBean>()

    // 睡眠模式
    val onSleepModeResult = UnFlawedLiveData<SleepModeBean>()

    // 压力模式设置
    val onPressureModeResult = UnFlawedLiveData<PressureModeBean>()

    // 通知设置
    val onNotificationSetting = UnFlawedLiveData<NotificationSettingsBean>()

    // 连续血氧设置
    val onContinuousBloodOxygenSetting = UnFlawedLiveData<ContinuousBloodOxygenSettingsBean>()

    // 找手环设置
    val onFindWearSettings = UnFlawedLiveData<FindWearSettingsBean>()

    // 灯光设置
    val onBreathingLightSettings = UnFlawedLiveData<BreathingLightSettingsBean>()

    // 电动车
    val onEvDataInfo = UnFlawedLiveData<EvDataInfoBean>()

    // 提醒
    val onEvRemindType = UnFlawedLiveData<Int>()

    // 自定义左键
    val onCustomizeLeftClickSettings = UnFlawedLiveData<Int>()

    // 久坐提醒
    val onSedentaryReminderResult = UnFlawedLiveData<CommonReminderBean>()

    // 喝水提醒
    val onDrinkWaterReminderResult = UnFlawedLiveData<CommonReminderBean>()

    // 吃药提醒
    val onMedicationReminderResult = UnFlawedLiveData<CommonReminderBean>()

    // 吃饭提醒
    val onHaveMealsReminderResult = UnFlawedLiveData<CommonReminderBean>()

    // 洗手提醒
    val onWashHandReminderResult = UnFlawedLiveData<CommonReminderBean>()

    // 睡眠提醒
    val onSleepReminder = UnFlawedLiveData<SleepReminder>()

    // 事件提醒
    val onEventInfoResult = UnFlawedLiveData<Pair<MutableList<EventInfoBean>, Int>>()

    // 闹钟提醒
    val onClockInfoResult = UnFlawedLiveData<Pair<MutableList<ClockInfoBean>, Int>>()

    // 世界时钟列表
    val onWorldClockResult = UnFlawedLiveData<MutableList<WorldClockBean>>()

    // 连续体温
    val onBodyTemperatureSettingResult = UnFlawedLiveData<BodyTemperatureSettingBean>()

    // 简单设置汇总
    val onSimpleSettingResult = UnFlawedLiveData<SimpleSettingSummaryBean>()

    // 运动自动识别
    val onMotionRecognitionResult = UnFlawedLiveData<Pair<Boolean, Boolean>>()

    // 无屏手环血氧检测提醒设置
    val onSWSPO2Monitor = UnFlawedLiveData<SWSPO2MonitorBean>()

    // 无屏手环心率变异性检测提醒设置
    val onSWHRVMonitor = UnFlawedLiveData<SWHRVMonitorBean>()

    // 无屏手环血氧检测提醒设置
    val onSWBRMonitor = UnFlawedLiveData<SWBRMonitorBean>()

    // 无屏手环心率检测提醒设置
    val onSWHRMonitor = UnFlawedLiveData<SWHRMonitorBean>()


    fun initMySettingMenuCallBack() {
        // 设备设置相关
        CallBackUtils.settingMenuCallBack = object : SettingMenuCallBack {
            override fun onVibrationResult(model: Int) {
                onVibrationResult.postValue(model)
            }

            override fun onVibrationDurationResult(duration: Int) {
                onVibrationDurationResult.postValue(duration)
            }

            override fun onPowerSavingResult(isOpen: Boolean) {
                onPowerSavingResult.postValue(isOpen)
            }

            override fun onOverlayScreenResult(isOpen: Boolean) {
                onOverlayScreenResult.postValue(isOpen)
            }

            override fun onRapidEyeMovementResult(isOpen: Boolean) {
                onRapidEyeMovementResult.postValue(isOpen)
            }

            override fun onWristScreenResult(bean: WristScreenBean) {
                onWristScreenResult.postValue(bean)
            }

            override fun onDoNotDisturbModeResult(bean: DoNotDisturbModeBean) {
                onDoNotDisturbModeResult.postValue(bean)
            }

            override fun onHeartRateMonitorResult(bean: HeartRateMonitorBean) {
                onHeartRateMonitorResult.postValue(bean)
            }

            override fun onScreenDisplayResult(bean: ScreenDisplayBean) {
                onScreenDisplayResult.postValue(bean)
            }

            override fun onScreenSettingResult(bean: ScreenSettingBean) {
                onScreenSettingResult.postValue(bean)
            }

            override fun onSedentaryReminderResult(bean: CommonReminderBean) {
                onSedentaryReminderResult.postValue(bean)
            }

            override fun onDrinkWaterReminderResult(bean: CommonReminderBean) {
                onDrinkWaterReminderResult.postValue(bean)
            }

            override fun onMedicationReminderResult(bean: CommonReminderBean) {
                onMedicationReminderResult.postValue(bean)
            }

            override fun onHaveMealsReminderResult(bean: CommonReminderBean) {
                onHaveMealsReminderResult.postValue(bean)
            }

            override fun onWashHandReminderResult(bean: CommonReminderBean) {
                onWashHandReminderResult.postValue(bean)
            }

            override fun onSleepReminder(bean: SleepReminder) {
                onSleepReminder.postValue(bean)
            }

            override fun onEventInfoResult(list: MutableList<EventInfoBean>, max: Int) {
                onEventInfoResult.postValue(Pair(list, max))
            }

            override fun onClockInfoResult(list: MutableList<ClockInfoBean>, max: Int) {
                onClockInfoResult.postValue(Pair(list, max))
            }

            override fun onSimpleSettingResult(bean: SimpleSettingSummaryBean) {
                onSimpleSettingResult.postValue(bean)
            }

            override fun onMotionRecognitionResult(isAutoRecognition: Boolean, isAutoPause: Boolean) {
                onMotionRecognitionResult.postValue(Pair(isAutoRecognition, isAutoPause))
            }

            override fun onWorldClockResult(list: MutableList<WorldClockBean>) {
                onWorldClockResult.postValue(list)
            }

            override fun onBodyTemperatureSettingResult(bean: BodyTemperatureSettingBean) {
                onBodyTemperatureSettingResult.postValue(bean)
            }

            override fun onClassicBleStateSetting(bean: ClassicBluetoothStateBean) {
                onClassicBleStateSetting.postValue(bean)
            }

            override fun onSchoolModeResult(bean: SchoolBean) {
                onSchoolModeResult.postValue(bean)
            }

            override fun onSchedulerResult(bean: SchedulerBean) {
                onSchedulerResult.postValue(bean)
            }

            override fun onSleepModeResult(bean: SleepModeBean) {
                onSleepModeResult.postValue(bean)
            }

            override fun onPressureModeResult(bean: PressureModeBean) {
                onPressureModeResult.postValue(bean)
            }

            override fun onNotificationSetting(bean: NotificationSettingsBean) {
                onNotificationSetting.postValue(bean)
            }

            override fun onContinuousBloodOxygenSetting(bean: ContinuousBloodOxygenSettingsBean) {
                onContinuousBloodOxygenSetting.postValue(bean)
            }

            override fun onFindWearSettings(bean: FindWearSettingsBean) {
                onFindWearSettings.postValue(bean)
            }

            override fun onBreathingLightSettings(bean: BreathingLightSettingsBean) {
                onBreathingLightSettings.postValue(bean)
            }

            override fun onEvDataInfo(bean: EvDataInfoBean) {
                onEvDataInfo.postValue(bean)
            }

            override fun onEvRemindType(remindType: Int) {
                onEvRemindType.postValue(remindType)
            }

            override fun onCustomizeLeftClickSettings(setType: Int) {
                onCustomizeLeftClickSettings.postValue(setType)
            }

            override fun onSWSPO2Monitor(bean: SWSPO2MonitorBean) {
                onSWSPO2Monitor.postValue(bean)
            }

            override fun onSWHRVMonitor(bean: SWHRVMonitorBean) {
                onSWHRVMonitor.postValue(bean)
            }

            override fun onSWBRMonitor(bean: SWBRMonitorBean) {
                onSWBRMonitor.postValue(bean)
            }

            override fun onSWHRMonitor(bean: SWHRMonitorBean) {
                onSWHRMonitor.postValue(bean)

            }

        }
    }
}