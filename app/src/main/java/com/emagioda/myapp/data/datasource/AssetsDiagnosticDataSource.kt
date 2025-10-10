package com.emagioda.myapp.data.datasource

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.BufferedReader
import java.io.InputStreamReader

class AssetsDiagnosticDataSource(
    private val context: Context,
    private val gson: Gson = Gson()
) {
    data class MachinesIndex(
        @SerializedName("machines") val machines: List<MachineMap>
    )
    data class MachineMap(
        @SerializedName("id") val id: String,
        @SerializedName("templateId") val templateId: String,
        @SerializedName("name") val name: String?
    )

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
        val providersShortcut: Boolean? = null
    )

    fun readMachinesIndex(): MachinesIndex {
        val json = readAsset("machines.json")
        return gson.fromJson(json, MachinesIndex::class.java)
    }

    fun readTemplateRaw(templateId: String): RawTree {
        val path = "diagnostics/templates/$templateId.json"
        val json = readAsset(path)
        return gson.fromJson(json, RawTree::class.java)
    }

    private fun readAsset(path: String): String {
        context.assets.open(path).use { input ->
            BufferedReader(InputStreamReader(input)).use { br ->
                return br.readText()
            }
        }
    }
}
