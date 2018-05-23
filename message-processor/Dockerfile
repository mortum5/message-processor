FROM openjdk:8

WORKDIR /app/

COPY target/message-processor-1.0-SNAPSHOT.jar /app/target/
COPY etc/config.properties /app/etc/

CMD java -jar target/message-processor-1.0-SNAPSHOT.jar etc/config.properties
