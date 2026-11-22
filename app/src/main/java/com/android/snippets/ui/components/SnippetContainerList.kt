package com.android.snippets.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.android.snippets.ui.theme.GoogleSansFlexRound
import com.android.snippets.ui.theme.getSnippetTextStyle
import com.android.snippets.ui.theme.rememberSnippetBackgroundColor
import com.android.snippets.ui.theme.rememberSnippetTextColor
import com.ln.android.snippets.R

@Composable
fun SnippetContainerList(
    snippets: List<String>,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val density = LocalDensity.current
        val textMeasurer = rememberTextMeasurer()
        val textStyle = getSnippetTextStyle(
            com.android.snippets.model.SnippetStyle.Default,
            MaterialTheme.typography.labelLarge.copy(
                fontFamily = GoogleSansFlexRound,
                fontWeight = FontWeight.SemiBold
            ),
            isCloud = true
        )

        val cardMaxWidth = if (maxWidth.isSpecified && maxWidth < Dp.Infinity) maxWidth else 400.dp
        // Card padding is 16.dp start + 16.dp end = 32.dp
        // Container inner padding is 10.dp start + 10.dp end = 20.dp
        val maxInnerWidth = (cardMaxWidth - 32.dp - 20.dp).coerceAtLeast(0.dp)
        val chipSpacing = 6.dp

        val (firstRow, secondRow) = remember(snippets, maxInnerWidth, textStyle, density) {
            if (snippets.isEmpty()) {
                emptyList<String>() to emptyList<String>()
            } else {
                val row1 = mutableListOf<String>()
                val row2 = mutableListOf<String>()
                var currentWidth = 0.dp

                for (i in snippets.indices) {
                    val snippet = snippets[i]
                    val textWidthPx = textMeasurer.measure(snippet, textStyle).size.width
                    val textWidthDp = with(density) { textWidthPx.toDp() }
                    // Snippet pill has 8.dp horizontal padding on start and end = 16.dp
                    val chipWidth = textWidthDp + 16.dp

                    val neededWidth = if (row1.isEmpty()) chipWidth else chipSpacing + chipWidth

                    if (row2.isEmpty() && (row1.isEmpty() || currentWidth + neededWidth <= maxInnerWidth)) {
                        row1.add(snippet)
                        currentWidth += neededWidth
                    } else {
                        row2.add(snippet)
                    }
                }
                row1 to row2
            }
        }

        val hasSecondContainer = secondRow.isNotEmpty()
        val firstContainerShape = if (hasSecondContainer) {
            RoundedCornerShape(4.dp)
        } else {
            RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 4.dp,
                bottomEnd = 16.dp,
                bottomStart = 16.dp
            )
        }
        val secondContainerShape = RoundedCornerShape(
            topStart = 4.dp,
            topEnd = 4.dp,
            bottomEnd = 16.dp,
            bottomStart = 16.dp
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (snippets.isEmpty()) {
                SegmentedSnippetContainer(
                    snippets = emptyList(),
                    shape = firstContainerShape,
                    isSelected = isSelected,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            } else if (hasSecondContainer) {
                SegmentedSnippetContainer(
                    snippets = firstRow,
                    shape = firstContainerShape,
                    isSelected = isSelected,
                    onClick = onClick,
                    onLongClick = onLongClick
                )

                SegmentedSnippetContainer(
                    snippets = secondRow,
                    shape = secondContainerShape,
                    isSelected = isSelected,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            } else {
                SegmentedSnippetContainer(
                    snippets = firstRow,
                    shape = firstContainerShape,
                    isSelected = isSelected,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SegmentedSnippetContainer(
    snippets: List<String>,
    shape: Shape,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val containerInteractionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .padding(start = 16.dp, end = 16.dp)
            .wrapContentWidth(Alignment.Start)
            .combinedClickable(
                interactionSource = containerInteractionSource,
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                },
                onLongClick = {
                    onLongClick?.let {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        it.invoke()
                    }
                }
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        if (snippets.isEmpty()) {
            Box(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_text_snippet_off),
                    contentDescription = "No snippets",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                snippets.forEach { snippet ->
                    SnippetPill(
                        snippet = snippet,
                        isSelected = isSelected,
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SnippetPill(
    snippet: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val view = LocalView.current
    val snippetColor = rememberSnippetBackgroundColor()
    val snippetInteractionSource = remember { MutableInteractionSource() }

    Surface(
        shape = CircleShape,
        color = if (isSelected) MaterialTheme.colorScheme.surface.copy(alpha = 0.88f) else snippetColor,
        border = null,
        modifier = Modifier
            .wrapContentWidth()
            .clip(CircleShape)
            .combinedClickable(
                interactionSource = snippetInteractionSource,
                indication = ripple(color = MaterialTheme.colorScheme.primary),
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    onClick()
                },
                onLongClick = {
                    onLongClick?.let {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        it.invoke()
                    }
                }
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = snippet,
                style = getSnippetTextStyle(
                    com.android.snippets.model.SnippetStyle.Default,
                    MaterialTheme.typography.labelLarge.copy(
                        fontFamily = GoogleSansFlexRound,
                        fontWeight = FontWeight.SemiBold
                    ),
                    isCloud = true
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary else rememberSnippetTextColor(),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}
