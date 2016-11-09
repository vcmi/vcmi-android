LOCAL_MODULE := vcmi-ai-stupid-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libvcmi-ai-stupid.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
