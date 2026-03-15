package com.emagioda.myapp.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class AssetsContactsDataSource(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    data class Envelope(@SerializedName("contacts") val contacts: List<ContactRaw>)

    data class ContactRaw(
        val id: String,
        val company: String? = null,
        val specialties: List<String>? = null,
        val phones: List<String>? = null,
        val whatsapp: String? = null,
        val emails: List<String>? = null,
        @SerializedName("seviceArea")
        val seviceArea: List<String>? = null,
        val isManufacturer: Boolean = false,
        val isTechnician: Boolean = false
    )

    private val contactsCache by lazy {
        load("contacts/contacts.json")
    }

    fun loadContacts(): List<ContactRaw> = contactsCache

    private fun load(path: String): List<ContactRaw> {
        val json = readAsset(path)
        return gson.fromJson(json, Envelope::class.java).contacts
    }
    private fun readAsset(path: String): String {
        context.assets.open(path).use { input ->
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { br ->
                return br.readText()
            }
        }
    }
}
