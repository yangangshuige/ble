package com.example.test.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken

/**
 * CreatedBy    xiaodong liu(mike)
 * CreateDate   3/31/21
 * Gson解析工具
 */
class JsonUtils private constructor(){
    companion object{
        private val gson: Gson = GsonBuilder().create()

        fun <T> decode(json: String?, targetClass: Class<T>): T? {
            return gson.fromJson(json, targetClass)
        }

        fun <T> decode(json: JsonElement?, targetType: Class<T>): T? {
            return gson.fromJson(json, targetType)
        }

        fun <T> decodeArray(json: String?, targetType: Class<T>): Array<T> {
            val typeToken = TypeToken.getArray(targetType)

            return gson.fromJson(json, typeToken.type) as Array<T>
        }

        fun <T> decodeArray(json: JsonElement?, targetType: Class<T>): Array<T> {
            val typeToken = TypeToken.getArray(targetType)

            return gson.fromJson(json, typeToken.type)
        }

        fun encode(obj: Any?): String {
            return gson.toJson(obj)
        }
    }
}