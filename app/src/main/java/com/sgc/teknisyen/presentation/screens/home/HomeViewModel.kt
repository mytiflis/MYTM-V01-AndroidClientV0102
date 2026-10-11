package com.sgc.teknisyen.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.JobRepository
import com.sgc.teknisyen.domain.model.Job
import com.sgc.teknisyen.domain.usecase.GetJobsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getJobsUseCase: GetJobsUseCase,
    private val jobRepository: JobRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs = _jobs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        loadJobs()
    }

    fun loadJobs() {
        val technician = authRepository.getCurrentTechnician() ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = getJobsUseCase(technician.id)
            _isLoading.value = false
            when (result) {
                is JobRepository.Result.Success -> {
                    _jobs.value = result.data
                }
                is JobRepository.Result.Error -> {
                    _error.value = result.exception.message ?: "İşler yüklenemedi"
                }
            }
        }
    }

    fun refresh() {
        loadJobs()
    }

    fun clearError() {
        _error.value = null
    }
}