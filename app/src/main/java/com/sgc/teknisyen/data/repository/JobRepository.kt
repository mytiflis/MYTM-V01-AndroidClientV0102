package com.sgc.teknisyen.data.repository

import com.sgc.teknisyen.data.remote.dto.JobDto
import com.sgc.teknisyen.domain.model.Job
import kotlinx.coroutines.flow.Flow

interface JobRepository {

    suspend fun getJobs(technicianId: Int): Result<List<Job>>

    suspend fun getJobDetail(jobNo: String): Result<Job>

    suspend fun checkIn(jobNo: String, request: JobDto.CheckInRequest): Result<JobDto.CheckInResponse>

    suspend fun checkOut(jobNo: String, request: JobDto.CheckOutRequest): Result<JobDto.CheckOutResponse>

    suspend fun updateStatus(jobNo: String, isDurumu: String): Result<JobDto.UpdateStatusResponse>

    suspend fun uploadPhoto(jobNo: String, filePath: String, description: String): Result<JobDto.PhotoUploadResponse>

    fun observeJobs(): Flow<List<Job>>

    fun observeJob(jobNo: String): Flow<Job?>

    suspend fun refreshFromServer(technicianId: Int)

    sealed class Result<out T> {
        data class Success<out T>(val data: T) : Result<T>()
        data class Error(val exception: Throwable) : Result<Nothing>()
    }
}