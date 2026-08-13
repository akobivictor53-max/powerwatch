package com.powerwatch.app.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.spacedBy
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.powerwatch.app.domain.model.MeterVerification

@Composable
fun MeterResultCard(result: MeterVerification, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32)
                )
                androidx.compose.foundation.layout.Spacer(Modifier.padding(start = 8.dp))
                Text("Meter verified", style = MaterialTheme.typography.titleLarge)
            }

            result.customerName?.let { ResultRow("Customer name", it) }
            result.meterNumberMasked?.let { ResultRow("Meter number", it) }
            result.meterType?.let { ResultRow("Meter type", it.replaceFirstChar(Char::uppercase)) }
            result.discoCode?.let { ResultRow("Disco", it) }
            result.address?.let { ResultRow("Address", it) }
            // outageStatus is intentionally only shown if the backend actually
            // returned it — never fabricated on-device.
            result.outageStatus?.let { ResultRow("Power status", it.replaceFirstChar(Char::uppercase)) }
        }
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}
