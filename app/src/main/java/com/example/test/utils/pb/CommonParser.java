package com.example.test.utils.pb;


import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.test.biz.bean.PolarisBleAlarm;
import com.google.protobuf.Descriptors;
import com.google.protobuf.util.JsonFormat;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: iot-codecs
 * @description:
 * @author: zhuangjiesen
 * @create: 2020-10-28 18:12
 */
public class CommonParser {

    /** 方法对应的解析类 **/
    private final static Map<String, Method> nameStructAndParser = new ConcurrentHashMap<>();
    /** 参数key 对应index **/
    private final static Map<String, Integer> paramKeyAndParamIndex = new ConcurrentHashMap<>();
    /** 参数的index **/
    private final static List<Integer> allParamKeyIndexList = new ArrayList<>();

    private final static ThreadLocal<JsonFormat.Printer> localJSONPrinter = new ThreadLocal<>();

    static  {
        initParamKeyIndex();
        initDecodeMethod();
    }

    /**
     * 初始化json打印工具
     */
    public static void initJSONPrinter(){
        JsonFormat.Printer printer = JsonFormat.printer();
        //枚举显示int
//        printer = printer.printingEnumsAsInts();
        //去掉空格
        printer = printer.omittingInsignificantWhitespace();
        //默认值显示
        printer = printer.includingDefaultValueFields();
        localJSONPrinter.set(printer);
    }

    public static JsonFormat.Printer getLocalJSONPrinter(){
        //double-check
        JsonFormat.Printer localPrinter = localJSONPrinter.get();
        if (localPrinter == null) {
            initJSONPrinter();
        }
        return localJSONPrinter.get();
    }

    /**
     * Key对应的index解析
     */
    public static void initParamKeyIndex(){
        List<Descriptors.FieldDescriptor> fieldDescriptorList = PolarisBleAlarm.ParamSet.InputMsg.getDescriptor().getFields();
        if (!fieldDescriptorList.isEmpty()) {
            for (Descriptors.FieldDescriptor fieldDescriptor : fieldDescriptorList) {
                String paramKey = fieldDescriptor.getName();
                int paramIndex = fieldDescriptor.getIndex() + 1;
//                System.out.println(paramKey);
                paramKeyAndParamIndex.put(paramKey, paramIndex);
                //参数的paramIndex 的设置
                allParamKeyIndexList.add(paramIndex);
            }
        }
    }

    /**
     * 编码方法初始化
     */
    public static void initDecodeMethod(){
        try {
            if (nameStructAndParser.isEmpty()) {
                for (PolarisBleAlarm.Msg.NameStruct value : PolarisBleAlarm.Msg.NameStruct.values()) {
                    String clazzName = PolarisBleAlarm.class.getName() + "$" + value.name();
                    Class clazz = null;
                    try {
                        clazz = Class.forName(clazzName);
                    } catch (Exception e) {
                    }
                    if (clazz == null) {
                        continue;
                    }
                    Method method = clazz.getDeclaredMethod("parseFrom", byte[].class);
//                    System.out.println(method.getDeclaringClass().getSimpleName() + "." + method.getName());
                    nameStructAndParser.put(value.name(), method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static com.google.protobuf.GeneratedMessageV3.Builder getBuilder(String actionType) throws Exception {
        String clazzName = PolarisBleAlarm.class.getName() + "$" + actionType;
        Class clazz = Class.forName(clazzName);
        Method newBuilderMethod = clazz.getDeclaredMethod("newBuilder", null);
        Object result = newBuilderMethod.invoke(null);
        if (result != null) {
            return (com.google.protobuf.GeneratedMessageV3.Builder)result;
        }
        return null;
    }


    public static com.google.protobuf.GeneratedMessageV3.Builder getInputMsgBuilder(String actionType) throws Exception {
        String clazzName = PolarisBleAlarm.class.getName() + "$" + actionType + "$InputMsg";
        Class clazz = Class.forName(clazzName);
        Method newBuilderMethod = clazz.getDeclaredMethod("newBuilder", null);
        Object result = newBuilderMethod.invoke(null);
        if (result != null) {
            return (com.google.protobuf.GeneratedMessageV3.Builder)result;
        }
        return null;
    }

    public static Descriptors.Descriptor getInputDescriptors(String actionType) throws Exception {
        String clazzName = PolarisBleAlarm.class.getName() + "$" + actionType + "$InputMsg";
        Class clazz = Class.forName(clazzName);
        Method getDescriptorMethod = clazz.getDeclaredMethod("getDescriptor", null);
        Object result = getDescriptorMethod.invoke(null);
        if (result != null) {
            return (Descriptors.Descriptor)result;
        }
        return null;
    }

    public static Descriptors.Descriptor getDescriptors(String actionType) throws Exception {
        String clazzName = PolarisBleAlarm.class.getName() + "$" + actionType;
        Class clazz = Class.forName(clazzName);
        Method getDescriptorMethod = clazz.getDeclaredMethod("getDescriptor", null);
        Object result = getDescriptorMethod.invoke(null);
        if (result != null) {
            return (Descriptors.Descriptor)result;
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static int getParamIndexByKey(String key) {
        return paramKeyAndParamIndex.getOrDefault(key, 0);
    }

    public static Method getNameStructAndParser(String key) {
        return nameStructAndParser.get(key);
    }

    public static List<Integer> getAllParamKeyIndexList() {
        return allParamKeyIndexList;
    }

}
