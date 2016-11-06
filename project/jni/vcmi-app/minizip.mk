LOCAL_PATH := $(VCMI_DIR)/lib/minizip
LOCAL_MODULE := vcmi-minizip

LOCAL_SRC_FILES := unzip.c zip.c ioapi.c

LOCAL_CFLAGS := -DIOAPI_NO_64
LOCAL_LDLIBS := -lz

include $(BUILD_SHARED_LIBRARY)