package com.example.enkkaufsliste

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.enkkaufsliste.data.ApiException
import com.example.enkkaufsliste.data.AppDatabase
import com.example.enkkaufsliste.data.ShoppingItem
import com.example.enkkaufsliste.data.SyncRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class ItemViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).itemDao()
    private val repo = SyncRepository(app)

    val items: StateFlow<List<ShoppingItem>> = dao.observeItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suggestions = MutableStateFlow<List<String>>(emptyList())

    /** true, sobald die App mit einer gemeinsamen Liste verbunden ist */
    val connected = MutableStateFlow(repo.listId != null)

    /** false, wenn der letzte Abgleich nicht geklappt hat (z. B. offline) */
    val online = MutableStateFlow(true)

    val pin = MutableStateFlow<String?>(null)
    val error = MutableStateFlow<String?>(null)

    private var pollJob: Job? = null

    fun startAutoSync() {
        if (pollJob?.isActive == true) return
        pollJob = viewModelScope.launch {
            while (true) {
                afterChange()
                delay(5000)
            }
        }
    }

    fun stopAutoSync() {
        pollJob?.cancel()
        pollJob = null
    }

    private suspend fun afterChange() {
        if (repo.listId != null) online.value = repo.sync()
    }

    fun add(name: String, quantity: String) {
        val n = name.trim()
        if (n.isEmpty()) return
        viewModelScope.launch {
            dao.upsert(ShoppingItem(name = n, quantity = quantity.trim().ifEmpty { "1" }))
            suggestions.value = emptyList()
            afterChange()
        }
    }

    fun setChecked(item: ShoppingItem, checked: Boolean) {
        viewModelScope.launch {
            dao.setChecked(item.id, checked, System.currentTimeMillis())
            afterChange()
        }
    }

    fun delete(item: ShoppingItem) {
        viewModelScope.launch {
            dao.markDeleted(item.id, System.currentTimeMillis())
            afterChange()
        }
    }

    fun clearChecked() {
        viewModelScope.launch {
            dao.clearChecked(System.currentTimeMillis())
            afterChange()
        }
    }

    fun updateSuggestions(input: String) {
        val p = input.trim()
        if (p.isEmpty()) {
            suggestions.value = emptyList()
            return
        }
        viewModelScope.launch {
            suggestions.value = dao.suggestions(p).filter { !it.equals(p, ignoreCase = true) }
        }
    }

    fun createList() {
        viewModelScope.launch {
            try {
                repo.createList()
                connected.value = true
                error.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = "Keine Verbindung zum Server"
            }
        }
    }

    fun joinWithPin(input: String) {
        val p = input.filter { it.isDigit() }
        if (p.length != 6) {
            error.value = "Bitte 6 Ziffern eingeben"
            return
        }
        viewModelScope.launch {
            try {
                repo.joinWithPin(p)
                connected.value = true
                error.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                error.value = if (e.code == 404) "PIN ungültig oder abgelaufen" else "Keine Verbindung zum Server"
            } catch (e: Exception) {
                error.value = "Keine Verbindung zum Server"
            }
        }
    }

    fun showPin() {
        viewModelScope.launch {
            try {
                pin.value = repo.createPin()
                error.value = null
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = "Keine Verbindung zum Server"
            }
        }
    }

    fun dismissPin() {
        pin.value = null
    }

    fun leaveList() {
        viewModelScope.launch {
            repo.leaveList()
            connected.value = false
            online.value = true
            pin.value = null
            error.value = null
        }
    }

    fun clearError() {
        error.value = null
    }
}