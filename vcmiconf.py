import sys
import json
import vcmiutil

def updateProjectProps():
	replacements = [ vcmiutil.ReplacementEntry("PROJECT_PATH_BASE", "PROJECT_PATH_BASE = " + config["projectRoot"]) ]	
	vcmiutil.fixFile("./project/gradle.properties", replacements, False)
	
def createLocalProps():
	vcmiutil.rewriteFile("./project/local.properties", 
		"sdk.dir=" + config["sdkRoot"].replace(":", "\\:") 
		+ "\nndk.dir=" + config["ndkRoot"].replace(":", "\\:"))
	
config = []
with open("./vcmiconf.json", "r") as confFile:
	try:
		config = json.loads(confFile.read())
		config["extOutput"] = config["projectRoot"] + "/ext-output"
		config["bash"]["extOutput"] = config["bash"]["projectRoot"] + "/ext-output"
	except Exception as ex:
		print("Could not load config", ex)
		sys.exit(1)