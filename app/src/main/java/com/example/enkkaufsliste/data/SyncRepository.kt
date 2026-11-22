package com.example.enkkaufsliste.data

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

class SyncRepository(context: Context) {
    private val dao = AppDatabase.get(context).itemDao()
    private val settings = Settings(context)
    private val mutex = Mutex()

    val listId: String? get() = settings.listId

    /** Gibt true zurück, wenn der Abgleich geklappt hat. */
    suspend fun sync(): Boolean {
        return mutex.withLock {
            val id = settings.listId ?: return@withLock false
            try {
                val dirty = dao.getDirty()
                val result = Api.sync(id, dirty, settings.cursor)
                dirty.forEach { dao.markClean(it.id, it.updatedAt) }
                result.items.forEach { remote ->
                    val local = dao.getById(remote.id)
                    if (local == null || remote.updatedAt > local.updatedAt) {
                        dao.upsert(remote)
                    }
                }
                settings.cursor = result.cursor
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                false
            }
        }
    }

    /** Neue Liste anlegen und die vorhandenen Artikel hochladen. */
    suspend fun createList() {
        connect(Api.createList())
    }

    /** Einer bestehenden Liste per PIN beitreten. */
    suspend fun joinWithPin(pin: String) {
        connect(Api.pair(pin))
    }

    /** PIN erzeugen, mit dem sich andere verbinden können. */
    suspend fun createPin(): String {
        val id = settings.listId ?: throw ApiException("Keine Liste verbunden")
        return Api.createPin(id)
    }

    /** Liste auf diesem Gerät verlassen: Verbindung und lokale Artikel entfernen. */
    suspend fun leaveList() {
        mutex.withLock {
            settings.listId = null
            settings.cursor = 0L
            dao.deleteAll()
        }
    }

    private suspend fun connect(newListId: String) {
        settings.listId = newListId
        settings.cursor = 0L
        dao.markAllDirty()
        sync()
    }
}