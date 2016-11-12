LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
include $(VCMI_PATH_PREBUILT)/SDL2-core.mk

LOCAL_MODULE := vcmi-main

LOCAL_C_INCLUDES := $(VCMI_INCL_SDL)

# Add your application source files here...
LOCAL_SRC_FILES := ./empty.c
LOCAL_SRC_FILES += $(VCMI_PATH_SDL)/core/src/main/android/SDL_android_main.c
	
	
LOCAL_SHARED_LIBRARIES := SDL2-core-prebuilt

LOCAL_LDLIBS := -lGLESv1_CM -lGLESv2 -llog

include $(BUILD_SHARED_LIBRARY)
