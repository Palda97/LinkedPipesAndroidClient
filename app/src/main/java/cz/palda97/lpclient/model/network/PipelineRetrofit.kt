package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface PipelineRetrofit {
    @GET("resources/pipelines")
    fun pipelineList(): Call<ResponseBody>

    @DELETE("resources/pipelines/{id}")
    fun deletePipeline(@Path("id") id: String): Call<ResponseBody>

    companion object {
        fun getInstance(baseUrl: String): PipelineRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .build()
            .create(PipelineRetrofit::class.java)
    }
}