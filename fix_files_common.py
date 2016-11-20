import os

class TmpReplacement:	
    def __init__(self, src, dst):
        self.src = src
        self.dst = dst
		
def rewriteFile(path, content):
	replacementFile = open(path + "__tmp", "w")
	replacementFile.write(content)
	replacementFile.close()	
	os.replace(path + "__tmp", path)
	
def entryMatches(entry, line, fullMatch):
	if fullMatch:
		return entry == line
	else:
		return str.find(line, entry) == 0 # basically line.startsWith(entry)
	
def fixFile(path, replacements, fullMatch=True):
	try:
		inputFile = open(path, "r")
		outputContent = ""
		replacedCount = 0
		for line in inputFile:
			replaced = False
			for rep in replacements:
				if entryMatches(rep.src.strip(), line.strip(), fullMatch):
					outputContent += rep.dst + "\n"
					replacedCount = replacedCount + 1
					replaced = True
					break
			if replaced == False:
				outputContent += line
		inputFile.close()
		rewriteFile(path, outputContent)
			
		if replacedCount > 0:
			print("Fixed " + path + " (replaced " + str(replacedCount) + " lines)")
		else:
			print("Didn't fix anything in " + path + " (already fixed?)")
	except FileNotFoundError:
		print("File " + path + " not found; skipping")