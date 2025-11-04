package com.zjw.sdkdemo.function.help

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityHelpBinding

@SuppressLint("SetJavaScriptEnabled")
class HelpActivity : BaseActivity() {
    val binding by lazy { ActivityHelpBinding.inflate(layoutInflater) }

    companion object {
        const val FUN_TAG = "FUN_TAG"
    }

    private var defaultUrl: String = "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.sdk_help)
        initWeb()
        loadUrl()
        binding.btnToSTop.setOnClickListener {
            scrollToTop()
        }
    }

    private fun initWeb() {
        val webSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
//        binding.webView.webViewClient = object : WebViewClient() {
//            override fun onPageFinished(view: WebView?, url: String?) {
//                super.onPageFinished(view, url)
//                ToastUtils.showToast("页面加载完成")
//            }
//        }
    }

    private fun scrollToTop() {
        val jsCode = """
        (function() {
            window.scrollTo({top: 0, behavior: 'smooth'});
            
            const scrollableElements = document.querySelectorAll('iframe, [scrollable], [overflow-y]');
            scrollableElements.forEach(el => {
                if (el.scrollTo) {
                    el.scrollTo({top: 0, behavior: 'smooth'});
                }
            });
            
            setTimeout(() => {
                window.scrollTo(0, 0);
            }, 300);
        })();
    """.trimIndent()
        binding.webView.evaluateJavascript(jsCode, null)
    }

    // 处理返回键，使WebView可以后退
    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_settings)?.isVisible = false
        return super.onPrepareOptionsMenu(menu)
    }

    private fun loadUrl() {
        val funValue = intent.getStringExtra(FUN_TAG)
        val keyValuePairs = hashMapOf(
            //连接相关
            getString(R.string.connect_info) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-QgNzd611poD57HxLf61c9rFznv1",
            //功能集合
            getString(R.string.ch_bind_unbind) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-DwSfdKdp8o6YbHxFKnqcSW90nth",
            getString(R.string.ch_bind_unbind_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-BfjIduMGooKmHOxlxkGcIUuBnfd",
            getString(R.string.ch_data) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-F7nhd7FVMouFdlxJei9cy9kGnme",
            getString(R.string.ch_file) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-CUAvdv6EJo5TuJxF8pscmDhbn5d",
            getString(R.string.ch_file_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-V1hRdfNJ3ofpxExIvCmczpwnnAc",
            getString(R.string.ch_dial) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-SHbIdaoNUodCeNxzi1qczDpunuc",
            getString(R.string.ch_dial_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-OMNhdSl7zoN8rCxp3Q6cPvvOnUf",
            getString(R.string.ch_weather) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-K3Ucdn5ToobbxIx0lwucY4qenbe",
            getString(R.string.ch_weather_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JOGidvyKzoB5qWxIBFmcPUCBnAd",
            getString(R.string.ch_weather_id_test_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JOGidvyKzoB5qWxIBFmcPUCBnAd",
            getString(R.string.ch_sport_auxiliary) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-UMQcdNE9yohCjexahbQcGLninFb",
            getString(R.string.ch_sport_screen) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Z7Yqdjm2HoO3oWxnSfjcfToLnQd",
            getString(R.string.ch_measure) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-N6T0dJNYqomVe3xZChucZVF2n2b",
            getString(R.string.ch_ev) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Lm5OdOVd9oeI28xMDv4cwkHInae",
            getString(R.string.ch_emoji) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-AqtTdziIBoSBUKxKTjvcgwkTnig",
            getString(R.string.ch_esim) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-QG8Bdp7EwoQ69HxbmDlcAUezn1c",
            getString(R.string.ch_breathing_light) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-UnZudgu84o2WqRxNPd2cBsGNnmT",
            getString(R.string.ch_ai_voice_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-UnZudgu84o2WqRxNPd2cBsGNnmT",
            getString(R.string.ch_offline_map_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-RX4SdRaG7o0sgxxgOfqc8SVxngh",
            getString(R.string.ch_socket_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-LkWLdYhsVoZE3Exw9SJcTJh9n1e",
            getString(R.string.ch_micro_function) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-RURQdbKSmo2arsx5OWBcgMqFn8b",
            //数据
            getString(R.string.data_get_device_history_log) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-EeBdd3dlwoGSBwxAJNMcsnSNnRf",
            getString(R.string.data_get_device_history_log_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-FbxXd8fPQojaQnxuy6bcpNVHnWg",
            //文件传输
            getString(R.string.file_ota) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TnQgdEH4hoqNXvx3wPocGiQGn9g",
            getString(R.string.file_agps) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TnQgdEH4hoqNXvx3wPocGiQGn9g",
            getString(R.string.file_ota_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Pdr1dRsDho8acGxcF9GcbADjnFd",
            getString(R.string.file_agps_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-AF21daJ1EoGWtTx14CucaZDunGd",
            //表盘
            getString(R.string.clock_dial_manage) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-DspNdYiqKoyiArxFl5wckf74nre",
            getString(R.string.clock_dial_ordinary) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-DspNdYiqKoyiArxFl5wckf74nre",
            getString(R.string.clock_dial_photo) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WDzCdGQdRoH1X6xvvdrcSEaOnjf",
            getString(R.string.clock_dial_diy_v1_demo) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WDzCdGQdRoH1X6xvvdrcSEaOnjf",
            getString(R.string.clock_dial_diy_v2_demo_number) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WDzCdGQdRoH1X6xvvdrcSEaOnjf",
            getString(R.string.clock_dial_diy_v2_demo_pointer) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WDzCdGQdRoH1X6xvvdrcSEaOnjf",
            getString(R.string.clock_dial_diy_v2) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WDzCdGQdRoH1X6xvvdrcSEaOnjf",
            getString(R.string.clock_dial_manage_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-CRtidm0N1oDfS3xlygmcxrbPnye",
            getString(R.string.clock_dial_ordinary_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-EhMQdM2Sbog95OxYOUicFtvJn9c",
            getString(R.string.clock_dial_photo_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-FAvPdfrHsohuKdxjpBuc5Sj7nsd",
            //小功能
            getString(R.string.micro_take_photo) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-KDxcd7SvfoZ85CxUmOEcEGlYnSe",
            getString(R.string.micro_widget_list) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-QYLNdLLVloLoT1xfApDcCLaynFE",
            getString(R.string.micro_application_list) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WGpkdImSEobrFQx5kDTc7Onjnb9",
            getString(R.string.micro_sports_icon_list) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-L1uhdhapWoIt8Jxx8qMc7ZREnoe",
            //设置集合
            getString(R.string.ch_set_user) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-XtyTdChAsohupMxdAahc6UaGnbp",
            getString(R.string.ch_set_reminder) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-PccQdN6DBoUUgVxm8ZccL6EEnVc",
            getString(R.string.ch_set_reminder_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Q5GVdiVtRoSK0VxJngpcZFyrn9d",
            getString(R.string.ch_set_fun) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-ZgJudXhuzocWFOxVTOsczELlnQg",
            getString(R.string.ch_set_child) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-LV5Dd2pG0oz85MxN5a5cL8F1nEf",
            getString(R.string.ch_set_child_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-J5bVdXNfjom90wxSAwzcEv9qnVg",
            getString(R.string.ch_set_other) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-KycKdCnUWof6GBxm6zIcGHaKnCe",
            getString(R.string.ch_set_other_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-U0YKdTJDpoatHsxvHDfc2ptendg",
            //用户设置
            getString(R.string.user_set_sync_time) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-B5TvdsNjNoRUVtxkTUMcajuSnTb",
            getString(R.string.user_set_language) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WkB7dB3H3otxPLx44hXcDTl4nCW",
            getString(R.string.user_set_user_config) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-NNthdfpBBoyonoxovWScHhTenDh",
            //提醒设置
            getString(R.string.reminder_notice_system) to "reminder_sleep_reminder",
            getString(R.string.reminder_notice_other) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-G6bPdr7AXo3PIjxxumBc7LuAn9f",
            getString(R.string.reminder_notice_set) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JCdDdHcp3oOHklx3KeYcylDDnKf",
            getString(R.string.reminder_bt_call_switch) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JiG0dI8c1odEqXxQnQec8Z85nie",
            getString(R.string.reminder_call_reminder_set) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-S406dmYefoKaDixYd7yc6kbvnhT",
            getString(R.string.reminder_quick_reply) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-NJECdFkO7omYgqxS3EdcfLAMn3d",
            getString(R.string.reminder_contacts_list) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-NJECdFkO7omYgqxS3EdcfLAMn3d",
            getString(R.string.reminder_large_contacts_list) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-PGmRdzaayowEKwx9dZucpEfGn0R",
            getString(R.string.reminder_sos_contacts) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-PGmRdzaayowEKwx9dZucpEfGn0R",
            getString(R.string.reminder_favorite_contacts) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-PGmRdzaayowEKwx9dZucpEfGn0R",
            getString(R.string.reminder_event) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-U04Ndy6LVoRZ3px8elIcI4bAnAc",
            getString(R.string.reminder_clock) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-XwgVd5pcNoU4qFxdqUXcO3pmnyc",
            getString(R.string.reminder_sedentary) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JDyQdqQxVoCb3Axvxc2cn5AWn6b",
            getString(R.string.reminder_medication) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-RZQxdfKzsoYpdfxDZUbc9vbDn7d",
            getString(R.string.reminder_drink_water) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-ERKDdIrqDoq9iFx8SWqc5mNUnpf",
            getString(R.string.reminder_meals) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-QiptdVNZBoe2LYxmeyBcNdh0n1e",
            getString(R.string.reminder_wash_hand) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Q9M8dQQBNoHGQTx5isJcMW21npf",
            getString(R.string.reminder_physiological_cycle) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-EHyidcBftobKuWx7dfLcm5Zfn7f",
            getString(R.string.reminder_sleep_reminder) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-I97LdnTVyo3YKjxJAdycCuIYnEg",
            getString(R.string.reminder_set_call_notice_switch_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-J2AndunJWorUSfxkogLcrw3Fn38",
            getString(R.string.reminder_whats_app_quick_reply_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-J2AndunJWorUSfxkogLcrw3Fn38",
            getString(R.string.reminder_super_notice_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-GxhCdBrwyoeiOMxfRyjcTD8ln8f",
            getString(R.string.reminder_favorite_contacts_head_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-GxhCdBrwyoeiOMxfRyjcTD8ln8f",
            //功能设置
            getString(R.string.fun_world_clock) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-CcWDdqRLtoypmUxOcDTcxySCnod",
            getString(R.string.fun_music) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-QflOdVW7uojxyyxtjahcjrJWnbb",
            getString(R.string.fun_stock) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-RjXxdSx8uoIixtxQlercNhGmngz",
            getString(R.string.fun_continuous_heart) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-NcxQdEFgdorwG6xQx5RcXfDunWq",
            getString(R.string.fun_hear_real_time) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-UcRKdQnz4oQlRKx1Vi5cTUREnvv",
            getString(R.string.fun_continuous_spo2) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-ArYXdc5r9o7tMUxC1GocmNQUnfd",
            getString(R.string.fun_continuous_body_temperature) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JjPvdb5XFoyhjzxJHe8cREUenib",
            getString(R.string.fun_pressure_mode) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-CusMdgXLgoKF7UxNNy3cLjWlnHg",
            getString(R.string.fun_rem) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TDtidRJhboXs8yxk6GbcqkT7nYf",
            getString(R.string.fun_sleep_mode) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-MYuud9MBaolfkIxumeycsi2lnsg",
            //儿童设置
            getString(R.string.child_schedule) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TeYbdstT9oqkMSxVOKkcDQSyn9b",
            getString(R.string.child_school_mode) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-OQwXdNYKwolVFwx5sSTcwMcInUf",
            getString(R.string.child_exam_mode) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-KzFLdsNwbo8zJuxMieJcbD9yn91",
            getString(R.string.child_child_info_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-VFiXdKC2lolFeMxcqzLcC22rnPe",
            getString(R.string.child_parent_info_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-JjBpdR6kRooYDqxOH0vcs0J5nM9",
            getString(R.string.child_fc_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-FcC8dVNQvowphbxbHqHcpGNDnNg",
            getString(R.string.child_challenge_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-OGmEddyIJo5V5fxGD0gcSs1tn7e",
            getString(R.string.child_revenue_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-IJE6dRO9eohAqbxBxnacuO1hn2d",
            getString(R.string.child_medal_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-BCoSdRXz6olLxpxLNcjc5Isfntb",
            //其他设置
            getString(R.string.other_wrist_screen) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-MNtfdkdJKou2UNxlVhBcf7bLnEV",
            getString(R.string.other_screen_set) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TbfmdKuhKo6dAoxooAjcIe9Onrf",
            getString(R.string.other_overlay_screen) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-DLMTdHBLxo2Cshx1yt4cxkFKn9g",
            getString(R.string.other_information_screen_display) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TbfmdKuhKo6dAoxooAjcIe9Onrf",
            getString(R.string.other_vibration_intensity) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TbfmdKuhKo6dAoxooAjcIe9Onrf",
            getString(R.string.other_vibration_time) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-QoTsdHKRaoYqcOxJgpxc08YsnKd",
            getString(R.string.other_do_not_disturb_mode) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-TbfmdKuhKo6dAoxooAjcIe9Onrf",
            getString(R.string.other_power_saving) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-SKTUd5EguoJD9Dx5dCHcT6vWnIg",
            getString(R.string.other_find_wear_set) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-R29JdPbbMoJk6dxmbZMcqddJnlg",
            getString(R.string.other_customize_set) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-R29JdPbbMoJk6dxmbZMcqddJnlg",
            getString(R.string.other_morning_news_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-YmmudmoGkol1v5x6esscpwu7nxg",
            getString(R.string.other_vault_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-YJ47dHHsSowORkxoUkmceK9Tnje",
            getString(R.string.other_recording_berry) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-WriXdtJR6ogtahx0rD5cMH9En8E",
            //戒指
            getString(R.string.ring_sport_screen) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-PgMrdqF0doTZAlxh83PcBmYinre",
            getString(R.string.ring_measure) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Ra1ud8SZYoSl2ExDrLPc6Qs4n9c",
            getString(R.string.ring_all_sleep_switch) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Ra1ud8SZYoSl2ExDrLPc6Qs4n9c",
            getString(R.string.ring_auto_activity_switch) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Ra1ud8SZYoSl2ExDrLPc6Qs4n9c",
            getString(R.string.ring_auto_motion_recognize) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-LXtBdIZREohjVIxXksPc05gGnVU",
            //思澈
            getString(R.string.sifil_ota) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-Yk2DdNozto3M4ZxyHFmcPpIWnVb",
            getString(R.string.sifil_dial) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-MYtfdoIXCoNdQOx5GBBcX2R6nhh",
            getString(R.string.sifil_photo) to "https://zhouhaismart.feishu.cn/docx/DuVEdRBAuoLvCPxv9njcV0B4nyf#share-OqoddBR8ior5tox8KGCc2qxSnKe",
        )
        val newUrl = keyValuePairs[funValue]
        if (newUrl != null && newUrl.isNotEmpty()) {
            binding.webView.loadUrl(newUrl)
        } else {
            binding.webView.loadUrl(defaultUrl)
        }
    }
}