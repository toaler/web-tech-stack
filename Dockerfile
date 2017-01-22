FROM treasureboat/java

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

ENTRYPOINT /app/run.sh
