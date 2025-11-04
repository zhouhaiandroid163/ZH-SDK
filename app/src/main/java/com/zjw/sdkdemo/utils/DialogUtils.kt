package com.zjw.sdkdemo.utils

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.ImageUtils
import com.blankj.utilcode.util.ScreenUtils
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.flag.BubbleFlag
import com.skydoves.colorpickerview.flag.FlagMode
import com.skydoves.colorpickerview.listeners.ColorPickerViewListener
import com.skydoves.colorpickerview.preference.ColorPickerPreferenceManager
import com.zhapp.ble.bean.PhysiologicalCycleBean
import com.zhapp.ble.bean.SettingTimeBean
import com.zhapp.ble.bean.TimeBean
import com.zhapp.ble.custom.DiyDialUtils
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.ui.customdialog.CustomDialog
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DialogUtils {

    @JvmStatic
    fun showTwoButtonDialog(
        context: Context,
        title: String,
        message: String,
        positiveText: String,
        negativeText: String,
        onPositiveClick: (DialogInterface) -> Unit,
        onNegativeClick: (DialogInterface) -> Unit = { dialog -> dialog.dismiss() },
    ) {
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveText) { dialog, _ ->
                onPositiveClick(dialog)
                dialog.dismiss()
            }
            .setNegativeButton(negativeText) { dialog, _ ->
                onNegativeClick(dialog)
                dialog.dismiss()
            }
            .create()
            .show()
    }

    @JvmStatic
    fun showAppDialog(context: Context, appName: AppCompatEditText, appPackName: AppCompatEditText) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_app)
        val appLayout: LinearLayout? = dialog.findViewById(R.id.appLayout)
        dialog.setCancelable(false)
        val params = dialog.window!!.attributes
        dialog.window!!.attributes = params
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        for (r in resolveInfos) {
            if (r.activityInfo != null && r.activityInfo.packageName != null) {
                val view = Button(context)
                view.text = r.loadLabel(pm).toString()
                view.setOnClickListener {
                    appName.setText(r.activityInfo.packageName)
                    appPackName.setText(r.loadLabel(pm).toString())
                    dialog.dismiss()
                }
                appLayout?.addView(view)
            }
        }
        if (resolveInfos.isNotEmpty()) {
            dialog.show()
        } else {
            ToastUtils.showToast(R.string.get_app_fail)
        }
    }


    @JvmStatic
    fun showSelectFileDialog(context: Context, filePath: String, fileSuffix: String, onFileSelected: (File) -> Unit) {
        val files = FileUtils.listFilesInDir(filePath)
        if (files.isNullOrEmpty()) {
            ToastUtils.showToast("$filePath is null")
            return
        }
        val width = ScreenUtils.getScreenWidth()
        val height = ((ScreenUtils.getScreenHeight()) * 0.8f).toInt()
        val dialog = CustomDialog.builder(context).setContentView(R.layout.dialog_select_file_list).setWidth(width).setHeight(height).build()
        for (file in files) {
            if (file.name.endsWith(fileSuffix)) {
                val view = Button(context)
                view.isAllCaps = false
                view.text = file.name
                view.setOnClickListener {
                    onFileSelected(file)
                    dialog.dismiss()
                }
                dialog.findViewById<LinearLayout>(R.id.listLayout)?.addView(view)
            }
        }
        dialog.show()
    }

    @JvmStatic
    fun showSelectImgDialog(context: Context, mFilePath: String, onFileSelected: (File) -> Unit) {
        val files = FileUtils.listFilesInDir(mFilePath)
        if (files.isNullOrEmpty()) {
            ToastUtils.showToast("$mFilePath is null")
            return
        }
        val width = ScreenUtils.getScreenWidth()
        val height = ((ScreenUtils.getScreenHeight()) * 0.8f).toInt()
        val dialog = CustomDialog.builder(context).setContentView(R.layout.dialog_select_file_list).setWidth(width).setHeight(height).build()
        for (file in files) {
            if (ImageUtils.isImage(file)) {
                val view = Button(context)
                view.isAllCaps = false
                view.text = file.name
                view.setOnClickListener {
                    onFileSelected(file)
                    dialog.dismiss()
                }
                dialog.findViewById<LinearLayout>(R.id.listLayout)?.addView(view)
            }
        }
        dialog.show()
    }

    @JvmStatic
    fun showSelectColorDialog(context: Context, colorPickerName: String, colorRGB: Int, colorListener: ColorPickerViewListener) {
        val colorPickerDialog = ColorPickerDialog.Builder(context)
            .setTitle("")
            .setPreferenceName(colorPickerName)
            .setNegativeButton(context.getString(R.string.btn_cancel)) { dialogInterface, i -> dialogInterface.dismiss() }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .setBottomSpace(12)
            .setPositiveButton(context.getString(R.string.btn_confirm), colorListener)

        colorPickerDialog.colorPickerView.apply {
            val bubbleFlag = BubbleFlag(context)
            bubbleFlag.flagMode = FlagMode.FADE
            setFlagView(bubbleFlag)
        }

        val colors = DiyDialUtils.getColorByRGBValue(colorRGB)
        ColorPickerPreferenceManager.getInstance(context).clearSavedAllData().setColor(colorPickerName, Color.argb(255, colors[0], colors[1], colors[2]))
        colorPickerDialog.show()
    }


    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

    @JvmStatic
    fun showTimeDialog(context: Context, textView: AppCompatTextView) {
        val calendar = Calendar.getInstance()
        if (textView.text.toString().isNotEmpty()) {
            calendar.time = timeFormat.parse(textView.text.toString()) ?: Date()
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                calendar.set(Calendar.MINUTE, selectedMinute)
                calendar.set(Calendar.SECOND, second)
                val selectedDateTime = timeFormat.format(calendar.time)
                textView.text = selectedDateTime
            }, hour, minute, true).show()
        }, year, month, day).show()
    }

    @JvmStatic
    fun getTimeBean(textView: AppCompatTextView): TimeBean {
        val calendar = Calendar.getInstance()
        calendar.time = timeFormat.parse(textView.text.toString()) ?: Date()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        return TimeBean(year, month, day, hour, minute, second)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    @JvmStatic
    fun showDateDialog(context: Context, textView: AppCompatTextView) {
        val calendar = Calendar.getInstance()
        if (textView.text.toString().isNotEmpty()) {
            calendar.time = dateFormat.parse(textView.text.toString()) ?: Date()
        }
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(context, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val selectedDateTime = dateFormat.format(calendar.time)
            textView.text = selectedDateTime
        }, year, month, day).show()
    }

    @JvmStatic
    fun getDateBean(textView: AppCompatTextView): PhysiologicalCycleBean.DateBean {
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(textView.text.toString()) ?: Date()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return PhysiologicalCycleBean.DateBean(year, month, day)
    }

    private val settingTimeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

    @JvmStatic
    fun showSettingTimeDialog(context: Context, textView: AppCompatTextView) {
        val calendar = Calendar.getInstance()
        if (textView.text.toString().isNotEmpty()) {
            calendar.time = settingTimeFormat.parse(textView.text.toString()) ?: Date()
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(context, { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            calendar.set(Calendar.SECOND, 0)
            val selectedDateTime = settingTimeFormat.format(calendar.time)
            textView.text = selectedDateTime
        }, hour, minute, true).show()
    }

    @JvmStatic
    fun getSettingTimeBean(textView: AppCompatTextView): SettingTimeBean {
        val calendar = Calendar.getInstance()
        calendar.time = settingTimeFormat.parse(textView.text.toString()) ?: Date()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return SettingTimeBean(hour, minute)
    }

    @JvmStatic
    fun showSystemInputDialog(context: Context, title: String, onConfirm: (String) -> Unit, onCancel: (() -> Unit)? = null) {
        val inputEditText = AppCompatEditText(context).apply {
            setPadding(100, 150, 100, 30)
            setText(SpUtils.getLogUserID())
        }
        AlertDialog.Builder(context).setTitle(title).setView(inputEditText).setCancelable(false)
            .setPositiveButton(context.getString(R.string.btn_confirm)) { dialog, _ ->
                val inputText = inputEditText.text.toString().trim()
                onConfirm(inputText)
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.btn_cancel)) { dialog, _ ->
                onCancel?.invoke()
                dialog.dismiss()
            }
            .create()
            .show()
    }
}