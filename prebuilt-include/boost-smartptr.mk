LOCAL_MODULE := boost-smartptr-prebuilt
LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libboost-smartptr.a
include $(PREBUILT_STATIC_LIBRARY)
include $(CLEAR_VARS)
