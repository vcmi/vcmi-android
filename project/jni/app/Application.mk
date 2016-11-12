APP_MODULES := vcmi-main 
APP_ABI := armeabi
APP_PLATFORM := android-16
PROJECT_PATH_BASE := Q:\Dev\VCMI\vcmi-android
include $(PROJECT_PATH_BASE)/build-hardcoded.mk
APP_STL := c++_shared
APP_CPPFLAGS := -std=c++11 -fcxx-exceptions -frtti -w#pragma-messages
