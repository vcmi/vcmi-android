LOCAL_MODULE := smpeg2-prebuilt
ifeq ($(filter $(modules-get-list),$(LOCAL_MODULE)),)
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libsmpeg2.so
include $(PREBUILT_SHARED_LIBRARY)
include $(CLEAR_VARS)
endif