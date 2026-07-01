package com.onus.demotest.core

/**
 * @Author: onuszhao
 * @Date: 2023-11-15 21:45
 * @Description:
 */
object KernelEngine {

    private var configs: EngineConfigs? = null

    val buildType: BuildType
        get() {
            configs?.buildType?.let {
                return it
            }
            throw ExceptionInInitializerError("KernelConfigs must be init and build type must set!")
        }

    val channelType: ChannelType
        get() {
            configs?.channelType?.let {
                return it
            }
            throw ExceptionInInitializerError("KernelConfigs must be init and channel type must set!")
        }

    fun init(configs: EngineConfigs) {
        KernelEngine.configs = configs
    }
}