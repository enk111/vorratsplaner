package com.example.enkkaufsliste.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class SyncResult(val items: List<ShoppingItem>, val cursor: Long)

class ApiException(message: String, val code: Int = 0) : Exception(message)

object Api {
    private const val BASE = "https://einkaufsliste-api.nkruse.workers.dev"

    private suspend fun post(path: String, body: JSONObject): JSONObject =
        withContext(Dispatchers.IO) {
            val conn = URL(BASE + path).openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                conn.connectTimeout = 10_000
                conn.readTimeout = 15_000
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                if (code !in 200..299) throw ApiException("HTTP $code", code)
                JSONObject(text)
            } finally {
                conn.disconnect()
            }
        }

    suspend fun createList(): String =
        post("/lists", JSONObject()).getString("listId")

    suspend fun createPin(listId: String): String =
        post("/lists/$listId/pin", JSONObject()).getString("pin")

    suspend fun pair(pin: String): String =
        post("/pair", JSONObject().put("pin", pin)).getString("listId")

    suspend fun sync(listId: String, items: List<ShoppingItem>, since: Long): SyncResult {
        val arr = JSONArray()
        items.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("quantity", it.quantity)
                    .put("checked", it.checked)
                    .put("deleted", it.deleted)
                    .put("updatedAt", it.updatedAt)
            )
        }
        val res = post(
            "/lists/$listId/sync",
            JSONObject().put("items", arr).put("since", since)
        )
        val list = res.getJSONArray("items")
        val out = (0 until list.length()).map { i ->
            val o = list.getJSONObject(i)
            ShoppingItem(
                id = o.getString("id"),
                name = o.getString("name"),
                quantity = o.getString("quantity"),
                checked = o.getBoolean("checked"),
                deleted = o.getBoolean("deleted"),
                updatedAt = o.getLong("updatedAt"),
                dirty = false
            )
        }
        return SyncResult(out, res.getLong("cursor"))
    }
}