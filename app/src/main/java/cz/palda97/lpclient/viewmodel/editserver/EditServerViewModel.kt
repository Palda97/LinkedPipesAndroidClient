package cz.palda97.lpclient.viewmodel.editserver

import androidx.lifecycle.*
import cz.palda97.lpclient.Injector
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.entities.server.ServerInstance
import cz.palda97.lpclient.model.repository.ServerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class EditServerViewModel : ViewModel() {

    private val serverRepository = Injector.serverRepository
    private val editServerRepository = Injector.editServerRepository

    private val dbScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.IO)

    init {
        l("init")
    }

    private val _saveSuccessful = MutableLiveData<SaveStatus>(SaveStatus.WAITING)
    val saveSuccessful: LiveData<SaveStatus>
        get() = _saveSuccessful

    var tmpServer: ServerInstance
        get() = editServerRepository.tmpServer
        set(value) {
            editServerRepository.tmpServer = value
        }

    private suspend fun savingRoutine(serverInstance: ServerInstance) {
        val match =
            serverRepository.matchUrlOrNameExcept(serverInstance, editServerRepository.serverToEdit)
        _saveSuccessful.postValue(
            when (match) {
                ServerRepository.MatchCases.NO_MATCH -> {
                    serverRepository.insertServer(serverInstance.apply {
                        id = editServerRepository.serverToEdit.id
                    })
                    SaveStatus.OK
                }
                ServerRepository.MatchCases.URL -> SaveStatus.URL
                ServerRepository.MatchCases.NAME -> SaveStatus.NAME
            }
        )
    }

    fun saveServer(serverInstance: ServerInstance = tmpServer) {
        val status = ServerInstanceAttributeCheck(serverInstance).status
        _saveSuccessful.value = status
        if (status != SaveStatus.WORKING)
            return
        dbScope.launch {
            savingRoutine(serverInstance)
        }
    }

    fun resetStatus() {
        _saveSuccessful.value = SaveStatus.WAITING
    }

    private val _pingStatus = MutableLiveData<MailPackage<Pair<String, Ping.Status>>>()
    val pingStatus: LiveData<MailPackage<Pair<String, Ping.Status>>>
        get() = _pingStatus

    fun resetPingStatus() {
        _pingStatus.value = MailPackage.loadingPackage()
    }

    private var pingStatusCnt: AtomicInteger = AtomicInteger(0)

    private fun updatePingStatus(order: Int, mail: MailPackage<Pair<String, Ping.Status>>) {
        if (pingStatusCnt.get() != order)
            return
        _pingStatus.postValue(mail)
        l("ping status updated: $order")
    }

    private fun reservePingOrder(): Int = pingStatusCnt.addAndGet(1)

    private suspend fun pingRoutine(server: ServerInstance) {
        val order = reservePingOrder()
        l("ping start: $order")
        val ping = Ping(server)
        val apiCallResult = ping.tryApiCall()
        val mail = MailPackage(
                if (apiCallResult == Ping.Status.API_OK)
                    ping.pingUrl to apiCallResult
                else
                    ping.pingUrl to ping.ping()
            )
        updatePingStatus(order, mail)
        l("ping end")
    }

    fun ping(server: ServerInstance) {
        CoroutineScope(Dispatchers.IO).launch {
            pingRoutine(server)
        }
    }

    companion object {
        private val l = Injector.generateLogFunction(this)
    }

    enum class SaveStatus {
        NAME, URL, OK, WAITING, EMPTY_NAME, EMPTY_URL, WORKING
    }
}