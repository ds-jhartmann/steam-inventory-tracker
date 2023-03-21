# Steam Inventory Tracker
[![CI](https://github.com/jrohrtmn/steam-inventory-tracker/actions/workflows/main.yml/badge.svg?branch=master)](https://github.com/jrohrtmn/steam-inventory-tracker/actions/workflows/main.yml)

## How to run
```
docker-compose up
mvn package
java -jar .\target\steam-inventory-tracker-0.0.1-SNAPSHOT.jar
```
```
helm dependency update /charts/steam-inventory-tracker/
helm install sit charts/steam-inventory-tracker/ 
```