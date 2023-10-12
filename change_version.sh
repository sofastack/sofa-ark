#!/bin/bash
shopt -s expand_aliases
if [ ! -n "$1" ] ;then
	echo "Please enter a version"
 	exit 1	
else
  	echo "The updated version is $1 !"
fi

currentVersion=`sed -n '/<revision>/p' pom.xml | cut -d '>' -f2 | cut -d '<' -f1`
echo "The current version is $currentVersion"

if [ `uname` == "Darwin" ] ;then
 	echo "This is OS X"
 	alias sed='sed -i ""'
else
 	echo "This is Linux"
 	alias sed='sed -i'
fi

for filename in `find . -name "README*.md"`;do
	echo "Deal with $filename"
	sed "/badge\/maven/! s/$currentVersion/$1/" $filename
done