#!/bin/bash

echo working dir = `pwd`
java --patch-module java.base=target/libs/alpn-boot-8.1.6.v20151105.jar --add-exports java.base/org.eclipse.jetty.alpn=ALL-UNNAMED -jar target/wts-1-SNAPSHOT.jar 
