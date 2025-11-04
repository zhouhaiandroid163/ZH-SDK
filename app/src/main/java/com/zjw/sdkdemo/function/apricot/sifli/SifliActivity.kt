package com.zjw.sdkdemo.function.apricot.sifli

import android.content.Intent
import android.os.Bundle
import com.zjw.sdkdemo.R
import com.zjw.sdkdemo.base.BaseActivity
import com.zjw.sdkdemo.databinding.ActivitySifliBinding

class SifliActivity : BaseActivity() {
    private val binding by lazy { ActivitySifliBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setTitle(R.string.ch_sifil)
        initListener()
    }

    fun initListener() {
        clickCheckConnect(binding.btnSifilOta) {
            startActivity(Intent(this, SifliOtaActivity::class.java))
        }

        clickCheckConnect(binding.btnSifilDial) {
            startActivity(Intent(this, SifliDialActivity::class.java))
        }

        clickCheckConnect(binding.btnSifilPhoto) {
            startActivity(Intent(this, SifliPhotoActivity::class.java))
        }
    }

}