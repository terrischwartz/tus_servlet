#tus_servlet  
Java servlet implementing server side of the [TUS Protocol 1.0.0](http://www.tus.io/protocols/resumable-upload.html) 


## Quick Start

### Build and launch a standalone server at http://localhost:8080
```bash
$ mvn jetty:run
```
#### Quick Upload
```bash
$ curl -X POST -I 'http://localhost:8080/files' \
               -H 'Tus-Resumable: 1.0.0' \
               -H 'Upload-Length: 12345678'

HTTP/1.1 201 Created
Location: http://localhost:8080/files/70ede2c5_f139_4aeb_b2da_774149c68286
Tus-Resumable: 1.0.0
X-Content-Type-Options: nosniff
Content-Length: 0
Server: Jetty(7.2.0.v20101020)

$ curl -X PATCH -I 'http://localhost:8080/files/70ede2c5_f139_4aeb_b2da_774149c68286' \
               -H 'Tus-Resumable: 1.0.0' \
               -H 'Upload-Offset: 0' \
               -H 'Content-Type: application/offset+octet-stream' \
               --upload-file path/to/file.mp4

HTTP/1.1 100 Continue

HTTP/1.1 204 No Content
Upload-Offset: 2299
Tus-Resumable: 1.0.0
X-Content-Type-Options: nosniff
Content-Length: 0
Server: Jetty(7.2.0.v20101020)
```






#### Upload using a [javascript client](https://github.com/tus/tus-js-client)
```bash
# Get the code
$ git clone  https://github.com/tus/tus-js-client
$ cd tus-js-client/demo
$ ls
demo.css	demo.js		index.html
```
Open index.html in a browser.  In the form, set the upload endpoint to http://localhost:8080/files and use the Browse button to try out uploads. This servlet doesn't implement GET so downloads won't work.
