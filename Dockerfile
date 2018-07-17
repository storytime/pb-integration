FROM openjdk:8-jre-alpine
COPY build/libs/pb-24-integration.jar /var/webapps/
EXPOSE 10080
WORKDIR /var/webapps/
CMD /usr/bin/java $JAVA_OPTIONS -jar pb-24-integration.jar