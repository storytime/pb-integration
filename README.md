# README #

![Build status](https://sonarcloud.io/api/project_badges/measure?project=pb-integration&metric=alert_status)
[![](https://images.microbadger.com/badges/image/bobobo/pb-integration.svg)](https://microbadger.com/images/bobobo/pb-integration "Size")
[![](https://images.microbadger.com/badges/version/bobobo/pb-integration.svg)](https://microbadger.com/images/bobobo/pb-integration "Version")


Simple integration service that collects data by schedule form bank and push it to zenmoney

### Requirements 
* Java 1.8 or more
* Gradle 4 or more
* Docker 16+ and Docker-compose 1.21+ 

### How to
To build simply run `bash ./gradlew clean build`

Also application can be automatically delivered to server. To need you need to:
1. 
    - Create `gradle.properties` with keys:
  
            pb.ssh.host = SERVER_IP
        
            pb.ssh.user = root
    
    - or set `PB_SSH_HOST` and `PB_SSH_USER` env variables

2. Run: 
    `./gradlew clean build deployJar` or `./gradlew clean build deployJar -Ppb.ssh.user=$PB_SSH_USER -Ppb.ssh.host=$PB_SSH_HOST` 
 
##### DB
Username and password can be found in *.property file

##### How to insert custom comment
`INSERT INTO MERCHANT_INFO_ADDITIONAL_COMMENT ( MERCHANT_INFO_ID , ADDITIONAL_COMMENT ) VALUES ( 1, 'NBU_PREV_MOUTH_LAST_BUSINESS_DAY')` 
 
##### Links
1. [How configure ssh auto deploy via pipeline](https://community.atlassian.com/t5/Bitbucket-questions/How-do-I-set-up-ssh-public-key-authentication-so-that-I-can-use/qaq-p/171671) 