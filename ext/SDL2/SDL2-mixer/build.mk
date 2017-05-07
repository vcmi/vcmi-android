TMP_PATH := $(call my-dir)
include $(CLEAR_VARS)
include $(TMP_PATH)/../SDL2-prebuilt-include.mk

include $(TMP_PATH)/code/external/smpeg2-2.0.0/Android.mk
include $(TMP_PATH)/code/Android.mk