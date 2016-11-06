LOCAL_PATH := $(VCMI_DIR)/AI/fuzzylite/fuzzylite
LOCAL_MODULE := vcmi-fuzzylite

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))
VCMI_HEADER_LIST := $(filter %.h %.hpp, $(VCMI_ALLFILES))

LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_C_INCLUDES := $(VCMI_HEADER_LIST:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../SDL2/include
LOCAL_C_INCLUDES += $(foreach L, $(COMPILED_LIBRARIES), $(LOCAL_PATH)/../$(L)/include)

LOCAL_LDLIBS := -lGLESv1_CM -ldl -llog -lz
LOCAL_LDFLAGS :=
LOCAL_CFLAGS := -fcxx-exceptions

include $(BUILD_SHARED_LIBRARY)