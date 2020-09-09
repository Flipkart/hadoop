#!/bin/bash

mkdir -p /tmp/dep

dirs=`find . -type f -name pom.xml `
if [ "./pom.xml" == "$dirs" ] 
then 
	dirname=`basename "$PWD"`
	cd ..
	dirs=$dirname
fi
echo $dirs | tr " " "\n" | while read dir
	do dirpath=`echo $dir | sed 's/.\/\(.*\)\/.*/\1/' `
	cd $dirpath
	dirname=`basename "$PWD"`
	mvn dependency:tree > /tmp/dep/$dirname
	grep jar /tmp/dep/$dirname  | awk -F "- " '{print $2}' > /tmp/dep/pkg$dirname
	if [ ! -s "/tmp/dep/pkg$dirname" ] 
	then 
		echo $dirpath" Build Failure" 
	else
		grep -v "^$" /tmp/dep/pkg$dirname | while read a ; do pkg=`echo $a | cut -d ":" -f"1,2"` ; ver=`echo $a | cut -d":" -f 4` ; echo $pkg" "$ver ; done | while read i j ; do output=`curl -s "http://10.85.59.116/artifactory/v1.0/artifacts/search?packageName="$i"&store=MAVEN&version="$j` ; echo $dirpath $i" "$j" "$output ; done | grep -v "AVAILABLE"
	fi
	cd - > /dev/null
done
