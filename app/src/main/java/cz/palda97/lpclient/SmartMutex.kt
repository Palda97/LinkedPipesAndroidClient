package cz.palda97.lpclient

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mutex with finished mechanism.
 * Call [done] when no more action should happen.
 */
class SmartMutex {

    private val mutex = Mutex()

    var finished = false
        private set

    /**
     * Call when no more action should happen.
     * Preferably from the inside of [sync] or [syncScope].
     * All waiting coroutines will be freed one by one by using mutex
     * and coroutines coming after will be not even interact with the mutex.
     */
    fun done() {
        finished = true
    }

    /**
     * The opposite of [done].
     * [Done][done] can be called again when needed.
     */
    fun reset() {
        finished = false
    }

    /**
     * If [done] has not been called yet, lock mutex, check [done] again, do the action and unlock the mutex.
     */
    suspend fun <T> sync(action: suspend SmartMutex.() -> T): T? = if (finished) null else mutex.withLock {
        if (finished)
            return@withLock null
        return action()
    }

    /**
     * If [done] has not been called yet, lock mutex, check [done] again, do the action and unlock the mutex.
     */
    fun <T> syncScope(scope: CoroutineScope, action: suspend SmartMutex.() -> T): Deferred<T?> = scope.async {
        return@async sync(action)
    }

    /**
     * If [done] has not been called yet, lock mutex, check [done] again, do the action and unlock the mutex.
     */
    fun <T> syncScope(dispatcher: CoroutineDispatcher, action: suspend SmartMutex.() -> T): Deferred<T?> = CoroutineScope(dispatcher).async {
        return@async sync(action)
    }
}