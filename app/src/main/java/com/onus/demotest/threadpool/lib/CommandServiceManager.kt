package com.onus.demotest.threadpool.lib

class CommandServiceManager private constructor() {
    private val commandPoolSupplier: CommandPoolSupplier = CommandPoolSupplier()

    fun getCommandSupplier(): CommandPoolSupplier {
        return commandPoolSupplier
    }

    fun shutdown() {
        commandPoolSupplier.shutdown()
        synchronized(CommandServiceManager::class.java) {
            instance = null
        }
    }

    fun shutdownNow(): List<Command> {
        synchronized(CommandServiceManager::class.java) {
            instance = null
        }
        return commandPoolSupplier.shutdownNow()
    }

    companion object {
        @Volatile
        private var instance: CommandServiceManager? = null

        @JvmStatic
        fun get(): CommandServiceManager {
            if (instance == null) {
                synchronized(CommandServiceManager::class.java) {
                    if (instance == null) {
                        instance = CommandServiceManager()
                    }
                }
            }
            return instance!!
        }
    }
}
