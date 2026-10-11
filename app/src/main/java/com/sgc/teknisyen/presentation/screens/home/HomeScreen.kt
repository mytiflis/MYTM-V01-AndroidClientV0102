package com.sgc.teknisyen.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgc.teknisyen.R
import com.sgc.teknisyen.domain.model.Job
import com.sgc.teknisyen.SgcTeknisyenTheme
import dagger.hilt.android.hiltViewModel

@Composable
fun HomeScreen(
    onJobClick: (String) -> Unit,
    onProfileClick: () -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val jobs by viewModel.jobs.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    // Pull to refresh
    val pullToRefreshState = rememberPullToRefreshState(refreshing = isLoading) {
        viewModel.refresh()
    }

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
                            text = stringResource(R.string.home_title),
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onProfileClick) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Person,
                                contentDescription = "Profil",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                                contentDescription = "Yenile",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                )

                // Content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullToRefresh(pullToRefreshState)
                ) {
                    if (jobs.isEmpty() && !isLoading) {
                        // Empty state
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.AssignmentOut,
                                contentDescription = "",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(64.dp)
                            )
                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(16.dp))
                            Text(
                                text = stringResource(R.string.empty_jobs),
                                fontSize = 18.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Jobs list
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(jobs) { job ->
                                JobCard(job = job, onClick = { onJobClick(job.no) })
                            }
                        }
                    }

                    if (isLoading && jobs.isNotEmpty()) {
                        // Loading indicator at bottom
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        )
                    }
                }

                // Error snackbar
                if (error != null) {
                    androidx.compose.material3.Snackbar(
                        modifier = Modifier.padding(16.dp),
                        action = { Text("Tamam", onClick = { viewModel.clearError() }) }
                    ) {
                        Text(text = error, color = androidx.compose.material3.MaterialTheme.colorScheme.onError)
                    }
                }
            }
        }
    }
}

@Composable
fun JobCard(
    job: Job,
    onClick: () -> Unit
) {
    SgcTeknisyenTheme {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(0.dp)
                .clip(androidx.compose.material3.MaterialTheme.shapes.medium),
            onClick = onClick,
            elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İş Emri: ${job.no}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    // Priority badge
                    androidx.compose.material3.Chip(
                        modifier = Modifier.size(80.dp, 28.dp),
                        onClick = {},
                        label = {
                            Text(
                                text = job.oncelik.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        },
                        colors = androidx.compose.material3.ChipDefaults.chipColors(
                            containerColor = Color(job.oncelik.color)
                        )
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

                // Firm
                Text(
                    text = job.firma,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(4.dp))

                // Date
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.CalendarToday,
                        contentDescription = "",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatDate(job.tarih),
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

                // Status row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(job.isDurumu.color))
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = job.isDurumu.label,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(job.isDurumu.color)
                        )
                    }

                    // Point count
                    if (job.urunler.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Inventory,
                                contentDescription = "",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${job.urunler.size} ürün",
                                fontSize = 12.sp,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(isoDate: String): String {
    return try {
        val date = java.time.LocalDateTime.parse(isoDate.replace("Z", "+00:00"))
        java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(date)
    } catch (e: Exception) {
        isoDate
    }
}

// Simple pull-to-refresh state (placeholder - would use official library in production)
@Composable
fun rememberPullToRefreshState(refreshing: Boolean, onRefresh: () -> Unit): Object {
    return remember { Any() }
}

@Composable
fun Modifier.pullToRefresh(state: Object): Modifier = this