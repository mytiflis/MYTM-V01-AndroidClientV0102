package com.sgc.teknisyen.data.remote

import com.sgc.teknisyen.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ============================================================
    // AUTH
    // ============================================================

    @POST("auth/login")
    suspend fun login(@Body request: AuthDto.LoginRequest): Response<AuthDto.LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<AuthDto.LogoutResponse>

    @GET("auth/me")
    suspend fun getMe(): Response<AuthDto.MeResponse>

    // ============================================================
    // HEARTBEAT
    // ============================================================

    @POST("heartbeat")
    suspend fun sendHeartbeat(@Body request: HeartbeatDto.Request): Response<HeartbeatDto.Response>

    // ============================================================
    // LOCATION REQUEST (Server -> APK)
    // ============================================================

    @GET("konum-istek/bekleyen")
    suspend fun getPendingLocationRequest(): Response<LocationRequestDto.PendingResponse>

    @POST("konum-gonder")
    suspend fun sendLocation(@Body request: LocationRequestDto.SendRequest): Response<LocationRequestDto.SendResponse>

    // ============================================================
    // JOBS (İş Emirleri)
    // ============================================================

    @GET("is-emirleri")
    suspend fun getJobs(
        @Query("atanan_id") technicianId: Int
    ): Response<List<JobDto.ListItem>>

    @GET("is-emirleri/{no}")
    suspend fun getJobDetail(@Path("no") jobNo: String): Response<JobDto.Detail>

    @POST("is-emirleri/{no}/checkin")
    suspend fun checkIn(
        @Path("no") jobNo: String,
        @Body request: JobDto.CheckInRequest
    ): Response<JobDto.CheckInResponse>

    @POST("is-emirleri/{no}/checkout")
    suspend fun checkOut(
        @Path("no") jobNo: String,
        @Body request: JobDto.CheckOutRequest
    ): Response<JobDto.CheckOutResponse>

    @POST("is-emirleri/{no}/atama")
    suspend fun updateJobStatus(
        @Path("no") jobNo: String,
        @Body request: JobDto.UpdateStatusRequest
    ): Response<JobDto.UpdateStatusResponse>

    @Multipart
    @POST("is-emirleri/{no}/foto")
    suspend fun uploadPhoto(
        @Path("no") jobNo: String,
        @Part("file") file: MultipartBody.Part,
        @Part("description") description: RequestBody
    ): Response<JobDto.PhotoUploadResponse>

    // ============================================================
    // FORMS
    // ============================================================

    @POST("formlar")
    suspend fun submitForm(@Body request: FormDto.SubmitRequest): Response<FormDto.SubmitResponse>

    // ============================================================
    // SYNC (Batch offline operations)
    // ============================================================

    @POST("sync/batch")
    suspend fun syncBatch(@Body request: SyncDto.BatchRequest): Response<SyncDto.BatchResponse>

    // ============================================================
    // NOTIFICATIONS (Optional MVP)
    // ============================================================

    @GET("bildirimler")
    suspend fun getNotifications(): Response<SyncDto.NotificationsResponse>
}