LOCAL_PATH := $(VCMI_PATH_VCMI)/lib
include $(CLEAR_VARS)

include $(VCMI_PATH_PREBUILT)/boost-datetime.mk
include $(VCMI_PATH_PREBUILT)/boost-filesystem.mk
include $(VCMI_PATH_PREBUILT)/boost-system.mk
include $(VCMI_PATH_PREBUILT)/boost-smartptr.mk
include $(VCMI_PATH_PREBUILT)/boost-thread.mk
include $(VCMI_PATH_PREBUILT)/boost-locale.mk
include $(VCMI_PATH_PREBUILT)/vcmi-minizip.mk

LOCAL_MODULE := vcmi-lib

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_TMP := $(filter %.c %.cpp, $(VCMI_ALLFILES))
VCMI_FILE_LIST := $(call FILTER_OUT,/minizip/, $(VCMI_TMP))

LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_SRC_FILES += ../Version.cpp

#LOCAL_C_INCLUDES += $(PROJECT_PATH_BASE)/ext/SDL2/core/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(VCMI_INCL_BOOST)
LOCAL_C_INCLUDES += $(VCMI_INCL_SDL)
LOCAL_C_INCLUDES += $(VCMI_PATH_VCMI)/include
LOCAL_CFLAGS := -DM_DATA_DIR="\"none\"" -DM_LIB_DIR="\"none\"" -DM_BIN_DIR="\"none\"" -DIOAPI_NO_64
LOCAL_STATIC_LIBRARIES := boost-datetime-prebuilt boost-filesystem-prebuilt boost-system-prebuilt boost-smartptr-prebuilt boost-thread-prebuilt boost-locale-prebuilt
LOCAL_SHARED_LIBRARIES := vcmi-minizip-prebuilt
LOCAL_LDLIBS := -llog -lz -latomic

include $(BUILD_SHARED_LIBRARY)