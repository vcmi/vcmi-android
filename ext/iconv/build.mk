LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS) 
 
LOCAL_MODULE    := iconv 
 
LOCAL_CFLAGS    := \
	-Wno-multichar \
	-D_ANDROID \
	-DBUILDING_LIBICONV \
	-DIN_LIBRARY \
	-DLIBDIR="\"c\"" \
    -I$(LOCAL_PATH)/code/ \
    -I$(LOCAL_PATH)/code/include/ \
    -I$(LOCAL_PATH)/code/lib/ \
	-I$(LOCAL_PATH)/code/libcharset/include \

LOCAL_SRC_FILES := \
     ./code/lib/iconv.c \
     ./code/lib/relocatable.c \
     ./code/libcharset/lib/localcharset.c \

include $(BUILD_SHARED_LIBRARY) 
 
LOCAL_LDLIBS    := -llog -lcharset
