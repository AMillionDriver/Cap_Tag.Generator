# ==============================================================================
# CONFIGURATION PROGUARD / R8 EXTREME (FULL MODE)
# ==============================================================================

# 1. JNI / NATIVE BRIDGE PROTECTION (VITAL)
# Menjaga agar jembatan C++ (native-lib.cpp) tidak putus.
# R8 tetap mengacak isi class-nya, tapi NAMA fungsi native tetap dipertahankan.
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.axoloth.captaggenerator.service.security.NativeSecurity { *; }

# 2. ROOM & SQLCIPHER PRECISION RULES
# Hanya menjaga bagian yang diperlukan agar mapping database tidak rusak.
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
    @androidx.room.Database *;
}
-keep class * extends androidx.room.RoomDatabase
-dontwarn net.zetetic.database.sqlcipher.**
-keep class net.zetetic.database.sqlcipher.** { *; }

# 3. JETPACK COMPOSE & MATERIAL 3
# Compose butuh beberapa metadata tetap ada saat runtime.
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.ui.platform.**

# 4. GOOGLE ML KIT (OCR) & FIREBASE
# Menjaga fungsionalitas scan teks on-device.
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.android.gms.**

# 5. METADATA & SIGNATURES
# Diperlukan untuk Kotlin Reflection dan Generics agar tidak crash.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# 6. OPTIMASI AGRESIF (EXTREME)
# Mengacak struktur package dan melakukan modifikasi akses untuk efisiensi maksimal.
-repackageclasses ''
-allowaccessmodification
-overloadaggressively
-optimizationpasses 5

# 7. SUPPRESS WARNINGS
# Library pihak ketiga seringkali punya warning yang bisa diabaikan saat shrinking.
-dontwarn coil.**
-dontwarn com.google.firebase.**
-dontwarn org.bouncycastle.**
