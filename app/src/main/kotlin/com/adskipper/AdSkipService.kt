package com.adskipper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AdSkipService : AccessibilityService() {
    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            packageNames = arrayOf("com.google.android.youtube")
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        trySkipAd(root)
        root.recycle()
    }
    private fun trySkipAd(root: AccessibilityNodeInfo) {
        val skipTexts = listOf("Skip Ad","Skip Ads","SKIP AD","skip ad","skip ads","Skip ad","跳过广告","跳過廣告","跳过","跳過")
        for (text in skipTexts) {
            val nodes = root.findAccessibilityNodeInfosByText(text) ?: continue
            for (node in nodes) {
                if (node.isClickable) { node.performAction(AccessibilityNodeInfo.ACTION_CLICK); node.recycle(); return }
                val p = node.parent; if (p != null && p.isClickable) { p.performAction(AccessibilityNodeInfo.ACTION_CLICK); p.recycle(); node.recycle(); return }
                node.recycle()
            }
        }
        for (id in listOf("com.google.android.youtube:id/skip_ad_button","com.google.android.youtube:id/skip_button_container")) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id) ?: continue
            for (node in nodes) { if (node.isVisibleToUser) { node.performAction(AccessibilityNodeInfo.ACTION_CLICK); node.recycle(); return }; node.recycle() }
        }
    }
    override fun onInterrupt() {}
}