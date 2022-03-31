package com.example.test.utils.pb;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;

import java.util.List;

/**
 * @program: iot-codecs
 * @description: pb消息设置
 * @author: zhuangjiesen
 * @create: 2021-04-19 14:10
 */
public class MsgBuilderFieldUtil {

    /**
     * 枚举类型的参数解析
     * @param bizTypeName
     * @param inputMsgBuilder
     * @param fieldDescriptor
     * @param arguments
     * @param paramKey
     * @param isRepeated
     */
    public static boolean setEnumMsgFieldValue(String bizTypeName,
                                            com.google.protobuf.GeneratedMessageV3.Builder inputMsgBuilder,
                                            Descriptors.FieldDescriptor fieldDescriptor,
                                            JSONObject arguments, String paramKey,
                                            boolean isRepeated, boolean isIgnoreNull){
        boolean isValueSet = false;
        //枚举处理
        if (!isRepeated) {
            Integer enumValueObj = arguments.getInteger(paramKey);
            if (!isIgnoreNull && enumValueObj == null) {
                throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
            }
            int enumValue = arguments.getIntValue(paramKey);
            Descriptors.EnumValueDescriptor enumValueDescriptor = fieldDescriptor.getEnumType().findValueByNumber(enumValue);
            inputMsgBuilder.setField(fieldDescriptor, enumValueDescriptor);
            isValueSet = true;
        } else {
            JSONArray jsonArray = null;
            //解不出 JSONArray 的话，可以适配逗号隔开数据
            boolean isJSONArray = true;
            try {
                jsonArray = arguments.getJSONArray(paramKey);
            } catch (Exception e) {
                isJSONArray = false;
            }
            List<Descriptors.EnumValueDescriptor> valueList = Lists.newArrayList();
            if (isJSONArray) {
                if (jsonArray == null || jsonArray.size() == 0) {
                    return false;
                }
                //防止字符串转换问题
                for (int i = 0; i < jsonArray.size(); i++) {
                    Integer enumValueObj = jsonArray.getInteger(i);
                    if (!isIgnoreNull && enumValueObj == null) {
                        throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
                    }
                    Descriptors.EnumValueDescriptor enumValueDescriptor = fieldDescriptor.getEnumType().findValueByNumber((Integer) enumValueObj);
                    valueList.add(enumValueDescriptor);
                }
                inputMsgBuilder.setField(fieldDescriptor, valueList);
                isValueSet = true;
            } else {
                //逗号隔开的数据处理
                String valueStr = arguments.getString(paramKey);
                if (!TextUtils.isEmpty(valueStr)) {
                    String[] valueArr = valueStr.split(",");
                    for (String valueItem : valueArr) {
                        if (!isIgnoreNull && TextUtils.isEmpty(valueItem)) {
                            continue;
                        }
                        Integer enumValue = Integer.valueOf(valueItem);
                        Descriptors.EnumValueDescriptor enumValueDescriptor = fieldDescriptor.getEnumType().findValueByNumber(enumValue);
                        valueList.add(enumValueDescriptor);
                    }
                    inputMsgBuilder.setField(fieldDescriptor, valueList);
                    isValueSet = true;
                } else {
                    throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
                }
            }
        }
        return isValueSet;
    }


    /**
     * 通用类型装换处理器
     *      * 解决3个问题
     *      * 1.非repeated参数，只有一个数据
     *      * 2.repeated 有JsonArray 格式
     *      * 3.repeated 有逗号隔开字符串格式，都适配
     * @param bizTypeName
     * @param inputMsgBuilder
     * @param fieldDescriptor
     * @param fieldType
     * @param arguments
     * @param paramKey
     * @param isRepeated
     * @param isIgnoreNull 是否接受到空值，直接跳过，=false 的话 【允许报错】
     */
    public static boolean setCommonMsgFieldValue(String bizTypeName,
                                              com.google.protobuf.GeneratedMessageV3.Builder inputMsgBuilder,
                                              Descriptors.FieldDescriptor fieldDescriptor,
                                              Descriptors.FieldDescriptor.JavaType fieldType,
                                              JSONObject arguments, String paramKey,
                                              boolean isRepeated, boolean isIgnoreNull){
        Object fieldValue = null;
        //获取数据结果
        if (!isRepeated) {
            //单属性的处理
            Object argValue = null;
            //类型转换
            switch (fieldType) {
                case INT:
                {
                    argValue = arguments.getInteger(paramKey);
                    break;
                }
                case LONG:
                {
                    argValue = arguments.getLong(paramKey);
                    break;
                }
                case FLOAT:
                {
                    argValue = arguments.getFloat(paramKey);
                    break;
                }
                case DOUBLE:
                {
                    argValue = arguments.getDouble(paramKey);
                    break;
                }
                case BOOLEAN:
                {
                    argValue = arguments.getBoolean(paramKey);
                    break;
                }
                case STRING:
                {
                    argValue = arguments.getString(paramKey);
                    if (TextUtils.isEmpty((String) argValue)) {
                        argValue = null;
                    }
                    break;
                }
                case BYTE_STRING:
                {
                    String plainStringValue = arguments.getString(paramKey);
                    if (!TextUtils.isEmpty(plainStringValue)) {
                        argValue = ByteString.copyFrom(plainStringValue.getBytes());
                    }
                    break;
                }
            }
            //设置属性内容
            fieldValue = argValue;
        } else {
            //repeated 标识 => 说明是个数组List
            List valueList = Lists.newArrayList();
            JSONArray jsonArray = null;
            //解不出 JSONArray 的话，可以适配逗号隔开数据
            boolean isJSONArray = true;
            try {
                jsonArray = arguments.getJSONArray(paramKey);
            } catch (Exception e) {
                isJSONArray = false;
            }
            if (isJSONArray) {
                if (jsonArray == null || jsonArray.size() == 0) {
                    return false;
                }
                //防止字符串转换问题
                for (int i = 0; i < jsonArray.size(); i++) {
                    Object itemValue = null;
                    //类型转换
                    switch (fieldType) {
                        case INT:
                        {
                            itemValue = jsonArray.getInteger(i);
                            break;
                        }
                        case LONG:
                        {
                            itemValue = jsonArray.getLong(i);
                            break;
                        }
                        case FLOAT:
                        {
                            itemValue = jsonArray.getFloat(i);
                            break;
                        }
                        case DOUBLE:
                        {
                            itemValue = jsonArray.getDouble(i);
                            break;
                        }
                        case BOOLEAN:
                        {
                            itemValue = jsonArray.getBoolean(i);
                            break;
                        }
                        case STRING:
                        {
                            itemValue = jsonArray.getString(i);
                            if (TextUtils.isEmpty((String) itemValue)) {
                                itemValue = null;
                            }
                            break;
                        }
                        case BYTE_STRING:
                        {
                            String plainItemValue = jsonArray.getString(i);
                            if (!TextUtils.isEmpty(plainItemValue)) {
                                itemValue = ByteString.copyFrom(((String) plainItemValue).getBytes());
                            }
                            break;
                        }
                    }
                    if (!isIgnoreNull && itemValue == null) {
                        throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
                    }
                    valueList.add(itemValue);
                }
            } else {
                //逗号隔开的数据处理
                String valueStr = arguments.getString(paramKey);
                if (!TextUtils.isEmpty(valueStr)) {
                    String[] valueArr = valueStr.split(",");
                    for (String valueItem : valueArr) {
                        Object itemValue = null;
                        //类型转换
                        switch (fieldType) {
                            case INT:
                            {
                                itemValue = Integer.valueOf(valueItem);
                                break;
                            }
                            case LONG:
                            {
                                itemValue = Long.valueOf(valueItem);
                                break;
                            }
                            case FLOAT:
                            {
                                itemValue = Float.valueOf(valueItem);
                                break;
                            }
                            case DOUBLE:
                            {
                                itemValue = Double.valueOf(valueItem);
                                break;
                            }
                            case BOOLEAN:
                            {
                                itemValue = Boolean.valueOf(valueItem);
                                break;
                            }
                            case STRING:
                            {
                                itemValue = valueItem;
                                if (TextUtils.isEmpty((String) itemValue)) {
                                    itemValue = null;
                                }
                                break;
                            }
                            case BYTE_STRING:
                            {
                                String plainItemValue = valueItem;
                                if (!TextUtils.isEmpty(plainItemValue)) {
                                    itemValue = ByteString.copyFrom(((String) plainItemValue).getBytes());
                                }
                                break;
                            }
                        }
                        if (!isIgnoreNull && itemValue == null) {
                            throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
                        }
                        valueList.add(itemValue);
                    }
                } else {
                    throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
                }
            }
            if (valueList == null || valueList.size() == 0) {
                throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
            }
            //赋值数据
            fieldValue = valueList;
        }
        if (!isIgnoreNull && fieldValue == null) {
            throw new RuntimeException(String.format("null param value of bizTypeName:%s, key:%s", bizTypeName, paramKey));
        }
        //设置参数数据
        if (fieldValue != null) {
            inputMsgBuilder.setField(fieldDescriptor, fieldValue);
            return true;
        }
        return false;
    }

}
