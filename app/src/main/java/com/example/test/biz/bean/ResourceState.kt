package com.example.test.biz.bean

sealed class ResourceState<T>(val data: T?) {
    class Loading<T>(data: T? = null) : ResourceState<T>(data)
    class Success<T>(data: T, code: Int = 0, message: String? = null) : ResourceState<T>(data)
    class Error<T>(data: T? = null, code: Int, message: String? = null) :
        ResourceState<T>(data)

    companion object {
        @JvmOverloads
        @JvmStatic
        fun <T> loading(data: T? = null): ResourceState<T> {
            return Loading(data)
        }

        @JvmOverloads
        @JvmStatic
        fun <T> success(data: T, code: Int = 0, message: String? = null): ResourceState<T> {
            return Success(data, code, message)
        }

        @JvmOverloads
        @JvmStatic
        fun <T> error(data: T? = null, code: Int, message: String? = null): ResourceState<T> {
            return Error(data, code, message)
        }
    }
}