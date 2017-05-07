import os
import shutil
import sys
import subprocess
import time
import vcmiutil

import vcmiconf
conf = vcmiconf.config

def copyLibs():
	vcmiutil.copytree("./libs/", conf["extOutput"])
	
def assertZero(code, cmd):
	if code != 0:
		sys.exit("\033[93mError code " + str(code) + "\033[0m during build on command: " + cmd)

def writeApplicationMk(path, modules, isCpp11):
	appFile = open(path, "w")
	appFile.write("APP_MODULES := " + modules + "\n")
	appFile.write("APP_ABI := " + conf["abis"] + "\n")
	appFile.write("APP_PLATFORM := android-" + str(conf["androidApi"]) + "\n")
	appFile.write("PROJECT_PATH_BASE := " + conf["projectRoot"] + "\n")
	appFile.write("APP_OPTIM := release\n")
	if isCpp11:
		appFile.write("APP_STL := c++_shared\n")
		appFile.write("APP_CPPFLAGS := -std=c++11 -fcxx-exceptions -frtti -w#pragma-messages\n")
	appFile.close()

def callBuild(path, modulesShared, modulesStatic, isCpp11):
	applicationMkPath = path + "/Application" + ".mk"
	buildMkPath = path + "/build" + ".mk"
	writeApplicationMk(applicationMkPath, modulesShared + " " + modulesStatic, isCpp11)
	
	cmd = conf["ndkRoot"] + "/ndk-build NDK_APPLICATION_MK=" + applicationMkPath + " APP_BUILD_SCRIPT=" + buildMkPath + " NDK_PROJECT_PATH=" + conf["projectRoot"] + " NDK_DEBUG=0"
	assertZero(os.system(cmd), cmd)
	
	copyLibs()
	
def fixLibsFiles():
	vcmiconf.updateProjectProps()
	import fix_boost_files
	import fix_sdl_makefiles
	import fix_ffmpeg_confs
		
def moveSDLIncludes():
	vcmiutil.flatCopyWithExt("ext/SDL2/core/code/include/", "ext/SDL2/core/include/", ".h")
	vcmiutil.flatCopyWithExt("ext/SDL2/SDL2-image/code/", "ext/SDL2/SDL2-image/include/", ".h")
	vcmiutil.flatCopyWithExt("ext/SDL2/SDL2-mixer/code/", "ext/SDL2/SDL2-mixer/include/", ".h")
	vcmiutil.flatCopyWithExt("ext/SDL2/SDL2-ttf/code/", "ext/SDL2/SDL2-ttf/include/", ".h")

def buildIconv():
	callBuild("ext/iconv", "iconv", "", False)
	
def buildSDL():
	callBuild("ext/SDL2/core", "SDL2", "", False)
	callBuild("ext/SDL2/SDL2-mixer", "SDL2_mixer", "", False)
	callBuild("ext/SDL2/SDL2-image", "SDL2_image", "", False)
	callBuild("ext/SDL2/SDL2-ttf", "SDL2_ttf", "", False)
	moveSDLIncludes()
	
def buildFFMPEG():
	subprocess.call(["bash", conf["bash"]["projectRoot"] + "/ext/ff/all.sh", conf["bash"]["ndkRoot"], conf["bash"]["extOutput"]])
	
def buildCMakeExternals():	
	os.chdir("project")
	cmd = "gradlew -a :vcmi-app:compileLibsOnlyDebugSources"
	assertZero(os.system(cmd), cmd)
	
def buildAll():
	timed(buildIconv, "iconv")
	#timed(buildSDL, "sdl")
	#timed(buildFFMPEG, "ffmpeg")
	#timed(buildCMakeExternals, "cmake external libs")
				
def timed(fun, name):
	startTime = time.time()
	fun()
	print("Built {} in {:.3}s".format(name, time.time() - startTime))
				
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
	
if flagBuild:
	timed(buildAll, "everything")