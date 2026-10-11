package com.sgc.teknisyen.domain.usecase

import com.sgc.teknisyen.data.repository.JobRepository
import com.sgc.teknisyen.domain.model.Job
import javax.inject.Inject

class GetJobsUseCase @Inject constructor(
    private val jobRepository: JobRepository
) {
    operator fun invoke(technicianId: Int): JobRepository.Result<List<Job>> {
        return jobRepository.getJobs(technicianId)
    }
}