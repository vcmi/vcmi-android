include $(CLEAR_VARS)
LOCAL_PATH := $(BOOST_ROOT)/libs/filesystem/src
LOCAL_MODULE := boost-filesystem

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))

LOCAL_CPPFLAGS += $(BOOST_CPPFLAGS)
LOCAL_C_INCLUDES += $(BOOST_ROOT)
LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)
include $(CLEAR_VARS)