package com.android.snippets.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.snippets.ui.shapes.CookieShape
import com.android.snippets.ui.theme.GoogleSansFlexRound

@Composable
fun SelectedPhotoIndicator(
    isSelected: Boolean,
    selectionMode: Boolean,
    count: Int,
    modifier: Modifier = Modifier
) {
    if (!isSelected) return

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CookieShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CookieShape)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Text(
                text = count.toString(),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = GoogleSansFlexRound,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp
                )
            )
        }
    }
}

@Composable
fun ExpandableCardPillIcon(
    icon: ImageVector,
    text: String,
    contentDescription: String,
    iconColor: Color,
    containerColor: Color,
    modifier: Modifier = Modifier,
    alignEnd: Boolean = false,
    defaultExpanded: Boolean = false,
    key: Any? = null,
    maxChars: Int = 25
) {
    val view = LocalView.current
    var isExpanded by remember(key, defaultExpanded) { mutableStateOf(defaultExpanded) }

    val displayText = remember(text, maxChars) {
        if (maxChars > 0 && text.length > maxChars) text.take(maxChars) else text
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        contentColor = iconColor,
        modifier = modifier
            .padding(8.dp)
            .height(28.dp)
            .widthIn(min = 28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    isExpanded = !isExpanded
                }
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBox = @Composable {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            val textBox = @Composable {
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 150)) + expandHorizontally(
                        expandFrom = if (alignEnd) Alignment.End else Alignment.Start,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 100)) + shrinkHorizontally(
                        shrinkTowards = if (alignEnd) Alignment.End else Alignment.Start,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                ) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = iconColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier
                            .padding(
                                start = if (alignEnd) 8.dp else 2.dp,
                                end = if (alignEnd) 2.dp else 8.dp
                            )
                    )
                }
            }

            if (alignEnd) {
                textBox()
                iconBox()
            } else {
                iconBox()
                textBox()
            }
        }
    }
}
