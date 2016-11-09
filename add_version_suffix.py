def add():
	version = "-android-testing"

	basePath = "./project/jni/vcmi-app/vcmi-app/Version.cpp"
	inFile = open(basePath + ".in", "r")
	versionFileContent = inFile.read()
	inFile.close()

	replacedVersion = versionFileContent.replace("@GIT_SHA1@", version)
	outFile = open(basePath, "w")
	outFile.write(replacedVersion)
	outFile.close()

if __name__ == "__main__":
	add()
	