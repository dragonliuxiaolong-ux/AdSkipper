package com.adskipper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var openSettingsButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        statusText = findViewById(R.id.statusText)
        openSettingsButton = findViewById(R.id.openSettingsButton)
        openSettingsButton.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
    }
    override fun onResume() {
        super.onResume()
        if (isEnabled()) {
            statusText.text = "✅ 已开启 — 自动跳过 YouTube 广告中"
            statusText.setTextColor(getColor(android.R.color.holo_green_dark))
            openSettingsButton.text = "无障碍设置"
        } else {
            statusText.text = "❌ 未开启 — 请点击下方按钮开启权限"
            statusText.setTextColor(getColor(android.R.color.holo_red_dark))
            openSettingsButton.text = "去开启权限"
        }
    }
    private fun isEnabled(): Boolean {
        val svc = packageName + "/" + AdSkipService::class.java.canonicalName
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        val s = TextUtils.SimpleStringSplitter(':'); s.setString(enabled)
        while (s.hasNext()) { if (s.next().equals(svc, ignoreCase = true)) return true }
        return false
    }
}