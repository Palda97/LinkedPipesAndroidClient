package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

interface ExecutionRetrofit {

    @GET("resources/executions")
    fun executionList(): Call<ResponseBody>

    companion object {
        fun getInstance(baseUrl: String): ExecutionRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()
            .create(ExecutionRetrofit::class.java)
    }
}