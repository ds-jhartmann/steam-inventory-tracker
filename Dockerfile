FROM openjdk:18-jdk-alpine
COPY target/steam-inventory-tracker-0.0.1-SNAPSHOT.jar steam-inventory-tracker-1.0.0.jar
ENTRYPOINT ["java","-jar","/steam-inventory-tracker-1.0.0.jar"]