#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_axoloth_captaggenerator_service_security_NativeSecurity_getDatabaseSalt(JNIEnv* env, jobject thiz) {
    // Salt rahasia untuk memperkuat enkripsi SQLCipher
    std::string salt = "Axo-CapTag-Secure-Salt-2024!";
    return env->NewStringUTF(salt.c_str());
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_axoloth_captaggenerator_service_security_NativeSecurity_getAiVerificationHash(JNIEnv* env, jobject thiz) {
    // Hash atau Signature untuk memvalidasi API Key dari Firebase Remote Config
    std::string verification_hash = "sha256:7b5e4c3a2b1a0f9e8d7c6b5a43210fedcba9876543210fedcba987654321";
    return env->NewStringUTF(verification_hash.c_str());
}
