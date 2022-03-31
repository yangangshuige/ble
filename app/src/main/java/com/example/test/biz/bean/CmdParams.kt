package com.example.test.biz.bean

import com.google.gson.JsonObject

data class CmdParams(
    val deviceId: String,
    val command: String,
    val params: JsonObject? = null
)