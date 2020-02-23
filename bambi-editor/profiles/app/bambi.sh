#!/bin/bash
java -Xms128M -Xmx1024M -cp ".:../../target/lib/*.jar" -Dlog4j.configuration=file:./log4j.xml -jar ../../target/bambi-editor-*.jar ./bambi.properties
