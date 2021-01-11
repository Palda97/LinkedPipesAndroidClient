package cz.palda97.lpclient.model.repository

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PersistRepository<T> (
    private val loadFun: suspend () -> T?,
    private val persistFun: suspend (T) -> Unit
) {
    private val mutex = Mutex()
    var entity: T? = null

    suspend fun prepareEntity(): Boolean {
        mutex.lock()
        entity = loadFun()
        return entity != null
    }

    private val persistEntityMutex = Mutex()
    suspend fun persistEntity() = persistEntityMutex.withLock {
        entity?.let {
            persistFun(it)
        }
        mutex.tryLock()
        mutex.unlock()
    }
}