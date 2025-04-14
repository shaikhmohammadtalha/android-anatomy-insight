/*
 * Copyright 2025 Shaikh Mohammad Talha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shaikhmohammadtalha.anatomyinsight.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * ExpandableCard displays a titled content section that can toggle between expanded and collapsed states.
 * It supports rich text formatting including bold and bullet points in the description.
 *
 * @param title The title displayed at the top of the card
 * @param description Multi-line formatted text shown when expanded
 */
@Composable
fun ExpandableCard(
    title: String,
    description: String
) {
    var expandedState by remember { mutableStateOf(true) }

    // Arrow rotation animation for expand/collapse icon
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f,
        label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    2.dp, Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    ), RoundedCornerShape(12.dp)
                )
                .animateContentSize(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { expandedState = !expandedState }
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {

                // Header row: Title and expand toggle button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.weight(6f),
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        fontWeight = FontWeight.Bold,
                        lineHeight = MaterialTheme.typography.headlineMedium.fontSize * 1.2f,
                        maxLines = if (expandedState) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    IconButton(
                        modifier = Modifier
                            .weight(1f)
                            .rotate(rotationState),
                        onClick = { expandedState = !expandedState }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Drop-Down Arrow",
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Body: Description block with formatting support
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    val lines = description.split("\n").filter { it.isNotBlank() }

                    // Build rich text with bold and bullet point handling
                    val formattedText = buildAnnotatedString {
                        lines.forEach { line ->
                            if (line.startsWith("-")) {
                                append("• ")
                                val textWithoutBullet = line.removePrefix("-").trim()
                                appendStyledText(textWithoutBullet)
                            } else {
                                appendStyledText(line)
                            }
                            append("\n")
                        }
                    }

                    // Show full or truncated description based on state
                    Text(
                        text = formattedText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = if (expandedState) Int.MAX_VALUE else 3,
                        overflow = if (expandedState) TextOverflow.Clip else TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Parses bold text enclosed in **double asterisks** and applies bold styling.
 * Used within ExpandableCard's description formatter.
 */
fun AnnotatedString.Builder.appendStyledText(input: String) {
    val regex = Regex("\\*\\*(.*?)\\*\\*")
    val matches = regex.findAll(input)

    var lastIndex = 0
    for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        // Append unstyled text before match
        append(input.substring(lastIndex, start))

        // Apply bold style to matched content
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }

        lastIndex = end
    }

    // Append remaining plain text after last match
    if (lastIndex < input.length) {
        append(input.substring(lastIndex))
    }
}
