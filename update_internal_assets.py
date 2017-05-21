import zipfile
import os
import sys
import hashlib

p = os.path

def writeFolder(archive, folderPath, rootPath):
	for root, dirs, filenames in os.walk(folderPath):
		for name in filenames:
			archive.write(p.normpath(p.join(root, name)), root[len(rootPath):] + "/" + name)
			
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
	[p.abspath(dir + "/../data/vcmi_submods/Mods"), p.abspath(dir + "/../data/vcmi_submods")] # templates + extra res
]

with zipfile.ZipFile(pathOutInternalData, "w", zipfile.ZIP_DEFLATED) as zf:
	for path in assetsPaths:
		if not p.exists(path[0]):
			print("Skipping path " + path[0] + " (not found)")
			continue
		writeFolder(zf, path[0], path[1])
		
createHash(pathOutInternalData, pathOutHash)