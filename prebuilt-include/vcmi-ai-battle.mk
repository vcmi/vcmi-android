LOCAL_MODULE := vcmi-ai-battle-prebuilt
ifeq ($(filter $(modules-get-list),$(LOCAL_MODULE)),)
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libvcmi-ai-battle.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
endif