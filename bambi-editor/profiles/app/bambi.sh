#!/bin/bash
java -Xms128M -Xmx1024M -Dlog4j.configuration=file:./log4j.xml -jar bambi-editor-{version}.jar ./bambi.properties
