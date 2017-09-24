# WALDAP - LDAP for WebApps [![Build Status](https://travis-ci.org/kounoike/ldap-and-web-test.svg?branch=master)](https://travis-ci.org/kounoike/waldap)

WALDAP - LDAP for WebApps aims to provide simply manageable LDAP solution.
It has LDAP server and WebConsole. It can start easily.

Current version(0.9.0) is pre-release version. It may involve non-compatible changes.

## Features

- Web console by Servlet.
    - It can be started by standalone or Servlet container such as Jetty, Tomcat and others.
- Builtin LDAP server
    - WALDAP provides LDAP server which starts on Servlet startup. No extra startup script required expect Web UI. 
- Manage Users and WebApps.
    - It can add/delete/edit Users, change password by Admin or oneself.
    - Administrator can add **WebApp**'s instance.

## Limitations

- WALDAP can't setup as flexible as OpenLDAP.
    - ex. photo/secretary/roomNumber/and many other field for User's attribute **CAN'T USE IN WALDAP**
- WALDAP can't provide authentication for Windows/Linux/Mac login.
    - PosixAccount requires uidNumber fields. It is complicated feature. I believe it can be discarded in WebApp authentication. 
- WALDAP can't provide SSO.
    - It is too complicated. I want just login to WebApp such as Jenkins, GitBucket etc. 

## Run

- sbt: `sbt jetty:start` and go to `http://localhost:10080/`.
- executable war: `java -jar waldap.war`. waldap.war can download from releases page.
- Servlet container: deploy waldap.war to webapps directory.

These instructions also starts LDAP server which listen on `127.0.0.1:10389`(default).

## first login

To login as administrator, use username:`admin` and password:`secret`.
Please change it after first login.

## typically instruction

### Add user

Press `+ Add` button in List of Users page, fill the form and push submit button.

### Edit user/Delete user/Change password

Press buttons in List of Users page.

### Add WebApps

Click **Add Instance** and fill url and "InstanceSuffix". InstanceSuffix is used for identify multiple instance of same WebApp.

## Data backup

I'm finding better backup techniques.
Currently if you want to backup data, simply backup `$HOME/.waldap` directory with stopping server. 

### LDAP data

save LDIFs to `$HOME/.waldap/server/partitions`.

### Database data

By default, WALDAP uses H2 Database engine.

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

## I18N

Currently, messages are translated to English and Japanese only. If you want to help translation, please create PR.
