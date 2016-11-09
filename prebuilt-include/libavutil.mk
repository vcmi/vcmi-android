LOCAL_MODULE := libavutil-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/liblibavutil.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
