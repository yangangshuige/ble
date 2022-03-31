package com.example.test.biz.bean

sealed class ConnectState<T>(val data: T?) {
    class ConnectStart<T>(data: T? = null) : ConnectState<T>(data)

    class ConnectFail<T>(data: T?) : ConnectState<T>(data)

    class ConnectSuccess<T>(data: T?) : ConnectState<T>(data)

    class DisConnected<T>(data: T?) : ConnectState<T>(data)

    companion object {
        @JvmStatic
        @JvmOverloads
        fun <T> connectStart(data: T? = null): ConnectState<T> {
            return ConnectStart(data)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> connectFail(data: T?): ConnectState<T> {
            return ConnectFail(data)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> connectSuccess(data: T? = null): ConnectState<T> {
            return ConnectSuccess(data)
        }

        @JvmStatic
        @JvmOverloads
        fun <T> disConnected(data: T? = null): ConnectState<T> {
            return DisConnected(data)
        }
    }
}