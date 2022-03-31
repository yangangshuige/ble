package com.example.test.biz.bean

import com.example.test.utils.JsonUtils
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.json.JSONException
import org.json.JSONObject

class RemoteCommand {
    companion object {
        const val CMD_SWITCH = "开关锁"
        const val CMD_ANFANG = "安防模式设置"
        const val CMD_WHISTLE = "寻车/响铃"
        const val CMD_GET_BATTERY_INFO = "电池信息获取"
        const val CMD_OPEN_CUSHION_LOCK = "开坐垫锁"
        const val CMD_ENABLE_LOCATION = "参数设置"
        const val CMD_SET_VOLUME = "参数设置"

        /** 查询蓝牙配对码指令 */
        const val CMD_BLUETOOTH_KEY = "手机钥匙"

        /** 蓝牙解绑 */
        const val CMD_DELETE_BLUETOOTH_KEY = "删除手机钥匙"
        const val CMD_HEART = "心跳查询"
        const val RET_HEART = "HBGet"

        /** 恢复出厂设置 */
        const val CMD_RECOVERY = "出厂设置"
        const val CMD_GET_DEVICE = "查询主机设备信息"
        const val RET_GET_DEVICE = "GetDeviceInfo"
        const val CMD_SET_RTC = "设置设备RTC"
        const val RET_SET_RTC = "DeviceRTCSet"
        const val CMD_UPDATE_REQUEST = "蓝牙信道请求设备更新固件"
        const val CMD_UPDATE_WRITE_FIRM_DATA = "写入固件数据"

        private const val KEY_CMD_ARGS = "actionsArgs"

        private const val model = "{\n" +
                "  \"actionInfoList\": [\n" +
                "    {\n" +
                "      \"btnName\": \"开关锁\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"ACC\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"ACCSet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"参数设置\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"LeadAcidFaultThreshold\": \"\",\n" +
                "        \"FASSET\": \"\",\n" +
                "        \"BleSignalStrengh\": \"\",\n" +
                "        \"KeyLockFirstEnable\": \"\",\n" +
                "        \"Read485ToMCTimesOut\": \"\",\n" +
                "        \"Read485Fre\": \"\",\n" +
                "        \"BatteryLockEnableMv\": \"\",\n" +
                "        \"BroadcastInterval\": \"\",\n" +
                "        \"NTSSDT\": \"\",\n" +
                "        \"LiBatFaultThreshold\": \"\",\n" +
                "        \"BleName\": \"\",\n" +
                "        \"NTOKST\": \"\",\n" +
                "        \"SaddleSensorStart\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"ParamSet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"参数查询\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"keys\": [\n" +
                "          \"BroadcastInterval\",\n" +
                "          \"BleName\",\n" +
                "          \"BleSignalStrengh\",\n" +
                "          \"LiBatFaultThreshold\",\n" +
                "          \"LeadAcidFaultThreshold\",\n" +
                "          \"Read485Fre\",\n" +
                "          \"Read485ToMCTimesOut\",\n" +
                "          \"KeyLockFirstEnable\",\n" +
                "          \"BatteryLockEnableMv\",\n" +
                "          \"SaddleSensorStart\",\n" +
                "          \"NTSSDT\",\n" +
                "          \"FASSET\",\n" +
                "          \"NTOKST\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"ParamGet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"获取硬件描述信息\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"HDGet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"设置硬件描述信息\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"WheelDiameter\": \"\",\n" +
                "        \"Ppairs\": \"\",\n" +
                "        \"BikeHeadLockEnable\": \"\",\n" +
                "        \"BMSEnable\": \"\",\n" +
                "        \"IBEnable\": \"\",\n" +
                "        \"BatLockEnable\": \"\",\n" +
                "        \"PbCapacity\": \"\",\n" +
                "        \"PreIKSenable\": \"\",\n" +
                "        \"BrakeEnable\": \"\",\n" +
                "        \"GsensorConfig\": \"\",\n" +
                "        \"PbMv\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"HDSet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"设备重启\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"DeviceRestart\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"查询主机设备信息\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"GetDeviceInfo\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"设置设备RTC\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"RTC\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"DeviceRTCSet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"开电池锁\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"BatteryLock\": \"1\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"BatteryLockSet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"寻车/响铃\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"FindBike\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"安防模式设置\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"SecurityState\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"SecuritySet\"\n" +
                "        ]\n" +
                "      },\n" +
                "      \"actionsArgsSchema\": {\n" +
                "        \"tcp.SecuritySet\": {\n" +
                "          \"SecurityState\": {\n" +
                "            \"values\": {\n" +
                "              \"0\": \"安防启动\",\n" +
                "              \"1\": \"安防关闭\"\n" +
                "            },\n" +
                "            \"type\": \"byte\",\n" +
                "            \"bindKey\": \"securityStatus\",\n" +
                "            \"required\": true,\n" +
                "            \"desc\": \"设置安防方法: 设防/撤防\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"电池信息获取\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"BatteryGet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"心跳查询\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"HBGet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"出厂设置\",\n" +
                "      \"actionsArgs\": {},\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"BikeReFactory\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"手机钥匙\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"PhoneUUID\": \"\",\n" +
                "        \"BlePairingCode\": \"\",\n" +
                "        \"BlePT\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"BlePairingCodeSet\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"删除手机钥匙\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"PhoneFeatureIndex\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"DeletePhoneFeature\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"蓝牙信道请求设备更新固件\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"TargetVersion\": \"\",\n" +
                "        \"FirmwareMd5\": \"\",\n" +
                "        \"FirmwareSize\": \"\",\n" +
                "        \"UpdateType\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"UpdateByBle\"\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"btnName\": \"写入固件数据\",\n" +
                "      \"actionsArgs\": {\n" +
                "        \"FirmwareDataRemainingLength\": \"\",\n" +
                "        \"FirmwareData\": \"\"\n" +
                "      },\n" +
                "      \"actionsInfo\": {\n" +
                "        \"ble\": [\n" +
                "          \"WriteFirmData\"\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}"

        fun deviceActions(): DeviceActions? {
            return JsonUtils.decode(model, DeviceActions::class.java)
        }

        /**
         * 读取指令参数
         * @param params 指令属性
         * @return 指令的执行参数
         */
        fun readArguments(params: JSONObject): JsonObject {
            val json = try {
                params.getJSONObject(KEY_CMD_ARGS).toString()
            } catch (e: JSONException) {
                JsonUtils.encode(Any())
            }

            return JsonUtils.decode(json, JsonObject::class.java) ?: JsonObject()
        }
    }

    class CmdParamsBuilder(params: JSONObject) {
        private val params: JsonObject =
            JsonUtils.decode(params.toString(), JsonObject::class.java) ?: JsonObject()

        private val args = readArguments(params)

        private fun updateArguments() {
            params.add(KEY_CMD_ARGS, args)
        }

        fun addArgument(key: String, value: Any): CmdParamsBuilder {
            val v = JsonUtils.decode(JsonUtils.encode(value), JsonElement::class.java)
            args.add(key, v)

            return this
        }

        fun removeArgument(key: String): CmdParamsBuilder {
            args.remove(key)

            return this
        }

        fun build(): String {
            updateArguments()

            return JsonUtils.encode(params)
        }

        fun buildJsonParams(): JsonObject {
            updateArguments()

            return params
        }
    }
}