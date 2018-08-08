FROM openjdk:8-jre-alpine
COPY build/libs/pb-24-integration.jar /var/webapps/
EXPOSE 10080
WORKDIR /var/webapps/
CMD /usr/bin/java -version
CMD /usr/bin/java $JAVA_OPTIONS -server -XX:+PrintCommandLineFlags -jar pb-24-integration.jar