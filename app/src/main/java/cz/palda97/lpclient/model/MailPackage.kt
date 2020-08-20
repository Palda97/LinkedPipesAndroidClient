package cz.palda97.lpclient.model

class MailPackage<content>(val mailContent: content?, private val status: Status, val msg: String) {
    val isLoading: Boolean
        get() = status == Status.LOADING
    val isOk: Boolean
        get() = status == Status.OK && mailContent != null
    val isError: Boolean
        get() = status == Status.ERROR

    constructor(mailContent: content) : this(mailContent, Status.OK, "")

    companion object {
        fun <dummy> loadingPackage() = MailPackage<dummy>(null, Status.LOADING, "")
        fun <dummy> brokenPackage(message: String = "") =
            MailPackage<dummy>(null, Status.ERROR, message)

        fun ok() = MailPackage(true)
        fun error(message: String = "") = MailPackage(false, Status.ERROR, message)
    }

    enum class Status {
        LOADING, OK, ERROR
    }
}

typealias StatusPackage = MailPackage<Boolean>