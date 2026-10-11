package com.sgc.teknisyen.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgc.teknisyen.data.repository.AuthRepository
import com.sgc.teknisyen.data.repository.SyncRepository
import com.sgc.teknisyen.data.remote.dto.AuthDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {

    private val _technician = MutableStateFlow<AuthDto.TechnicianInfo?>(null)
    val technician = _technician.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount = _pendingCount.asStateFlow()

    private val _locationServiceActive = MutableStateFlow(false)
    val locationServiceActive = _locationServiceActive.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _technician.value = authRepository.getCurrentTechnician()
        loadPendingCount()
        // Location service status would come from a preference/service manager
    }

    fun loadPendingCount() {
        viewModelScope.launch {
            val count = syncRepository.getPendingOperations().size
            _pendingCount.value = count
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            syncRepository.performBatchSync()
            loadPendingCount()
        }
    }

    fun toggleLocationService() {
        val newState = !_locationServiceActive.value
        _locationServiceActive.value = newState
        // Would start/stop LocationService here
    }
}