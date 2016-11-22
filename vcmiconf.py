import sys
import fix_files_common
fix = fix_files_common

pathsFile = open("./vcmiconf.in", "r")

mappedPaths = {}
for line in pathsFile:
	parts = line.split("=")
	if len(parts) != 2:
		print("Could not understand line in vcmiconf.in: " + parts)
		sys.exit(1)
	mappedPaths[parts[0].strip()] = parts[1].strip()

pathsFile.close()

pathProjRoot = mappedPaths["PROJECT_ROOT"]
pathProjRootBash = mappedPaths["PROJECT_ROOT_BASH"]
ndkRoot = mappedPaths["NDK_ROOT"]
ndkRootBash = mappedPaths["NDK_ROOT_BASH"]
sdkRoot = mappedPaths["SDK_ROOT"]
abis = mappedPaths["ABIS"]
boostFolder = mappedPaths["BOOST_FOLDER_NAME"]

def buildAbisGradleArray():
	return "[\"" + "\", \"".join(abis.split(" ")) + "\"]"

def updateProjectProps():
	replacements = [ fix.TmpReplacement("PROJECT_PATH_BASE", "PROJECT_PATH_BASE = " + pathProjRoot) ]	
	fix.fixFile("./project/gradle.properties", replacements, False)
	
	replacements = [ fix.TmpReplacement("VCMI_ABIS", "\tVCMI_ABIS = " + buildAbisGradleArray()),
		fix.TmpReplacement("VCMI_PATH_BOOST", "\tVCMI_PATH_BOOST = \"${VCMI_PATH_EXT}/boost/" + boostFolder + "\"")	]
	fix.fixFile("./project/defs.gradle", replacements, False)
	
	replacements = [ fix.TmpReplacement("boostFolder = ", "boostFolder = \"" + boostFolder + "\"") ]	
	fix.fixFile("./fix_boost_files.py", replacements, False)