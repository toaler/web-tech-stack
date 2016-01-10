#!/bin/bash

alias=wts
validity=99999
storepass=jettyjetty
keypass=storepwd
keystore=./src/main/resources/ssl/keystore

rm -rf ${keystore}

#  Using the keytool utility that ships as
#  $JAVA_HOME/bin/keytool with Sun's Java
#  distributions, create a new keystore and populate
#  it with a new key.
#  
#  It is important that you choose good passwords for
#  the keystore and for the key itself. These
#  passwords may be the same or different to each
#  other. Using different and strong passwords here
#  protects your server's private key in the event
#  the keystore file falls into the wrong hands. You
#  should take precautions to keep this from
#  happening, including setting filesystem user and
#  group permissions so that unauthorized individuals
#  with accounts on the OpenNMS server will not have
#  read (or write) access to the keystore.
#  
#  By default, keytool will create DSA keys, but
#  Jetty requires an RSA key. Make sure you are
#  passing the -keyalg RSA option to keytool.

keytool \
 -alias ${alias} \
 -genkeypair -keyalg RSA -keysize 2048 -validity ${validity} \
 -dname "CN=*, OU=ID, O=org.bpt, L=Danville, S=CA, C=US" \
 -keystore ${keystore} \
 -storepass ${storepass} \
 -keypass ${keypass} 

#  If you are content with a self-signed certificate,
#  you need to perform just one step to add a
#  signature to your new SSL certificate.
#  
#  As in the key generation process above, be sure
#  that you specify an appropriate number of days for
#  the validity parameter.

keytool -selfcert -validity ${validity} -keystore ${keystore} -storepass ${storepass} -keypass ${keypass} -alias ${alias} 
