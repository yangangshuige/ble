package com.example.test.biz.bean

import org.json.JSONObject
import java.io.Serializable
import java.util.*

class DeviceActions : Serializable {
    private val actionInfoList: List<Any> = emptyList()

    fun getTargetJson(action: String): JSONObject {
        var targetJson = JSONObject()
        actionInfoList.forEach {
            val obj = it as Map<*, *>
            val jsonObject = JSONObject(obj)
            if (Objects.equals(jsonObject.optString("btnName"), action)) {
                targetJson = jsonObject
                return@forEach
            }
        }
        return targetJson
    }

    fun hasCommandTarget(cmd: String): Boolean {
        for (params in actionInfoList) {
            val jsonObject = JSONObject(params as Map<*, *>)
            if (Objects.equals(jsonObject.optString("btnName"), cmd)) {
                return true
            }
        }
        return false
    }
}