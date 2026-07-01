package com.onus.demotest.utils

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import java.lang.reflect.Field
import java.lang.reflect.Method

class ReflectionUtils private constructor() {
    companion object {
        @JvmStatic
        @Nullable
        fun getStaticField(@NonNull className: String, @NonNull fieldName: String): Any? {
            return try {
                val cls = Class.forName(className)
                val field = cls.getDeclaredField(fieldName)
                field.isAccessible = true
                field.get(null)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun getStaticField(@NonNull className: String, @NonNull fieldName: String, @NonNull loader: ClassLoader): Any? {
            return try {
                val cls = Class.forName(className, true, loader)
                val field = cls.getDeclaredField(fieldName)
                field.isAccessible = true
                field.get(null)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        fun setStaticField(@NonNull className: String, @NonNull fieldName: String, @NonNull obj: Any, @Nullable value: Any?) {
            try {
                val cls = Class.forName(className)
                val field = cls.getDeclaredField(fieldName)
                field.isAccessible = true
                field.set(obj, value)
            } catch (_: Throwable) {
            }
        }

        @JvmStatic
        fun setInstanceField(@NonNull obj: Any, @NonNull fieldName: String, @Nullable value: Any?): Boolean {
            return try {
                val field = getDeclaredField(obj, fieldName) ?: return false
                field.isAccessible = true
                field.set(obj, value)
                true
            } catch (_: Exception) {
                false
            }
        }

        @JvmStatic
        @Nullable
        fun getInstanceField(@NonNull obj: Any, @NonNull fieldName: String): Any? {
            return getInstanceField(obj, null, fieldName)
        }

        @JvmStatic
        @Nullable
        fun getInstanceField(@NonNull obj: Any, @Nullable declareClassName: String?, @NonNull fieldName: String): Any? {
            return try {
                val cls = if (declareClassName != null) {
                    Class.forName(declareClassName, true, obj.javaClass.classLoader)
                } else {
                    obj.javaClass
                }
                val field = cls.getDeclaredField(fieldName)
                field.isAccessible = true
                field.get(obj)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun getDeclaredField(@Nullable obj: Any?, @Nullable fieldName: String?): Field? {
            if (obj == null) {
                return null
            }

            var clazz: Class<*>? = obj.javaClass
            while (clazz != null && clazz != Any::class.java) {
                try {
                    return clazz.getDeclaredField(fieldName)
                } catch (_: Exception) {
                }
                clazz = clazz.superclass
            }
            return null
        }

        @JvmStatic
        @Nullable
        fun invokeStatic(@NonNull className: String, @NonNull methodName: String, @Nullable loader: ClassLoader?): Any? {
            return try {
                val cls = if (loader == null) Class.forName(className) else Class.forName(className, true, loader)
                val method = cls.getMethod(methodName)
                method.invoke(null)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun invokeStatic(@NonNull className: String, @NonNull methodName: String): Any? {
            return invokeStatic(className, methodName, null)
        }

        @JvmStatic
        @Nullable
        fun invokeStatic(
            @NonNull classObj: Class<*>,
            @NonNull methodName: String,
            @Nullable parameterTypes: Array<Class<*>>?,
            @Nullable vararg args: Any?
        ): Any? {
            return try {
                val method = classObj.getMethod(methodName, *(parameterTypes ?: emptyArray()))
                method.isAccessible = true
                method.invoke(null, *args)
            } catch (ignore: Throwable) {
                ignore.printStackTrace()
                null
            }
        }

        @JvmStatic
        @Nullable
        fun invokeInstance(@NonNull obj: Any, @NonNull methodName: String): Any? {
            return invokeInstance(obj, methodName, null)
        }

        @JvmStatic
        @Nullable
        fun invokeInstance(
            @NonNull obj: Any,
            @NonNull methodName: String,
            @Nullable paramTypes: Array<Class<*>>?,
            @NonNull vararg params: Any
        ): Any? {
            return try {
                val method = obj.javaClass.getMethod(methodName, *(paramTypes ?: emptyArray()))
                method.isAccessible = true
                if (params.isEmpty()) method.invoke(obj) else method.invoke(obj, *params)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun invokeStaticMethod(
            @Nullable classLoader: ClassLoader?,
            @NonNull className: String,
            @NonNull methodName: String,
            @Nullable parameterTypes: Array<Class<*>>?,
            @Nullable vararg args: Any?
        ): Any? {
            return try {
                val method = getClass(classLoader, className).getMethod(methodName, *(parameterTypes ?: emptyArray()))
                method.isAccessible = true
                method.invoke(null, *args)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun invokeMethod(
            @Nullable classLoader: ClassLoader?,
            @NonNull target: Any,
            @NonNull className: String,
            @NonNull methodName: String,
            @Nullable parameterTypes: Array<Class<*>>?,
            @Nullable vararg args: Any?
        ): Any? {
            return try {
                val method = getClass(classLoader, className).getMethod(methodName, *(parameterTypes ?: emptyArray()))
                method.isAccessible = true
                method.invoke(target, *args)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun loadClass(@Nullable classLoader: ClassLoader?, @NonNull className: String): Class<*>? {
            return try {
                if (classLoader != null) classLoader.loadClass(className) else Class.forName(className)
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun newInstance(
            @Nullable classLoader: ClassLoader?,
            @NonNull className: String,
            @Nullable parameterTypes: Array<Class<*>>?,
            @Nullable vararg args: Any?
        ): Any? {
            return try {
                val cls = getClass(classLoader, className)
                if (parameterTypes == null) {
                    cls.newInstance()
                } else {
                    cls.getConstructor(*parameterTypes).newInstance(*args)
                }
            } catch (ta: Throwable) {
                if ("com.tencent.smtt.webkit.adapter.X5WebViewAdapter".equals(className, ignoreCase = true)) {
                    ta
                } else {
                    null
                }
            }
        }

        @JvmStatic
        @Nullable
        fun newInstance(@NonNull className: String, @Nullable parameterTypes: Array<Class<*>>?, @Nullable vararg args: Any?): Any? {
            return try {
                if (parameterTypes == null) {
                    Class.forName(className).newInstance()
                } else {
                    Class.forName(className).getConstructor(*parameterTypes).newInstance(*args)
                }
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun newInstance(@Nullable classLoader: ClassLoader?, @NonNull className: String): Any? {
            return try {
                getClass(classLoader, className).newInstance()
            } catch (_: Throwable) {
                null
            }
        }

        @Throws(Throwable::class)
        private fun getClass(@Nullable classLoader: ClassLoader?, @NonNull className: String): Class<*> {
            return if (classLoader != null) {
                classLoader.loadClass(className)
            } else {
                Class.forName(className)
            }
        }

        @JvmStatic
        @Nullable
        fun getClass(@Nullable className: String?): Class<*>? {
            if (className == null) {
                return null
            }
            return try {
                Class.forName(className)
            } catch (_: ClassNotFoundException) {
                null
            }
        }

        @JvmStatic
        @Nullable
        fun getMemberClass(@Nullable target: Any?, @Nullable className: String?): Class<*>? {
            if (target == null || className == null || className.isEmpty()) {
                return null
            }

            return try {
                val fields = target.javaClass.declaredFields
                for (field in fields) {
                    if (field.type.toString().equals(className, ignoreCase = true)) {
                        field.isAccessible = true
                        val memberObject = field.get(target)
                        return memberObject.javaClass
                    }
                }
                null
            } catch (_: Throwable) {
                null
            }
        }

        @JvmStatic
        fun hasMethod(
            @Nullable clazz: Class<*>?,
            @Nullable methodName: String?,
            @Nullable vararg parameterTypes: Class<*>?
        ): Boolean {
            if (clazz == null || methodName == null || methodName.isEmpty()) {
                return false
            }
            return try {
                clazz.getDeclaredMethod(methodName, *parameterTypes)
                true
            } catch (_: NoSuchMethodException) {
                false
            }
        }
    }
}
