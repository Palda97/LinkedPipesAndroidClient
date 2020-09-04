package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET

interface PipelineRetrofit {
    @GET("resources/pipelines")
    fun pipelineList(): Call<ResponseBody>

    companion object {
        fun getInstance(baseUrl: String): PipelineRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()
            .create(PipelineRetrofit::class.java)
    }
}