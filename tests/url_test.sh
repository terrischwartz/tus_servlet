#!/bin/bash

# All these requests should reach the servlet and return 404 NOT FOUND

BASE_URL='http://localhost:8080/files'

# Should return 404 Not Found because url lacks a file id 
curl -X HEAD -i $BASE_URL -H 'Tus-Resumable: 1.0.0'  

# Should return 404 Not Found because url lacks a valid id 
curl -X HEAD -i $BASE_URL/abcd -H 'Tus-Resumable: 1.0.0'  

# Should return 404 Not Found because url form is wrong. 
curl -X HEAD -i $BASE_URL/abcd/fgh -H 'Tus-Resumable: 1.0.0'  
