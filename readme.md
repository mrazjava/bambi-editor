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

## Initial Checkin
This is a port of a project from [sourceforge](https://sourceforge.net/projects/bambieditor/). Bambi Editor was 
created in 2013, then left unattended. As the author of the original, returning to it after several years, I 
have plans to fix project structure, the build, and add new features. Except for this readme, I pulled git repo 
from sourceforge in completely original state with broken build issues and everything else. The initial checkin 
is tagged as 0.9.2 release and any further development will follow.
