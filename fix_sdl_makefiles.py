import fix_files_common
fix = fix_files_common

def fixSDLImage():
	replacements = [
	fix.TmpReplacement("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/../../core/code/include"),
	fix.TmpReplacement("LOCAL_SHARED_LIBRARIES := SDL2", "LOCAL_SHARED_LIBRARIES := SDL2-prebuilt") ]
	
	fix.fixFile("./ext/SDL2/SDL2-image/code/Android.mk", replacements)
	
def fixSDLTTF():
	replacements = [
	fix.TmpReplacement("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/../../core/code/include"),
	fix.TmpReplacement("LOCAL_SHARED_LIBRARIES := SDL2", "LOCAL_SHARED_LIBRARIES := SDL2-prebuilt") ]
	
	fix.fixFile("./ext/SDL2/SDL2-ttf/code/Android.mk", replacements)
	
def fixSDLMixer():
	replacements = [
	fix.TmpReplacement("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/../../core/code/include"),
	fix.TmpReplacement("LOCAL_SHARED_LIBRARIES := SDL2", "LOCAL_SHARED_LIBRARIES := SDL2-prebuilt"),
	fix.TmpReplacement("SUPPORT_MOD_MODPLUG ?= true", "SUPPORT_MOD_MODPLUG ?= false"),
	fix.TmpReplacement("SUPPORT_MOD_MIKMOD ?= true", "SUPPORT_MOD_MIKMOD ?= false") ]
	
	fix.fixFile("./ext/SDL2/SDL2-mixer/code/Android.mk", replacements)
	
def fixSDLMixerExtSMPEG():
	replacements = [
	fix.TmpReplacement("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/../../../../core/code/include"),
	fix.TmpReplacement("LOCAL_SHARED_LIBRARIES := SDL2", "LOCAL_SHARED_LIBRARIES := SDL2-prebuilt") ]
	
	fix.fixFile("./ext/SDL2/SDL2-mixer/code/external/smpeg2-2.0.0/Android.mk", replacements)
	
def fixSDLMixerExtOGG():
	replacements = [
	fix.TmpReplacement("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(LOCAL_PATH)/../../../../core/code/include"),
	fix.TmpReplacement("LOCAL_SHARED_LIBRARIES := SDL2", "LOCAL_SHARED_LIBRARIES := SDL2-prebuilt") ]
	
	fix.fixFile("./ext/SDL2/SDL2-mixer/code/external/smpeg2-2.0.0/Android.mk", replacements)

fixSDLImage()
fixSDLTTF()
fixSDLMixer()
fixSDLMixerExtSMPEG()
	