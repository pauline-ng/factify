README for pdf-to-xml

pdf-to-xml is a simple command line tool provided to quickly extract text and meta-data from a given list of PDFs. These PDFs are expected to contain scientific articles, where a single PDF contains just a single article.

There are two modes of operation:
* default, generates a single .xml file
* annotation mode, generates a .txt file and an .xml file


== Usage ==

bin/pdf-to-xml [-a] <pdf-file>+
The tool takes a list of PDF file as argument. The output will be written into the same directory as the PDFs reside. The option -a indicates to use the annotation mode

Requirement:
In the current working directory there need to be 'models' sub-directory, which contains all the .bin files and a language-model directory.

== Default Mode ==

The .xml file contains the core meta-data, as well as the references, for example:

<scientific-article>
  <core-metadata>
    <journal>Journal of Experimental & Clinical Cancer Research</journal>
    <title>Nestin and CD133: valuable stem cell-specific markers for determining clinical outcome of glioma patients</title>
    <author>
      <given-name>Mingyu</given-name>
      <sur-name>Zhang</sur-name>
    </author>
  </core-metadata>
  <references>
    <reference>* Dell'Albani Paola: Stem Cell Markers in Gliomas. Neurochem Res in press </reference>
  </references>
</scientific-article>


== Annotation Mode ==

Metadata Annotations:
The relevant annotations are marked with type="Scientific Article Metadata", e.g.:
<annotation end="53" start="7" type="Scientific Article Metadata"><feature value="title"/></annotation>
The feature value might be one of (only the main types are listed here):
* title
* subtitle
* journal
* academic-title
* given-name
* middle-name
* surname
* affiliation
* email
* doi
And for references:
* ref-authorGivenName
* ref-authorSurname
* ref-title
* ref-date
* ref-editor
* ref-issueTitle
* ref-publisher
* ref-bookTitle
* ref-pages
* ref-location
* ref-conference
* ref-source
* ref-volume
* ref-edition
* ref-issue
* ref-url

== References ==

[1] Kern, R., Jack, K., Hristakeva, M., & Granitzer, M. (2012). TeamBeam Meta-Data Extraction from Scientific Literature. D-Lib Magazine, 18(7), 1., http://www.dlib.org/dlib/july12/kern/07kern.html
[2] Klampfl, S., & Kern, R. (2013). An Unsupervised Machine Learning Approach to Body Text and Table of Contents Extraction from Digital Scientific Articles. In Research and Advanced Technology for Digital Libraries (pp. 144-155). Springer Berlin Heidelberg.
FAQ: 
[3] Kern, R., & Kampfl, S. (2013). Extraction of References Using Layout and Formatting Information from Scientific Articles. D-Lib Magazine, 19(9), 2., http://www.dlib.org/dlib/september13/kern/09kern.html

== FAQ ==

Q: I get an error message "java.lang.RuntimeException: Couldn't load model(s)"
A: Probably there is no directory named "models" in the current working directory


