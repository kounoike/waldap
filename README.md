LDAP And A / Ldap-Panda - easy LDAP for WebApps. [![Build Status](https://travis-ci.org/kounoike/ldap-and-web-test.svg?branch=master)](https://travis-ci.org/kounoike/ldap-and-web-test)
=================

This repository is experimental version!

## Run

`sbt jetty:start` and go to `http://localhost:10080/`. It also starts LDAP server which listen on port 10389.

### Data directory

save LDIFs to `$HOME/.ldapanda`


## Components

* Scala 2.12.1
* scalatra 2.5.0
* twirl 1.3.0
* scalatra-forms 1.1.0
* Apache DS 1.0.0M24
