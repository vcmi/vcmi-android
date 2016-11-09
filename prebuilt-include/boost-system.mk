LOCAL_MODULE := boost-system-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libboost-system.a
include $(PREBUILT_STATIC_LIBRARY)
include $(CLEAR_VARS)
