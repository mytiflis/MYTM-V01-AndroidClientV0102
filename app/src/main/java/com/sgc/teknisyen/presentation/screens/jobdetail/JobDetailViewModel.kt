package com.sgc.teknisyen.presentation.screens.jobdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgc.teknisyen.data.remote.dto.JobDto
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.JobRepository
import com.sgc.teknisyen.domain.model.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobDetailViewModel @Inject constructor(
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _job = MutableStateFlow<Job?>(null)
    val job = _job.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var currentJobNo: String = ""

    fun loadJob(jobNo: String) {
        currentJobNo = jobNo
        _isLoading.value = true
        viewModelScope.launch {
            val result = jobRepository.getJobDetail(jobNo)
            _isLoading.value = false
            when (result) {
                is JobRepository.Result.Success -> {
                    _job.value = result.data
                }
                is JobRepository.Result.Error -> {
                    // Error handling
                }
            }
        }
    }

    fun checkIn() {
        val job = _job.value ?: return
        val technician = authRepository.getCurrentTechnician() ?: return
        val request = JobDto.CheckInRequest.createCheckInRequest(
            lat = 0.0,
            lng = 0.0,
            accuracy = 0f,
            isMock = false,
            source = "fused",
            userAgent = "SGC-Tech/1.0.0 (Android 14; ${android.os.Build.MODEL})"
        )
        viewModelScope.launch {
            jobRepository.checkIn(currentJobNo, request)
        }
    }

    fun checkOut() {
        val job = _job.value ?: return
        val technician = authRepository.getCurrentTechnician() ?: return
        val request = JobDto.CheckOutRequest.createCheckOutRequest(
            lat = 0.0,
            lng = 0.0,
            accuracy = 0f,
            isMock = false,
            source = "fused",
            userAgent = "SGC-Tech/1.0.0 (Android 14; ${android.os.Build.MODEL})"
        )
        viewModelScope.launch {
            jobRepository.checkOut(currentJobNo, request)
        }
    }

    fun startJob() {
        viewModelScope.launch {
            jobRepository.updateStatus(currentJobNo, "BASLADI")
            loadJob(currentJobNo)
        }
    }

    fun completeJob() {
        viewModelScope.launch {
            jobRepository.updateStatus(currentJobNo, "TAMAMLANDI")
            loadJob(currentJobNo)
        }
    }

    fun openForm() {
        // Navigate to form screen
    }

    fun openPhoto() {
        // Open camera
    }
}