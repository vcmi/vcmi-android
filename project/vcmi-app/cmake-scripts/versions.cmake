execute_process(COMMAND git describe --match= --always --abbrev=40 
	WORKING_DIRECTORY "${VCMI_PATH_VCMI}"
	OUTPUT_VARIABLE GIT_SHA1_RAW
	ERROR_QUIET OUTPUT_STRIP_TRAILING_WHITESPACE)
set(GIT_SHA1 ${GIT_SHA1_RAW}-android)

configure_file("${VCMI_PATH_VCMI}/Version.cpp.in" "${CMAKE_BINARY_DIR}/Version.cpp" @ONLY)

file(READ "${VCMI_PATH_VCMI}/CMakeLists.txt" MAIN_VCMI_CMAKE_CONTENTS)
string(REGEX REPLACE ".*set\\(VCMI_VERSION_MAJOR ([0-9]+).*" "\\1" VCMI_VERSION_MAJOR ${MAIN_VCMI_CMAKE_CONTENTS})
string(REGEX REPLACE ".*set\\(VCMI_VERSION_MINOR ([0-9]+).*" "\\1" VCMI_VERSION_MINOR ${MAIN_VCMI_CMAKE_CONTENTS})
string(REGEX REPLACE ".*set\\(VCMI_VERSION_PATCH ([0-9]+).*" "\\1" VCMI_VERSION_PATCH ${MAIN_VCMI_CMAKE_CONTENTS})

string(LENGTH ${VCMI_VERSION_MAJOR} VERSION_HAS_MAJOR)
string(LENGTH ${VCMI_VERSION_MINOR} VERSION_HAS_MINOR)

if (${VERSION_HAS_MAJOR} AND ${VERSION_HAS_MINOR})
	set(VCMI_VERSION "${VCMI_VERSION_MAJOR}.${VCMI_VERSION_MINOR}.${VCMI_VERSION_PATCH} (${GIT_SHA1_RAW})")
else()
	set(VCMI_VERSION "unknown (${GIT_SHA1_RAW})")
endif()

message(STATUS "Detected vcmi version: ${VCMI_VERSION}")
configure_file(
	"${VCMI_ROOT}/project/GeneratedVersion.java.in" 
	"${VCMI_ROOT}/project/vcmi-app/src/main/java/eu/vcmi/vcmi/util/GeneratedVersion.java" 
	@ONLY)