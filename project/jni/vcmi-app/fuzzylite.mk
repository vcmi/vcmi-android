LOCAL_PATH := $(VCMI_DIR)/AI/FuzzyLite/fuzzylite
include $(CLEAR_VARS)

LOCAL_MODULE := vcmi-fuzzylite

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))

LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_LDFLAGS :=
LOCAL_CPPFLAGS := -DFL_CPP11

include $(BUILD_SHARED_LIBRARY)