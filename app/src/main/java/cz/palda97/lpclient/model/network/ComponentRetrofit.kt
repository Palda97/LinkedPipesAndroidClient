package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface ComponentRetrofit {

    /**
     * Html for making [ConfigInputs][cz.palda97.lpclient.model.entities.pipeline.ConfigInput].
     */
    @GET("api/v1/components/dialog?name=config&file=dialog.html")
    fun dialog(@Query("iri") iri: String): Call<ResponseBody>

    /**
     * Javascript for making [DialogJs][cz.palda97.lpclient.model.entities.pipeline.DialogJs].
     */
    @GET("api/v1/components/dialog?name=config&file=dialog.js")
    fun dialogJs(@Query("iri") iri: String): Call<ResponseBody>

    /**
     * JsonLd with [Bindings][cz.palda97.lpclient.model.entities.pipeline.DialogJs].
     */
    @GET("api/v1/components/definition")
    fun bindings(@Query("iri") iri: String): Call<ResponseBody>

    companion object {
        val Retrofit.Builder.componentRetrofit: ComponentRetrofit
            get() = build().create(ComponentRetrofit::class.java)
    }
}