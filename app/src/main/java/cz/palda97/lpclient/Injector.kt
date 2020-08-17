package cz.palda97.lpclient

import cz.palda97.lpclient.model.repository.ServerRepositoryFake

object Injector {
    val serverRepository = ServerRepositoryFake()
}