package com.emagioda.myapp.presentation.screen.diagnostic.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emagioda.myapp.R
import com.emagioda.myapp.domain.model.PartRefResolved

@Composable
fun DiagnosticPartsSection(
    parts: List<PartRefResolved>,
    onContactClick: (List<String>, List<String>) -> Unit
) {
    if (parts.isEmpty()) {
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.diagnostic_parts_title),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(16.dp))
        parts.forEachIndexed { index, part ->
            TransformablePartCard(
                part = part,
                onContactClick = onContactClick
            )
            if (index < parts.lastIndex) {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TransformablePartCard(
    part: PartRefResolved,
    onContactClick: (List<String>, List<String>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val resId = remember(part.detail.imageResName) {
        part.detail.imageResName?.let { resName ->
            context.resources.getIdentifier(resName, "drawable", context.packageName)
        } ?: 0
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
            .border(
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.elevatedCardColors()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!expanded) {
                    if (resId != 0) {
                        androidx.compose.foundation.Image(
                            painter = painterResource(resId),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BrokenImage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(12.dp))
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = part.detail.product,
                        style = MaterialTheme.typography.titleMedium
                    )
                    part.detail.code?.let {
                        Text(
                            text = stringResource(R.string.diagnostic_part_code, it),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Icon(
                    imageVector = if (expanded) {
                        Icons.Filled.KeyboardArrowUp
                    } else {
                        Icons.Filled.KeyboardArrowDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }

            if (expanded) {
                val supplierIds = part.detail.supplier.orEmpty().map { it.id }
                val technicianIds = part.detail.technicalContacts.orEmpty().map { it.id }

                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    part.qty?.let { Text(text = stringResource(R.string.diagnostic_part_qty, it)) }
                    if (supplierIds.isNotEmpty()) {
                        Text(text = stringResource(R.string.diagnostic_part_supplier, supplierIds.joinToString(", ")))
                    }
                    if (technicianIds.isNotEmpty()) {
                        Text(text = stringResource(R.string.diagnostic_part_contacts, technicianIds.joinToString(", ")))
                    }
                }

                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { onContactClick(supplierIds, technicianIds) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.diagnostic_part_contact_support))
                }

                if (resId != 0) {
                    Spacer(Modifier.height(12.dp))
                    ZoomablePartImage(
                        resId = resId,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                }
            }
        }
    }
}
