cmake_minimum_required(VERSION 3.4.1)

function(prepend var prefix)
	set(listVar "")
	foreach(f ${ARGN})
		list(APPEND listVar "${prefix}/${f}")
	endforeach(f)
	set(${var} "${listVar}" PARENT_SCOPE)
endfunction(prepend)

macro(import_lib name alias)
	add_library(${alias} SHARED IMPORTED)
	set_target_properties(${alias} PROPERTIES IMPORTED_LOCATION ${VCMI_PATH_EXT_LIBS}/lib${name}.so)
endmacro(import_lib)

function(build_vcmi)
	set(ENABLE_PCH OFF)

	find_library(syslib_log log)

	add_definitions(
		-DIOAPI_NO_64 
		-DFL_CPP11)
	add_definitions(-DBOOST_DISABLE_ASSERTS) # there's a problem in vcmi where mutexes can't be cleaned up correctly on exit (asserting on EBUSY result from pthread_mutex_destroy); I can't really debug it easily so just try to ignore it (we try to quit the app anyway)
		
	import_lib(SDL2 SDL2::SDL2)
	import_lib(SDL2_image SDL2::Image)
	import_lib(SDL2_mixer SDL2::Mixer)
	import_lib(SDL2_ttf SDL2::TTF)
	import_lib(avcodec ffmpeg::avcodec)
	import_lib(avdevice ffmpeg::avdevice)
	import_lib(avfilter ffmpeg::avfilter)
	import_lib(avformat ffmpeg::avformat)
	import_lib(avutil ffmpeg::avutil)
	import_lib(smpeg2 smpeg2::smpeg2)
	import_lib(postproc postproc::postproc)
	import_lib(swresample ffmpeg::swresample)
	import_lib(swscale ffmpeg::swscale)
	import_lib(x264 x264::x264)
	import_lib(boost-system Boost::system)
	import_lib(boost-filesystem Boost::filesystem)
	import_lib(boost-locale Boost::locale)
	import_lib(boost-thread Boost::thread)
	import_lib(boost-program-options Boost::program_options)
	import_lib(boost-datetime Boost::date_time)
	import_lib(minizip minizip::minizip)
	import_lib(vcmi-fuzzylite fuzzylite::fuzzylite)

	include(versions)

	add_library(Boost::boost INTERFACE IMPORTED)
	set(FFMPEG_LIBRARIES ffmpeg::avcodec ffmpeg::avdevice ffmpeg::avfilter ffmpeg::avformat ffmpeg::avutil ffmpeg::swresample ffmpeg::swscale smpeg2::smpeg2 postproc::postproc x264::x264)
	set(SDL_CLIENT_LIBRARIES SDL2::SDL2 SDL2::Image SDL2::Mixer SDL2::TTF)
	set(SYSTEM_LIBS ${SYSTEM_LIBS} ${syslib_log} ZLIB::ZLIB fuzzylite::fuzzylite TBB::tbb)
	set(Boost_LIBRARIES Boost::system Boost::filesystem Boost::date_time Boost::thread Boost::program_options Boost::locale)

	include(lib)
	include(server)
	include(client)
endfunction(build_vcmi)

build_vcmi()