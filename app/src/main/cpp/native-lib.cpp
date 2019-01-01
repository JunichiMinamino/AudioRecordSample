#include <jni.h>

extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_loopsessions_audiorecordsample_MainActivity_processAudio(JNIEnv* env, jobject thiz, jlong numberFrames, jfloatArray inputBuffer, jfloatArray outputBuffer, jfloat fRateTest)
{
    jfloat* fInputBuffer = env->GetFloatArrayElements(inputBuffer, 0);
    jfloat* fOutputBuffer = env->GetFloatArrayElements(outputBuffer, 0);

    // テスト処理（fRateTest倍）
    for (int i = 0; i < numberFrames; i++) {
        fOutputBuffer[i] = fInputBuffer[i] * fRateTest;
    }

    env->ReleaseFloatArrayElements(inputBuffer, fInputBuffer, 0);
    env->ReleaseFloatArrayElements(outputBuffer, fOutputBuffer, 0);
    env->DeleteLocalRef(thiz);

    return outputBuffer;
}
