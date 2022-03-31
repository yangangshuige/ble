package com.example.test.utils.pb

object PbString {
    const val ACC_SET_RESULT = "ACCSetResult"
    const val ACC_OPEN_SUCCESS = "ACCOpenSuccess"
    const val ACC_Open_Already = "ACCOpenAlready"
}

enum class ActionName(value: String) {
    ACCSet("ACCSet"),
    SecuritySet("SecuritySet"),
    BlePairingCodeSet("BlePairingCodeSet"),
    HDGet("HDGet"),
    HBGet("HBGet"),
    ParamGet("ParamGet"),
    GetDeviceInfo("GetDeviceInfo"),
    BatteryGet("BatteryGet"),
    DeletePhoneFeature("DeletePhoneFeature"),
    UpdateByBle("UpdateByBle"),
    WriteFirmData("WriteFirmData"),
    HDSet("HDSet"),
    DeviceRestart("DeviceRestart"),
    DeviceRTCSet("DeviceRTCSet"),
    BatteryLockSet("BatteryLockSet"),
    BikeReFactory("BikeReFactory"),
    FindBike("FindBike")
}
