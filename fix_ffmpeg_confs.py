import os
import fix_files_common
fix = fix_files_common

# fixes ffmpeg library naming, so that they're built as libXXX.so, instead of libXXX.so.version (android cannot load libraries like that)

def fixFFConf():
	replacements = [ 
	fix.TmpReplacement("SLIBNAME_WITH_VERSION=$(SLIBNAME).$(LIBVERSION)", "SLIBNAME_WITH_VERSION=$(SLIBNAME)"),
	fix.TmpReplacement("SLIBNAME_WITH_MAJOR=$(SLIBNAME).$(LIBMAJOR)", "SLIBNAME_WITH_MAJOR=$(SLIBNAME)"),
	fix.TmpReplacement("SLIB_INSTALL_NAME=$(SLIBNAME_WITH_VERSION) ", "SLIB_INSTALL_NAME=$(SLIBNAME)"),
	fix.TmpReplacement("SLIB_INSTALL_LINKS=$(SLIBNAME_WITH_MAJOR) $(SLIBNAME)", "SLIB_INSTALL_LINKS=") 
	]
	
	fix.fixFile("./ext/ff/ffmpeg/configure", replacements)

def fixX264Conf():
	replacements = [ fix.TmpReplacement("echo \"SONAME=libx264.so.$API\" >> config.mak", "echo \"SONAME=libx264.so\" >> config.mak") ]
	
	fix.fixFile("./ext/ff/x264/configure", replacements)
	
fixFFConf()
fixX264Conf()