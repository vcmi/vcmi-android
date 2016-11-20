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
	
	basePathIn = "./project/jni/vcmi-app/vcmi-app/Version.cpp.in"
	basePathOut = "./project/jni/vcmi-app/generated-version-dir/Version.cpp"
	inFile = open(basePathIn, "r")
	versionFileContent = inFile.read()
	inFile.close()

	replacedVersion = versionFileContent.replace("@GIT_SHA1@", version)
	outFile = open(basePathOut, "w")
	outFile.write(replacedVersion)
	outFile.close()

if __name__ == "__main__":
	add()
	