#!/bin/bash

# Should do upload (NO_CONTENT status with upload-offset = size) and return Access-Control
# headers.

SIZE=2299
FILE="./testdata.txt"

URL=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  -H "Upload-Length: $SIZE" -H 'Origin: here'|  \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "URL is $URL"

curl -X PATCH -i $URL \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
	-H 'Origin: here' \
	--upload-file $FILE 

