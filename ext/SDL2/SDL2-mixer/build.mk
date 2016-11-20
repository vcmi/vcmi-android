TMP_PATH := $(call my-dir)
include $(CLEAR_VARS)
include $(PROJECT_PATH_BASE)/prebuilt-include/SDL2.mk

include $(TMP_PATH)/code/external/smpeg2-2.0.0/Android.mk
include $(TMP_PATH)/code/Android.mk