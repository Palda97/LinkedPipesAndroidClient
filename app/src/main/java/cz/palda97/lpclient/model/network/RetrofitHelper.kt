package cz.palda97.lpclient.model.network

import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.Either
import cz.palda97.lpclient.model.entities.server.ServerInstance
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import java.io.IOException

object RetrofitHelper {

    private val l = Injector.generateLogFunction("RetrofitHelper")

    /**
     * Executes the call and returns response.
     * @return String with response if successful, http code otherwise.
     */
    suspend fun getStringFromCallOrCode(call: Call<ResponseBody>): Either<Int, String?> {
        try {
            val executedCall = call.execute()
            val code = executedCall.code()
            if (code != 200) {
                l(code)
                val body = executedCall.errorBody() ?: return Either.Left(code)
                val string = body.string() //this also closes the response
                l(string)
                return Either.Left(code)
            }
            val response = executedCall.body()
            //response?.string() ?: "There is no ResponseBody"
            return Either.Right(response?.string())
        } catch (e: IOException) {
            l("getStringFromCall ${e.toString()}")
            return Either.Right(null)
        }
    }

    /**
     * Executes the call and returns response.
     * @return String with response if successful, null if not.
     */
    suspend fun getStringFromCall(call: Call<ResponseBody>): String? = when(val res = getStringFromCallOrCode(call)) {
        is Either.Left -> null
        is Either.Right -> res.value
    }

    private const val TEXT_PLAIN = "text/plain"

    /**
     * Transform String to Form Data.
     */
    fun stringToFormData(string: String): RequestBody = RequestBody.create(
        MediaType.parse(TEXT_PLAIN),
        string
    )

    private fun getBuilder(baseUrl: String) = Retrofit.Builder().baseUrl(baseUrl)

    /**
     * Create a retrofit builder from [ServerInstance] and full url (including port).
     */
    fun getBuilder(server: ServerInstance, url: String): Retrofit.Builder {
        val auth = server.credentials
        return getBuilder(url).client(
            OkHttpClient.Builder()
                .addInterceptor {
                    var request = it.request()
                    request = request.newBuilder()
                        .header("Accept", "application/ld+json")
                        .let {
                            if (auth == null) {
                                it
                            } else {
                                it.header("Authorization", Credentials.basic(auth.first, auth.second))
                            }
                        }.build()
                    it.proceed(request)
                }
                .build()
        )
    }
}