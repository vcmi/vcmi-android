LOCAL_PATH := $(VCMI_PATH_VCMI)/AI/VCAI
include $(CLEAR_VARS)

include $(VCMI_PATH_PREBUILT)/vcmi-fuzzylite.mk
include $(VCMI_PATH_PREBUILT)/vcmi-lib.mk

LOCAL_MODULE := vcmi-ai-vcai

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))

LOCAL_C_INCLUDES += $(VCMI_INCL_BOOST)
LOCAL_C_INCLUDES += $(VCMI_PATH_VCMI)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../FuzzyLite/fuzzylite
LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_SHARED_LIBRARIES := vcmi-lib-prebuilt vcmi-fuzzylite-prebuilt
LOCAL_CPPFLAGS := -DFL_CPP11

include $(BUILD_SHARED_LIBRARY)