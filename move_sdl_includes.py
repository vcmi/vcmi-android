import os
import shutil

def flatCopyWithExt(srcDir, dstDir, ext):
	if not os.path.exists(dstDir):
		os.makedirs(dstDir)
	for basename in os.listdir(srcDir):
		if basename.endswith(ext):
			pathname = os.path.join(srcDir, basename)
			if os.path.isfile(pathname):
				shutil.copy2(pathname, dstDir)

def moveSDLIncludes():
	flatCopyWithExt("./ext/SDL2/core/code/include/", "./ext/SDL2/core/include/", ".h")
	flatCopyWithExt("./ext/SDL2/SDL2-image/code/", "./ext/SDL2/SDL2-image/include/", ".h")
	flatCopyWithExt("./ext/SDL2/SDL2-mixer/code/", "./ext/SDL2/SDL2-mixer/include/", ".h")
	flatCopyWithExt("./ext/SDL2/SDL2-ttf/code/", "./ext/SDL2/SDL2-ttf/include/", ".h")
	
#this is rather bad, because SDL_android_main ideally should be in repo, but it's tangled in the SDL sources and probably can't be moved somewhere else
def overwriteMainSDLFile():
	mainDst = "./ext/SDL2/core/code/src/main/android/SDL_android_main.cpp"
	if not os.path.isfile(mainDst): # don't overwrite
		shutil.copy2("./ext/SDL2/core/SDL_android_main.cpp", mainDst)
	try:
		os.remove("./ext/SDL2/core/code/src/main/android/SDL_android_main.c")
	except FileNotFoundError:
		print("Built-in SDL_android_main.c already deleted, ignoring")