package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ComponentRetrofit {

    @GET("api/v1/components/dialog?name=config&file=dialog.html")
    fun dialog(@Query("iri") iri: String): Call<ResponseBody>

    @GET("api/v1/components/dialog?name=config&file=dialog.js")
    fun dialogJs(@Query("iri") iri: String): Call<ResponseBody>

    companion object {
        val Retrofit.Builder.componentRetrofit: ComponentRetrofit
            get() = build().create(ComponentRetrofit::class.java)
    }
}