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
