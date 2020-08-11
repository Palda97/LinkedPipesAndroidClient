package cz.palda97.lpclient.model

class MailPackage<content>(val mailContent: content?, private val status: Int, val msg: String) {
    val isLoading: Boolean
        get() = status == LOADING
    val isOk: Boolean
        get() = status == OK && mailContent != null
    val isError: Boolean
        get() = status == ERROR

    companion object {
        const val LOADING = 0
        const val OK = 1
        const val ERROR = 2
        fun <dummy> loadingPackage() = MailPackage<dummy>(null, LOADING,"")
    }
}