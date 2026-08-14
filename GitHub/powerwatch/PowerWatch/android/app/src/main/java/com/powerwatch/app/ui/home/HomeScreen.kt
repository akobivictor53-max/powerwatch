package com.powerwatch.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powerwatch.app.ui.common.AppLogo
import com.powerwatch.app.ui.common.CommunityReportsSection
import com.powerwatch.app.ui.common.DiscoSelector
import com.powerwatch.app.ui.common.ErrorBanner
import com.powerwatch.app.ui.common.MeterResultCard

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AppLogo()

            Text(
                text = "Verify your prepaid meter",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Check that a meter number is genuine before you buy units.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            DiscoSelector(
                discos = uiState.discos,
                selectedCode = uiState.selectedDiscoCode,
                onDiscoSelected = viewModel::onDiscoSelected,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.meterNumberInput,
                onValueChange = viewModel::onMeterNumberChanged,
                label = { Text("Meter number") },
                placeholder = { Text("e.g. 04512345678") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            val isLoading = uiState.verifyStatus is VerifyStatus.Loading
            Button(
                onClick = viewModel::onVerifyClicked,
                enabled = !isLoading && uiState.meterNumberInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp).then(Modifier),
                        strokeWidth = 2.dp
                    )
                    Text("Verifying…")
                } else {
                    Text("Verify Meter")
                }
            }

            when (val status = uiState.verifyStatus) {
                is VerifyStatus.Success -> MeterResultCard(result = status.result, modifier = Modifier.fillMaxWidth())
                is VerifyStatus.Error -> ErrorBanner(
                    message = status.message,
                    onDismiss = viewModel::dismissError,
                    modifier = Modifier.fillMaxWidth()
                )
                else -> Unit
            }

            if (uiState.selectedDiscoCode != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                CommunityReportsSection(
                    reports = uiState.communityReports,
                    loading = uiState.communityReportsLoading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
