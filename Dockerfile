FROM adoptopenjdk/openjdk11

RUN mkdir -p /app/target
RUN cd /app

RUN ls -lrt /app/
COPY run.sh /app/
RUN ls -lrt /app/run.sh
COPY target/ /app/target/
RUN ls -lrt /app/
RUN ls -lrt /app/target/*jar
RUN ls -lrt /app/target/libs

WORKDIR /app

EXPOSE 8080 

CMD ["java", "--patch-module", "java.base=target/libs/alpn-boot-8.1.6.v20151105.jar", "--add-exports", "java.base/org.eclipse.jetty.alpn=ALL-UNNAMED", "-jar", "target/wts-1-SNAPSHOT.jar"]
