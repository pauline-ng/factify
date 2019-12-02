import subprocess
import json
import os


def process_json (json_filename, desired_journals, ofp):
    with open (json_filename, 'r') as f:
        json_dict = json.load (f)
    pubs_array = json_dict["message"]["items"]
    for citation in pubs_array:
        #print (citation)
        if "container-title" in citation:
            journal_title = citation["container-title"][0]
           # paper_title = citation["title"][0] # minimize what to require so get everything
            if "URL" in citation:
                URL = citation["URL"]
            else:
                URL = ""           
           #Chem covers Chemistry, Chemical, Chemie, etc
            if journal_title in  desired_journals or "Chem" in journal_title:
                DOI = citation["DOI"]
            #line = DOI + "\t" + URL + "\t" + paper_title + "\t" + journal_title
                line = DOI + "\t" + URL + "\t" + journal_title
                #print (line)
                ofp.write (line + "\n")
    f.close()

def extract_file (crossref_zip_file, filename, output_folder):
    command = "7z e " + crossref_zip_file + " -o" + output_folder + " " + filename + " -r -y -bsp0 -bso0"
    #print (command)
    subprocess.run (command.split (' '))

def read_desired_journals (filename):
    journal_dict = {}
    
    with open (filename, "r") as f:
        line_array = f.readlines()
        for line in line_array:
            if not line.startswith ("#"):
                journal = line.rstrip()
                journal_dict[journal] = 1
    f.close()

    return journal_dict

desired_journals = read_desired_journals ("chemical_journals.txt")
outfile = "dois_for_chemistry_journals"
ofp = open (outfile, "w")

with open ("crossref-works-files", "r") as xref_f:
    xref_file_array = xref_f.readlines()
xref_f.close()

for line in xref_file_array:
    fields = line.split ()
    xref_file = fields[5]
    print (xref_file)
    extract_file ("crossref-works.zip", xref_file, "./")
    process_json (xref_file, desired_journals, ofp)
    os.remove ("./" + xref_file) #cleanup

ofp.close()