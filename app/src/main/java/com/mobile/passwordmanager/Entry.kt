package com.mobile.passwordmanager

import org.json.JSONArray
import org.json.JSONObject

/**
 * 一条密码记录。所有字段在内存中是明文,落盘时整体被 AES-GCM 加密。
 * [groupId] 为 null 时该记录显示在主页;非 null 时归属于对应分组。
 * [iconKey] 为用户从离线图标库中选择的品牌图标标识(见 [IconCatalog]),为 null 时使用默认图标。
 */
data class Entry(
    val id: String,
    val title: String,
    val username: String,
    val password: String,
    val url: String,
    val notes: String,
    val groupId: String?,
    val updatedAt: Long,
    val iconKey: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("username", username)
        put("password", password)
        put("url", url)
        put("notes", notes)
        if (groupId != null) put("groupId", groupId)
        put("updatedAt", updatedAt)
        if (iconKey != null) put("iconKey", iconKey)
    }

    companion object {
        fun fromJson(o: JSONObject): Entry = Entry(
            id = o.optString("id"),
            title = o.optString("title"),
            username = o.optString("username"),
            password = o.optString("password"),
            url = o.optString("url"),
            notes = o.optString("notes"),
            groupId = if (o.has("groupId")) o.getString("groupId") else null,
            updatedAt = o.optLong("updatedAt"),
            iconKey = if (o.has("iconKey")) o.getString("iconKey") else null
        )

        fun listToJson(items: List<Entry>): String {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJson(s: String): List<Entry> {
            val arr = JSONArray(s)
            val out = ArrayList<Entry>(arr.length())
            for (i in 0 until arr.length()) {
                out.add(fromJson(arr.getJSONObject(i)))
            }
            return out
        }
    }
}
