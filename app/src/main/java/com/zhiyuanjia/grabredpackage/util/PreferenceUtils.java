package com.zhiyuanjia.grabredpackage.util;

import android.content.Context;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PreferenceUtils {


    public static void remove(Context context, String key) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().remove(key).commit();
    }

    public static void clear(Context context) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().clear().commit();
    }

    public static boolean hasKey(Context context, String key) {
        return context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).contains(key);
    }


    // ------------------------------------------------
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).getBoolean(key, defaultValue);
    }

    public static float getFloat(Context context, String key, float defaultValue) {
        return context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).getFloat(key, defaultValue);
    }

    public static int getInt(Context context, String key, int defaultValue) {
        return context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).getInt(key, defaultValue);
    }

    public static long getLong(Context context, String key, long defaultValue) {
        return context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).getLong(key, defaultValue);
    }

    public static String getString(Context context, String key, String defaultValue) {
        return context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).getString(key, defaultValue);
    }


    // ------------------------------------------------
    public static void setBoolean(Context context, String key, boolean value) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().putBoolean(key, value).commit();
    }

    public static void setFloat(Context context, String key, float value) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().putFloat(key, value).commit();
    }

    public static void setLong(Context context, String key, long value) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().putLong(key, value).commit();
    }

    public static void setInt(Context context, String key, int value) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().putInt(key, value).commit();
    }

    public static void setString(Context context, String key, String value) {
        context.getSharedPreferences("data", Context.BIND_NOT_FOREGROUND).edit().putString(key, value).commit();
    }


    /**
     * -- 存储数据
     *
     * @param context
     * @param key
     * @param object
     */
    public static void setObject(Context context, String key, Object object) {

        if (object == null)
            object = "";
        // 实例化一个ByteArrayOutputStream对象，用来装载压缩后的字节文件。
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // 然后将得到的字符数据装载到ObjectOutputStream
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // writeObject 方法负责写入特定类的对象的状态，以便相应的 readObject 方法可以还原它
            objectOutputStream.writeObject(object);
            // 最后，用Base64.encode将字节文件转换成Base64编码保存在String中
            String ListString = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
            setString(context, key, ListString);
            // 关闭objectOutputStream
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * -- 获取泛型对象
     *
     * @param context
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getObject(Context context, String key, Class<T> clazz) {
        String entityString = getString(context, key, "");
        byte[] mobileBytes = Base64.decode(entityString.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
        ObjectInputStream objectInputStream;
        try {
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object obj = objectInputStream.readObject();
            objectInputStream.close();
            if (null == obj || obj.toString().equals("")) {
                return null;
            }
            Map<String, Object> maps = new HashMap<>();
            T dataBean;
            Class<?> cls = obj.getClass();
            dataBean = clazz.newInstance();
            Field[] fields = cls.getDeclaredFields();
            Field[] beanFields = clazz.getDeclaredFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                String strGet = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length());
                Method methodGet = null;
                try {
                    methodGet = cls.getDeclaredMethod(strGet);
                } catch (Exception e) {
                    //System.err.println(String.format("%s对象反射方法%s()错误，取消属性赋值", cls.getName(), strGet));
                    //e.printStackTrace();
                    continue;
                }
                Object object = methodGet.invoke(obj);
                maps.put(fieldName, object == null ? "" : object);
            }
            for (Field field : beanFields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                Class<?> fieldType = field.getType();
                String fieldValue = maps.get(fieldName) == null ? null : maps.get(fieldName).toString();
                if (fieldValue != null) {
                    if (String.class.equals(fieldType))
                        field.set(dataBean, fieldValue);
                    else if (byte.class.equals(fieldType))
                        field.setByte(dataBean, Byte.parseByte(fieldValue));
                    else if (Byte.class.equals(fieldType))
                        field.set(dataBean, Byte.valueOf(fieldValue));
                    else if (boolean.class.equals(fieldType))
                        field.setBoolean(dataBean, Boolean.parseBoolean(fieldValue));
                    else if (Boolean.class.equals(fieldType))
                        field.set(dataBean, Boolean.valueOf(fieldValue));
                    else if (short.class.equals(fieldType))
                        field.setShort(dataBean, Short.parseShort(fieldValue));
                    else if (Short.class.equals(fieldType))
                        field.set(dataBean, Short.valueOf(fieldValue));
                    else if (int.class.equals(fieldType))
                        field.setInt(dataBean, Integer.parseInt(fieldValue));
                    else if (Integer.class.equals(fieldType))
                        field.set(dataBean, Integer.valueOf(fieldValue));
                    else if (long.class.equals(fieldType))
                        field.setLong(dataBean, Long.parseLong(fieldValue));
                    else if (Long.class.equals(fieldType))
                        field.set(dataBean, Long.valueOf(fieldValue));
                    else if (float.class.equals(fieldType))
                        field.setFloat(dataBean, Float.parseFloat(fieldValue));
                    else if (Float.class.equals(fieldType))
                        field.set(dataBean, Float.valueOf(fieldValue));
                    else if (double.class.equals(fieldType))
                        field.setDouble(dataBean, Double.parseDouble(fieldValue));
                    else if (Double.class.equals(fieldType))
                        field.set(dataBean, Double.valueOf(fieldValue));
                    else if (Date.class.equals(fieldType)) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
                        field.set(dataBean, sdf.parse(fieldValue));
                    }
                }
            }
            return dataBean;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * -- 获取泛型集合
     *
     * @param context
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getArray(Context context, String key, Class<T> clazz) {
        String listString = getString(context, key, "");
        if (!listString.equals("")) {
            byte[] mobileBytes = Base64.decode(listString.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(mobileBytes);
            ObjectInputStream objectInputStream = null;
            try {
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                List<?> weatherList = (List<?>) objectInputStream.readObject();
                objectInputStream.close();
                return new ArrayList<>(Arrays.asList(weatherList.toArray((T[]) Array.newInstance(clazz, weatherList.size()))));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
}