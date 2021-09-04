/*
    SDL_android_main.c, placed in the public domain by Sam Lantinga  3/13/14
*/
#ifdef __ANDROID__

/* Include the SDL main definition header */
extern "C" {
#include "SDL_main.h"
}

#include <SDL_mouse.h>
#include "lib/CAndroidVMHelper.h"

/*******************************************************************************
                 Functions called by JNI
*******************************************************************************/

extern "C" {
/* Called before SDL_main() to initialize JNI bindings in SDL library */
extern void SDL_Android_Init(JNIEnv *env, jclass cls);

/* This prototype is needed to prevent a warning about the missing prototype for global function below */
JNIEXPORT int JNICALL Java_org_libsdl_app_SDLActivity_nativeInit(JNIEnv *env, jclass cls,
																 jobjectArray array);
JNIEXPORT void JNICALL Java_org_libsdl_app_SurfaceTouchHandler_retrieveCursorPositions(JNIEnv * env, jclass cls,
																					   jintArray outValues);
}
//JNIEXPORT int JNICALL Java_org_libsdl_app_SDLActivity_nativeQuit(JNIEnv* env, jclass cls)
//{
//	return 0;
//}
/* Start up the SDL app */
JNIEXPORT int JNICALL Java_org_libsdl_app_SDLActivity_nativeInit(JNIEnv *env, jclass cls, jobjectArray array)
{
	int i;
	int argc;
	int status;
	int len;
	char **argv;

	CAndroidVMHelper::cacheVM(env);

	/* This interface could expand with ABI negotiation, callbacks, etc. */
	SDL_Android_Init(env, cls);

	SDL_SetMainReady();

	/* Prepare the arguments. */

	len = env->GetArrayLength(array);
	argv = SDL_stack_alloc(char*, 1 + len + 1);
	argc = 0;
	/* Use the name "app_process" so PHYSFS_platformCalcBaseDir() works.
	   https://bitbucket.org/MartinFelis/love-android-sdl2/issue/23/release-build-crash-on-start
	 */
	argv[argc++] = SDL_strdup("app_process");
	for (i = 0; i < len; ++i)
	{
		const char *utf;
		char *arg = NULL;
		jstring string = (jstring) env->GetObjectArrayElement(array, i);
		if (string)
		{
			utf = env->GetStringUTFChars(string, 0);
			if (utf)
			{
				arg = SDL_strdup(utf);
				env->ReleaseStringUTFChars(string, utf);
			}
			env->DeleteLocalRef(string);
		}
		if (!arg)
		{
			arg = SDL_strdup("");
		}
		argv[argc++] = arg;
	}
	argv[argc] = NULL;


	/* Run the application. */
	status = SDL_main(argc, argv);
	/* Release the arguments. */

	for (i = 0; i < argc; ++i)
	{
		SDL_free(argv[i]);
	}
	SDL_stack_free(argv);
	/* Do not issue an exit or the whole application will terminate instead of just the SDL thread */
	/* exit(status); */

	return status;
}

JNIEXPORT void JNICALL Java_org_libsdl_app_SurfaceTouchHandler_retrieveCursorPositions(JNIEnv * env, jclass cls,
																					   jintArray outValues)
{
	auto len = env->GetArrayLength(outValues);
	if (len != 2)
	{
		return;
	}
	int x, y;
	SDL_GetMouseState(&x, &y);
	jint elements[] = {x, y};
	env->SetIntArrayRegion(outValues, 0, 2, elements);
}

#endif /* __ANDROID__ */

/* vi: set ts=4 sw=4 expandtab: */
