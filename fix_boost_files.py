import os
import vcmiutil
import vcmiconf

def fixReaddirRUsage():
	replacements = [ vcmiutil.ReplacementEntry("#   if !defined(__CYGWIN__)\\", "#if 0\\") ]
	
	vcmiutil.fixFile("./ext/boost/" + vcmiconf.config["boostFolderName"] + "/libs/filesystem/src/operations.cpp", replacements)

def fixBrokenFeatureDetectionInPthreadMutex():
	replacements = [ vcmiutil.ReplacementEntry("#if (defined(_POSIX_TIMEOUTS) && (_POSIX_TIMEOUTS-0)>=200112L) \\", "#if 0 \\") ]
	replacements2 = [ vcmiutil.ReplacementEntry("#if (defined _POSIX_TIMEOUTS && (_POSIX_TIMEOUTS-0)>=200112L) \\", "#if 0 \\") ]
	
	vcmiutil.fixFile("./ext/boost/" + vcmiconf.config["boostFolderName"] + "/boost/thread/pthread/mutex.hpp", replacements)
	vcmiutil.fixFile("./ext/boost/" + vcmiconf.config["boostFolderName"] + "/boost/thread/pthread/recursive_mutex.hpp", replacements2)
	
def fixBrokenEpollDetectionOnOldApi():
	replacements = [ vcmiutil.ReplacementEntry("#if defined(EPOLL_CLOEXEC)", "#if defined(EPOLL_CLOEXEC) && __ANDROID_API__ >= 21") ]
	
	vcmiutil.fixFile("./ext/boost/" + vcmiconf.config["boostFolderName"] + "/boost/asio/detail/impl/epoll_reactor.ipp", replacements)

fixBrokenFeatureDetectionInPthreadMutex()
fixReaddirRUsage()
fixBrokenEpollDetectionOnOldApi()