package cz.palda97.lpclient.model

/**
 * Wrapper for an object, one of three statuses and a message.
 * @param content Type of object to be potentially stored in mail content.
 */
class MailPackage<content>(val mailContent: content?, val status: Status, val msg: String) {
    val isLoading: Boolean
        get() = status == Status.LOADING
    val isOk: Boolean
        get() = status == Status.OK && mailContent != null
    val isError: Boolean
        get() = status == Status.ERROR

    /**
     * Makes a MailPackage instance with this object, [OK][Status.OK] status and empty message.
     */
    constructor(mailContent: content) : this(mailContent, Status.OK, "")

    companion object {

        /**
         * Returns MailPackage instance with [LOADING][Status.OK] status.
         */
        fun <dummy> loadingPackage() = MailPackage<dummy>(null, Status.LOADING, "")

        /**
         * Returns MailPackage instance with [ERROR][Status.ERROR] status.
         * @param message Message to be stored in the MailPackage instance.
         */
        fun <dummy> brokenPackage(message: String = "") =
            MailPackage<dummy>(null, Status.ERROR, message)

        /**
         * Returns MailPackage instance with [OK][Status.OK] status
         * and Boolean true as a mail content.
         */
        fun ok() = MailPackage(true)

        /**
         * Returns MailPackage instance with [ERROR][Status.ERROR] status
         * and Boolean false as a mail content.
         * @param message Message to be stored in the MailPackage instance.
         */
        fun error(message: String = "") = MailPackage(false, Status.ERROR, message)

        /**
         * Returns MailPackage instance with [LOADING][Status.LOADING] status
         * and Boolean false as a mail content.
         */
        fun loading() = MailPackage(false, Status.LOADING, "")
    }

    enum class Status {
        LOADING, OK, ERROR
    }
}

/**
 * MailPackage type with potential Boolean content.
 */
typealias StatusPackage = MailPackage<Boolean>