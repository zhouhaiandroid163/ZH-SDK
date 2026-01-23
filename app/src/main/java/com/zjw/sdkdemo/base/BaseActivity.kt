package com.zjw.sdkdemo.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.blankj.utilcode.util.ClickUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.ThreadUtils
import com.zhapp.ble.ControlBleTools
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.function.help.HelpActivity
import com.zjw.sdkdemo.utils.DialogUtils
import com.zjw.sdkdemo.utils.LogSendUtil
import com.zjw.sdkdemo.utils.MyFormatUtils
import com.zjw.sdkdemo.utils.SpUtils
import com.zjw.sdkdemo.utils.ToastUtils
import org.json.JSONArray
import org.json.JSONObject

@SuppressLint("SetTextI18n")
open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initBack()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    fun initBack() {
        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            R.id.action_settings -> {
                startActivity(Intent(this, HelpActivity::class.java).apply { putExtra(HelpActivity.FUN_TAG, title) })
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun clickCheckConnect(view: View, block: () -> Unit) {
        ClickUtils.applySingleDebouncing(view) {
            if (!ControlBleTools.getInstance().isConnect) {
                ToastUtils.showToast(R.string.device_no_connect)
            }
            try {
                block()
            } catch (e: Exception) {
                e.printStackTrace()
                addLogI(getString(R.string.get_data_err1) + " e=${e.toString()}")
                ToastUtils.showToast(getString(R.string.get_data_err1) + "\n" + e.toString())
            }
        }
    }

    fun executeCheckConnect(block: () -> Unit) {
        if (!ControlBleTools.getInstance().isConnect) {
            ToastUtils.showToast(R.string.device_no_connect)
        }
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            addLogI(getString(R.string.get_data_err1) + " e=${e.toString()}")
            ToastUtils.showToast(getString(R.string.get_data_err1) + "\n" + e.toString())
        }
    }

    fun setMyCheckBox(checkBox: CheckBox, linearLayoutCompat: LinearLayoutCompat) {
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkBox.setTextColor(ContextCompat.getColor(this, R.color.color_2A2A2C))
                checkBox.setBackgroundColor(ContextCompat.getColor(this, R.color.color_F0F0F0))
                linearLayoutCompat.visibility = View.VISIBLE
            } else {
                checkBox.setTextColor(ContextCompat.getColor(this, R.color.color_888888))
                checkBox.setBackgroundColor(ContextCompat.getColor(this, R.color.color_D3D3D3))
                linearLayoutCompat.visibility = View.GONE
            }
        }
    }

    fun setMyCheckBox(checkBox: CheckBox, linearLayoutCompat: LinearLayoutCompat, ivHelp: AppCompatImageView) {
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                checkBox.setTextColor(ContextCompat.getColor(this, R.color.color_2A2A2C))
                checkBox.setBackgroundColor(ContextCompat.getColor(this, R.color.color_F0F0F0))
                linearLayoutCompat.visibility = View.VISIBLE
            } else {
                checkBox.setTextColor(ContextCompat.getColor(this, R.color.color_888888))
                checkBox.setBackgroundColor(ContextCompat.getColor(this, R.color.color_D3D3D3))
                linearLayoutCompat.visibility = View.GONE
            }
        }
        ivHelp.setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java).apply { putExtra(HelpActivity.FUN_TAG, checkBox.text) })
        }
    }

    fun formatObject(mObject: Any): String? {
        return MyFormatUtils.format(mObject)
    }

    lateinit var logTag: String
    lateinit var logTextView: LinearLayoutCompat
    var logArr = JSONArray()

    fun initLogSet(tag: String, logView: LinearLayoutCompat, logCheckBox: CheckBox, textView: LinearLayoutCompat, clearButton: AppCompatButton, setButton: AppCompatButton, sendButton: AppCompatButton) {
        this.logTag = tag
        this.logTextView = textView
        logArr = JSONArray()
        //设置logView高度为屏幕高度的1/3
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        val layoutParams = logView.layoutParams
        layoutParams.height = screenHeight / 3
        logView.layoutParams = layoutParams

        setMyCheckBox(logCheckBox, logView)

        clearButton.setOnClickListener {
            textView.removeAllViews()
            logArr = JSONArray()
        }

        setButton.setOnClickListener {
            DialogUtils.showSystemInputDialog(this, getString(R.string.log_dialog_title), { inputText ->
                if (inputText.isEmpty()) {
                    ToastUtils.showToast(getString(R.string.log_id_null))
                    return@showSystemInputDialog
                }
                SpUtils.setLogUserID(inputText)
            }, {
            })
        }

        sendButton.setOnClickListener {
            if(SpUtils.getLogUserID().isEmpty()){
                ToastUtils.showToast(getString(R.string.log_id_null))
                return@setOnClickListener
            }
            LogSendUtil.sendLogDataFile( logTag, logArr)
        }
    }

    fun addLogI(str: String) {
        ThreadUtils.runOnUiThread {
            Log.i(logTag, str)
            logTextView.addView(AppCompatTextView(this).apply {
                text = str
                textSize = 11.0f
            },0)
            logArr.put(JSONObject().put("time", MyFormatUtils.getTime()).put("content", str))
        }
    }

    fun addLogBean(str: String, data: Any) {
        ThreadUtils.runOnUiThread {
            val dataStr1 = "$str data=${formatObject(data)}"
            Log.i(logTag, dataStr1)
            logTextView.addView(AppCompatTextView(this).apply {
                text = dataStr1
                textSize = 11.0f
            },0)
            val contentStr = "$str data=${GsonUtils.toJson(data)}"
            logArr.put(JSONObject().put("time", MyFormatUtils.getTime()).put("content", contentStr))
        }
    }

    fun addLogE(str: String) {
        ThreadUtils.runOnUiThread {
            Log.e(logTag, str)
            logTextView.addView(AppCompatTextView(this).apply {
                text = str
                textSize = 11.0f
            },0)
            logArr.put(JSONObject().put("time", MyFormatUtils.getTime()).put("content", str))
        }
    }

    fun selectTime(textView: AppCompatTextView) {
        textView.setOnClickListener {
            DialogUtils.showTimeDialog(this, textView)
        }
    }

    fun selectDate(textView: AppCompatTextView) {
        textView.setOnClickListener {
            DialogUtils.showDateDialog(this, textView)
        }
    }

    fun selectSettingTime(textView: AppCompatTextView) {
        textView.setOnClickListener {
            DialogUtils.showSettingTimeDialog(this, textView)
        }
    }

}