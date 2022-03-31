package com.yg.ble.bluetooth

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import com.yg.ble.BleManager
import com.yg.ble.callback.BleWriteCallback
import com.yg.ble.data.BleMsg
import com.yg.ble.exception.BleException
import com.yg.ble.exception.OtherException
import com.yg.ble.utils.BleLog
import java.util.*
import kotlin.math.roundToInt

class SplitWriter {
    private var mHandlerThread: HandlerThread = HandlerThread("splitWriter")
    private var mHandler: Handler

    private var mBleBluetooth: BleBluetooth? = null
    private var mUuidService: String = ""
    private var mUuidWrite: String = ""
    private var mData: ByteArray = byteArrayOf()
    private var mCount = 0
    private var mSendNextWhenLastSuccess = false
    private var mIntervalBetweenTwoPackage: Long = 0
    private var mCallback: BleWriteCallback? = null
    private var mDataQueue: Queue<ByteArray> = LinkedList()
    private var mTotalNum = 0

    init {
        mHandlerThread.start()
        mHandler = object : Handler(mHandlerThread.looper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                if (msg.what == BleMsg.MSG_SPLIT_WRITE_NEXT) {
                    write()
                }
            }
        }
    }

    fun splitWrite(
        bleBluetooth: BleBluetooth,
        uuid_service: String,
        uuid_write: String,
        data: ByteArray,
        sendNextWhenLastSuccess: Boolean,
        intervalBetweenTwoPackage: Long,
        callback: BleWriteCallback
    ) {
        mBleBluetooth = bleBluetooth
        mUuidService = uuid_service
        mUuidWrite = uuid_write
        mData = data
        mSendNextWhenLastSuccess = sendNextWhenLastSuccess
        mIntervalBetweenTwoPackage = intervalBetweenTwoPackage
        mCount = BleManager.instance.splitWriteNum
        mCallback = callback
        splitWrite()
    }

    private fun splitWrite() {
        require(mCount >= 1) { "split count should higher than 0!" }
        mDataQueue = splitByte(mData, mCount)
        mTotalNum = mDataQueue.size
        write()
    }

    private fun write() {
        if (mDataQueue.peek() == null) {
            release()
            return
        }
        val data = mDataQueue.poll()
        mBleBluetooth?.newBleConnector()
            ?.withUUIDString(mUuidService, mUuidWrite)
            ?.writeCharacteristic(
                data,
                object : BleWriteCallback() {
                    override fun onWriteSuccess(current: Int, total: Int, justWrite: ByteArray) {
                        val position = mTotalNum - mDataQueue.size
                        if (mCallback != null) {
                            mCallback!!.onWriteSuccess(position, mTotalNum, justWrite)
                        }
                        if (mSendNextWhenLastSuccess) {
                            val message =
                                mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT)
                            //                                    mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage);
                            mHandler.sendMessageDelayed(message, 100)
                        }
                    }

                    override fun onWriteFailure(exception: BleException) {
                        if (mCallback != null) {
                            mCallback!!.onWriteFailure(OtherException("exception occur while writing: " + exception.description))
                        }
//                        if (mSendNextWhenLastSuccess) {
//                            val message = mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT);
//                            mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage);
//                        }
                    }
                },
                mUuidWrite)

//        if (!mSendNextWhenLastSuccess) {
//            val message = mHandler.obtainMessage(BleMsg.MSG_SPLIT_WRITE_NEXT);
//            mHandler.sendMessageDelayed(message, mIntervalBetweenTwoPackage);
//        }
    }
    private fun release() {
        mHandlerThread.quit()
        mHandler.removeCallbacksAndMessages(null)
    }
    private fun splitByte(data: ByteArray, count: Int): Queue<ByteArray> {
        if (count > 20) {
            BleLog.w("Be careful: split count beyond 20! Ensure MTU higher than 23!")
        }
        val byteQueue: Queue<ByteArray> = LinkedList()
        val pkgCount: Int = if (data.size % count == 0) {
            data.size / count
        } else {
            (data.size / count + 1).toFloat().roundToInt()
        }
        if (pkgCount > 0) {
            for (i in 0 until pkgCount) {
                var dataPkg: ByteArray
                var j: Int
                if (pkgCount == 1 || i == pkgCount - 1) {
                    j = if (data.size % count == 0) count else data.size % count
                    System.arraycopy(data, i * count, ByteArray(j).also {
                        dataPkg = it
                    }, 0, j)
                } else {
                    System.arraycopy(data, i * count, ByteArray(count).also {
                        dataPkg = it
                    }, 0, count)
                }
                byteQueue.offer(dataPkg)
            }
        }
        return byteQueue
    }
}