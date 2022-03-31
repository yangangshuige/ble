package com.example.test.biz.bean

sealed class ScanState<T>(val data: T?) {
    class ScanStart<T>(data: T? = null) : ScanState<T>(data)

    class Scanning<T>(data: T?) : ScanState<T>(data)

    class ScanFinish<T>(data: T? = null) : ScanState<T>(data)

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> scanStart(data: T? = null): ScanState<T> {
            return ScanStart(data)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> scanning(data: T?): ScanState<T> {
            return Scanning(data)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> scanFinish(data: T? = null): ScanState<T> {
            return ScanFinish(data)
        }
    }
}