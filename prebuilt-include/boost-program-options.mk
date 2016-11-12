LOCAL_MODULE := boost-program-options-prebuilt
ifeq ($(filter $(modules-get-list),$(LOCAL_MODULE)),)
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libboost-program-options.a
include $(PREBUILT_STATIC_LIBRARY)
include $(CLEAR_VARS)
endif