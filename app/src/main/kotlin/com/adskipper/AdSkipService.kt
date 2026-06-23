package com.adskipper

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AdSkipService : AccessibilityService() {

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_VIEW_FOCUSED
            packageNames = arrayOf("com.google.android.youtube")
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val root = rootInActiveWindow ?: return
        trySkipAd(root)
        root.recycle()
    }

    private fun trySkipAd(root: AccessibilityNodeInfo) {
        // 所有可能的跳过按钮 resource ID（覆盖多个 YouTube 版本）
        val skipIds = listOf(
            "com.google.android.youtube:id/skip_ad_button",
            "com.google.android.youtube:id/skip_button",
            "com.google.android.youtube:id/skip_button_container",
            "com.google.android.youtube:id/skip_ad_button_text",
            "com.google.android.youtube:id/ad_skip_button",
            "com.google.android.youtube:id/player_skip_button"
        )
        for (id in skipIds) {
            val nodes = root.findAccessibilityNodeInfosByViewId(id) ?: continue
            for (node in nodes) {
                if (node.isVisibleToUser) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    node.recycle()
                    return
                }
                node.recycle()
            }
        }

        // 通过文字查找（支持多语言）
        val skipTexts = listOf(
            "Skip Ad", "Skip Ads", "SKIP AD", "Skip ad", "skip ad", "skip ads",
            "跳过广告", "跳過廣告", "跳过", "跳過",
            "Ignorer l'annonce", "Überspringen", "Saltar anuncio",
            "Pular anúncio", "広告をスキップ", "광고 건너뛰기"
        )
        for (text in skipTexts) {
            val nodes = root.findAccessibilityNodeInfosByText(text) ?: continue
            for (node in nodes) {
                if (node.isVisibleToUser) {
                    if (node.isClickable) {
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        node.recycle()
                        return
                    }
                    // 试父节点
                    var parent = node.parent
                    var depth = 0
                    while (parent != null && depth < 5) {
                        if (parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            parent.recycle()
                            node.recycle()
                            return
                        }
                        val next = parent.parent
                        parent.recycle()
                        parent = next
                        depth++
                    }
                    parent?.recycle()
                }
                node.recycle()
            }
        }

        // 兜底：查找所有可点击的小按钮（广告跳过按钮通常在角落）
        findSkipButtonByPosition(root)
    }

    private fun findSkipButtonByPosition(root: AccessibilityNodeInfo) {
        val count = root.childCount
        for (i in 0 until count) {
            val child = root.getChild(i) ?: continue
            if (child.isClickable && child.isVisibleToUser) {
                val text = child.text?.toString() ?: ""
                val desc = child.contentDescription?.toString() ?: ""
                val combined = (text + desc).lowercase()
                if (combined.contains("skip") || combined.contains("跳过") || combined.contains("跳過")) {
                    child.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    child.recycle()
                    return
                }
            }
            findSkipButtonByPosition(child)
            child.recycle()
        }
    }

    override fun onInterrupt() {}
}
