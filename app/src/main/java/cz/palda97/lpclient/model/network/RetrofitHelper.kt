package cz.palda97.lpclient.model.network

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.entities.server.ServerInstance
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import java.io.IOException

object RetrofitHelper {
    private val TAG = Injector.tag(this)
    private fun l(msg: String) = Log.d(TAG, msg)

    suspend fun getStringFromCall(call: Call<ResponseBody>): String? = try {
        val executedCall = call.execute()
        val response = executedCall.body()
        //response?.string() ?: "There is no ResponseBody"
        response?.string()
    } catch (e: IOException) {
        l("getStringFromCall ${e.toString()}")
        null
    }

    private const val TEXT_PLAIN = "text/plain"

    fun stringToFormData(string: String): RequestBody = RequestBody.create(
        MediaType.parse(TEXT_PLAIN),
        string
    )

    private fun getBuilder(baseUrl: String) = Retrofit.Builder().baseUrl(baseUrl)

    private fun Retrofit.Builder.basicAuth(username: String, password: String) = client(
        OkHttpClient.Builder()
            .addInterceptor {
                var request = it.request()
                request = request.newBuilder()
                    .header("Authorization", Credentials.basic(username, password)).build()
                it.proceed(request)
            }
            .build()
    )

    fun getBuilder(server: ServerInstance, url: String): Retrofit.Builder {
        val builder = getBuilder(url)
        val auth = server.credentials ?: return builder
        return builder.basicAuth(auth.first, auth.second)
    }
}