package com.example.test.biz.bean

import com.google.gson.annotations.SerializedName

class BleReceiverData {
    @SerializedName("Data")
    val data: ReceiverData = ReceiverData()

    @SerializedName("Name")
    val name = ""

    @SerializedName("PbVersion")
    val pbVersion = ""

    @SerializedName("ReplySeqId")
    val replySeqId: Int = 0

    @SerializedName("SeqId")
    val seqId: Int = 0

    @SerializedName("Time")
    val time: String = ""

    override fun toString(): String {
        return "BleReceiverData(data=$data, name='$name', pbVersion='$pbVersion', replySeqId=$replySeqId, seqId=$seqId, time='$time')"
    }

}

class ReceiverData {
    @SerializedName("Output")
    val output: Output = Output()

    @SerializedName("TrackId")
    val trackId: Int = 0

    @SerializedName("ACC")
    val aCC = ""

    @SerializedName("SecurityState")
    val securityState = ""

    @SerializedName("BikeSetSource")
    val bikeSetSource = ""

    @SerializedName("GT")
    val gT = ""

    @SerializedName("TriggerType")
    val triggerType = ""

    @SerializedName("BatEventType")
    val batEventType = ""

    @SerializedName("BatteryMv")
    val batteryMv: Int = 0

    @SerializedName("BatterySoc")
    val batterySoc: Int = 0

    override fun toString(): String {
        return "ReceiverData(output=$output, trackId=$trackId, aCC='$aCC', securityState='$securityState', bikeSetSource='$bikeSetSource', gT='$gT', triggerType='$triggerType', batEventType='$batEventType', batteryMv=$batteryMv, batterySoc=$batterySoc)"
    }

}

class Output {
    @SerializedName("ACC")
    val aCC = ""

    @SerializedName("BmsFault")
    val bmsFault: List<Any> = listOf()

    @SerializedName("ECUFault")
    val eCUFault: List<Any> = listOf()

    @SerializedName("MCFault")
    val mCFault: List<Any> = listOf()

    @SerializedName("ACCSetResult")
    val aCCSetResult = ""

    @SerializedName("SSResult")
    val sSResult = ""

    @SerializedName("BikeSetSource")
    val bikeSetSource = ""

    @SerializedName("BlePairingCodeSetResult")
    val blePairingCodeSetResult = ""

    @SerializedName("DPFR")
    val dPFR = ""

    @SerializedName("UpdateActionResult")
    val updateActionResult = ""

    @SerializedName("WriteFirmDataResult")
    val writeFirmDataResult = ""

    override fun toString(): String {
        return "Output(aCC='$aCC', bmsFault=$bmsFault, eCUFault=$eCUFault, mCFault=$mCFault, aCCSetResult='$aCCSetResult', sSResult='$sSResult', bikeSetSource='$bikeSetSource', blePairingCodeSetResult='$blePairingCodeSetResult', dPFR='$dPFR', updateActionResult='$updateActionResult', writeFirmDataResult='$writeFirmDataResult')"
    }


}
