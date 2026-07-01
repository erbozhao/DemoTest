package com.onus.demotest.utils;

/**
 * @Author: onuszhao
 * @Date: 2023-09-28 10:22
 * @Description:
 */

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射相关的工具类
 *
 * @author xiandongluo
 * @date 2022-06-02
 */
public class ReflectionUtils
{

    /**
     * 获取静态变量的值
     *
     * @param className
     * @param fieldName
     * @return
     */
    @Nullable
    public static Object getStaticField(@NonNull String className, @NonNull String fieldName)
    {
        try
        {
            Class<?> cls = Class.forName(className);
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(null);
        }
        catch (Throwable ignore)
        {
            return null;
        }
    }

    /**
     * 获取静态变量的值，传递ClassLoader
     *
     * @param className
     * @param fieldName
     * @param loader
     * @return
     */
    @Nullable
    public static Object getStaticField(@NonNull String className, @NonNull String fieldName, @NonNull ClassLoader loader)
    {
        try
        {
            Class<?> cls = Class.forName(className, true, loader);
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(null);
        }
        catch (Throwable ignore)
        {
            return null;
        }
    }

    /**
     * 设置静态变量的值
     *
     * @param className
     * @param fieldName
     * @param obj
     * @param value
     */
    public static void setStaticField(@NonNull String className, @NonNull String fieldName, @NonNull Object obj, @Nullable Object value)
    {
        try
        {
            Class<?> cls = Class.forName(className);
            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        }
        catch (Throwable ignore)
        {
        }
    }

    /**
     * 设置变量的值
     *
     * @param obj
     * @param fieldName
     * @param value
     * @return
     */
    public static boolean setInstanceField(@NonNull Object obj, @NonNull String fieldName, @Nullable Object value)
    {
        if (obj == null)
        {
            return false;
        }

        try
        {
            Field f = getDeclaredField(obj, fieldName);
            if (f != null)
            {
                f.setAccessible(true);
                f.set(obj, value);
                return true;
            }
            else
            {
                return false;
            }
        }
        catch (Exception ignore)
        {
            return false;
        }
    }

    /**
     * 获取变量的值
     *
     * @param obj
     * @param fieldName
     * @return
     */
    @Nullable
    public static Object getInstanceField(@NonNull Object obj, @NonNull String fieldName)
    {
        return getInstanceField(obj, null, fieldName);
    }

    /**
     * Gets the value of given field of the object.
     *
     * @param obj
     * @param declareClassName The class in which the field is declared. Pass
     *            null if it's declared in the class of the given obj(not in its
     *            parent).
     * @param fieldName
     * @return
     */
    @Nullable
    public static Object getInstanceField(@NonNull Object obj, @Nullable String declareClassName, @NonNull String fieldName)
    {
        try
        {
            Class<?> cls;
            if (declareClassName != null)
            {
                cls = Class.forName(declareClassName, true, obj.getClass().getClassLoader());
            }
            else
            {
                cls = obj.getClass();
            }

            Field f = cls.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(obj);
        }
        catch (Throwable ignore)
        {
            return null;
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     *
     * @param object : 子类对象
     * @param fieldName : 父类中的属性名
     * @return 父类中的属性对象
     */
    @Nullable
    public static Field getDeclaredField(@Nullable Object object, @Nullable String fieldName)
    {
        if (object == null)
        {
            return null;
        }

        Field field;

        Class<?> clazz = object.getClass();

        for (; clazz != Object.class; clazz = clazz.getSuperclass())
        {
            try
            {
                field = clazz.getDeclaredField(fieldName);
                return field;
            }
            catch (Exception e)
            {
                //这里甚么都不要做！并且这里的异常必须这样写，不能抛出去。
                //如果这里的异常打印或者往外抛，则就不会执行clazz = clazz.getSuperclass(),最后就不会进入到父类中了
            }
        }

        return null;
    }

    /**
     * 反射静态方法
     *
     * @param className
     * @param methodName
     * @param loader
     * @return
     */
    @Nullable
    public static Object invokeStatic(@NonNull String className, @NonNull String methodName, @Nullable ClassLoader loader)
    {
        try
        {
            Class<?> cls = loader == null ? Class.forName(className) : Class.forName(className, true, loader);
            Method m = cls.getMethod(methodName);
            return m.invoke(null);
        }
        catch (Throwable ignore)
        {
            return null;
        }
    }

    /**
     * 反射静态方法
     *
     * @param className
     * @param methodName
     * @return
     */
    @Nullable
    public static Object invokeStatic(@NonNull String className, @NonNull String methodName)
    {
        return invokeStatic(className, methodName, null);
    }

    /**
     * 反射带参数的静态方法
     *
     * @param classObj
     * @param methodName
     * @param parameterTypes
     * @param args
     * @return
     */
    @Nullable
    public static Object invokeStatic(@NonNull Class<?> classObj, @NonNull String methodName, @Nullable Class<?>[] parameterTypes,
                                      @Nullable Object... args)
    {
        try
        {
            Method method = classObj.getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, args);
        }
        catch (Throwable ignore)
        {
            ignore.printStackTrace();
            return null;
        }
    }

    /**
     * 反射方法
     *
     * @param obj
     * @param methodName
     * @return
     */
    @Nullable
    public static Object invokeInstance(@NonNull Object obj, @NonNull String methodName)
    {
        return invokeInstance(obj, methodName, null);
    }

    /**
     * 反射方法
     *
     * @param obj
     * @param methodName
     * @param paramTypes
     * @param params
     * @return
     */
    @Nullable
    public static Object invokeInstance(@NonNull Object obj, @NonNull String methodName, @Nullable Class<?>[] paramTypes, @NonNull Object... params)
    {
        try
        {
            Class<?> cls = obj.getClass();
            Method m = cls.getMethod(methodName, paramTypes);
            m.setAccessible(true);
            if (params.length == 0)
            {
                return m.invoke(obj);
            }
            return m.invoke(obj, params);
        }
        catch (Throwable ignore)
        {
            return null;
        }
    }

    /**
     * 反射静态方法
     *
     * @param classLoader
     * @param className
     * @param methodName
     * @param parameterTypes
     * @param args
     * @return
     */
    @Nullable
    public static Object invokeStaticMethod(@Nullable ClassLoader classLoader, @NonNull String className, @NonNull String methodName,
                                            @Nullable Class<?>[] parameterTypes, @Nullable Object... args)
    {
        try
        {
            Method method = getClass(classLoader, className).getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(null, args);
        }
        catch (Throwable ta)
        {
            return null;
        }
    }

    /**
     * 反射方法
     *
     * @param classLoader
     * @param target
     * @param className
     * @param methodName
     * @param parameterTypes
     * @param args
     * @return
     */
    public static Object invokeMethod(@Nullable ClassLoader classLoader, @NonNull Object target, @NonNull String className,
                                      @NonNull String methodName, @Nullable Class<?>[] parameterTypes, @Nullable Object... args)
    {
        try
        {

            Method method = getClass(classLoader, className).getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        }
        catch (Throwable ta)
        {
            return null;
        }
    }

    /**
     * 加载类
     *
     * @param classLoader
     * @param className
     * @return
     */
    public static Class<?> loadClass(@Nullable ClassLoader classLoader, @NonNull String className)
    {
        try
        {
            if (classLoader != null)
            {
                return classLoader.loadClass(className);
            }
            else
            {
                return Class.forName(className);
            }
        }
        catch (Throwable ta)
        {
            return null;
        }
    }

    /**
     * 获取实例
     *
     * @param classLoader
     * @param className
     * @param parameterTypes
     * @param args
     * @return
     */
    public static Object newInstance(@Nullable ClassLoader classLoader, @NonNull String className, @Nullable Class<?>[] parameterTypes,
                                     @Nullable Object... args)
    {
        try
        {
            Class cls = getClass(classLoader, className);
            if (parameterTypes == null)
            {
                return cls.newInstance();
            }
            else
            {
                return cls.getConstructor(parameterTypes).newInstance(args);
            }
        }
        catch (Throwable ta)
        {
            // createSDKWebview
            if ("com.tencent.smtt.webkit.adapter.X5WebViewAdapter".equalsIgnoreCase(className))
            {
                return ta;
            }

            return null;
        }
    }

    /**
     * 获取实例
     *
     * @param className
     * @param parameterTypes
     * @param args
     * @return
     */
    public static Object newInstance(@NonNull String className, @Nullable Class<?>[] parameterTypes, @Nullable Object... args)
    {
        try
        {
            if (parameterTypes == null)
            {
                return Class.forName(className).newInstance();
            }
            else
            {
                return Class.forName(className).getConstructor(parameterTypes).newInstance(args);
            }
        }
        catch (Throwable ta)
        {
            return null;
        }
    }

    /**
     * 获取实例
     *
     * @param classLoader
     * @param className
     * @return
     */
    public static Object newInstance(@Nullable ClassLoader classLoader, @NonNull String className)
    {
        try
        {
            return getClass(classLoader, className).newInstance();
        }
        catch (Throwable ta)
        {
            return null;
        }
    }

    /**
     * 获取Class
     *
     * @param classLoader
     * @param className
     * @return
     * @throws Throwable
     */
    private static Class<?> getClass(@Nullable ClassLoader classLoader, @NonNull String className) throws Throwable
    {
        Class<?> cls = null;
        if (classLoader != null)
        {
            cls = classLoader.loadClass(className);
        }
        else
        {
            cls = Class.forName(className);
        }
        return cls;
    }

    /**
     * 获取Class对象
     *
     * @param className
     * @return
     */
    public static Class<?> getClass(@Nullable String className)
    {
        Class<?> cls = null;
        if (className != null)
        {
            try
            {
                cls = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
            }
        }
        return cls;
    }

    /**
     * 获取成员Class
     *
     * @param o
     * @param className
     * @return
     */
    public static Class<?> getMemberClass(@Nullable Object o, @Nullable String className)
    {
        if (o == null | className == null || className.isEmpty())
            return null;

        try
        {
            Class<?> c = o.getClass();
            Field[] fs = c.getDeclaredFields();
            Field field = null;

            for (int i = 0; i < fs.length; i++)
            {
                field = fs[i];
                if (field.getType().toString().equalsIgnoreCase(className))
                {
                    field.setAccessible(true);
                    Object memberObject = field.get(o);
                    Class<?> memberClass = memberObject.getClass();
                    return memberClass;
                }
            }

            return null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 判断是否有对应方法
     *
     * @param c
     * @param method
     * @param args
     * @return
     */
    public static boolean hasMethod(@Nullable Class<?> c, @Nullable String method, @Nullable Class<?>... args)
    {
        if (c == null || method == null || method.isEmpty())
            return false;

        try
        {
            c.getDeclaredMethod(method, args);
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }

        return true;
    }
}
