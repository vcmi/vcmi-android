LOCAL_PATH := $(VCMI_PATH_VCMI)/client
include $(CLEAR_VARS)

include $(VCMI_PATH_PREBUILT)/boost-datetime.mk
include $(VCMI_PATH_PREBUILT)/boost-filesystem.mk
include $(VCMI_PATH_PREBUILT)/boost-system.mk
include $(VCMI_PATH_PREBUILT)/boost-smartptr.mk
include $(VCMI_PATH_PREBUILT)/boost-thread.mk
include $(VCMI_PATH_PREBUILT)/boost-locale.mk
include $(VCMI_PATH_PREBUILT)/boost-program-options.mk

include $(VCMI_PATH_PREBUILT)/SDL2-core.mk
include $(VCMI_PATH_PREBUILT)/SDL2-image.mk
include $(VCMI_PATH_PREBUILT)/SDL2-mixer.mk
include $(VCMI_PATH_PREBUILT)/SDL2-ttf.mk

include $(VCMI_PATH_PREBUILT)/avutil.mk
include $(VCMI_PATH_PREBUILT)/avcodec.mk
include $(VCMI_PATH_PREBUILT)/avformat.mk
include $(VCMI_PATH_PREBUILT)/swresample.mk
include $(VCMI_PATH_PREBUILT)/swscale.mk

include $(VCMI_PATH_PREBUILT)/vcmi-lib.mk
include $(VCMI_PATH_PREBUILT)/vcmi-ai-stupid.mk
include $(VCMI_PATH_PREBUILT)/vcmi-ai-battle.mk
include $(VCMI_PATH_PREBUILT)/vcmi-ai-vcai.mk

define vcmiwalk
  $(wildcard $(1)) $(foreach e, $(wildcard $(1)/*), $(call vcmiwalk, $(e)))
endef

LOCAL_MODULE := vcmi-client

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))
$(warning $(LOCAL_PATH))
LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)
LOCAL_SRC_FILES += $(LOCAL_PATH)/../CCallback.cpp
LOCAL_C_INCLUDES += $(VCMI_INCL_BOOST)
LOCAL_C_INCLUDES += $(VCMI_INCL_SDL) $(VCMI_INCL_SDL_IMAGE) $(VCMI_INCL_SDL_MIXER) $(VCMI_INCL_SDL_TTF)
LOCAL_C_INCLUDES += $(VCMI_INCL_FFMPEG)
LOCAL_C_INCLUDES += $(VCMI_PATH_VCMI)/include
LOCAL_CFLAGS := -DIOAPI_NO_64
LOCAL_STATIC_LIBRARIES := boost-datetime-prebuilt boost-filesystem-prebuilt boost-system-prebuilt boost-smartptr-prebuilt boost-thread-prebuilt boost-locale-prebuilt boost-program-options-prebuilt
LOCAL_SHARED_LIBRARIES += SDL2-core-prebuilt SDL2-image-prebuilt SDL2-mixer-prebuilt SDL2-ttf-prebuilt
LOCAL_SHARED_LIBRARIES += avutil-prebuilt avcodec-prebuilt avformat-prebuilt swresample-prebuilt swscale-prebuilt
LOCAL_SHARED_LIBRARIES += vcmi-lib-prebuilt vcmi-ai-stupid-prebuilt vcmi-ai-battle-prebuilt vcmi-ai-vcai-prebuilt

include $(BUILD_SHARED_LIBRARY)