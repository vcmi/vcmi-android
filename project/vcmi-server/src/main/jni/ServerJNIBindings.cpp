#include "StdInc.h"

#ifdef VCMI_ANDROID

#include <jni.h>
#include <android/log.h>
#include <AndroidVMHelper.h>

#include "CVCMIServer.h"

extern "C" JNIEXPORT void JNICALL Java_eu_vcmi_vcmi_NativeMethods_createServer(JNIEnv *env, jobject cls)
{
    __android_log_write(ANDROID_LOG_INFO, "VCMI", "Got jni call to init server");
    AndroidVMHelper::cacheVM(env);
    CVCMIServer::create();
}

#endif