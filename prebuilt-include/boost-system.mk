LOCAL_MODULE := boost-system-prebuilt
ifeq ($(filter $(modules-get-list),$(LOCAL_MODULE)),)
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libboost-system.a
include $(PREBUILT_STATIC_LIBRARY)
include $(CLEAR_VARS)
endif