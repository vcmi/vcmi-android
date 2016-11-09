LOCAL_MODULE := libswscale-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/liblibswscale.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
