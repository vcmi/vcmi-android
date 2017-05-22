import os

def rewriteFile(path, content):
	replacementFile = open(path + "__tmp", "w")
	replacementFile.write(content)
	replacementFile.close()	
	os.replace(path + "__tmp", path)
	
dir = os.path.dirname(os.path.realpath(__file__))
with open(dir + "/vcmiconf.json.in", "r") as templateFile:
	rootDir = os.getcwd()
	config = templateFile.read()
	config = config.replace("$PROJECT_ROOT", rootDir)
	config = config.replace("$NDK_ROOT", os.getenv("ANDROID_NDK_HOME", ""))
	config = config.replace("$SDK_ROOT", "/usr/local/android-sdk")
	config = config.replace("$JAVA_HOME", os.getenv("JAVA_HOME", ""))
	rewriteFile(rootDir + "/vcmiconf.json", config)