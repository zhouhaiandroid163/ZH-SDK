package com.zjw.sdkdemo.function.apricot.test

import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.zhapp.ble.utils.NotificationUtils
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivityNotifySendBinding
import java.util.Timer
import java.util.TimerTask

/**
 * Created by Android on 2025/9/8.
 */
class NotifySendTestActivity : BaseActivity() {
    private val binding: ActivityNotifySendBinding by lazy { ActivityNotifySendBinding.inflate(layoutInflater) }
    private var notificationTimer: Timer? = null
    private var sendCount = 0
    private var maxCount = 0
    private var notification: Notification? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(R.string.ch_loop_notify)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() {
        binding.btnSend.setOnClickListener {
            startSendingNotifications()
        }

        binding.btnStop.setOnClickListener {
            stopSendingNotifications()
        }
        supportActionBar?.apply {
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun startSendingNotifications() {
        val title = binding.etTitle.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val subText = binding.etSubText.text.toString().trim()
        val intervalStr = binding.etIn.text.toString().trim()
        val numStr = binding.etNum.text.toString().trim()

        if (title.isEmpty() || content.isEmpty()) {
            Toast.makeText(this, "请填写通知标题和内容", Toast.LENGTH_SHORT).show()
            return
        }

        if (intervalStr.isEmpty()) {
            Toast.makeText(this, "请填写通知间隔", Toast.LENGTH_SHORT).show()
            return
        }

        if (numStr.isEmpty()) {
            Toast.makeText(this, "请填写通知次数", Toast.LENGTH_SHORT).show()
            return
        }

        val interval = intervalStr.toLong()
        maxCount = numStr.toInt()

        if (interval <= 0) {
            Toast.makeText(this, "通知间隔必须是大于0的数字", Toast.LENGTH_SHORT).show()
            return
        }

        if (maxCount <= 0) {
            Toast.makeText(this, "通知次数必须是大于0的数字", Toast.LENGTH_SHORT).show()
            return
        }

        // 停止之前的通知发送任务
        stopSendingNotifications(false)

        // 重置计数器
        sendCount = 0

        // 创建通知
        val notificationIntent = Intent() // 这里使用空Intent，实际项目中可根据需要设置
        notification = NotificationUtils.createNotification(
            this, title, content, subText,
            applicationInfo.icon, notificationIntent
        )

        // 启动定时器发送通知
        notificationTimer = Timer()
        notificationTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (sendCount < maxCount!!) {
                    sendCount++
                    showNotificationWithCount()
                    handler.post {
                        binding.tvLog.text = "已发送通知: $sendCount/$maxCount 次"
                    }
                } else {
                    handler.post {
                        stopSendingNotifications()
                        binding.tvLog.text = "通知发送完成: $sendCount/$maxCount 次"
                    }
                }
            }
        }, 0, interval)

        binding.btnSend.isEnabled = false
        binding.btnStop.isEnabled = true
        Toast.makeText(this, "开始发送通知", Toast.LENGTH_SHORT).show()
    }

    private fun showNotificationWithCount() {
        val updatedTitle = "${binding.etTitle.text.toString().trim()}$sendCount"
        val updatedContent = "${binding.etContent.text.toString().trim()}$sendCount"
        val updatedSubText = "${binding.etSubText.text.toString().trim()}$sendCount"
        val notificationIntent = Intent()
        val updatedNotification = NotificationUtils.createNotification(
            this,
            updatedTitle,
            updatedContent,
            updatedSubText,
            applicationInfo.icon,
            notificationIntent
        )

        // 使用通知管理器发送通知
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, updatedNotification)
    }

    private fun stopSendingNotifications(isLog: Boolean = true) {
        notificationTimer?.cancel()
        notificationTimer = null
        binding.btnSend.isEnabled = true
        binding.btnStop.isEnabled = false
        if(isLog) {
            binding.tvLog.text = "已停止发送通知，共发送: $sendCount 次"
            Toast.makeText(this, "已停止发送通知", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        if (!NotificationUtils.areNotificationsEnabled(this)) {
            NotificationUtils.goToSetNotification(
                this@NotifySendTestActivity,
                10
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSendingNotifications()
    }

    override fun onBackPressed() {
        showExitConfirmationDialog()
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("确认退出")
            .setMessage("您确定要退出通知发送测试页面吗？当前正在进行的任务将会停止。")
            .setPositiveButton("确定") { _, _ ->
                stopSendingNotifications()
                super.onBackPressed()
            }
            .setNegativeButton("取消", null)
            .setCancelable(false)
            .show()
    }
}
