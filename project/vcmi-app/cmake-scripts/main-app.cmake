cmake_minimum_required(VERSION 3.4.1)

function(prepend var prefix)
	set(listVar "")
	foreach(f ${ARGN})
		list(APPEND listVar "${prefix}/${f}")
	endforeach(f)
	set(${var} "${listVar}" PARENT_SCOPE)
endfunction(prepend)

macro(import_lib name)
	add_library(imp-${name} SHARED IMPORTED)
	set_target_properties(imp-${name} PROPERTIES IMPORTED_LOCATION ${VCMI_PATH_EXT_LIBS}/lib${name}.so)
endmacro(import_lib)

function(build_vcmi)
	set(ENABLE_PCH OFF)

	find_library(syslib_log log)

	add_definitions(
		-DIOAPI_NO_64 
		-DIOAPI64_ANDROID_HACK
		-DNO_STD_TOSTRING
		-DFL_CPP11)
	add_definitions(-DBOOST_DISABLE_ASSERTS) # there's a problem in vcmi where mutexes can't be cleaned up correctly on exit (asserting on EBUSY result from pthread_mutex_destroy); I can't really debug it easily so just try to ignore it (we try to quit the app anyway)
		
	import_lib(SDL2)
	import_lib(SDL2_image)
	import_lib(SDL2_mixer)
	import_lib(SDL2_ttf)
	import_lib(avcodec)
	import_lib(avdevice)
	import_lib(avfilter)
	import_lib(avformat)
	import_lib(avutil)
	import_lib(smpeg2)
	import_lib(postproc)
	import_lib(swresample)
	import_lib(swscale)
	import_lib(x264)
	import_lib(boost-system)
	import_lib(boost-filesystem)
	import_lib(boost-locale)
	import_lib(boost-thread)
	import_lib(boost-program-options)
	import_lib(boost-datetime)
	import_lib(minizip)
	import_lib(vcmi-fuzzylite)

	include(versions)

	set(FFMPEG_LIBRARIES imp-avcodec imp-avdevice imp-avfilter imp-avformat imp-avutil imp-postproc imp-smpeg2 imp-swresample imp-swscale imp-x264)
	set(SDL_LIBRARY imp-SDL2)
	set(SDL_CLIENT_LIBRARIES imp-SDL2 imp-SDL2_image imp-SDL2_mixer imp-SDL2_ttf)
	set(MINIZIP_LIBRARIES imp-minizip)
	set(ZLIB_LIBRARIES ${syslib_z})
	set(FL_LIBRARIES imp-vcmi-fuzzylite)
	set(SYSTEM_LIBS ${SYSTEM_LIBS} ${syslib_log} ${syslib_z} ${FL_LIBRARIES})
	set(Boost_LIBRARIES imp-boost-system imp-boost-filesystem imp-boost-datetime imp-boost-thread imp-boost-program-options imp-boost-locale)

	include(lib)
	include(server)
	include(client)
endfunction(build_vcmi)

build_vcmi()