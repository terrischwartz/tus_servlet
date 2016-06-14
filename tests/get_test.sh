#!/bin/bash


# First three commands complain about GET not supported, next three complain about 
# invalid version number. 

echo ==== GET $TUS_URL ====
curl  -i -H 'Tus-Resumable: 1.0.0' $TUS_URL 
echo ==== GET $TUS_URL/abc ====
curl  -i -H 'Tus-Resumable: 1.0.0' $TUS_URL/abc
echo ==== GET $TUS_URL/abc/def ====
curl  -i -H 'Tus-Resumable: 1.0.0' $TUS_URL/abc/def
echo ==== GET $TUS_URL ====
curl  -i $TUS_URL 
echo ==== GET $TUS_URL/abc ====
curl  -i $TUS_URL/abc
echo ==== GET $TUS_URL/abc/def ====
curl  -i $TUS_URL/abc/def

