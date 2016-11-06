LOCAL_PATH := $(VCMI_DIR)/AI/EmptyAI
LOCAL_MODULE := vcmi-ai-empty

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))
#VCMI_HEADER_LIST := $(filter %.h %.hpp, $(VCMI_ALLFILES))

LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_SHARED_LIBRARY)