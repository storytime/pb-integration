FROM openjdk:8-jre-alpine
RUN apk add tzdata
RUN ls /usr/share/zoneinfo
RUN cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime
RUN echo "Europe/Kiev" >  /etc/timezone
RUN date
COPY build/libs/pb-24-integration.jar /var/webapps/
EXPOSE 10080
WORKDIR /var/webapps/
CMD /usr/bin/java -version
CMD /usr/bin/java $JAVA_OPTIONS -server -XX:+PrintCommandLineFlags -jar pb-24-integration.jar