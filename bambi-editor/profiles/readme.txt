Production deployment profiles.

ws .......... Java Web Start for bambieditor demo
app ......... Production Desktop Release

Web Start artifacts should be packaged with the /bambi-webstart JAR, therefore 
there is no ws/ profile in the /bambi-editor project. For configuring webstart 
deployment, see /bambi-webstart project.

Desktop artifacts should be packaged outside of JAR (pointed to by startup 
script classpath config).