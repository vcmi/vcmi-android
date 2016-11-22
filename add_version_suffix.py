import os
import re

def add():
	version = "android"

	versionPropPattern = re.compile("^APP_VERSION\s*=\s*(.+)$")
	launcherPropsPath = "./project/gradle.properties"
	launcherPropsFile = open(launcherPropsPath, "r")
	for line in launcherPropsFile:
		versionMatch = versionPropPattern.search(line)
		if versionMatch:
			version += "-" + versionMatch.group(1)
			break
	launcherPropsFile.close()
	
	basePathIn = "./ext/vcmi/Version.cpp.in"
	baseDirOut = "./project/vcmi-lib/src/main/jni/generated-version-dir/"
	basePathOut = baseDirOut + "Version.cpp"
	inFile = open(basePathIn, "r")
	versionFileContent = inFile.read()
	inFile.close()

	replacedVersion = versionFileContent.replace("@GIT_SHA1@", version)
	if os.path.exists(baseDirOut) == False:
		os.makedirs(baseDirOut)
	outFile = open(basePathOut, "w")
	outFile.write(replacedVersion)
	outFile.close()

if __name__ == "__main__":
	add()
	