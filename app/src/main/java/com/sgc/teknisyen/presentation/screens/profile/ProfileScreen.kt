package com.sgc.teknisyen.presentation.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgc.teknisyen.R
import com.sgc.teknisyen.SgcTeknisyenTheme
import dagger.hilt.android.hiltViewModel

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val technician by viewModel.technician.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    val locationServiceActive by viewModel.locationServiceActive.collectAsStateWithLifecycle()

    SgcTeknisyenTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top App Bar
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(R.string.profile_title),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onLogout) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Logout,
                                contentDescription = "Çıkış",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                )

                // Content
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile header
                    technician?.let { tech ->
                        ProfileHeaderCard(technician = tech)
                    }

                    // Sync status
                    SyncStatusCard(
                        pendingCount = pendingCount,
                        onSyncNow = { viewModel.triggerSync() }
                    )

                    // Location service toggle
                    LocationServiceCard(
                        isActive = locationServiceActive,
                        onToggle = { viewModel.toggleLocationService() }
                    )

                    // App info
                    AppInfoCard()

                    // Logout button
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.errorContainer,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.logout),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeaderCard(technician: com.sgc.teknisyen.data.remote.dto.AuthDto.TechnicianInfo) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(24.dp)) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = technician.firstName.firstOrNull().toString().uppercase() +
                               technician.lastName.firstOrNull().toString().uppercase(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = "${technician.firstName} ${technician.lastName}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            technician.phone?.let { phone ->
                Text(
                    text = phone,
                    fontSize = 16.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SyncStatusCard(
    pendingCount: Int,
    onSyncNow: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Senkronizasyon",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (pendingCount > 0) {
                    androidx.compose.material3.Chip(
                        onClick = {},
                        label = {
                            Text("$pendingCount bekliyor", color = Color.White, fontSize = 12.sp)
                        },
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = Color(0xFFFF9800)
                        )
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = if (pendingCount > 0) {
                    stringResource(R.string.sync_status_pending, pendingCount)
                } else {
                    stringResource(R.string.sync_status_synced)
                },
                fontSize = 14.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (pendingCount > 0) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Button(
                    onClick = onSyncNow,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                ) {
                    Text("Şimdi Senkronize Et")
                }
            }
        }
    }
}

@Composable
fun LocationServiceCard(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Konum Takibi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isActive) stringResource(R.string.location_service_active)
                        else stringResource(R.string.location_service_inactive),
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                androidx.compose.material3.Switch(
                    checked = isActive,
                    onCheckedChange = { onToggle() },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        thumbColor = if (isActive) Color.White else Color.White,
                        trackColor = if (isActive)
                            androidx.compose.material3.MaterialTheme.colorScheme.primary
                        else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

            Text(
                text = "Arka planda konum paylaşımı ve heartbeat gönderimi için bu servis açık olmalıdır.",
                fontSize = 12.sp,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AppInfoCard() {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Uygulama Bilgileri",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))

            InfoRow("Versiyon", "1.0.0")
            InfoRow("Paket", "com.sgc.teknisyen")
            InfoRow("Min SDK", "24 (Android 7.0)")
            InfoRow("Hedef SDK", "34 (Android 14)")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}