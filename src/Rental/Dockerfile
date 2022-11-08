FROM openjdk:17-jdk-alpine
MAINTAINER sltn011
EXPOSE 8060
COPY target/Rental-0.0.1-SNAPSHOT.jar rental.jar
ENTRYPOINT ["java","-jar","./rental.jar"]