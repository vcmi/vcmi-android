import vcmiutil

def fixSDLImage():
	replacements = [
	vcmiutil.ReplacementEntry("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(PROJECT_PATH_BASE)/ext/SDL2/core/code/include") ]
	
	vcmiutil.fixFile("./ext/SDL2/SDL2-image/code/Android.mk", replacements)
	
def fixSDLTTF():
	replacements = [
	vcmiutil.ReplacementEntry("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(PROJECT_PATH_BASE)/ext/SDL2/core/code/include") ]
	
	vcmiutil.fixFile("./ext/SDL2/SDL2-ttf/code/Android.mk", replacements)
	
def fixSDLMixer():
	replacements = [
	vcmiutil.ReplacementEntry("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(PROJECT_PATH_BASE)/ext/SDL2/core/code/include"),
	vcmiutil.ReplacementEntry("SUPPORT_MOD_MODPLUG ?= true", "SUPPORT_MOD_MODPLUG ?= false"),
	vcmiutil.ReplacementEntry("SUPPORT_MOD_MIKMOD ?= true", "SUPPORT_MOD_MIKMOD ?= false") ]
	
	vcmiutil.fixFile("./ext/SDL2/SDL2-mixer/code/Android.mk", replacements)
	
def fixSDLMixerExtSMPEG():
	replacements = [
	vcmiutil.ReplacementEntry("LOCAL_C_INCLUDES := $(LOCAL_PATH)", "LOCAL_C_INCLUDES := $(LOCAL_PATH) $(PROJECT_PATH_BASE)/ext/SDL2/core/code/include") ]
	
	vcmiutil.fixFile("./ext/SDL2/SDL2-mixer/code/external/smpeg2-2.0.0/Android.mk", replacements)
	
fixSDLImage()
fixSDLTTF()
fixSDLMixer()
fixSDLMixerExtSMPEG()
	