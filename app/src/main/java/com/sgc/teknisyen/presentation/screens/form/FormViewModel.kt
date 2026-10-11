package com.sgc.teknisyen.presentation.screens.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.SyncRepository
import com.sgc.teknisyen.data.remote.dto.FormDto
import com.sgc.teknisyen.data.remote.dto.AuthDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FormViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _formState = MutableStateFlow(FormState())
    val formState = _formState.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    fun loadDraft(jobNo: String) {
        viewModelScope.launch {
            val drafts = syncRepository.getFormDrafts(jobNo)
            if (drafts.isNotEmpty()) {
                val draft = drafts[0]
                _formState.value = _formState.value.copy(
                    date = draft.tarih,
                    startTime = draft.baslangic_saati,
                    endTime = draft.bitis_saati,
                    subject = draft.konu,
                    workPerformed = draft.yapilan_isler,
                    materialsUsed = draft.kullanilan_malzemeler,
                    faults = draft.arizalar ?: "",
                    notes = draft.notlar ?: "",
                    signatureBase64 = draft.imza_base64,
                    photoBase64List = draft.foto_base64_list_json?.let {
                        kotlinx.serialization.json.Json.decodeFromString<List<String>>(it)
                    } ?: emptyList()
                )
            } else {
                // Set default date to today
                val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                _formState.value = _formState.value.copy(date = today)
            }
        }
    }

    fun updateDate(value: String) {
        _formState.value = _formState.value.copy(date = value)
    }

    fun updateStartTime(value: String) {
        _formState.value = _formState.value.copy(startTime = value)
    }

    fun updateEndTime(value: String) {
        _formState.value = _formState.value.copy(endTime = value)
    }

    fun updateSubject(value: String) {
        _formState.value = _formState.value.copy(subject = value)
    }

    fun updateWorkPerformed(value: String) {
        _formState.value = _formState.value.copy(workPerformed = value)
    }

    fun updateMaterialsUsed(value: String) {
        _formState.value = _formState.value.copy(materialsUsed = value)
    }

    fun updateFaults(value: String) {
        _formState.value = _formState.value.copy(faults = value)
    }

    fun updateNotes(value: String) {
        _formState.value = _formState.value.copy(notes = value)
    }

    fun updateSignature(base64: String?) {
        _formState.value = _formState.value.copy(signatureBase64 = base64)
    }

    fun updatePhotos(list: List<String>) {
        _formState.value = _formState.value.copy(photoBase64List = list)
    }

    fun saveDraft() {
        val technician = authRepository.getCurrentTechnician() ?: return
        val state = _formState.value

        val draft = FormDto.CachedForm(
            is_emri_no = "", // Would need jobNo passed in
            teknisyen_id = technician.id,
            tarih = state.date,
            baslangic_saati = state.startTime,
            bitis_saati = state.endTime,
            konu = state.subject,
            yapilan_isler = state.workPerformed,
            kullanilan_malzemeler = state.materialsUsed,
            arizalar = state.faults.ifBlank() { null },
            notlar = state.notes.ifBlank() { null },
            imza_base64 = state.signatureBase64,
            foto_base64_list_json = kotlinx.serialization.json.Json.encodeToString(state.photoBase64List),
            created_at = System.currentTimeMillis(),
            synced = false
        )

        viewModelScope.launch {
            syncRepository.saveFormDraft(draft)
        }
    }

    fun submitForm() {
        val technician = authRepository.getCurrentTechnician() ?: return
        val state = _formState.value

        if (!state.isValid) return

        _isSubmitting.value = true

        val request = FormDto.SubmitRequest(
            is_emri_no = "", // Would need jobNo
            teknisyen_id = technician.id,
            tarih = state.date,
            baslangic_saati = state.startTime,
            bitis_saati = state.endTime,
            konu = state.subject,
            yapilan_isler = state.workPerformed,
            kullanilan_malzemeler = state.materialsUsed,
            arizalar = state.faults.ifBlank() { null },
            notlar = state.notes.ifBlank() { null },
            imza_base64 = state.signatureBase64,
            foto_base64_list = state.photoBase64List
        )

        viewModelScope.launch {
            try {
                // syncRepository.submitForm(request) // Would need this method
                _isSubmitting.value = false
                // Navigate back on success
            } catch (e: Exception) {
                _isSubmitting.value = false
            }
        }
    }

    data class FormState(
        val date: String = "",
        val startTime: String = "",
        val endTime: String = "",
        val subject: String = "",
        val workPerformed: String = "",
        val materialsUsed: String = "",
        val faults: String = "",
        val notes: String = "",
        val signatureBase64: String? = null,
        val photoBase64List: List<String> = emptyList()
    ) {
        val isValid: Boolean
            get() = date.isNotBlank() && startTime.isNotBlank() && endTime.isNotBlank() &&
                    subject.isNotBlank() && workPerformed.isNotBlank()
    }
}