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
package com.shaikhmohammadtalha.anatomyinsight

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.shaikhmohammadtalha.anatomyinsight.ui.theme.AnatomyInsightTheme

@Composable
fun ExpandableCard(
    title: String,
    description: String
) {
    var expandedState by remember { mutableStateOf(true) }
    val rotationState by animateFloatAsState(
        targetValue = if (expandedState) 180f else 0f, label = ""
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp) // ✅ Adds padding around the card
            .shadow(
                elevation = 8.dp, // ✅ Creates a glow effect
                shape = RoundedCornerShape(12.dp), // ✅ Rounded corners
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), // ✅ Glowing color
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Brush.radialGradient( // ✅ Gradient border for glow
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                ), RoundedCornerShape(12.dp))
                .animateContentSize(),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = { expandedState = !expandedState }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // 🔹 Title + Expand Button
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(6f),
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                        fontWeight = FontWeight.Bold,
                        lineHeight = MaterialTheme.typography.headlineMedium.fontSize * 1.2f, // ✅ Fix added here
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
                // ✅ Fix: Add spacing between Title and Description
                Spacer(modifier = Modifier.height(8.dp))


                // 🔹 Description Handling (Always Follows Proper Formatting)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    val lines = description.split("\n").filter { it.isNotBlank() }

                    // ✅ Parse the text to handle Bold (`**text**`) and Bullets (`-`)
                    val formattedText = buildAnnotatedString {
                        lines.forEach { line ->
                            if (line.startsWith("-")) {
                                // Bullet Point Formatting
                                append("• ")
                                val textWithoutBullet = line.removePrefix("-").trim()
                                appendStyledText(textWithoutBullet)
                            } else {
                                // Normal Paragraph or Bold Text Handling
                                appendStyledText(line)
                            }
                            append("\n") // Add a line break after each item
                        }
                    }

                    // ✅ Show formatted text (Collapses after 3 lines)
                    if (expandedState) {
                        Text(
                            text = formattedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = formattedText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
// 🔹 Helper function to handle Bold (`**text**`) in Text
fun AnnotatedString.Builder.appendStyledText(input: String) {
    val regex = Regex("\\*\\*(.*?)\\*\\*") // Detects **bold text**
    val matches = regex.findAll(input)

    var lastIndex = 0
    for (match in matches) {
        val start = match.range.first
        val end = match.range.last + 1

        // Append normal text before bold part
        append(input.substring(lastIndex, start))

        // Apply bold style
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1]) // Extracts text inside **bold**
        }

        lastIndex = end
    }

    // Append any remaining normal text
    if (lastIndex < input.length) {
        append(input.substring(lastIndex))
    }
}


@Preview(showBackground = true)
@Composable
fun ExpandableCardPreview() {
    AnatomyInsightTheme(darkTheme = true) {
        ExpandableCard(
            title = "Splanchnology (Study of Viscera/Organs)",
            description ="- **Scientific Name:** Systema splanchnicum\n" +
                    "\n" +
                    "Splanchnology is the study of internal organs (viscera), including:\n" +
                    "\n" +
                    "- **Digestive system:** Stomach, intestines, liver, pancreas.\n" +
                    "- **Respiratory system:** Lungs, trachea.\n" +
                    "- **Urogenital system:** Kidneys, bladder, reproductive organs.\n" +
                    "\n" +
                    "These systems work together to support digestion, respiration, reproduction, and excretion.\n" +
                    "\n" +
                    "**Source:** Gray’s Anatomy (1918 Edition) - Public Domain\n"
        )
    }
}

