package com.powerwatch.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.powerwatch.app.domain.model.CommunityReport
import com.powerwatch.app.domain.model.PowerStatus

/**
 * Displays community-submitted power status reports. This section is
 * ALWAYS clearly labeled as user-generated — it must never be presented
 * as official Sogo/Disco data.
 */
@Composable
fun CommunityReportsSection(
    reports: List<CommunityReport>,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.People, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("Community reports (user-submitted)", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            "These reports are submitted by other users, not verified by Sogo or any Disco.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        when {
            loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
            reports.isEmpty() -> Text(
                "No community reports yet for this Disco.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            else -> Column {
                reports.take(5).forEach { report ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(report.areaOrMeterHint, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (report.status == PowerStatus.ON) "Power ON" else "Power OFF",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (report.status == PowerStatus.ON) {
                                Color(0xFF2E7D32)
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
