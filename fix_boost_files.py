import os
import fix_files_common
fix = fix_files_common

boostFolder = "boost_1_61_0"

#android build doesn't seems to be able to handle including .ipp file so just rename it to cpp + fix include

def fixLocaleIppInclude():
	replacements = [ fix.TmpReplacement("#include \"iconv_codepage.ipp\"", "#include \"iconv_codepage.cpp\"") ]
	
	fix.fixFile("./ext/boost/" + boostFolder + "/libs/locale/src/encoding/codepage.cpp", replacements)

def renameIpp():
	try:
		os.replace("./ext/boost/" + boostFolder + "/libs/locale/src/encoding/iconv_codepage.ipp",
			"./ext/boost/" + boostFolder + "/libs/locale/src/encoding/iconv_codepage.cpp")
	except OSError:
		print("Couldn't rename iconv_codepage.ipp (already fixed?)")
		
fixLocaleIppInclude()
renameIpp()