#!/bin/bash

# From tus.io protocol spec:
# An OPTIONS request MAY be used to gather information about the Server’s current configuration. 
# A successful response indicated by the 204 No Content status MUST contain the Tus-Version header. 
# It MAY include the Tus-Extension and Tus-Max-Size headers.
#
# However, the tusd server returns 200 instead of 204 for compatability with cors 
# preflight handling in certain browsers. 


curl -X OPTIONS -i $TUS_URL -H 'Tus-Resumable: 1.0.0'  

