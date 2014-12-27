#!/bin/bash

COOKBOOK=$1
FILE_NAME=$COOKBOOK'.tar.gz'

wget https://supermarket.getchef.com/cookbooks/$COOKBOOK/download -O $FILE_NAME
tar -vzxf $FILE_NAME
rm $FILE_NAME
