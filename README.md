# web-tech-stack

##Requires
* maven 3.0
* JDK 1.8.0_65 64-bit

##Building

```
export JAVA_HOME=<path to jdk1.8.0_65>
export PATH=${JAVA_HOME}/bin:$PATH
```

To build the project run the following commands

```
./create_keystore.sh
mvn package
```
