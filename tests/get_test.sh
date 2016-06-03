#!/bin/bash

URL='http://localhost:8080/files'
# URL='http://localhost:7070/restusers/upload'

# Should return 404 not found (GET is not implemented by this servlet)
curl  -i $URL 

# Should return 404 not found (GET is not implemented by this servlet)
curl  -i $URL/abc

# Should return 404 not found (GET is not implemented by this servlet)
curl  -i $URL/abc/def

