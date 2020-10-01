package cz.palda97.lpclient.model.repository

import kotlinx.coroutines.*

class DeleteRepository<T>(
    private val deleteFunction: suspend (T) -> Unit
) {

    private val deleteJobs: MutableMap<T, Job> = HashMap()

    fun toBeDeleted(item: T): Boolean = deleteJobs[item]?.isActive ?: false

    fun addPending(item: T, timeMillis: Long) {
        if (toBeDeleted(item))
            return
        deleteJobs[item] = CoroutineScope(Dispatchers.IO).launch {
            delay(timeMillis)
            deleteFunction(item)
        }
    }

    fun cancelDeletion(item: T) {
        deleteJobs[item]?.let {
            deleteJobs.remove(item)
            it.cancel()
        }
    }
}