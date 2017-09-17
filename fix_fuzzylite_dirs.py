import os
import vcmiutil

# changes fuzzylite hardcoded output dirs (FL/out/bin) to conditional set, so it doesn't override our arch-based output folder

def fixFuzzyliteHardcodedOutputDirs():
	replacements = [ 
	vcmiutil.ReplacementEntry("set(CMAKE_ARCHIVE_OUTPUT_DIRECTORY bin)", "if (NOT CMAKE_ARCHIVE_OUTPUT_DIRECTORY)\n\tset(CMAKE_ARCHIVE_OUTPUT_DIRECTORY bin) #fixed via vcmi python\nendif()"),
	vcmiutil.ReplacementEntry("set(CMAKE_LIBRARY_OUTPUT_DIRECTORY bin)", "if (NOT CMAKE_LIBRARY_OUTPUT_DIRECTORY)\n\tset(CMAKE_LIBRARY_OUTPUT_DIRECTORY bin) #fixed via vcmi python\nendif()"),
	vcmiutil.ReplacementEntry("set(CMAKE_RUNTIME_OUTPUT_DIRECTORY bin)", "if (NOT CMAKE_RUNTIME_OUTPUT_DIRECTORY)\n\tset(CMAKE_RUNTIME_OUTPUT_DIRECTORY bin) #fixed via vcmi python\nendif()")
	]
	
	vcmiutil.fixFile("./ext/vcmi/AI/FuzzyLite/fuzzylite/CMakeLists.txt", replacements)
	
fixFuzzyliteHardcodedOutputDirs()