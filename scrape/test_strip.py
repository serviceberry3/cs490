import os

res = None
PRODS_FOLDER = "../products"


def absoluteFilePaths(directory):
        for dirpath, _, filenames in os.walk(directory):
            for f in filenames:
                #yield suspends function’s execution and sends value back to caller, but retains state to enable fxn to resume where left off. When the function resumes, it continues execution immediately after the last yield run. 
                #This allows it to produce series of values over time, rather than computing them at once and sending them back like a list.
                yield os.path.abspath(os.path.join(dirpath, f))



for filename in absoluteFilePaths(PRODS_FOLDER):
    print("Now opening", filename)
    #iterate over all of the JSON product files, removing the 65533 characters (junk/invalid characters) in each file
    with open(filename, "r", encoding='latin-1') as fp:
        #each JSON should have just one line
        for line in fp:
            #line = line.strip()
            #line = bytes(line, 'utf-8').decode('utf-8', 'ignore')
            res = line.replace("\ufffd", "")
            #print("found one line in 244407")

    #open file and overwrite contents with the corrected JSON
    with open(filename, "w") as fp:
        fp.write(res)
