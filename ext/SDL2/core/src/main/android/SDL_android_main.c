/*
    SDL_android_main.c, placed in the public domain by Sam Lantinga  3/13/14
*/
#include "../../SDL_internal.h"

#ifdef __ANDROID__
#include <android/log.h>

/* Include the SDL main definition header */
#include "SDL_main.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/
#include <jni.h>

/* Called before SDL_main() to initialize JNI bindings in SDL library */
extern void SDL_Android_Init(JNIEnv* env, jclass cls);

/* This prototype is needed to prevent a warning about the missing prototype for global function below */
JNIEXPORT int JNICALL Java_org_libsdl_app_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jobject array, jstring dataPath);

JNIEXPORT int JNICALL Java_org_libsdl_app_SDLActivity_nativeQuit(JNIEnv* env, jclass cls)
{
	return 0;
}
/* Start up the SDL app */
JNIEXPORT int JNICALL Java_org_libsdl_app_SDLActivity_nativeInit(JNIEnv* env, jclass cls, jobject array, jstring dataPath)
{
    int i;
    int argc;
    int status;
    int len;
    char** argv;
	
	const char *nativeDataPath = (*env)->GetStringUTFChars(env, dataPath, 0);
	setenv("VCMI_DATA_ROOT", dataPath, 1);
	__android_log_write(ANDROID_LOG_ERROR, "xx#", dataPath);
	(*env)->ReleaseStringUTFChars(env, dataPath, nativeDataPath);

    /* This interface could expand with ABI negotiation, callbacks, etc. */
    SDL_Android_Init(env, cls);

    SDL_SetMainReady();

    /* Prepare the arguments. */

    len = (*env)->GetArrayLength(env, array);
    argv = SDL_stack_alloc(char*, 1 + len + 1);
    argc = 0;
    /* Use the name "app_process" so PHYSFS_platformCalcBaseDir() works.
       https://bitbucket.org/MartinFelis/love-android-sdl2/issue/23/release-build-crash-on-start
     */
    argv[argc++] = SDL_strdup("app_process");
    for (i = 0; i < len; ++i) {
        const char* utf;
        char* arg = NULL;
        jstring string = (*env)->GetObjectArrayElement(env, array, i);
        if (string) {
            utf = (*env)->GetStringUTFChars(env, string, 0);
            if (utf) {
                arg = SDL_strdup(utf);
                (*env)->ReleaseStringUTFChars(env, string, utf);
            }
            (*env)->DeleteLocalRef(env, string);
        }
        if (!arg) {
            arg = SDL_strdup("");
        }
        argv[argc++] = arg;
    }
    argv[argc] = NULL;


    /* Run the application. */
	__android_log_write(ANDROID_LOG_ERROR, "xx#", "CALLING main");
    status = SDL_main(argc, argv);
	__android_log_write(ANDROID_LOG_ERROR, "xx#", "AFTER main");
    /* Release the arguments. */

    for (i = 0; i < argc; ++i) {
        SDL_free(argv[i]);
    }
    SDL_stack_free(argv);
    /* Do not issue an exit or the whole application will terminate instead of just the SDL thread */
    /* exit(status); */

    return status;
}

#endif /* __ANDROID__ */

/* vi: set ts=4 sw=4 expandtab: */
