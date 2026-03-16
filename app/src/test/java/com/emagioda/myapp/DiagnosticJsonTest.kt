package com.emagioda.myapp

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        val type: String? = null,
        val mode: String? = null,
        val result: String? = null,
        val parts: List<RawPartRef>? = null,
        val schematicIds: List<String>? = null,
        val yes: String? = null,
        val no: String? = null
    )

    data class RawPartRef(
        val id: String
    )

    data class RawMachinesEnvelope(
        val machines: List<RawMachine>
    )

    data class RawMachine(
        val id: String,
        val templateId: String,
        val imageName: String? = null
    )

    data class RawPartsEnvelope(
        val parts: List<RawPart>
    )

    data class RawPart(
        val id: String,
        val supplier: List<RawContactRef>? = null,
        val technicalContacts: List<RawContactRef>? = null,
        val imageResName: String? = null
    )

    data class RawContactsEnvelope(
        val contacts: List<RawContact>
    )

    data class RawContact(
        val id: String
    )

    data class RawContactRef(
        val id: String
    )

    data class RawSchematicsEnvelope(
        val schematics: List<RawSchematic>
    )

    data class RawSchematic(
        val id: String,
        val assetPath: String
    )

    @Test
    fun diagnosticAssetsAreConsistent() {
        val assetsDir = assetsRoot()
        val drawableDir = drawableRoot()
        val templatesDir = File(assetsDir, "diagnostics/templates")
        assertTrue("Templates directory not found: ${templatesDir.path}", templatesDir.exists())

        val templateFiles = templatesDir.listFiles { file ->
            file.isFile && file.extension == "json"
        }?.toList().orEmpty()

        assertTrue("No diagnostic templates found in ${templatesDir.path}", templateFiles.isNotEmpty())

        val gson = Gson()
        val contactsFile = File(assetsDir, "contacts/contacts.json")
        val partsFile = File(assetsDir, "diagnostics/parts.json")
        val machinesFile = File(assetsDir, "machines.json")
        val schematicsFile = File(assetsDir, "diagnostics/schematics/catalog.json")

        assertTrue("Contacts file not found: ${contactsFile.path}", contactsFile.exists())
        assertTrue("Parts file not found: ${partsFile.path}", partsFile.exists())
        assertTrue("Machines file not found: ${machinesFile.path}", machinesFile.exists())
        assertTrue("Schematics file not found: ${schematicsFile.path}", schematicsFile.exists())

        val contacts = gson.fromJson(contactsFile.readText(), RawContactsEnvelope::class.java).contacts
        val parts = gson.fromJson(partsFile.readText(), RawPartsEnvelope::class.java).parts
        val machines = gson.fromJson(machinesFile.readText(), RawMachinesEnvelope::class.java).machines
        val schematics = gson.fromJson(
            schematicsFile.readText(),
            RawSchematicsEnvelope::class.java
        ).schematics

        val contactIds = contacts.map { it.id }
        val contactIdSet = contactIds.toSet()
        assertEquals("Duplicate contact ids", contactIds.size, contactIdSet.size)

        val partIds = parts.map { it.id }
        val partIdSet = partIds.toSet()
        assertEquals("Duplicate part ids", partIds.size, partIdSet.size)

        val machineIds = machines.map { it.id }
        val machineIdSet = machineIds.toSet()
        assertEquals("Duplicate machine ids", machineIds.size, machineIdSet.size)

        val schematicIds = schematics.map { it.id }
        val schematicIdSet = schematicIds.toSet()
        assertEquals("Duplicate schematic ids", schematicIds.size, schematicIdSet.size)

        machines.forEach { machine ->
            val templateFile = File(templatesDir, "${machine.templateId}_it.json")
            assertTrue(
                "Template for machine ${machine.id} not found: ${templateFile.path}",
                templateFile.exists()
            )
            machine.imageName?.let { imageName ->
                assertTrue(
                    "Machine image $imageName not found in ${drawableDir.path}",
                    drawableExists(drawableDir, imageName)
                )
            }
        }

        schematics.forEach { schematic ->
            assertTrue(
                "Schematic asset ${schematic.assetPath} not found",
                File(assetsDir, schematic.assetPath).exists()
            )
        }

        parts.forEach { part ->
            part.supplier.orEmpty().forEach { ref ->
                assertTrue(
                    "Supplier ${ref.id} referenced by part ${part.id} was not found",
                    contactIdSet.contains(ref.id)
                )
            }
            part.technicalContacts.orEmpty().forEach { ref ->
                assertTrue(
                    "Technical contact ${ref.id} referenced by part ${part.id} was not found",
                    contactIdSet.contains(ref.id)
                )
            }
            part.imageResName?.let { imageName ->
                assertTrue(
                    "Part image $imageName referenced by ${part.id} not found in ${drawableDir.path}",
                    drawableExists(drawableDir, imageName)
                )
            }
        }

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
                assertFalse(
                    "Template ${file.name} still references deprecated END sentinel",
                    node.yes == "END" || node.no == "END"
                )
                if (node.type.equals("END", ignoreCase = true)) {
                    assertNotNull("End node ${node.id} in ${file.name} must define result", node.result)
                }
                node.parts.orEmpty().forEach { ref ->
                    assertTrue(
                        "Part ${ref.id} referenced by node ${node.id} not found in ${file.name}",
                        partIdSet.contains(ref.id)
                    )
                }
                node.schematicIds.orEmpty().forEach { schematicId ->
                    assertTrue(
                        "Schematic $schematicId referenced by node ${node.id} not found in ${file.name}",
                        schematicIdSet.contains(schematicId)
                    )
                }
                listOf(node.yes, node.no).filterNotNull().forEach { ref ->
                    assertTrue(
                        "Reference $ref not found in ${file.name}",
                        idSet.contains(ref)
                    )
                }
            }

            val visited = mutableSetOf<String>()
            fun visit(id: String) {
                if (!visited.add(id)) return
                val current = nodeMap[id] ?: return
                listOf(current.yes, current.no).filterNotNull().forEach { ref ->
                    visit(ref)
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

    private fun assetsRoot(): File =
        listOf(
            File("app/src/main/assets"),
            File("src/main/assets")
        ).firstOrNull(File::exists)
            ?: error("Assets directory not found")

    private fun drawableRoot(): File =
        listOf(
            File("app/src/main/res/drawable"),
            File("src/main/res/drawable")
        ).firstOrNull(File::exists)
            ?: error("Drawable directory not found")

    private fun drawableExists(drawableDir: File, resourceName: String): Boolean =
        drawableDir.listFiles().orEmpty().any { it.nameWithoutExtension == resourceName }
}
