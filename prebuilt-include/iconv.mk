LOCAL_MODULE := iconv-prebuilt
ifeq ($(filter $(modules-get-list),$(LOCAL_MODULE)),)
	LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/libiconv.so
	include $(PREBUILT_SHARED_LIBRARY)
	include $(CLEAR_VARS)
endif