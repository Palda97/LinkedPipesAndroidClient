package cz.palda97.lpclient.model.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ExecutionRetrofit {

    /**
     * JsonLd containing all executions.
     */
    @GET("resources/executions")
    fun executionList(): Call<ResponseBody>

    /**
     * Delete the execution.
     * @param id **idNumber** of the [Execution][cz.palda97.lpclient.model.entities.execution.Execution].
     */
    @DELETE("resources/executions/{id}")
    fun delete(@Path("id") id: String): Call<ResponseBody>

    /**
     * Get a specific execution.
     * @param id **idNumber** of the [Execution][cz.palda97.lpclient.model.entities.execution.Execution].
     */
    @GET("resources/executions/{id}")
    fun execution(@Path("id") id: String): Call<ResponseBody>

    /**
     * Get an overview of a specific execution.
     * @param id **idNumber** of the [Execution][cz.palda97.lpclient.model.entities.execution.Execution].
     */
    @GET("resources/executions/{id}/overview")
    fun overview(@Path("id") id: String): Call<ResponseBody>

    companion object {
        val Retrofit.Builder.executionRetrofit: ExecutionRetrofit
            get() = build().create(ExecutionRetrofit::class.java)
    }
}