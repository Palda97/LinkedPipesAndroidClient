package cz.palda97.lpclient.model.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface PipelineRetrofit {
    @GET("resources/pipelines")
    fun pipelineList(): Call<ResponseBody>

    @DELETE("resources/pipelines/{id}")
    fun deletePipeline(@Path("id") id: String): Call<ResponseBody>

    @GET("resources/pipelines/{id}")
    fun getPipeline(@Path("id") id: String): Call<ResponseBody>

    @Multipart
    @POST("resources/executions")
    fun executePipeline(@Part("pipeline") pipeline: RequestBody): Call<ResponseBody>

    @GET("resources/components")
    fun componentList(): Call<ResponseBody>

    @GET("api/v1/components/configTemplate")
    fun componentDefaultConfiguration(@Query("iri") templateId: String): Call<ResponseBody>

    @GET("api/v1/components/config")
    fun templateConfiguration(@Query("iri") id: String): Call<ResponseBody>

    @Multipart
    @POST("resources/pipelines")
    fun createPipeline(@Part("options") options: RequestBody): Call<ResponseBody>

    @PUT("resources/pipelines/{id}?unchecked=true")
    fun updatePipeline(@Path("id") pipelineIdNumber: String, @Body jsonld: RequestBody): Call<ResponseBody>

    companion object {
        val Retrofit.Builder.pipelineRetrofit: PipelineRetrofit
            get() = build().create(PipelineRetrofit::class.java)

        const val OPTIONS = "{\"@id\":\"http://localhost/options\",\"@type\":\"http://linkedpipes.com/ontology/UpdateOptions\"}"
    }
}