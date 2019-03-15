FROM openjdk:11-jre-slim
RUN cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime
RUN echo "Europe/Kiev" >  /etc/timezone
EXPOSE 10080
RUN groupadd -r appuser &&  useradd -r -g app -d /home/app app
WORKDIR /home/appuser/
COPY build/libs/pb-integration.jar /home/appuser/
RUN chown -R app:app /home/appuser/
CMD /usr/bin/java -version
CMD mkdir /home/appuser/logs
CMD /usr/bin/java  -Dserver.port=8080 $JAVA_OPTIONS -jar pb-integration.jar
USER app