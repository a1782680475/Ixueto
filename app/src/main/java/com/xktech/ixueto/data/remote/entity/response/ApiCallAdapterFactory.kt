package com.xktech.ixueto.data.remote.entity.response

import okhttp3.Request
import okio.Timeout
import retrofit2.*
import retrofit2.Response
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ApiCallAdapterFactory : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        check(getRawType(returnType) == Call::class.java) { "$returnType must be retrofit2.Call." }
        check(returnType is ParameterizedType) { "$returnType must be parameterized. Raw types are not supported" }

        val apiResultType = getParameterUpperBound(0, returnType)
        check(getRawType(apiResultType) == ApiResult::class.java) { "$apiResultType must be ApiResult." }
        check(apiResultType is ParameterizedType) { "$apiResultType must be parameterized. Raw types are not supported" }

        val dataType = getParameterUpperBound(0, apiResultType)
        return ApiResultCallAdapter<Any>(dataType)
    }
}
class ApiResultCallAdapter<T>(private val type: Type) : CallAdapter<T, Call<ApiResult<T>>> {
    override fun responseType(): Type = type

    override fun adapt(call: Call<T>): Call<ApiResult<T>> {
        return ApiResultCall(call)
    }
}

class ApiResultCall<T>(private val delegate: Call<T>) : Call<ApiResult<T>> {

    override fun enqueue(callback: Callback<ApiResult<T>>) {
        delegate.enqueue(object : Callback<T> {

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    val apiResult = if (response.body() == null) {
                        ApiResult.Failure(ApiError.emptyData.errorCode, ApiError.emptyData.errorMsg)
                    } else {
                        ApiResult.Success(response.body()!!)
                    }
                    callback.onResponse(this@ApiResultCall, Response.success(apiResult))
                } else {
                    val failureApiResult = ApiResult.Failure(response.code(), response.message())
                    callback.onResponse(this@ApiResultCall, Response.success(failureApiResult))
                }

            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                //Interceptor里会通过throw ApiException 来直接结束请求 同时ApiException里会包含错误信息
                val failureApiResult = if (t is ApiException) {
                    ApiResult.Failure(t.errorCode, t.errorMessage)
                } else {
                    ApiResult.Failure(ApiError.netError.errorCode, ApiError.netError.errorMsg)
                }
                callback.onResponse(this@ApiResultCall, Response.success(failureApiResult))
            }
        })
    }

    override fun clone(): Call<ApiResult<T>> = ApiResultCall(delegate.clone())

    override fun execute(): Response<ApiResult<T>> {
        throw UnsupportedOperationException("ApiResultCall does not support synchronous execution")
    }

    override fun isExecuted(): Boolean {
        return delegate.isExecuted
    }

    override fun cancel() {
        delegate.cancel()
    }

    override fun isCanceled(): Boolean {
        return delegate.isCanceled
    }

    override fun request(): Request {
        return delegate.request()
    }

    override fun timeout(): Timeout {
        return delegate.timeout()
    }
}