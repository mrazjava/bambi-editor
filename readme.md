# Bambi Editor

BambiEditor is a user friendly desktop image manipulation tool capable of easy and fast filter transformations 
and no-nonse printing. BambiEditor is also a web authoring tool for developers to allow their end users to 
upload images scaled exactly to specific web requirements. Simply deploy Bambi as Java Web Start, replace the 
default `FilesystemImageExporter` plugin with one of web upload plugins and you're good to go! It offers cam 
support by integrating native libs from OpenIMAJ, so it still runs on all platforms. Bambi's roots date back 
to 2010 when I wrote a customized applet image uploader for a friend of mine. Since then I've added so many 
enhancements it grew to a stand alone app that you see today ...

BambiEditor utlizes in part JavaFX, therefore the mimimum Java required to run Bambi is Java SE 7 update 
6 - first Java to bundle FX.

For more information and to try BambiEditor's web uploading capabilities visit our web site (below). Have fun!

## Features
 - Image Editing
 - Image Printing
 - Web Cam Picture Taking
 - Photo Uploading (to any web destination)
 - Built-In Security (MD5, SHA-1, SHA-256, RSA)
 - Plugin Architecture

## Screenshots
<img src="/images/cam_in_action_windows.png" width="500" />
*Cam in action on Windows 8*

Cam in action on Ubuntu 13.04
![Cam in Action Ubuntu 13.04](/images/cam_in_action_ubuntu.png?raw=true&x=1)


## Project Origins
This is a port of a project from [sourceforge](https://sourceforge.net/projects/bambieditor/). Bambi Editor was 
created in 2013, then left unattended. As the author of the original, returning to it after several years, I 
have plans to fix project structure, the build, and add new features. Except for this readme, I pulled git repo 
from sourceforge in completely original state with broken build issues and everything else. The initial checkin 
is tagged as 0.9.2 release and any further development will follow.

## Building
First, build reusable artifacts:
```
cd bambi/
mvn clean install
```
Next build editor (webstart is deprecated and no longer supported):
```
cd bambi-editor/
mvn clean package -P app -Dclient= -Dsignalias=selfsigned -Dsignpass=password
```

## Release Notes

### 0.9.2.1 
*April 6, 2018*
Fixed build problems. Deprecated webstart (no longer supported).

### 0.9.2 
*May 9, 2014*
Last release pushed to SourceForge with compiled binary. Uploaded SourceForge binary works well but the source 
build is broken and needs fixes. For example, contains hard coded paths to system dependencies (eg: jfx) and 
refers to custom built OpenIMAJ (which back in the day was required as at that time only 1.0 was available with 
some critical bugs).
