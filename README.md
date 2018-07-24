# README #

[![](https://circleci.com/bb/boevry/pb-integration.svg?style=svg)](https://circleci.com/bb/boevry/pb-integration "Build and status")
[![](https://sonarcloud.io/api/project_badges/measure?project=pb-integration&metric=alert_status)](https://sonarcloud.io/dashboard?id=pb-integration "DEV Quality status")
[![](https://images.microbadger.com/badges/version/openjdk.svg)](https://microbadger.com/images/openjdk "MASTER JDK version")
[![](https://images.microbadger.com/badges/image/bobobo/pb-integration.svg)](https://microbadger.com/images/bobobo/pb-integration "MASTER Full container size")
[![](https://images.microbadger.com/badges/version/bobobo/pb-integration.svg)](https://microbadger.com/images/bobobo/pb-integration "MASTER The latest container build version")

## Overview
Simple integration service that collects data by schedule form bank and pushes it to [ZenMoney](https://zenmoney.ru/) with some customizations like currency rate.

Supported banks:

- [Private bank (not for business)](https://www.privat24.ua/)

## Requirements 
- Java 1.8 or more
- Gradle 4 or more
- Docker 16+ and Docker-compose 1.21+ 

## How to

To build (gradle push docker-compose with all params to server): 

- Copy `gradle.properties.template` to `gradle.properties` where  `PB_SSH_HOST` and `PB_SSH_USER` - credentials for ssh login
- Run: `./gradlew clean build` to build application
- Copy `variables.env.template` to `variables.env` - settings for app in docker container; `./gradlew upladComposeFileToServer` - to push docker files to server

## Docker
On prod app is running in docker container check `docker-compose.yml`,  **watchtower** container is used for auto-updates for the main container.

## Database
**Docker:** DB configs can be found in `variables.env` file

**Without docker:** Some properties are taken from env variable(if exists), all other located in: `application.properties` 

### How to insert custom comment generator 
`INSERT INTO MERCHANT_INFO_ADDITIONAL_COMMENT ( MERCHANT_INFO_ID , ADDITIONAL_COMMENT ) VALUES ( 1, 'NBU_PREV_MOUTH_LAST_BUSINESS_DAY')` 

## Links
1. [How configure ssh auto deploy via pipeline](https://community.atlassian.com/t5/Bitbucket-questions/How-do-I-set-up-ssh-public-key-authentication-so-that-I-can-use/qaq-p/171671) 
2. [Top-like interface for container metrics](https://github.com/bcicen/ctop) 