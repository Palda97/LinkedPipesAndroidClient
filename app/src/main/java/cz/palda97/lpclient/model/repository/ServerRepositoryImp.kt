package cz.palda97.lpclient.model.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import cz.palda97.lpclient.model.MailPackage
import cz.palda97.lpclient.model.ServerInstance
import cz.palda97.lpclient.model.db.dao.ServerInstanceDao

class ServerRepositoryImp(private val serverInstanceDao: ServerInstanceDao) : ServerRepository() {

    override val liveServers: LiveData<MailPackage<List<ServerInstance>>> = Transformations.map(serverInstanceDao.serverList()) {
        if (it == null)
            return@map MailPackage.loadingPackage<List<ServerInstance>>()
        return@map MailPackage(it)
    }

    override val serverToEdit: MutableLiveData<ServerInstance> = MutableLiveData()

    override fun saveServer(serverInstance: ServerInstance) {
        Thread { serverInstanceDao.insertServer(serverInstance) }.start()
    }

    override fun deleteAndCreate(delete: ServerInstance, create: ServerInstance) {
        Thread {
            serverInstanceDao.deleteServer(delete)
            Thread {
                serverInstanceDao.insertServer(create)
            }.start()
        }.start()
    }

    override fun findServerByUrl(url: String): LiveData<MailPackage<ServerInstance>> {
        TODO("not implemented")
    }

    override fun matchingUrlAndName(serverInstance: ServerInstance): LiveData<MailPackage<MatchCases>> =
        matchingUrlExcept(serverInstance, ServerInstance())

    override fun matchingUrlExcept(
        serverInstance: ServerInstance,
        except: ServerInstance
    ): LiveData<MailPackage<MatchCases>> {
        val live = MutableLiveData<MailPackage<MatchCases>>(MailPackage.loadingPackage())
        Thread {
            val list = serverInstanceDao.matchExcept(
                serverInstance.url,
                serverInstance.name,
                except.url,
                except.name
            )
            if (list.isEmpty()) {
                live.postValue(MailPackage.brokenPackage())
            } else {
                val match = list[0]
                if (serverInstance.url == match.url)
                    live.postValue(MailPackage(MatchCases.URL))
                else
                    live.postValue(MailPackage(MatchCases.NAME))
            }
        }.start()
        return live
    }

    override fun deleteAll() {
        Thread {
            serverInstanceDao.deleteAll()
        }.start()
    }
}