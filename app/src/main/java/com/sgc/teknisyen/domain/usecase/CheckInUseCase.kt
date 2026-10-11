package com.sgc.teknisyen.domain.usecase

import com.sgc.teknisyen.data.remote.dto.JobDto
import com.sgc.teknisyen.data.repository.JobRepository
import javax.inject.Inject

class CheckInUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    operator fun invoke(
        jobNo: String,
        request: JobDto.CheckInRequest
    ): JobRepository.Result<JobDto.CheckInResponse> {
        return jobRepository.checkIn(jobNo, request)
    }
}