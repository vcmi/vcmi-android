import zipfile
import os
import sys
import hashlib
import shutil

p = os.path

def writeFolder(archive, folderPath, rootPath):
	for root, dirs, filenames in os.walk(folderPath):
		for name in filenames:
			outName = root[len(rootPath):] + "/" + name
			try:
				archive.getinfo(outName[1:])
			except KeyError:
				archive.write(p.normpath(p.join(root, name)), outName)

def createHash(path, outPath):
	blocksize = 65536
	hasher = hashlib.md5()
	with open(path, "rb") as file:
		buf = file.read(blocksize)
		while len(buf) > 0:
			hasher.update(buf)
			buf = file.read(blocksize)
	with open(outPath, "w") as file:
		file.write(hasher.hexdigest())

dir = p.dirname(p.realpath(sys.argv[0]))
pathAssets = dir + "/project/vcmi-app/src/main/assets"
pathOutInternalData = pathAssets + "/internalData.zip"
pathOutHash = pathAssets + "/internalDataHash.txt"
pathBase = dir + "/ext/vcmi"
pathConfig = pathBase + "/config"
pathMods = pathBase + "/Mods"

assetsPaths = [
	[pathConfig, pathBase],
	[pathMods, pathBase],
	[p.abspath(dir + "/vcmi_submods"), p.abspath(dir + "/vcmi_submods")] # templates + extra res
]

with zipfile.ZipFile(pathOutInternalData, "w", zipfile.ZIP_DEFLATED) as zf:
	for path in assetsPaths:
		if not p.exists(path[0]):
			print("Skipping path " + path[0] + " (not found)")
			continue
		writeFolder(zf, path[0], path[1])

createHash(pathOutInternalData, pathOutHash)

#copy authors file into app resources so that we can display it in about view
try:
	shutil.copy2(dir + "/ext/vcmi/AUTHORS", dir + "/project/vcmi-app/src/main/res/raw/authors.txt")
except IOError as e:
	print("Could not update authors file: " + e.strerror)
