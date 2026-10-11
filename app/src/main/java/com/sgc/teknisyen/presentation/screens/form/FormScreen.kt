package com.sgc.teknisyen.presentation.screens.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sgc.teknisyen.R
import com.sgc.teknisyen.SgcTeknisyenTheme
import dagger.hilt.android.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScreen(
    jobNo: String,
    onBack: () -> Unit
) {
    val viewModel: FormViewModel = hiltViewModel()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    viewModel.loadDraft(jobNo)

    SgcTeknisyenTheme {
        androidx.compose.material3.Surface(
            modifier = Modifier.fillMaxSize(),
            color = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = "${stringResource(R.string.form_title)} - $jobNo",
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

                androidx.compose.foundation.layout.Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Date & Time row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = formState.date,
                            onValueChange = { viewModel.updateDate(it) },
                            label = { Text(stringResource(R.string.form_date)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default
                        )
                        OutlinedTextField(
                            value = formState.startTime,
                            onValueChange = { viewModel.updateStartTime(it) },
                            label = { Text(stringResource(R.string.form_start_time)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default
                        )
                        OutlinedTextField(
                            value = formState.endTime,
                            onValueChange = { viewModel.updateEndTime(it) },
                            label = { Text(stringResource(R.string.form_end_time)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default
                        )
                    }

                    // Subject
                    OutlinedTextField(
                        value = formState.subject,
                        onValueChange = { viewModel.updateSubject(it) },
                        label = { Text(stringResource(R.string.form_subject)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Work performed
                    OutlinedTextField(
                        value = formState.workPerformed,
                        onValueChange = { viewModel.updateWorkPerformed(it) },
                        label = { Text(stringResource(R.string.form_work_performed)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        maxLines = 6
                    )

                    // Materials used
                    OutlinedTextField(
                        value = formState.materialsUsed,
                        onValueChange = { viewModel.updateMaterialsUsed(it) },
                        label = { Text(stringResource(R.string.form_materials_used)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Faults
                    OutlinedTextField(
                        value = formState.faults,
                        onValueChange = { viewModel.updateFaults(it) },
                        label = { Text(stringResource(R.string.form_faults)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Notes
                    OutlinedTextField(
                        value = formState.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        label = { Text(stringResource(R.string.form_notes)) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )

                    // Signature placeholder
                    SignatureSection(
                        signature = formState.signatureBase64,
                        onSignatureChanged = { viewModel.updateSignature(it) }
                    )

                    // Photos
                    PhotoSection(
                        photos = formState.photoBase64List,
                        onPhotosChanged = { viewModel.updatePhotos(it) }
                    )

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.saveDraft() },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                            enabled = !isSubmitting
                        ) {
                            Text(stringResource(R.string.btn_save_draft))
                        }

                        Button(
                            onClick = { viewModel.submitForm() },
                            modifier = Modifier.weight(1f),
                            enabled = !isSubmitting && formState.isValid
                        ) {
                            if (isSubmitting) {
                                androidx.compose.material.ProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(stringResource(R.string.btn_submit))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SignatureSection(
    signature: String?,
    onSignatureChanged: (String?) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "İmza",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .border(1.dp, androidx.compose.material3.MaterialTheme.colorScheme.outline, androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (signature != null && signature.isNotBlank()) {
                    // Show signature image (base64)
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.foundation.rememberImagePainter(
                            data = "data:image/png;base64,$signature"
                        ),
                        contentDescription = "İmza",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "İmza için tıklayın",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoSection(
    photos: List<String>,
    onPhotosChanged: (List<String>) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = 2.dp) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fotoğraflar (${photos.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (photos.size < 5) {
                    IconButton(onClick = { /* TODO: Open camera/gallery */ }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.AddAPhoto,
                            contentDescription = "Fotoğraf ekle"
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))

            if (photos.isEmpty()) {
                Text(
                    text = "Henüz fotoğraf yok",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .wrapContentSize(Alignment.Center)
                )
            } else {
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos) { photo ->
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.foundation.rememberImagePainter(
                                    data = "data:image/png;base64,$photo"
                                ),
                                contentDescription = "Fotoğraf",
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}