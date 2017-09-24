WALDAP - LDAP for WebApps [![Build Status](https://travis-ci.org/kounoike/ldap-and-web-test.svg?branch=master)](https://travis-ci.org/kounoike/waldap)
=================

Under development now.

## Run

- sbt: `sbt jetty:start` and go to `http://localhost:10080/`.
- executable war: `java -jar waldap.war`. waldap.war can download from releases page.
- Servlet container: deploy waldap.war to webapps directory.

These instructions also starts LDAP server which listen on `127.0.0.1:10389`(default).

## first login

To login as administrator, use username:`admin` and password:`secret`.
Please change it after first login.

### Data directory

save LDIFs to `$HOME/.waldap/server/partitions`

## Components

* Scala 2.12.x
* sbt-0.13.x
* scalatra 2.5.1
* twirl 1.3.0
* scalatra-forms 1.1.0
* Apache DS 1.0.0M24

## Development

- `sbt executable` for building executable single war file.
- JRebel supported.
