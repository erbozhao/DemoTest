package com.onus.demotest.core

/**
 * @Author: onuszhao
 * @Date: 2023-11-15 21:48
 * @Description:
 */
enum class BuildType {
    DEBUG, PREVIEW, GRAY, RELEASE;

    open fun isDevelop(): Boolean {
        return this == DEBUG || this == PREVIEW
    }

    open fun isRelease(): Boolean {
        return this == RELEASE
    }
}