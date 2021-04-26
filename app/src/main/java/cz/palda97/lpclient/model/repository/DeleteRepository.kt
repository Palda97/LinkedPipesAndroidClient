package cz.palda97.lpclient.model.repository

import kotlinx.coroutines.*

/**
 * Repository for pending delete requests.
 * @param T Type of the items intended to be used in delete requests.
 * @param deleteFunction Function for deleting the selected item type.
 */
class DeleteRepository<T>(
    private val deleteFunction: suspend (T) -> Unit
) {

    private val deleteJobs: MutableMap<T, Job> = HashMap()

    /**
     * Check if there is an active pending delete request for this item.
     */
    fun toBeDeleted(item: T): Boolean = deleteJobs[item]?.isActive ?: false

    /**
     * Add a pending delete request to the repository.
     * Does nothing if a delete request for this item is already pending.
     * @param item Item to be deleted.
     * @param timeMillis Time after which the [deleteFunction] will be called.
     */
    fun addPending(item: T, timeMillis: Long) {
        if (toBeDeleted(item))
            return
        deleteJobs[item] = CoroutineScope(Dispatchers.IO).launch {
            delay(timeMillis)
            deleteFunction(item)
        }
    }

    /**
     * Cancel the pending delete request.
     * Does nothing if there is not a pending request for this item.
     * @param item Cancel this item's request.
     */
    fun cancelDeletion(item: T) {
        deleteJobs[item]?.let {
            deleteJobs.remove(item)
            it.cancel()
        }
    }
}