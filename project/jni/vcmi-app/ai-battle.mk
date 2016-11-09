LOCAL_PATH := $(VCMI_PATH_VCMI)/AI/BattleAI
include $(CLEAR_VARS)

LOCAL_MODULE := vcmi-ai-battle

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))

LOCAL_C_INCLUDES += $(VCMI_INCL_BOOST)
LOCAL_C_INCLUDES += $(VCMI_PATH_VCMI)/include
LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_SHARED_LIBRARIES := vcmi-lib-prebuilt

include $(BUILD_SHARED_LIBRARY)