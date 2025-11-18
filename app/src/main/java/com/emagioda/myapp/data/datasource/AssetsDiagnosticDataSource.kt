package com.emagioda.myapp.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class AssetsDiagnosticDataSource(
    private val context: Context,
    private val gson: Gson = Gson()
) {

    // --------------------------
    // Machines Index
    // --------------------------
    data class MachinesIndex(
        @SerializedName("machines") val machines: List<MachineMap>
    )

    data class MachineMap(
        @SerializedName("id") val id: String,
        @SerializedName("templateId") val templateId: String,
        @SerializedName("name") val name: String?
    )


    // --------------------------
    // Raw Diagnostic Tree
    // --------------------------
    data class RawTree(
        val templateId: String,
        val version: Int,
        val locale: String,
        val root: String,
        val nodes: List<RawNode>
    )

    data class RawNode(
        val id: String,
        val type: String,
        val title: String,
        val description: String? = null,
        val yes: String? = null,
        val no: String? = null,
        val providersShortcut: Boolean? = null,
        val result: String? = null,
        val parts: List<RawPartRef>? = null,
        val mode: String? = null
    )

    data class RawPartRef(
        val id: String,
        val qty: Int? = null
    )


    // --------------------------
    // Parts catalog
    // --------------------------
    data class PartsCatalog(
        val parts: List<PartDetailRaw>
    )

    data class PartDetailRaw(
        val id: String,
        val product: String,
        val code: String?,
        val features: String?,
        val supplier: String?,
        val technicalContacts: String?,
        val imageResName: String?,
        val nodeRefs: List<String>
    )


    // --------------------------
    // Public read functions
    // --------------------------
    fun readMachinesIndex(): MachinesIndex =
        gson.fromJson(readAsset("machines.json"), MachinesIndex::class.java)

    fun readTemplateRaw(templateId: String): RawTree {

        // Detectar idioma del celular
        val lang = Locale.getDefault().language.lowercase()

        // Seleccionar sufijo
        val suffix = when {
            lang.startsWith("es") -> "es"       // español
            lang.startsWith("it") -> "it"       // italiano
            else -> "it"                        // fallback
        }

        // Construir ruta con idioma preferido
        val requestedPath = "diagnostics/templates/${templateId}_${suffix}.json"

        // Si el archivo existe → usarlo
        val finalJson = try {
            readAsset(requestedPath)
        } catch (e: Exception) {
            // Fallback seguro a italiano (templateId_it.json)
            val fallbackPath = "diagnostics/templates/${templateId}_it.json"
            readAsset(fallbackPath)
        }

        return gson.fromJson(finalJson, RawTree::class.java)
    }

    fun readPartsCatalog(): PartsCatalog {
        val json = readAsset("diagnostics/parts.json")
        return gson.fromJson(json, PartsCatalog::class.java)
    }


    // --------------------------
    // Asset loader
    // --------------------------
    private fun readAsset(path: String): String {
        context.assets.open(path).use { input ->
            BufferedReader(InputStreamReader(input)).use { br ->
                return br.readText()
            }
        }
    }
}
