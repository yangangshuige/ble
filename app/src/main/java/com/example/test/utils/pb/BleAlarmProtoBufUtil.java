package com.example.test.utils.pb;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.test.biz.bean.PolarisBleAlarm;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙报警器 协议编解码工具
 */
public class BleAlarmProtoBufUtil {

    public static int seqId = 10001;
    public static final String[] actionNeedToAck = {
            "UpdateFinish",
            "BlePairingFinish",
            "PowerBatteryEvent",
            "BatteryLockChange",
            "ACCChange",
            "SecurityChange",
            "BikeReFactoryFinish",
            "BikeWarningEvent",
            "PowerBatteryFault",
            "ECUFaultEvent",
            "MCFaultEvent"
    };

    public static String decrypt(byte[] input){
        try {
            PolarisBleAlarm.Msg msg =PolarisBleAlarm.Msg.parseFrom(input);

            JSONObject msgJson =JSONObject.parseObject(CommonParser.getLocalJSONPrinter().print(msg));

            PolarisBleAlarm.Msg.NameStruct nameStruct = msg.getName();
            Method method = CommonParser.getNameStructAndParser(nameStruct.name());
            if (method == null) {
                return "not method found : " + nameStruct.name();
            }
            Object result = method.invoke(null, msg.getData().toByteArray());
            String dataJson =CommonParser.getLocalJSONPrinter().print((MessageOrBuilder) result);
            msgJson.put("Data", JSONObject.parseObject(dataJson));
            return msgJson.toString();

        } catch (InvalidProtocolBufferException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static byte[] encrypt(String actionJson){

        try{
            JSONObject action = (JSONObject) JSONObject.parse(actionJson);
            String actionName =action.getJSONObject("actionsInfo").getJSONArray("ble").getString(0);

            JSONObject argsObj =action.getJSONObject("actionsArgs");
            com.google.protobuf.ByteString data=dataPackage(actionName, argsObj);

            PolarisBleAlarm.Msg.Builder msgBuilder = PolarisBleAlarm.Msg.newBuilder();
            msgBuilder.setSeqId(seqId++);
            msgBuilder.setTime(System.currentTimeMillis());
            msgBuilder.setPbVersion("0.01");
            msgBuilder.setData(data);
            msgBuilder.setName(PolarisBleAlarm.Msg.NameStruct.valueOf(actionName));

            byte[] msg =msgBuilder.build().toByteArray();
            return msg;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static com.google.protobuf.ByteString dataPackage(String actionName, JSONObject argsObj) throws Exception{
        Descriptors.Descriptor inputDescriptor = CommonParser.getInputDescriptors(actionName);
        com.google.protobuf.GeneratedMessageV3.Builder inputMsgBuilder = CommonParser.getInputMsgBuilder(actionName);

        Descriptors.Descriptor actionDescriptor = CommonParser.getDescriptors(actionName);
        Descriptors.FieldDescriptor inputMsgDescriptor = actionDescriptor.findFieldByName("Input");
        if (inputMsgDescriptor == null) {
            return null;
        }

        switch (actionName){

            case "ParamSet":{
                Descriptors.FieldDescriptor paramIndexDescriptor = inputDescriptor.findFieldByName("ParamIndex");
                for (String key : argsObj.keySet()) {
                    Descriptors.FieldDescriptor fieldDescriptor = inputDescriptor.findFieldByName(key);
                    if (fieldDescriptor != null) {
                        boolean isValueSet;
                        if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                            //枚举处理
                            isValueSet = MsgBuilderFieldUtil.setEnumMsgFieldValue(actionName, inputMsgBuilder,
                                    fieldDescriptor, argsObj, key, fieldDescriptor.isRepeated(), true);
                        } else {
                            //其他基本类型
                            Descriptors.FieldDescriptor.JavaType fieldType = fieldDescriptor.getJavaType();
                            isValueSet = MsgBuilderFieldUtil.setCommonMsgFieldValue(actionName, inputMsgBuilder,
                                    fieldDescriptor, fieldType, argsObj, key, fieldDescriptor.isRepeated(), true);
                        }
                        //设置参数才需要加枚举
                        if (isValueSet) {
                            int fieldIndex = fieldDescriptor.getIndex() + 1;
                            inputMsgBuilder.addRepeatedField(paramIndexDescriptor, fieldIndex);
                        }
                    }
                }
                break;
            }

            case "ParamGet":{
                Set<String> keys = new HashSet<>();
                Object keysObj = argsObj.get("keys");
                if (keysObj != null) {
                    if (keysObj instanceof Set) {
                        keys = (Set<String>) keysObj;
                    } else {
                        keys = new HashSet((List<String>)keysObj);
                    }
                }

                Descriptors.FieldDescriptor paramIndexDescriptor = inputDescriptor.findFieldByName("ParamIndex");
                if (paramIndexDescriptor != null) {
                    if (keys.isEmpty()) {
                        //全量参数查询
                        for (Integer paramIndex : CommonParser.getAllParamKeyIndexList()) {
                            if (paramIndex != null) {
                                inputMsgBuilder.addRepeatedField(paramIndexDescriptor, paramIndex);
                            }
                        }
                    } else {
                        for (String key : keys) {
                            int paramIndex = CommonParser.getParamIndexByKey(key);
                            inputMsgBuilder.addRepeatedField(paramIndexDescriptor, paramIndex);
                        }
                    }
                }
                break;
            }

            case "WriteFirmData":{
                Long dataLen =argsObj.getLongValue("FirmwareDataRemainingLength");
                JSONArray dataArray =argsObj.getJSONArray("FirmwareData");

                byte[] dataBuff =new byte[dataArray.size()];
                for(int i=0; i<dataArray.size(); i++){
                    dataBuff[i] =dataArray.getByte(i);
                }

                inputMsgBuilder.setField(PolarisBleAlarm.WriteFirmData.InputMsg.getDescriptor().findFieldByName("FirmwareDataRemainingLength"), dataLen);
                inputMsgBuilder.setField(PolarisBleAlarm.WriteFirmData.InputMsg.getDescriptor().findFieldByName("FirmwareData"), ByteString.copyFrom(dataBuff));
                break;
            }

            default:{
                for(String key: argsObj.keySet()){
                    Descriptors.FieldDescriptor fieldDescriptor = inputDescriptor.findFieldByName(key);
                    if (fieldDescriptor != null) {
                        if (fieldDescriptor.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
                            //枚举处理
                            MsgBuilderFieldUtil.setEnumMsgFieldValue(actionName, inputMsgBuilder,
                                    fieldDescriptor, argsObj, key, fieldDescriptor.isRepeated(), true);
                        } else {
                            //其他基本类型
                            Descriptors.FieldDescriptor.JavaType fieldType = fieldDescriptor.getJavaType();
                            MsgBuilderFieldUtil.setCommonMsgFieldValue(actionName, inputMsgBuilder,
                                    fieldDescriptor, fieldType, argsObj, key, fieldDescriptor.isRepeated(), true);
                        }
                    }
                }
                break;
            }
        }

        com.google.protobuf.GeneratedMessageV3.Builder actionBuilder = CommonParser.getBuilder(actionName);
        actionBuilder.setField(inputMsgDescriptor, inputMsgBuilder.build());
        return actionBuilder.build().toByteString();
    }
}
