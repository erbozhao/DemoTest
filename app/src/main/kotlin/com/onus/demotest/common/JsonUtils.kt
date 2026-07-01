package com.onus.demotest.common

import org.json.JSONObject

/**
 * @Author: onuszhao
 * @Date: 2024-03-28 14:05
 * @Description:
 */
object JsonUtils {

    fun getStringFromJson(jsonStr: String, key: String, default: String): String {
        kotlin.runCatching {
            return JSONObject(jsonStr).optString(key, default)
        }
        return default
    }
}