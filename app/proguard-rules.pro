# ============================================================
# SGC Teknisyen - ProGuard / R8 Rules
# ============================================================

# ------------------------------------------------------------
# Hilt / Dagger
# ------------------------------------------------------------
-keep class dagger.hilt.** { *; }
-keep class com.google.dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep class * extends dagger.hilt.android.HiltApplication { *; }
-keep class * extends dagger.hilt.android.HiltFragment { *; }
-keep class * extends dagger.hilt.android.HiltViewModel { *; }
-keep class * extends dagger.hilt.android.HiltActivity { *; }

# Generated Hilt components
-keep class * extends dagger.hilt.internal.GeneratedComponent { *; }
-keep class * extends dagger.hilt.internal.GeneratedComponentManager { *; }

# ------------------------------------------------------------
# Room
# ------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * implements androidx.room.Dao { *; }
-keep class * implements androidx.room.EntityDeletionOrUpdateAdapter { *; }
-keep class * implements androidx.room.EntityInsertionAdapter { *; }
-keep class * implements androidx.room.RawQuery { *; }
-keep class * implements androidx.room.RoomDatabase.QueryCallback { *; }

# Room entities (keep field names for JSON serialization)
-keepclassmembers class com.sgc.teknisyen.data.local.entity.** {
    *;
}

# ------------------------------------------------------------
# Retrofit / OkHttp / Gson / Moshi
# ------------------------------------------------------------
-keep class com.squareup.okhttp3.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.moshi.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.squareup.retrofit2.** { *; }
-keep class retrofit2.** { *; }

# Keep data model classes
-keep class com.sgc.teknisyen.data.remote.dto.** { *; }
-keep class com.sgc.teknisyen.domain.model.** { *; }

# ------------------------------------------------------------
# Coroutines
# ------------------------------------------------------------
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.flow.** { *; }

# ------------------------------------------------------------
# WorkManager
# ------------------------------------------------------------
-keep class androidx.work.** { *; }

# ------------------------------------------------------------
# Google Play Services Location
# ------------------------------------------------------------
-keep class com.google.android.gms.location.** { *; }

# ------------------------------------------------------------
# Coil
# ------------------------------------------------------------
-keep class coil3.** { *; }
-keep class coil3.compose.** { *; }

# ------------------------------------------------------------
# Security Crypto (EncryptedSharedPreferences)
# ------------------------------------------------------------
-keep class androidx.security.crypto.** { *; }

# ------------------------------------------------------------
# Kotlinx Serialization
# ------------------------------------------------------------
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.serialization.json.** { *; }

# ------------------------------------------------------------
# Compose
# ------------------------------------------------------------
-keep class androidx.compose.** { *; }

# ------------------------------------------------------------
# Application classes
# ------------------------------------------------------------
-keep class com.sgc.teknisyen.MainApplication { *; }
-keep class com.sgc.teknisyen.SgcApp { *; }
-keep class com.sgc.teknisyen.MainActivity { *; }

# ------------------------------------------------------------
# ViewModels
# ------------------------------------------------------------
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class com.sgc.teknisyen.presentation.screens.**.*ViewModel { *; }

# ------------------------------------------------------------
# Services / Workers
# ------------------------------------------------------------
-keep class com.sgc.teknisyen.service.** { *; }
-keep class com.sgc.teknisyen.worker.** { *; }

# ------------------------------------------------------------
# Navigation
# ------------------------------------------------------------
-keep class androidx.navigation.** { *; }

# ------------------------------------------------------------
# Prevent obfuscation of parcelable / serializable
# ------------------------------------------------------------
-keep class * implements android.os.Parcelable { *; }
-keep class * implements java.io.Serializable { *; }

# ------------------------------------------------------------
# Keep annotations
# ------------------------------------------------------------
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod