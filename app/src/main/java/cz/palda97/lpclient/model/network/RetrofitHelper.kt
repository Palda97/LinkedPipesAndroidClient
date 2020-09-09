package cz.palda97.lpclient.model.network

import android.util.Log
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.repository.PipelineRepository
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
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
}