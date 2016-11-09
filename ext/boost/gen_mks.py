makeTemplate = """include $(CLEAR_VARS)
LOCAL_PATH := $(BOOST_ROOT)/{}
LOCAL_MODULE := boost-{}

VCMI_ALLFILES = $(call vcmiwalk, $(LOCAL_PATH))
VCMI_FILE_LIST := $(filter %.c %.cpp, $(VCMI_ALLFILES))

LOCAL_CPPFLAGS += $(BOOST_CPPFLAGS)
LOCAL_C_INCLUDES += $(BOOST_ROOT)
LOCAL_SRC_FILES := $(VCMI_FILE_LIST:$(LOCAL_PATH)/%=%)

include $(BUILD_STATIC_LIBRARY)
include $(CLEAR_VARS)"""

boostData = [["datetime", "libs/date_time/src"], 
	#["locale", "libs/locale/src"],
	["smartptr", "libs/smart_ptr/src"],
	["filesystem", "libs/filesystem/src"],
	["thread", "libs/thread/src"],
	["system", "libs/system/src"]
	]

callingFilePre = "BASE_LOCAL_PATH := $(call my-dir)\n"
callingFileLine = "include $(BASE_LOCAL_PATH)/boost-{}.mk\n"
callingFile = open("boost.mk", "w")
callingFile.write(callingFilePre)

for entry in boostData:
	outFile = open("boost-" + entry[0] + ".mk", "w")
	outFile.write(makeTemplate.format(entry[1], entry[0]))
	outFile.close()
	callingFile.write(callingFileLine.format(entry[0]))
	
callingFile.close()
