project(vcmiserver)

add_subdirectory(${VCMI_PATH_VCMI}/server ${VCMI_PATH_VCMI}/out/server)
get_directory_property(LOCAL_SERVER_SRCS DIRECTORY ${VCMI_PATH_VCMI}/server DEFINITION server_SRCS)
prepend(VCMISERVER_SRCS ${VCMI_PATH_VCMI}/server ${LOCAL_SERVER_SRCS})

set(SERVER_ADDITIONAL_SRCS ${VCMI_ROOT}/project/vcmi-server/src/main/cpp/ServerJNIBindings.cpp)

add_library(vcmiserver SHARED ${VCMISERVER_SRCS} ${SERVER_ADDITIONAL_SRCS})

target_include_directories(vcmiserver PRIVATE ${VCMI_PATH_VCMI}/server)
target_include_directories(vcmiserver PRIVATE ${VCMI_PATH_VCMI}/include)
target_include_directories(vcmiserver PRIVATE ${VCMI_PATH_VCMI})

target_link_libraries(vcmiserver vcmi)

set_target_properties(vcmiserver PROPERTIES OUTPUT_NAME vcmi-server)