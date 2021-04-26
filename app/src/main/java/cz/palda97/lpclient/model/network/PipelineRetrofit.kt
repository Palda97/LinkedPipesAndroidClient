package cz.palda97.lpclient.model.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.*

interface PipelineRetrofit {

    /**
     * JsonLd containing all [PipelineViews][cz.palda97.lpclient.model.entities.pipelineview.PipelineView].
     */
    @GET("resources/pipelines")
    fun pipelineList(): Call<ResponseBody>

    /**
     * Delete the pipeline.
     * @param id **idNumber** of the [PipelineView][cz.palda97.lpclient.model.entities.pipelineview.PipelineView].
     */
    @DELETE("resources/pipelines/{id}")
    fun deletePipeline(@Path("id") id: String): Call<ResponseBody>

    /**
     * Get a specific pipeline.
     * @param id **idNumber** of the [PipelineView][cz.palda97.lpclient.model.entities.pipelineview.PipelineView].
     */
    @GET("resources/pipelines/{id}")
    fun getPipeline(@Path("id") id: String): Call<ResponseBody>

    /**
     * Execute a specific pipeline.
     * @param pipeline The entire [Pipeline's][cz.palda97.lpclient.model.entities.pipeline.Pipeline] jsonLd.
     */
    @Multipart
    @POST("resources/executions")
    fun executePipeline(@Part("pipeline") pipeline: RequestBody): Call<ResponseBody>

    /**
     * JsonLd containing all possible components.
     */
    @GET("resources/components")
    fun componentList(): Call<ResponseBody>

    /**
     * Default configuration for a new [Component][cz.palda97.lpclient.model.entities.pipeline.Component].
     */
    @GET("api/v1/components/configTemplate")
    fun componentDefaultConfiguration(@Query("iri") templateId: String): Call<ResponseBody>

    /**
     * Configuration for [Template][cz.palda97.lpclient.model.entities.pipeline.Template].
     */
    @GET("api/v1/components/config")
    fun templateConfiguration(@Query("iri") id: String): Call<ResponseBody>

    /**
     * Create new pipeline.
     * @param options [Companion.OPTIONS] wrapped into RequestBody.
     * @see RetrofitHelper.stringToFormData
     * @return [PipelineView][cz.palda97.lpclient.model.entities.pipelineview.PipelineView]
     * of the newly created pipeline.
     */
    @Multipart
    @POST("resources/pipelines")
    fun createPipeline(@Part("options") options: RequestBody): Call<ResponseBody>

    /**
     * Update pipeline.
     * @param pipelineIdNumber **idNumber** of the
     * [PipelineView][cz.palda97.lpclient.model.entities.pipelineview.PipelineView].
     * @param jsonld JsonLd of the [Pipeline][cz.palda97.lpclient.model.entities.pipeline.Pipeline].
     */
    @PUT("resources/pipelines/{id}?unchecked=true")
    fun updatePipeline(@Path("id") pipelineIdNumber: String, @Body jsonld: RequestBody): Call<ResponseBody>

    companion object {
        val Retrofit.Builder.pipelineRetrofit: PipelineRetrofit
            get() = build().create(PipelineRetrofit::class.java)

        /**
         * Json used for creating new pipeline.
         */
        const val OPTIONS = "{\"@id\":\"http://localhost/options\",\"@type\":\"http://linkedpipes.com/ontology/UpdateOptions\"}"
    }
}