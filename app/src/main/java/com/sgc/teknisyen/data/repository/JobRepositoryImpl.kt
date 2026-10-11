package com.sgc.teknisyen.data.repository

import com.sgc.teknisyen.data.local.dao.JobDao
import com.sgc.teknisyen.data.local.entity.CachedJobEntity
import com.sgc.teknisyen.data.remote.ApiService
import com.sgc.teknisyen.data.remote.dto.JobDto
import com.sgc.teknisyen.domain.model.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val jobDao: JobDao
) : JobRepository {

    override suspend fun getJobs(technicianId: Int): Result<List<Job>> {
        return try {
            val response = apiService.getJobs(technicianId)
            if (response.isSuccessful && response.body() != null) {
                val jobs = response.body()!!.map { Job.fromListDto(it) }
                response.body()?.let { dtoList ->
                    dtoList.forEach { dto ->
                        val entity = CachedJobEntity(
                            no = dto.no,
                            firma = dto.firma,
                            adres = "",
                            telefon = null,
                            tarih = dto.tarih,
                            oncelik = dto.oncelik,
                            is_durumu = dto.is_durumu,
                            urunler_json = "[]",
                            latitude = null,
                            longitude = null
                        )
                        jobDao.insert(entity)
                    }
                }
                Result.Success(jobs)
            } else {
                val cached = jobDao.getAll().map { it.toDomain() }
                Result.Success(cached)
            }
        } catch (e: Exception) {
            val cached = jobDao.getAll().map { it.toDomain() }
            Result.Success(cached)
        }
    }

    override suspend fun getJobDetail(jobNo: String): Result<Job> {
        return try {
            val response = apiService.getJobDetail(jobNo)
            if (response.isSuccessful && response.body() != null) {
                val job = Job.fromDto(response.body()!!)
                val dto = response.body()!!
                val entity = CachedJobEntity(
                    no = dto.no,
                    firma = dto.firma,
                    adres = dto.adres,
                    telefon = dto.telefon,
                    tarih = dto.tarih,
                    oncelik = dto.oncelik,
                    is_durumu = dto.is_durumu,
                    urunler_json = kotlinx.serialization.json.Json.encodeToString(dto.urunler),
                    latitude = dto.latitude,
                    longitude = dto.longitude
                )
                jobDao.insert(entity)
                Result.Success(job)
            } else {
                jobDao.getByNo(jobNo)?.let { entity ->
                    return Result.Success(entity.toDomain())
                }
                Result.Error(Exception("İş emri bulunamadı: $jobNo"))
            }
        } catch (e: Exception) {
            jobDao.getByNo(jobNo)?.let { entity ->
                return Result.Success(entity.toDomain())
            }
            Result.Error(e)
        }
    }

    override suspend fun checkIn(jobNo: String, request: JobDto.CheckInRequest): Result<JobDto.CheckInResponse> {
        return try {
            val response = apiService.checkIn(jobNo, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception(response.message() ?: "Check-in başarısız"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun checkOut(jobNo: String, request: JobDto.CheckOutRequest): Result<JobDto.CheckOutResponse> {
        return try {
            val response = apiService.checkOut(jobNo, request)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception(response.message() ?: "Check-out başarısız"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun updateStatus(jobNo: String, isDurumu: String): Result<JobDto.UpdateStatusResponse> {
        return try {
            val response = apiService.updateJobStatus(jobNo, JobDto.UpdateStatusRequest(isDurumu))
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(Exception(response.message() ?: "Durum güncelleme başarısız"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override suspend fun uploadPhoto(jobNo: String, filePath: String, description: String): Result<JobDto.PhotoUploadResponse> {
        return Result.Error(Exception("Not implemented yet"))
    }

    override fun observeJobs(): Flow<List<Job>> {
        return jobDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeJob(jobNo: String): Flow<Job?> {
        return jobDao.observeByNo(jobNo).map { entity ->
            entity?.toDomain()
        }
    }

    override suspend fun refreshFromServer(technicianId: Int) {
        getJobs(technicianId)
    }

    private fun CachedJobEntity.toDomain(): Job {
        val priority = when (oncelik.uppercase()) {
            "ACIL" -> Job.Priority.ACIL
            "DUSUK", "DÜŞÜK" -> Job.Priority.DUSUK
            else -> Job.Priority.NORMAL
        }

        val status = when (is_durumu.uppercase()) {
            "BASLADI", "BAŞLADI" -> Job.Status.BASLADI
            "TAMAMLANDI" -> Job.Status.TAMAMLANDI
            else -> Job.Status.ATANDI
        }

        val urunler = kotlinx.serialization.json.Json.decodeFromString<List<Job.Urun>>(urunler_json)

        return Job(
            no = no,
            firma = firma,
            adres = adres,
            telefon = telefon,
            tarih = tarih,
            oncelik = priority,
            isDurumu = status,
            urunler = urunler,
            latitude = latitude,
            longitude = longitude
        )
    }
}