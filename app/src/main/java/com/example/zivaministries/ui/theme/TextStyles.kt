package com.example.zivaministries.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun magazineTitleStyle() = TextStyle(
    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 18.sp,
    letterSpacing = 0.5.sp
)

@Composable
fun magazineSubtitleStyle() = TextStyle(
    fontFamily = MaterialTheme.typography.titleMedium.fontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    letterSpacing = 0.5.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun magazineMetaStyle() = TextStyle(
    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 11.sp,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)

@Composable
fun goldAccentStyle() = TextStyle(
    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    color = Gold80
)