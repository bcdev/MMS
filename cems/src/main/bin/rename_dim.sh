#!/bin/bash

path=$1
old_name=$2
new_name=$3

FILES=$path/*.nc

for f in $FILES
do
        nrename -d "${old_name}","${new_name}" $f
        echo $f
done