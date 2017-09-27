#!/bin/bash

echo working dir = `pwd`
java -Xbootclasspath/p:target/libs/alpn-boot-8.1.6.v20151105.jar -jar target/wts-1-SNAPSHOT.jar 
