#!/bin/bash

for filename in lib/*.jar; do
	groupid="${filename/lib\//}"
	groupid="${groupid/\.jar/}"
	#groupid="${groupid/\///}"
	printf "###\n#Adding $filename with groupid $groupid\n###\n"
        #mvn install:install-file "–Dfile=$filename" "-DgroupId=$groupid" "-DartifactId=localversion" "-Dversion=1.0" "-Dpackaging=jar" "-DgeneratePom=false"
        mvn install:install-file "-Dfile=$filename" "-DgroupId=$groupid" "-DartifactId=localversion" "-Dversion=1.0" "-Dpackaging=jar" "-DgeneratePom=false"
	#exit
done

