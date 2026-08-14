package com.powerwatch.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * PowerWatch's mark: a bolt on a dark, gold-ringed disc. Self-contained
 * (no drawable asset needed), so it renders correctly regardless of
 * light/dark theme.
 */
@Composable
fun AppLogo(modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 88.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.secondary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = "PowerWatch logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
