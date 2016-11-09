LOCAL_MODULE := vcmi-ai-vcai-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libvcmi-ai-vcai.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
