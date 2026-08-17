package com.zjw.sdkdemo.base

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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

    private lateinit var tvToolbarTitle: AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Android 15/16 强制边到边显示。改用自定义标题栏（fitsSystemWindows=true 自动避开状态栏）
        // 替代系统 ActionBar，彻底规避 AppCompat 不再自动处理 ActionBar 偏移导致内容被遮挡的问题。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            // 状态栏透明 + 深色图标（自定义标题栏下方的浅色背景）
            enableEdgeToEdge(
//                statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
//                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            )
        }
        // 隐藏系统 ActionBar，改用自定义标题栏
        initBack()
    }

    /**
     * 隐藏系统 ActionBar（改为自定义标题栏）。
     */
    fun initBack() {
        supportActionBar?.hide()
    }

    override fun setContentView(layoutResID: Int) {
        val content = LayoutInflater.from(this).inflate(layoutResID, null, false)
        super.setContentView(wrapWithToolbar(content))
    }

    override fun setContentView(view: View?) {
        super.setContentView(wrapWithToolbar(view))
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(wrapWithToolbar(view), params)
    }

    /**
     * 用"自定义标题栏 + 内容"包裹原始内容，解决内容被系统栏/ActionBar 遮挡的问题。
     * 标题栏手动加 statusBars.top padding，精确避开状态栏。
     */
    private fun wrapWithToolbar(content: View?): View {
        val container = LinearLayoutCompat(this)
        container.orientation = LinearLayoutCompat.VERTICAL
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        val toolbar = LayoutInflater.from(this)
            .inflate(R.layout.layout_custom_toolbar, container, false)
        tvToolbarTitle = toolbar.findViewById(R.id.tvToolbarTitle)
        tvToolbarTitle.text = title

        // 返回按钮：通过 onBackPressedDispatcher 触发返回逻辑（兼容子类注册的 OnBackPressedCallback）
        toolbar.findViewById<View>(R.id.ivToolbarBack).setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        // 帮助按钮
        toolbar.findViewById<View>(R.id.ivToolbarHelp).setOnClickListener {
            startActivity(Intent(this, HelpActivity::class.java).apply { putExtra(HelpActivity.FUN_TAG, title) })
        }
        // HelpActivity 自身是帮助页，隐藏帮助按钮避免重复打开
        if (this is HelpActivity) {
            toolbar.findViewById<View>(R.id.ivToolbarHelp).visibility = View.INVISIBLE
        }

        container.addView(toolbar)

        // 手动给标题栏加 statusBars.top padding（精确避开状态栏，不处理导航栏避免底部空白）
        ViewCompat.setOnApplyWindowInsetsListener(toolbar) { v, insets ->
            val statusTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            if (v.paddingTop != statusTop) {
                v.setPadding(v.paddingLeft, statusTop, v.paddingRight, v.paddingBottom)
            }
            insets
        }
        ViewCompat.requestApplyInsets(toolbar)

        if (content != null) {
            container.addView(
                content,
                LinearLayoutCompat.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
            )
        }
        return container
    }

    override fun onTitleChanged(title: CharSequence?, color: Int) {
        super.onTitleChanged(title, color)
        if (::tvToolbarTitle.isInitialized) {
            tvToolbarTitle.text = title
        }
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