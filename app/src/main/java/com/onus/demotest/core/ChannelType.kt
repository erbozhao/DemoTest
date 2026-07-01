package com.onus.demotest.core

/**
 * @Author: onuszhao
 * @Date: 2023-11-15 17:37
 * @Description:
 */
enum class ChannelType {
    NORMAL,
    TRANSSION_BUILDIN,
    TRANSSION_PREINSTALL,
    VIVO_PREINSTALL,
    OPPO_PREINSTALL,
    SAMSUNG_PREINSTALL;

    open fun isNormal(): Boolean {
        return this == NORMAL
    }

    open fun isChannel(): Boolean {
        return this == TRANSSION_BUILDIN || this == TRANSSION_PREINSTALL || this == VIVO_PREINSTALL || this == OPPO_PREINSTALL || this == SAMSUNG_PREINSTALL
    }

    open fun isTranssionBuildin(): Boolean {
        return this == TRANSSION_BUILDIN
    }

    open fun isTranssionPreinstall(): Boolean {
        return this == TRANSSION_PREINSTALL
    }

    open fun isVivoPreinstall(): Boolean {
        return this == VIVO_PREINSTALL
    }

    open fun isOppoPreinstall(): Boolean {
        return this == OPPO_PREINSTALL
    }

    open fun isSamsungPreinstall(): Boolean {
        return this == SAMSUNG_PREINSTALL
    }
}