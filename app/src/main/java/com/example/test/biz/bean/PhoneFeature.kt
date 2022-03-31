package com.example.test.biz.bean

import com.google.gson.annotations.SerializedName

class PhoneFeature {
    //{"PhoneFeatureIndex":0,"PhoneUUID":"3dba1fcf25bed0f4","PhoneMac":"00:00:00:00:00:00"}
    @SerializedName("PhoneFeatureIndex")
    val phoneFeatureIndex = -100

    @SerializedName("PhoneUUID")
    val phoneUUID = ""

    @SerializedName("PhoneMac")
    private val phoneMac = ""
}