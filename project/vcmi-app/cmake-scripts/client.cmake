project(vcmiclient)

add_subdirectory(${VCMI_PATH_VCMI}/client ${VCMI_PATH_VCMI}/out/client)
get_directory_property(LOCAL_CLIENT_SRCS DIRECTORY ${VCMI_PATH_VCMI}/client DEFINITION client_SRCS)
prepend(VCMICLIENT_SRCS ${VCMI_PATH_VCMI}/client ${LOCAL_CLIENT_SRCS})

set(CLIENT_ADDITIONAL_SRCS ${VCMI_ROOT}/project/vcmi-client/src/main/cpp/SDL_android_main.cpp)

add_library(vcmiclient SHARED ${VCMICLIENT_SRCS} ${CLIENT_ADDITIONAL_SRCS})

target_include_directories(vcmiclient PRIVATE ${VCMI_PATH_VCMI}/client)
target_include_directories(vcmiclient PRIVATE ${VCMI_PATH_VCMI}/include)
target_include_directories(vcmiclient PRIVATE ${VCMI_PATH_VCMI})
target_include_directories(vcmiclient PRIVATE ${VCMI_ROOT}/ext/SDL2/core/include)
target_include_directories(vcmiclient PRIVATE ${VCMI_ROOT}/ext/SDL2/SDL2-image/include)
target_include_directories(vcmiclient PRIVATE ${VCMI_ROOT}/ext/SDL2/SDL2-mixer/include)
target_include_directories(vcmiclient PRIVATE ${VCMI_ROOT}/ext/SDL2/SDL2-ttf/include)
target_include_directories(vcmiclient PRIVATE ${VCMI_ROOT}/ext/ff/include)

target_link_libraries(vcmiclient vcmi ${Boost_LIBRARIES} ${SDL_CLIENT_LIBRARIES} ${FFMPEG_LIBRARIES} ${SYSTEM_LIBS})

set_target_properties(vcmiclient PROPERTIES OUTPUT_NAME vcmi-client)