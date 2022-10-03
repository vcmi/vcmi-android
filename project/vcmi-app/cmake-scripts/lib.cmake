project(vcmi)

add_subdirectory(${VCMI_PATH_VCMI}/AI/BattleAI ${VCMI_PATH_VCMI}/out/battleai)
add_subdirectory(${VCMI_PATH_VCMI}/AI/VCAI ${VCMI_PATH_VCMI}/out/vcai)
add_subdirectory(${VCMI_PATH_VCMI}/AI/Nullkiller ${VCMI_PATH_VCMI}/out/nullkiller)
get_directory_property(LOCAL_BATTLEAI_SRCS DIRECTORY ${VCMI_PATH_VCMI}/AI/BattleAI DEFINITION battleAI_SRCS)
get_directory_property(LOCAL_VCAI_SRCS DIRECTORY ${VCMI_PATH_VCMI}/AI/VCAI DEFINITION VCAI_SRCS)
get_directory_property(LOCAL_NULLKILLER_SRCS DIRECTORY ${VCMI_PATH_VCMI}/AI/Nullkiller DEFINITION Nullkiller_SRCS)
list(REMOVE_ITEM LOCAL_BATTLEAI_SRCS main.cpp)
list(REMOVE_ITEM LOCAL_VCAI_SRCS main.cpp)
list(REMOVE_ITEM LOCAL_NULLKILLER_SRCS main.cpp)
prepend(VCMILIB_ADDITIONAL_SRC_BATTLEAI ${VCMI_PATH_VCMI}/AI/BattleAI ${LOCAL_BATTLEAI_SRCS})
prepend(VCMILIB_ADDITIONAL_SRC_VCAI ${VCMI_PATH_VCMI}/AI/VCAI ${LOCAL_VCAI_SRCS})
prepend(VCMILIB_ADDITIONAL_SRC_NULLKILLER ${VCMI_PATH_VCMI}/AI/Nullkiller ${LOCAL_NULLKILLER_SRCS})

set(VCMILIB_ADDITIONAL_SOURCES ${VCMILIB_ADDITIONAL_SRC_BATTLEAI} ${VCMILIB_ADDITIONAL_SRC_VCAI} ${VCMILIB_ADDITIONAL_SRC_NULLKILLER})

set(MAIN_LIB_DIR "${VCMI_PATH_VCMI}/lib")
include(VCMI_lib)

add_subdirectory(${VCMI_PATH_VCMI}/lib ${VCMI_PATH_VCMI}/out/lib)

target_compile_definitions(vcmi PRIVATE -DM_DATA_DIR="none" -DM_BIN_DIR="none" -DM_LIB_DIR="none") # these aren't used by android code, but need to be defined to compile correctly

target_include_directories(vcmi PRIVATE ${VCMI_PATH_VCMI}/lib)
target_include_directories(vcmi PRIVATE ${VCMI_PATH_VCMI}/include)
target_include_directories(vcmi PRIVATE ${VCMI_PATH_VCMI})
target_include_directories(vcmi PRIVATE ${VCMI_PATH_VCMI}/AI/FuzzyLite/fuzzylite)
target_include_directories(vcmi PRIVATE ${VCMI_ROOT}/ext/SDL2/core/include)
target_include_directories(vcmi PRIVATE ${VCMI_ROOT}/ext/oneTBB/include)

set_target_properties(vcmi PROPERTIES OUTPUT_NAME vcmi-lib)