#!/bin/bash

# In this test, we've split testdata.txt into 2 parts, testdata1.txt (2000 bytes)
# and testdata2.txt (299) bytes.  We'll do HEAD commands before and after each
# PATCH to see how many bytes the server says it's received.

# From tus.io protcol spec, regarding HEAD:
# Server must always return Upload-Offset and Upload-Length if the upload exists.
# If upload doesn't exist, server must return 404, 410 or 403, without Upload-Offset header.
# Server must prevent caching by sending Cache-Control: no store header


SIZE1=2000
SIZE2=299
SIZE=2299
PART1="./testdata1.txt"
PART2="./testdata2.txt"

URL=$(curl -X POST -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  -H "Upload-Length: $SIZE" |  \
	grep 'Location:' |  \
	sed "s/$(printf '\r')\$//" | \
	awk '{print $2}')

echo "### URL is $URL"

echo "Send HEAD before first PATCH.  Upload-Offset should be 0"
curl -X HEAD -i $URL -H 'Tus-Resumable: 1.0.0'

echo "### Sending PATCH of first part of $SIZE1 bytes"
curl -X PATCH -i $URL \
	-H 'Tus-Resumable: 1.0.0'  \
	-H 'Upload-Offset: 0' \
	-H 'Content-Type: application/offset+octet-stream' \
	--upload-file $PART1

echo "### Send HEAD.  Upload-Offset will be be $SIZE1."
curl -X HEAD -i $URL -H 'Tus-Resumable: 1.0.0'

echo "### Sending PATCH of 2nd part of $SIZE2 bytes"
curl -X PATCH -i $URL \
	-H 'Tus-Resumable: 1.0.0'  \
	-H "Upload-Offset: $SIZE1" \
	-H 'Content-Type: application/offset+octet-stream' \
	--upload-file $PART2


echo "### Send HEAD. Upload-Offset will be be $SIZE now that both parts of file have been uploaded."
curl -X HEAD -i $URL -H 'Tus-Resumable: 1.0.0'

