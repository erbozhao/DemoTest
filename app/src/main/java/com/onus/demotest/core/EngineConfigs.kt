package com.onus.demotest.core

/**
 * @Author: onuszhao
 * @Date: 2023-11-15 21:47
 * @Description:
 */
class EngineConfigs private constructor() {

    @JvmField
    internal var buildType: BuildType? = null

    @JvmField
    internal var channelType: ChannelType? = null

    class Builder {

        private val configs: EngineConfigs = EngineConfigs()

        /**
         * 构建类型
         * @param type
         */
        fun setBuildType(type: BuildType): Builder {
            configs.buildType = type
            return this
        }

        /**
         * 渠道类型
         */
        fun setChannelType(type: ChannelType): Builder {
            configs.channelType = type
            return this
        }

        /**
         * 构建Configs
         */
        fun build(): EngineConfigs {
            return configs
        }
    }
}