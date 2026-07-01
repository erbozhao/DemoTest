package com.onus.demotest.utils

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

class ReflectUtil private constructor() {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun getField(clazzName: String, target: Any?, name: String): Any? {
            return getField(Class.forName(clazzName), target, name)
        }

        @JvmStatic
        @Throws(Exception::class)
        fun getField(clazz: Class<*>, target: Any?, name: String): Any? {
            val field: Field = clazz.getDeclaredField(name)
            field.isAccessible = true
            return field.get(target)
        }

        @JvmStatic
        fun getFieldNoException(clazzName: String, target: Any?, name: String): Any? {
            return try {
                getFieldNoException(Class.forName(clazzName), target, name)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun getMethod(clazz: Class<*>, methodName: String, parameterTypes: Class<*>): Method? {
            return try {
                val method = clazz.getMethod(methodName)
                method.isAccessible = true
                method
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
                null
            } catch (e: SecurityException) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun invokeMethod(method: Method, obj: Any?): Any? {
            return try {
                method.invoke(obj)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
                null
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun getFieldNoException(clazz: Class<*>, target: Any?, name: String): Any? {
            return try {
                getField(clazz, target, name)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        @Throws(Exception::class)
        fun setField(clazzName: String, target: Any?, name: String, value: Any?) {
            setField(Class.forName(clazzName), target, name, value)
        }

        @JvmStatic
        @Throws(Exception::class)
        fun setField(clazz: Class<*>, target: Any?, name: String, value: Any?) {
            val field = clazz.getDeclaredField(name)
            field.isAccessible = true
            field.set(target, value)
        }

        @JvmStatic
        fun setFieldNoException(clazzName: String, target: Any?, name: String, value: Any?) {
            try {
                setFieldNoException(Class.forName(clazzName), target, name, value)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        fun setFieldNoException(clazz: Class<*>, target: Any?, name: String, value: Any?) {
            try {
                setField(clazz, target, name, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        @JvmStatic
        @Throws(Exception::class)
        fun invoke(clazzName: String, target: Any?, name: String, vararg args: Any?): Any? {
            return invoke(Class.forName(clazzName), target, name, *args)
        }

        @JvmStatic
        @Throws(Exception::class)
        fun invoke(clazz: Class<*>, target: Any?, name: String, vararg args: Any?): Any? {
            val parameterTypes = args.map { it!!.javaClass }.toTypedArray()
            val method = clazz.getDeclaredMethod(name, *parameterTypes)
            method.isAccessible = true
            return method.invoke(target, *args)
        }

        @JvmStatic
        @Throws(Exception::class)
        fun invoke(
            clazzName: String,
            target: Any?,
            name: String,
            parameterTypes: Array<Class<*>>,
            vararg args: Any?
        ): Any? {
            return invoke(Class.forName(clazzName), target, name, parameterTypes, *args)
        }

        @JvmStatic
        @Throws(Exception::class)
        fun invoke(
            clazz: Class<*>,
            target: Any?,
            name: String,
            parameterTypes: Array<Class<*>>,
            vararg args: Any?
        ): Any? {
            val method = clazz.getDeclaredMethod(name, *parameterTypes)
            method.isAccessible = true
            return method.invoke(target, *args)
        }

        @JvmStatic
        fun invokeNoException(
            clazzName: String,
            target: Any?,
            name: String,
            parameterTypes: Array<Class<*>>,
            vararg args: Any?
        ): Any? {
            return try {
                invokeNoException(Class.forName(clazzName), target, name, parameterTypes, *args)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        @JvmStatic
        fun invokeNoException(
            clazz: Class<*>,
            target: Any?,
            name: String,
            parameterTypes: Array<Class<*>>,
            vararg args: Any?
        ): Any? {
            return try {
                invoke(clazz, target, name, parameterTypes, *args)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
