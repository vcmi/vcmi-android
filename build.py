import os
import sys

os.environ["PATH"] = os.environ["PATH"] + ";Q:/P/Android/android-ndk-r13b;Q:/P/ant/bin;Q:/P/Android/android-sdk/platform-tools"
os.environ["JAVA_HOME"] = "Q:/p/java/8"

pathProjBase = os.getcwd()
targetAbis = "armeabi"
targetPlatform = "android-16"

def assertZero(code, cmd):
	if code != 0:
		sys.exit("\033[93mError code " + str(code) + "\033[0m during build on command: " + cmd)

def writeApplicationMk(path, modules, isCpp11):
	appFile = open(path, "w")
	appFile.write("APP_MODULES := " + modules + "\n")
	appFile.write("APP_ABI := " + targetAbis + "\n")
	appFile.write("APP_PLATFORM := " + targetPlatform + "\n")
	appFile.write("PROJECT_PATH_BASE := " + pathProjBase + "\n")
	appFile.write("include $(PROJECT_PATH_BASE)/build-hardcoded.mk\n")
	if isCpp11:
		appFile.write("APP_STL := c++_shared\n")
		appFile.write("APP_CPPFLAGS := -std=c++11 -fcxx-exceptions -frtti -w#pragma-messages\n")
	appFile.close()

def writePrebuiltInclude(name, isStatic):
	if len(name) == 0:
		return
	if isStatic:
		prebuiltType = "STATIC"
		prebuiltName = "lib" + name + ".a"
	else:
		prebuiltType = "SHARED"
		prebuiltName = "lib" + name + ".so"
	prebuiltInc = open(pathProjBase + "/prebuilt-include/" + name + ".mk", "w")
	prebuiltInc.write("LOCAL_MODULE := " + name + "-prebuilt\n"
		"LOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/" + prebuiltName + "\n"
		"include $(PREBUILT_" + prebuiltType + "_LIBRARY)\n"
		"include $(CLEAR_VARS)\n")
	prebuiltInc.close()
	
def writePrebuiltIncludes(modulesShared, modulesStatic):
	for module in modulesShared.split(" "):
		writePrebuiltInclude(module, False)
	for module in modulesStatic.split(" "):
		writePrebuiltInclude(module, True)
	
def callBuild(path, mkSuffix, modulesShared, modulesStatic, isCpp11):
	applicationMkPath = path + "/Application" + mkSuffix + ".mk"
	buildMkPath = path + "/build" + mkSuffix + ".mk"
	writeApplicationMk(applicationMkPath, modulesShared + " " + modulesStatic, isCpp11)
	
	cmd = "ndk-build NDK_APPLICATION_MK=" + applicationMkPath + " APP_BUILD_SCRIPT=" + buildMkPath + " NDK_PROJECT_PATH=" + pathProjBase
	assertZero(os.system(cmd), cmd)
	
	writePrebuiltIncludes(modulesShared, modulesStatic)
	
def addVersionSuffix():
	import add_version_suffix
	add_version_suffix.add()

def buildSDL():
	callBuild("ext/SDL2/core", "", "SDL2-core", "", False)
	callBuild("ext/SDL2/SDL2-mixer-external", "", "smpeg2", "", False)
	callBuild("ext/SDL2/SDL2-mixer", "", "SDL2-mixer", "", False)
	callBuild("ext/SDL2/SDL2-image", "", "SDL2-image", "", False)
	callBuild("ext/SDL2/SDL2-ttf", "", "SDL2-ttf", "", False)

def buildBoost():
	#TODO fix boost files here
	callBuild("ext/boost", "", "boost-shared", "boost-datetime boost-filesystem boost-system boost-smartptr boost-thread boost-locale boost-program-options", True)
	
def ffmpegHelper():
	writePrebuiltIncludes("avcodec avformat avutil swresample swscale", "")
	
def buildMain():
	#callBuild("project/jni/vcmi-app", "-extras", "vcmi-minizip vcmi-fuzzylite", "", True)
	#callBuild("project/jni/vcmi-app", "-lib", "vcmi-lib", "", True)
	#callBuild("project/jni/vcmi-app", "-ai", "vcmi-ai-battle vcmi-ai-empty vcmi-ai-stupid vcmi-ai-vcai", "", True)
	callBuild("project/jni/vcmi-app", "-server", "vcmi-server", "", True)
	#callBuild("project/jni/vcmi-app", "-client", "vcmi-client", "", True)
	
	
#buildSDL()
#addVersionSuffix()
#buildBoost()
#ffmpegHelper()
buildMain()