package com.sgc.teknisyen.presentation.screens.jobdetail

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
import androidx.compose.material3.Chip
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
import com.sgc.teknisyen.domain.model.Job
import com.sgc.teknisyen.SgcTeknisyenTheme
import dagger.hilt.android.hiltViewModel

@Composable
fun JobDetailScreen(
    jobNo: String,
    onBack: () -> Unit
) {
    val viewModel: JobDetailViewModel = hiltViewModel()
    val job by viewModel.job.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

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
                            text = "İş Emri: $jobNo",
                            fontWeight = FontWeight.Bold,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                                contentDescription = "Geri",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                )

                if (isLoading && job == null) {
                    // Loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else if (job != null) {
                    // Content
                    androidx.compose.foundation.layout.Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Job header card
                        JobHeaderCard(job = job)

                        // Products section
                        if (job.urunler.isNotEmpty()) {
                            ProductsSection(products = job.urunler)
                        }

                        // Actions
                        ActionsSection(
                            job = job,
                            onCheckIn = { viewModel.checkIn() },
                            onCheckOut = { viewModel.checkOut() },
                            onStart = { viewModel.startJob() },
                            onComplete = { viewModel.completeJob() },
                            onForm = { viewModel.openForm() },
                            onPhoto = { viewModel.openPhoto() }
                        )
                    }
                } else {
                    // Error
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "İş emri yüklenemedi",
                            fontSize = 18.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JobHeaderCard(job: Job) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = job.firma,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = job.no,
                        fontSize = 14.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Chip(
                    onClick = {},
                    label = {
                        Text(job.oncelik.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    },
                    colors = androidx.compose.material3.ChipDefaults.chipColors(containerColor = Color(job.oncelik.color))
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))

            // Address
            if (job.adres.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.LocationOn,
                        contentDescription = "",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = job.adres,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
            }

            // Phone
            job.telefon?.let { phone ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Phone,
                        contentDescription = "",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = phone,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))
            }

            // Date
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CalendarToday,
                    contentDescription = "",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Oluşturulma: ${formatDate(job.tarih)}",
                    fontSize = 14.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(8.dp))

            // Status
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
                    text = "Durum: ${job.isDurumu.label}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(job.isDurumu.color)
                )
            }
        }
    }
}

@Composable
fun ProductsSection(products: List<Job.Urun>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Ürünler (${products.size})",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductRow(product = product)
                }
            }
        }
    }
}

@Composable
fun ProductRow(product: Job.Urun) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = product.adi,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            product.aciklama?.let { desc ->
                Text(
                    text = desc,
                    fontSize = 12.sp,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${product.miktar} ${product.birim}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ActionsSection(
    job: Job,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onForm: () -> Unit,
    onPhoto: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Primary actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onCheckIn,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Check-in", fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = onCheckOut,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Check-out", fontWeight = FontWeight.Medium)
            }
        }

        // Secondary actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f),
                enabled = job.isDurumu != Job.Status.BASLADI && job.isDurumu != Job.Status.TAMAMLANDI,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("Başlat", fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = onComplete,
                modifier = Modifier.weight(1f),
                enabled = job.isDurumu == Job.Status.BASLADI,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50)
                )
            ) {
                Text("Tamamla", fontWeight = FontWeight.Medium)
            }
        }

        // Utility actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onForm,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Form", fontWeight = FontWeight.Medium)
            }

            Button(
                onClick = onPhoto,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Fotoğraf", fontWeight = FontWeight.Medium)
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