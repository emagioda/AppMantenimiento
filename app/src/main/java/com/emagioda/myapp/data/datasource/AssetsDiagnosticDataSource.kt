package com.emagioda.myapp.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap

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
        @SerializedName("name") val name: String?,
        @SerializedName("description") val description: String? = null,
        @SerializedName("imageName") val imageName: String? = null
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
        val safetyWarning: Boolean? = null,
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
        val supplier: List<ContactRefRaw>?,
        val technicalContacts: List<ContactRefRaw>?,
        val imageResName: String?
        // ELIMINADO: val nodeRefs: List<String>
    )

    data class ContactRefRaw(
        val id: String
    )

    private val machinesIndexCache by lazy {
        gson.fromJson(readAsset("machines.json"), MachinesIndex::class.java)
    }

    private val partsCatalogCache by lazy {
        gson.fromJson(readAsset("diagnostics/parts.json"), PartsCatalog::class.java)
    }

    private val templateCache = ConcurrentHashMap<String, RawTree>()


    // --------------------------
    // Public read functions
    // --------------------------
    fun readMachinesIndex(): MachinesIndex =
        machinesIndexCache

    fun readTemplateRaw(templateId: String): RawTree {
        // L'app è solo in italiano: usiamo sempre il template italiano.
        return templateCache.getOrPut(templateId) {
            val path = "diagnostics/templates/${templateId}_it.json"
            gson.fromJson(readAsset(path), RawTree::class.java)
        }
    }

    fun readPartsCatalog(): PartsCatalog = partsCatalogCache


    // --------------------------
    // Asset loader
    // --------------------------
    private fun readAsset(path: String): String {
        context.assets.open(path).use { input ->
            BufferedReader(InputStreamReader(input, StandardCharsets.UTF_8)).use { br ->
                return br.readText()
            }
        }
    }
}
