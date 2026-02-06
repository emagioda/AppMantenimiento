package com.emagioda.myapp

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DiagnosticJsonTest {

    data class RawTree(
        val root: String,
        val nodes: List<RawNode>
    )

    data class RawNode(
        val id: String,
        val yes: String? = null,
        val no: String? = null
    )

    @Test
    fun diagnosticTemplatesAreConsistent() {
        val templatesDir = File("src/main/assets/diagnostics/templates")
        assertTrue("Templates directory not found: ${templatesDir.path}", templatesDir.exists())

        val templateFiles = templatesDir.listFiles { file ->
            file.isFile && file.extension == "json"
        }?.toList().orEmpty()

        assertTrue("No diagnostic templates found in ${templatesDir.path}", templateFiles.isNotEmpty())

        val gson = Gson()

        templateFiles.forEach { file ->
            val rawTree = gson.fromJson(file.readText(), RawTree::class.java)
            val ids = rawTree.nodes.map { it.id }
            val idSet = ids.toSet()

            assertEquals(
                "Duplicate node ids in ${file.name}",
                ids.size,
                idSet.size
            )

            val nodeMap = rawTree.nodes.associateBy { it.id }
            assertTrue(
                "Root node ${rawTree.root} not found in ${file.name}",
                nodeMap.containsKey(rawTree.root)
            )

            rawTree.nodes.forEach { node ->
                listOf(node.yes, node.no).filterNotNull().forEach { ref ->
                    if (ref != "END") {
                        assertTrue(
                            "Reference $ref not found in ${file.name}",
                            idSet.contains(ref)
                        )
                    }
                }
            }

            val visited = mutableSetOf<String>()
            fun visit(id: String) {
                if (!visited.add(id)) return
                val current = nodeMap[id] ?: return
                listOf(current.yes, current.no).filterNotNull().forEach { ref ->
                    if (ref != "END") {
                        visit(ref)
                    }
                }
            }

            visit(rawTree.root)

            val unreachable = idSet - visited
            assertTrue(
                "Unreachable nodes in ${file.name}: ${unreachable.joinToString()}",
                unreachable.isEmpty()
            )
        }
    }
}
