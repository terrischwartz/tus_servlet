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
# Get the client code
$ git clone  https://github.com/tus/tus-js-client
$ cd tus-js-client/demo
$ ls
demo.css	demo.js		index.html
```
Open index.html in a browser.  In the form, set the upload endpoint to http://localhost:8080/files and use the Browse button to try out uploads. This servlet doesn't implement GET so downloads won't work.

## Incorporate the Servlet in Your Application
```bash
# Build a jar containing the servlet
$ mvn package
# Install the jar in your local mvn repo
$ mvn install
```

### Add the dependency to pom.xml 
```xml
<!-- The tus_servlet itself: -->
<dependency>
    <groupId>org.tus</groupId>
    <artifactId>tus_servlet</artifactId>
    <version>0.1-SNAPSHOT</version>
</dependency>

<!-- The tus_servlet uses these jars for json marshaling:  -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-core</artifactId>
    <version>2.6.3</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.6.3</version>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <version>2.6.3</version>
</dependency>
<!-- end for json marshaling -->

<!-- slf4j for logging -->
<dependency>
	<groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
    <version>1.7.5</version>
</dependency>
<!-- This causes slf4j to use log4j. You can use a different logger if desired. -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-log4j12</artifactId>
    <version>1.7.5</version>
</dependency>




```

### Add the filter and servlet to web.xml 
```xml
    <filter>
    	<filter-name>MethodOverrideFilter</filter-name>
        <filter-class>org.tus.filter.methodoverride.HttpMethodOverrideFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>MethodOverrideFilter</filter-name>
        <servlet-name>upload</servlet-name>
    </filter-mapping>

    <servlet>
        <servlet-name>upload</servlet-name>
        <servlet-class> org.tus.servlet.upload.Upload </servlet-class>
        <!-- More info about init-params below -->
		<init-param>
            <param-name>uploadFolder</param-name>
            <param-value>/tmp</param-value>
        </init-param>
        <init-param>
            <param-name>maxFileSize</param-name>
            <param-value>0</param-value>
        </init-param>
        <init-param>
            <param-name>maxStorage</param-name>
            <param-value>0</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>

    <servlet-mapping>
        <servlet-name>upload</servlet-name>
        <url-pattern>/files/*</url-pattern>
    </servlet-mapping>
```

If using the servlet in a struts app, you'll need to configure struts.xml to ignore the path that this servlet is handling. For example:
```
<constant name="struts.action.excludePattern" value="/files,/files/[0-9a-zA-Z_]*"/>
```

## Configuration (web.xml init-param)
* **uploadFolder** - Absolute pathname of the directory to which files will be uploaded.  Directory must already exist.
* **maxFileSize** - Maxiumum number of bytes allowed for a single file.  0 means unlimited.
* **maxStorage** - Maximum number of bytes this servlet will use for uploaded files.  0 means unlimited.  *NOT IMPLEMENTED.*
* **maxRequest** - Maximum number of bytes server will accept in a single patch request.  0 means unlimited.
