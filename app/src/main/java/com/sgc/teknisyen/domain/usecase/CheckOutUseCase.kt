package com.sgc.teknisyen.domain.usecase

import com.sgc.teknisyen.data.remote.dto.JobDto
import com.sgc.teknisyen.data.repository.JobRepository
import javax.inject.Inject

class CheckOutUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    operator fun invoke(
        jobNo: String,
        request: JobDto.CheckOutRequest
    ): JobRepository.Result<JobDto.CheckOutResponse> {
        return jobRepository.checkOut(jobNo, request)
    }
}