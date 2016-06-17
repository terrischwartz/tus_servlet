#!/bin/bash

SIZE=2299
FILE="./testdata.txt"

URL=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  -H "Upload-Length: $SIZE" |  \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "URL is $URL"

curl -X PATCH -i $URL \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
	--upload-file $FILE 

echo "Doing DELETE, should return NO CONTENT"
curl -X DELETE -i $URL -H 'Tus-Resumable: 1.0.0'  

echo "Try DELETE again.  Should return NOT FOUND."
curl -X DELETE -i $URL -H 'Tus-Resumable: 1.0.0'  

echo "Try HEAD.  Should return NOT FOUND"
curl -X HEAD -i $URL -H 'Tus-Resumable: 1.0.0'

