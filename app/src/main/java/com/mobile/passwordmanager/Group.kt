package com.mobile.passwordmanager

import org.json.JSONArray
import org.json.JSONObject

/**
 * 密码分组。类似于目录,密码记录可以归属于某个分组,也可以不属于任何分组(显示在主页)。
 */
data class Group(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("createdAt", createdAt)
        put("updatedAt", updatedAt)
    }

    companion object {
        fun fromJson(o: JSONObject): Group = Group(
            id = o.optString("id"),
            name = o.optString("name"),
            createdAt = o.optLong("createdAt"),
            updatedAt = o.optLong("updatedAt")
        )

        fun listToJson(items: List<Group>): String {
            val arr = JSONArray()
            items.forEach { arr.put(it.toJson()) }
            return arr.toString()
        }

        fun listFromJson(s: String): List<Group> {
            val arr = JSONArray(s)
            val out = ArrayList<Group>(arr.length())
            for (i in 0 until arr.length()) {
                out.add(fromJson(arr.getJSONObject(i)))
            }
            return out
        }
    }
}
