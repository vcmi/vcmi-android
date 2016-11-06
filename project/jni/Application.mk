
# Uncomment this if you're using STL in your project
# See CPLUSPLUS-SUPPORT.html in the NDK documentation for more information
APP_STL := gnustl_shared
APP_CPPFLAGS += -std=c++11

APP_MODULES := boost-shared #vcmi-ai-empty vcmi-minizip vcmi-fuzzylite main
#APP_AVAILABLE_STATIC_LIBS := boost

APP_ABI := armeabi

# Min SDK level
APP_PLATFORM=android-16

