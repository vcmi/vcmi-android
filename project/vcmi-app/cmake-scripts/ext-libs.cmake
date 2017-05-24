cmake_minimum_required(VERSION 3.4.1)

macro(setup_boost_lib name recurse)
	set(tmpList "")
	foreach(f ${ARGN})
		if (${recurse})
			file(GLOB_RECURSE tmpGlob ${BOOST_ROOT}/libs/${f}/*.cpp ${BOOST_ROOT}/libs/${f}/*.ipp) 
		else()
			file(GLOB tmpGlob ${BOOST_ROOT}/libs/${f}/*.cpp ${BOOST_ROOT}/libs/${f}/*.ipp) 
		endif()
		list(APPEND tmpList ${tmpGlob})
	endforeach(f)
	add_library(${name} SHARED ${tmpList})
	set_target_properties(${name} PROPERTIES LINKER_LANGUAGE CXX)
	target_link_libraries(${name} atomic)
endmacro(setup_boost_lib)

function(build_fuzzylite)
	set(FL_BUILD_STATIC OFF CACHE BOOL "Build static library")
	set(FL_BUILD_BINARY OFF CACHE BOOL "Build fuzzylite binary")
	set(FL_CPP11 ON CACHE BOOL "Builds utilizing C++11, i.e., passing -std=c++11")
	set(FL_INSTALL_LIBDIR ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
	set(FL_INSTALL_BINDIR ${CMAKE_LIBRARY_OUTPUT_DIRECTORY})
	add_subdirectory(${VCMI_PATH_VCMI}/AI/FuzzyLite/fuzzylite ${VCMI_PATH_VCMI}/AI/FuzzyLite/OUT)
	set_target_properties(fl-shared PROPERTIES OUTPUT_NAME vcmi-fuzzylite)
	set_target_properties(fl-shared PROPERTIES DEBUG_POSTFIX "")
endfunction(build_fuzzylite)

function(build_minizip)
	set(minizippath ${VCMI_PATH_VCMI}/lib/minizip)
	add_library(minizip SHARED ${minizippath}/zip.c ${minizippath}/unzip.c ${minizippath}/ioapi.c)
	set_target_properties(minizip PROPERTIES LINKER_LANGUAGE C)
	target_link_libraries(minizip ${syslib_z})
endfunction(build_minizip)

function(build_boost)
	add_definitions(-DBOOST_DISABLE_ASSERTS)

	add_library(iconv SHARED IMPORTED)
	set_target_properties(iconv PROPERTIES IMPORTED_LOCATION ${VCMI_PATH_EXT_LIBS}/libiconv.so)

	setup_boost_lib(boost-datetime ON date_time/src)
	setup_boost_lib(boost-filesystem ON filesystem/src)
	setup_boost_lib(boost-system ON system/src)
	setup_boost_lib(boost-program-options ON program_options/src)
	setup_boost_lib(boost-locale ON locale/src/encoding locale/src/shared locale/src/std locale/src/util)

	target_include_directories(boost-locale PRIVATE ${VCMI_ROOT}/ext/iconv/code/include)
	target_compile_definitions(boost-locale PRIVATE -DBOOST_LOCALE_NO_WINAPI_BACKEND -DBOOST_LOCALE_NO_POSIX_BACKEND -DBOOST_LOCALE_WITH_ICONV)

	#adding boost-thread manually, because they #include .cpp files directly and we can't just add everything to project
	add_library(boost-thread SHARED ${BOOST_ROOT}/libs/thread/src/future.cpp ${BOOST_ROOT}/libs/thread/src/tss_null.cpp ${BOOST_ROOT}/libs/thread/src/pthread/once.cpp ${BOOST_ROOT}/libs/thread/src/pthread/thread.cpp)
	set_target_properties(boost-thread PROPERTIES LINKER_LANGUAGE CXX)	
	target_link_libraries(boost-thread atomic)

	target_link_libraries(boost-locale iconv)
	target_link_libraries(boost-thread boost-system)
	target_link_libraries(boost-filesystem boost-system)
endfunction(build_boost)

function(build_libs)
	set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${VCMI_PATH_EXT_LIBS})
	set(CMAKE_INSTALL_LIBDIR ${VCMI_PATH_EXT_LIBS})

	set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-deprecated")
	set(CMAKE_BUILD_TYPE Release)

	add_definitions(
		-DBOOST_ERROR_CODE_HEADER_ONLY 
		-DBOOST_SYSTEM_NO_DEPRECATED
		-DIOAPI_NO_64)
			
	build_boost()
	build_minizip()
	build_fuzzylite()

endfunction(build_libs)

build_libs()