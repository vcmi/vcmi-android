LOCAL_MODULE := boost-locale-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libboost-locale.a
include $(PREBUILT_STATIC_LIBRARY)
include $(CLEAR_VARS)
