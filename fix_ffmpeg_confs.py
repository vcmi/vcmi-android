import os
import vcmiutil

# fixes ffmpeg library naming, so that they're built as libXXX.so, instead of libXXX.so.version (android cannot load libraries like that)

def fixFFConf():
	replacements = [ 
	vcmiutil.ReplacementEntry("SLIBNAME_WITH_VERSION=$(SLIBNAME).$(LIBVERSION)", "SLIBNAME_WITH_VERSION=$(SLIBNAME)"),
	vcmiutil.ReplacementEntry("SLIBNAME_WITH_MAJOR=$(SLIBNAME).$(LIBMAJOR)", "SLIBNAME_WITH_MAJOR=$(SLIBNAME)"),
	vcmiutil.ReplacementEntry("SLIB_INSTALL_NAME=$(SLIBNAME_WITH_VERSION) ", "SLIB_INSTALL_NAME=$(SLIBNAME)"),
	vcmiutil.ReplacementEntry("SLIB_INSTALL_LINKS=$(SLIBNAME_WITH_MAJOR) $(SLIBNAME)", "SLIB_INSTALL_LINKS=") 
	]
	
	vcmiutil.fixFile("./ext/ff/ffmpeg/configure", replacements)

def fixX264Conf():
	replacements = [ vcmiutil.ReplacementEntry("echo \"SONAME=libx264.so.$API\" >> config.mak", "echo \"SONAME=libx264.so\" >> config.mak") ]
	
	vcmiutil.fixFile("./ext/ff/x264/configure", replacements)
	
fixFFConf()
fixX264Conf()