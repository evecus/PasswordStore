package com.mobile.passwordmanager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

object ClipboardHelper {

    /** 复制文本到剪贴板并提示。注意:剪贴板内容明文保存,30 秒后自动清空。 */
    fun copy(ctx: Context, label: String, text: String, autoClearMs: Long = 30_000L) {
        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
        Toast.makeText(ctx, "已复制 $label,30 秒后自动清空", Toast.LENGTH_SHORT).show()

        val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
        mainHandler.postDelayed({
            // 仅当剪贴板仍是这段文本时才清空,避免覆盖用户后续复制
            if (cm.primaryClip?.getItemAt(0)?.text == text) {
                cm.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        }, autoClearMs)
    }
}
