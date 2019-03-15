FROM openjdk:11-jre-slim
RUN cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime
RUN echo "Europe/Kiev" >  /etc/timezone
RUN groupadd -g 1500 appuser &&  useradd -m -d /home/appuser -r -u 1500 -g appuser appuser
RUN sudo appuser
COPY build/libs/pb-integration.jar /home/appuser/
EXPOSE 10080
WORKDIR /home/appuser
CMD /usr/bin/java -version
CMD mkdir /home/appuser/logs
CMD /usr/bin/java  -Dserver.port=8080 $JAVA_OPTIONS -jar pb-integration.jar