package com.emagioda.myapp.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader

class AssetsContactsDataSource(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    // Envelope para parsear el JSON { "contacts": [...] }
    data class Envelope(@SerializedName("contacts") val contacts: List<ContactRaw>)

    // Forma "raw" tal como viene del JSON
    data class ContactRaw(
        val id: String,
        val type: String,
        val name: String,
        val company: String? = null,
        val roles: List<String>? = null,
        val specialties: List<String>? = null,
        val phones: List<String>? = null,
        val whatsapp: String? = null,
        val emails: List<String>? = null,
        val location: String? = null,
        val coverage: List<String>? = null,
        val isEmergency: Boolean = false,
        val isFavorite: Boolean = false,
        val notes: String? = null
    )

    fun loadTechnicians(): List<ContactRaw> = load("contacts/technicians.json")
    fun loadProviders(): List<ContactRaw> = load("contacts/providers.json")

    private fun load(path: String): List<ContactRaw> {
        val json = readAsset(path)
        return gson.fromJson(json, Envelope::class.java).contacts
    }

    private fun readAsset(path: String): String {
        context.assets.open(path).use { input ->
            BufferedReader(InputStreamReader(input)).use { br ->
                return br.readText()   // 👈 devolvemos el contenido
            }
        }
    }
}
