cmake_minimum_required(VERSION 3.17)

set(CMAKE_MODULE_PATH "${VCMI_ROOT}/project/vcmi-app/cmake-scripts;${VCMI_ROOT}/ext/vcmi/cmake_modules")
include(VCMIUtils)
include(VersionDefinition)

set(ANDROID ON)

set(BOOST_ROOT ${VCMI_ROOT}/ext/boost/boost_1_68_0)
set(VCMI_PATH_VCMI ${VCMI_ROOT}/ext/vcmi)
set(VCMI_PATH_EXT_LIBS ${VCMI_ROOT}/ext-output/${ANDROID_ABI})

find_library(syslib_z z)
add_library(ZLIB::ZLIB UNKNOWN IMPORTED)
set_target_properties(ZLIB::ZLIB PROPERTIES IMPORTED_LOCATION ${syslib_z})

include_directories(${BOOST_ROOT})

include(ext-libs)
include(main-app)