FROM openjdk:17-jdk-alpine
MAINTAINER sltn011
EXPOSE 8050
COPY target/Payment-0.0.1-SNAPSHOT.jar payment.jar
ENTRYPOINT ["java","-jar","./payment.jar"]