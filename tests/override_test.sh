#!/bin/bash

# Test method override header.  Should start a new upload, and send the testdata.txt file contents:
# HTTP/1.1 204 No Content
# Tus-Resumable: 1.0.0
# Upload-Offset: 2299


SIZE=2299
FILE="./testdata.txt"

URL=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  -H "Upload-Length: $SIZE" |  \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "STARTED NEW UPLOAD.  URL=|$URL|"


curl -X POST -i $URL \
	-H 'X-HTTP-Method-Override: PATCH' \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
	--upload-file $FILE 

