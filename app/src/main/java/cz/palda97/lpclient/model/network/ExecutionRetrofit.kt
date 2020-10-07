package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ExecutionRetrofit {

    @GET("resources/executions")
    fun executionList(): Call<ResponseBody>

    @DELETE("resources/executions/{id}")
    fun delete(@Path("id") id: String): Call<ResponseBody>

    @GET("resources/executions/{id}")
    fun execution(@Path("id") id: String): Call<ResponseBody>

    companion object {
        val Retrofit.Builder.executionRetrofit: ExecutionRetrofit
            get() = build().create(ExecutionRetrofit::class.java)
    }
}