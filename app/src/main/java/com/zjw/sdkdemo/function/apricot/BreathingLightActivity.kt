package com.zjw.sdkdemo.function.apricot

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ScreenUtils
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.zhapp.ble.ControlBleTools
import com.zhapp.ble.bean.BreathingLightSettingsBean
import com.zhapp.ble.callback.SettingMenuCallBack
import com.zhapp.ble.custom.DiyDialUtils
import com.zhapp.ble.parsing.ParsingStateManager.SendCmdStateListener
import com.zhapp.ble.parsing.SendCmdState
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityBreathingLightBinding
import com.zjw.sdkdemo.livedata.MySettingMenuCallBack
import com.zjw.sdkdemo.ui.customdialog.CustomDialog
import com.zjw.sdkdemo.ui.view.CustomSwitchMaterial
import com.zjw.sdkdemo.utils.DialogUtils

class BreathingLightActivity : BaseActivity() {
    private val binding by lazy { ActivityBreathingLightBinding.inflate(layoutInflater) }
    private val tag: String = BreathingLightActivity::class.java.simpleName

    private var settingsBean: BreathingLightSettingsBean? = null
    private var data: MutableList<BreathingLightSettingsBean.LightItem> = mutableListOf()

    val colorPickerName = "LightColorPickerDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_breathing_light)
        initLogSet(tag, binding.layoutLog.llLog, binding.layoutLog.cxLog, binding.layoutLog.tvLog, binding.layoutLog.btnClear, binding.layoutLog.btnSet,binding.layoutLog.btnSendLog)
        initView()
        initListener()
        initCallBack()
    }

    private fun initView() {
        binding.rvData.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvData.adapter = LightItemAdapter(this, data)
        binding.swSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (settingsBean != null) {
                var isSetNew = false
                if (settingsBean!!.mainSwitch != buttonView.isChecked) {
                    isSetNew = true
                }
                settingsBean!!.mainSwitch = buttonView.isChecked
                if (isSetNew) {
                    setBreathingLightSettings()
                    refUiByData()
                }
            }
        }

        binding.swChangLiang.setOnCheckedChangeListener { buttonView, isChecked ->
            if (settingsBean != null) {
                var isSetNew = false
                if (settingsBean!!.lighttingSwitch != buttonView.isChecked) {
                    isSetNew = true
                }
                settingsBean!!.lighttingSwitch = buttonView.isChecked
                if (isSetNew) {
                    setBreathingLightSettings()
                    refUiByData()
                }
            }
        }
    }

    private fun initListener() {
        clickCheckConnect(binding.vColor) {
            addLogI("vColor")
            if (settingsBean != null) {
                DialogUtils.showSelectColorDialog(this, colorPickerName, settingsBean!!.lighttingColor, object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        if (envelope != null) {
                            settingsBean!!.lighttingColor = DiyDialUtils.getRGBValueByColor(envelope.argb[1], envelope.argb[2], envelope.argb[3])
                            setBreathingLightSettings()
                            refUiByData()
                        }
                    }
                })
            }
        }

        clickCheckConnect(binding.btnGet) {
            addLogI("btnGet")
            executeCheckConnect {
                addLogI("getBreathingLightSettings")
                ControlBleTools.getInstance().getBreathingLightSettings(object : SendCmdStateListener() {
                    override fun onState(state: SendCmdState) {
                        addLogI("getBreathingLightSettings state=$state")
                    }
                })
            }
        }

        clickCheckConnect(binding.btnSet) {
            addLogI("btnSet")
            showAddDialog()
        }
    }

    private fun initCallBack() {
        MySettingMenuCallBack.onBreathingLightSettings.observe(this, Observer { bean ->
            addLogBean("MySettingMenuCallBack.onBreathingLightSettings", bean!!)
            settingsBean = bean
            refUiByData()
        })
    }

    private fun setBreathingLightSettings() {
        addLogI("setBreathingLightSettings")
        executeCheckConnect {
            val bean = settingsBean!!
            addLogBean("setBreathingLightSettings",bean)
            ControlBleTools.getInstance().setBreathingLightSettings(bean, object : SendCmdStateListener() {
                override fun onState(state: SendCmdState) {
                    addLogI("setBreathingLightSettings state=$state")
                }
            })
        }
    }

    private fun refUiByData() {
        if (settingsBean != null) {
            binding.swSwitch.isChecked = settingsBean!!.mainSwitch
            binding.llData.visibility = if (settingsBean!!.mainSwitch) View.VISIBLE else View.GONE
            binding.swChangLiang.isChecked = settingsBean!!.lighttingSwitch
            val color = DiyDialUtils.getColorByRGBValue(settingsBean!!.lighttingColor)
            binding.vColor.setBackgroundColor(Color.rgb(color[0], color[1], color[2]))
            if (settingsBean!!.lightItems != null) {
                val oldSize = data.size
                data.clear()
                binding.rvData.adapter?.notifyItemRangeRemoved(0, oldSize)
                data.addAll(settingsBean!!.lightItems)
                binding.rvData.adapter?.notifyItemRangeInserted(0, data.size)
            }
        }
    }

    inner class LightItemAdapter(private val context: Context, private val data: MutableList<BreathingLightSettingsBean.LightItem>) :
        RecyclerView.Adapter<LightItemAdapter.LightItemViewHolder>() {

        inner class LightItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var tvTitle: AppCompatTextView = view.findViewById(R.id.tvTitle)
            val colorView: View = view.findViewById(R.id.view)
            val mSwitch: CustomSwitchMaterial = view.findViewById(R.id.mSwitch)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightItemViewHolder {
            return LightItemViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_light, parent, false)
            )
        }

        override fun onBindViewHolder(holder: LightItemViewHolder, position: Int) {
            val light = data[position]
            holder.tvTitle.text = getNameByLightType(light.lightType)
            holder.mSwitch.isChecked = light.lightSwitch
            val color = DiyDialUtils.getColorByRGBValue(light.lightColor)
            holder.colorView.setBackgroundColor(Color.rgb(color[0], color[1], color[2]))

            holder.mSwitch.setOnClickListener {
                if (holder.mSwitch.isChecked != light.lightSwitch) {
                    light.lightSwitch = holder.mSwitch.isChecked
                    setBreathingLightSettings()
                    refUiByData()
                }
            }

            holder.colorView.setOnClickListener {
                DialogUtils.showSelectColorDialog(this@BreathingLightActivity, colorPickerName, light.lightColor, object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        if (envelope != null) {
                            light.lightColor = DiyDialUtils.getRGBValueByColor(envelope.argb[1], envelope.argb[2], envelope.argb[3])
                            setBreathingLightSettings()
                            refUiByData()
                        }
                    }
                })
            }

            holder.colorView.setOnLongClickListener {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION) {
                    data.removeAt(currentPosition)
                    settingsBean?.lightItems = data
                    setBreathingLightSettings()
                    notifyItemRemoved(currentPosition)
                }
                return@setOnLongClickListener true
            }
        }

        override fun getItemCount(): Int = data.size
    }

    private fun showAddDialog() {
        val parent = findViewById<ViewGroup>(android.R.id.content)
        val rootView = layoutInflater.inflate(R.layout.dialog_add_light, parent, false)
        val dialog = CustomDialog.builder(this)
            .setContentView(rootView)
            .setWidth(ScreenUtils.getScreenWidth())
            .setHeight((ScreenUtils.getScreenHeight() * 0.8f).toInt())
            .setGravity(Gravity.CENTER)
            .build()

        val btnOk: AppCompatButton = rootView.findViewById(R.id.btnOk)
        val rvAdd: RecyclerView = rootView.findViewById(R.id.rvAdd)

        rvAdd.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val addData = mutableListOf<LightAddItem>()
        SettingMenuCallBack.BreathingLightType.entries.forEach { value ->
            addData.add(LightAddItem(value.type, data.lastOrNull { it.lightType == value.type } != null))
        }
        rvAdd.adapter = LightAddAdapter(this, addData)
        btnOk.setOnClickListener {
            if (settingsBean != null) {
                for (addItem in addData) {
                    val item = settingsBean!!.lightItems.lastOrNull { it.lightType == addItem.type }
                    if (item == null && addItem.isCheck) {
                        settingsBean!!.lightItems.add(BreathingLightSettingsBean.LightItem().apply {
                            lightType = addItem.type
                            lightSwitch = false
                            lightColor = DiyDialUtils.getRGBValueByColor(255, 255, 255)
                        })
                    }
                    if (item != null && !addItem.isCheck) {
                        settingsBean!!.lightItems.remove(item)
                    }
                }
            }
            setBreathingLightSettings()
            refUiByData()
            dialog.dismiss()
        }
        dialog.show()
    }

    data class LightAddItem(var type: Int, var isCheck: Boolean)

    inner class LightAddAdapter(private val context: Context, private val data: MutableList<LightAddItem>) : RecyclerView.Adapter<LightAddAdapter.LightAddViewHolder>() {

        inner class LightAddViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var tvTitle: AppCompatTextView = view.findViewById(R.id.tvTitle)
            val mSwitch: CheckBox = view.findViewById(R.id.mSwitch)

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LightAddViewHolder {
            return LightAddViewHolder(
                LayoutInflater.from(context).inflate(R.layout.list_item_add_light, parent, false)
            )
        }

        override fun onBindViewHolder(holder: LightAddViewHolder, position: Int) {
            holder.tvTitle.text = getNameByLightType(data[position].type)
            holder.mSwitch.isChecked = data[position].isCheck

            holder.mSwitch.setOnClickListener {
                data[position].isCheck = holder.mSwitch.isChecked
            }
        }

        override fun getItemCount(): Int = data.size
    }


    private fun getNameByLightType(lightType: Int): String {
        return when (lightType) {
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_CHARGE_INDEX.type -> "charge"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_LOW_POWER_INDEX.type -> "low battery"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_CALL_INDEX.type -> "incoming call"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_MISS_CALL_INDEX.type -> "MissCall"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_SMS_INDEX.type -> "SMS"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_QQ_INDEX.type -> "QQ"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_WECHAT_INDEX.type -> "WECHAT"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_SKYPE_INDEX.type -> "SKYPE"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_WHATSAPP_INDEX.type -> "WHATSAPP"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_FACEBOOK_INDEX.type -> "FACEBOOK"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_MESSENGER_INDEX.type -> "MESSENGER"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_LINKEDLN_INDEX.type -> "LINKEDLN"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_TWITTER_INDEX.type -> "TWITTER"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_VIBER_INDEX.type -> "VIBER"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_LINE_INDEX.type -> "LINE"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_GMAIL_INDEX.type -> "GMAIL"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_OUTLOOK_INDEX.type -> "OUTLOOK"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_INSTAGRAM_INDEX.type -> "INSTAGRAM"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_SNAPCHAT_INDEX.type -> "SNAPCHAT"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_MAIL_INDEX.type -> "MAIL"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_CALENDAR_INDEX.type -> "CALENDAR"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_ZALO_INDEX.type -> "ZALO"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_TELEGRAM_INDEX.type -> "TELEGRAM"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_KAKAOTALK_INDEX.type -> "KAKAOTALK"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_VK_INDEX.type -> "VK"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_OK_INDEX.type -> "OK"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_ICQ_INDEX.type -> "ICQ"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_YOUTUBE_INDEX.type -> "YOUTUBE"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_PINTEREST_INDEX.type -> "PINTEREST"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_HANGOUT_INDEX.type -> "HANGOUT"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_PHONRPE_INDEX.type -> "PHONRP"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_GOOGLE_PLAY_INDEX.type -> "GOOGLE PLAY"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_PAYTM_INDEX.type -> "PAYTM"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_NAUKRI_INDEX.type -> "NAUKRI"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_INSHOT_INDEX.type -> "INSHOT"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_GOOGLE_NEWS_INDEX.type -> "GOOGLE NEWS"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_OLA_INDEX.type -> "OLA"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_UBER_INDEX.type -> "UBER"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_FLIPKART_INDEX.type -> "FLIPKART"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_AMAZON_INDEX.type -> "AMAZON"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_WHATSAPP_BUSINESS_INDEX.type -> "WHATSAPP BUSINESS"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_NOISEFIT_INDEX.type -> "NOISEFIT"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_GOOGLE_CLASSROOM_INDEX.type -> "GOOGLE CLASSROOM"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_TIKTOK_INDEX.type -> "TIKTOK"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_TEXTNOW_INDEX.type -> "TEXTNOW"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_DISCORD_INDEX.type -> "DISCORD"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_AIRTEL_THANKS_INDEX.type -> "AIRTEL THANKS"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_CHARGE_INDEX.type -> "CHARGE"
            SettingMenuCallBack.BreathingLightType.BREATHING_LIGHT_OTHER_INDEX.type -> "other"
            else -> "unknown"
        }
    }
}