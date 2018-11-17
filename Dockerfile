FROM openjdk:11-jre-slim
RUN ls /usr/share/zoneinfo
RUN cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime
RUN echo "Europe/Kiev" >  /etc/timezone
RUN date
COPY build/libs/pb-integration.jar /var/webapps/
EXPOSE 8080
WORKDIR /var/webapps/
CMD /usr/bin/java -version
CMD /usr/bin/java $JAVA_OPTIONS -jar pb-integration.jar