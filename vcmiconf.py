import sys
import json
import vcmiutil

def buildAbisGradleArray():
	return "[\"" + "\", \"".join(abis.split(" ")) + "\"]"

def updateProjectProps():
	replacements = [ vcmiutil.ReplacementEntry("PROJECT_PATH_BASE", "PROJECT_PATH_BASE = " + pathProjRoot) ]	
	vcmiutil.fixFile("./project/gradle.properties", replacements, False)
	
	replacements = [ vcmiutil.ReplacementEntry("VCMI_ABIS", "\tVCMI_ABIS = " + buildAbisGradleArray()),
		vcmiutil.ReplacementEntry("VCMI_PATH_BOOST", "\tVCMI_PATH_BOOST = \"${VCMI_PATH_EXT}/boost/" + boostFolder + "\"")	]
	vcmiutil.fixFile("./project/defs.gradle", replacements, False)
	
	replacements = [ vcmiutil.ReplacementEntry("boostFolder = ", "boostFolder = \"" + boostFolder + "\"") ]	
	vcmiutil.fixFile("./fix_boost_files.py", replacements, False)
	
config = []
with open("./vcmiconf.json", "r") as confFile:
	try:
		config = json.loads(confFile.read())
		config["extOutput"] = config["projectRoot"] + "/ext-output"
		config["bash"]["extOutput"] = config["bash"]["projectRoot"] + "/ext-output"
	except Exception as ex:
		print("Could not load config", ex)
		sys.exit(1)