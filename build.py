import os
import shutil
import sys
import subprocess
import fix_files_common

#os.environ["PATH"] = os.environ["PATH"] + ";Q:/P/Android/android-ndk-r13b;Q:/P/Android/android-sdk/platform-tools"
#os.environ["JAVA_HOME"] = "Q:/p/java/8"

import vcmiconf

pathExtOutput = vcmiconf.pathProjRoot + "/ext-output/"
pathExtOutputForBash = vcmiconf.pathProjRootBash + "/ext-output/"
targetAbis = "armeabi armeabi-v7a arm64-v8a x86 x86_64"
targetPlatform = "android-16"

def copyLibs():
	fix_files_common.copytree("./libs/", pathExtOutput)
	
def assertZero(code, cmd):
	if code != 0:
		sys.exit("\033[93mError code " + str(code) + "\033[0m during build on command: " + cmd)

def writeApplicationMk(path, modules, isCpp11):
	appFile = open(path, "w")
	appFile.write("APP_MODULES := " + modules + "\n")
	appFile.write("APP_ABI := " + targetAbis + "\n")
	appFile.write("APP_PLATFORM := " + targetPlatform + "\n")
	appFile.write("PROJECT_PATH_BASE := " + vcmiconf.pathProjRoot + "\n")
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
	prebuiltInc = open(vcmiconf.pathProjRoot + "/prebuilt-include/" + name + ".mk", "w")
	prebuiltInc.write("LOCAL_MODULE := " + name + "-prebuilt\n"
		"ifeq ($(filter $(modules-get-list),$(LOCAL_MODULE)),)\n"
		"\tLOCAL_SRC_FILES := $(PROJECT_PATH_BASE)/obj/local/$(TARGET_ARCH_ABI)/" + prebuiltName + "\n"
		"\tinclude $(PREBUILT_" + prebuiltType + "_LIBRARY)\n"
		"\tinclude $(CLEAR_VARS)\n"
		"endif")
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
	
	cmd = vcmiconf.ndkRoot + "/ndk-build NDK_APPLICATION_MK=" + applicationMkPath + " APP_BUILD_SCRIPT=" + buildMkPath + " NDK_PROJECT_PATH=" + vcmiconf.pathProjRoot
	assertZero(os.system(cmd), cmd)
	
	writePrebuiltIncludes(modulesShared, modulesStatic)
	copyLibs()
	
def fixLibsFiles():
	vcmiconf.updateProjectProps()
	import fix_boost_files
	import fix_sdl_makefiles
	import fix_ffmpeg_confs

def addVersionSuffix():
	import add_version_suffix
	add_version_suffix.add()

def buildIconv():
	callBuild("ext/iconv", "", "iconv", "", False)
	
def buildSDL():
	callBuild("ext/SDL2/core", "", "SDL2", "", False)
	callBuild("ext/SDL2/SDL2-mixer", "", "SDL2_mixer", "", False)
	callBuild("ext/SDL2/SDL2-image", "", "SDL2_image", "", False)
	callBuild("ext/SDL2/SDL2-ttf", "", "SDL2_ttf", "", False)
	import move_sdl_includes
	move_sdl_includes.moveSDLIncludes()
	move_sdl_includes.overwriteMainSDLFile()
	
def buildFFMPEG():
	subprocess.call(["bash", vcmiconf.pathProjRootBash + "/ext/ff/all.sh", vcmiconf.ndkRootBash, pathExtOutputForBash])
		
args = len(sys.argv)
if args != 2:
	print("run with any of these: all, build, fixpaths")
	sys.exit(1)
	
flagBuild = False
flagPaths = False
if sys.argv[1] == "all":
	flagBuild = True
	flagPaths = True
elif sys.argv[1] == "build":
	flagBuild = True
elif sys.argv[1] == "fixpaths":
	flagPaths = True
else:
	print("run with any of these: all, build, fixpaths")
	sys.exit(1)

if flagPaths:
	fixLibsFiles()
	addVersionSuffix()
	
if flagBuild:
	buildIconv()
	buildSDL()
	buildFFMPEG()