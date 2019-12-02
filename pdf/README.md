**get_dois.py**  gets the list of DOI for certain journals from CrossRef


In this example, chemical journals are shown.

1) Download crossref-works.zip from
https://github.com/elifesciences/datacapsule-crossref/blob/analysis/notebooks/citation-stats.ipynb
by looking for the appropriate link.

crossref-works.zip is large, so install 7z if not already installed

2) Get list of the filenames in the crossref-works.zip file

```
7z l crossref-works.zip > crossref-works-files
```

3) Because I'm lazy, the following is hardcoded in the program and everything is in the same directory as the python program:

* crossref-works.zip
* crossref-works-file
* desired journal file (called "desired_journals", e.g. chemical_journals.txt)
* outfile 

Find the above terms and change as needed

Run as

```
python3 get_dois.py
```
