# Steam Inventory Tracker

[![Build, Test and Push](https://github.com/ds-jhartmann/steam-inventory-tracker/actions/workflows/main.yml/badge.svg)](https://github.com/ds-jhartmann/steam-inventory-tracker/actions/workflows/main.yml)
[![CodeQL Advanced](https://github.com/ds-jhartmann/steam-inventory-tracker/actions/workflows/codeql.yaml/badge.svg)](https://github.com/ds-jhartmann/steam-inventory-tracker/actions/workflows/codeql.yaml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ds-jhartmann_steam-inventory-tracker&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ds-jhartmann_steam-inventory-tracker)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ds-jhartmann_steam-inventory-tracker&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ds-jhartmann_steam-inventory-tracker)

## How to run

### Docker compose

```
docker-compose up
```

### Helm

```
helm repo add sit https://ds-jhartmann.github.io/steam-inventory-tracker
helm install sit sit/steam-inventory-tracker
```
