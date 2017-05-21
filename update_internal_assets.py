import zipfile
import os
import sys

p = os.path

def writeFolder(archive, folderPath, rootPath):
	for root, dirs, filenames in os.walk(folderPath):
		for name in filenames:
			archive.write(p.normpath(p.join(root, name)), root[len(rootPath):] + "/" + name)

dir = p.dirname(p.realpath(sys.argv[0]))
outPath = dir + "/project/vcmi-app/src/main/assets/internalData.zip"
pathBase = dir + "/ext/vcmi"
pathConfig = pathBase + "/config"
pathMods = pathBase + "/Mods"

assetsPaths = [
	[pathConfig, pathBase],
	[pathMods, pathBase],
	[p.abspath(dir + "/../data/vcmi_submods/Mods"), p.abspath(dir + "/../data/vcmi_submods")] # templates + extra res
]

with zipfile.ZipFile(outPath, "w", zipfile.ZIP_DEFLATED) as zf:
	for path in assetsPaths:
		if not p.exists(path[0]):
			print("Skipping path " + path[0] + " (not found)")
			continue
		writeFolder(zf, path[0], path[1])