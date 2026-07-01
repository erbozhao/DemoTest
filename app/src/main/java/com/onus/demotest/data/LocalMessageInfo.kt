package com.onus.demotest.data

import androidx.annotation.Keep

/**
 * @Author: onuszhao
 * @Date: 2024-03-26 18:00
 * @Description:
 */
@Keep
data class LocalMessageInfo(val taskId: Int, val timeToLive: Long, val uniqueKey: String)