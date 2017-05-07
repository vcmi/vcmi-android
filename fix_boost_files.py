import os
import vcmiutil
import vcmiconf

def fixBrokenFeatureDetectionInPthreadMutex():
	replacements = [ vcmiutil.ReplacementEntry("#if (defined(_POSIX_TIMEOUTS) && (_POSIX_TIMEOUTS-0)>=200112L) \\", "#if 0 \\") ]
	replacements2 = [ vcmiutil.ReplacementEntry("#if (defined _POSIX_TIMEOUTS && (_POSIX_TIMEOUTS-0)>=200112L) \\", "#if 0 \\") ]
	
	vcmiutil.fixFile("./ext/boost/" + vcmiconf.config["boostFolderName"] + "/boost/thread/pthread/mutex.hpp", replacements)
	vcmiutil.fixFile("./ext/boost/" + vcmiconf.config["boostFolderName"] + "/boost/thread/pthread/recursive_mutex.hpp", replacements2)

fixBrokenFeatureDetectionInPthreadMutex()