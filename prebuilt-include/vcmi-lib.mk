LOCAL_MODULE := vcmi-lib-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libvcmi-lib.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
