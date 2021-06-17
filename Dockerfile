FROM amazoncorretto:16.0.1
RUN cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime
RUN echo "Europe/Kiev" > /etc/timezone
EXPOSE 10080
ENV aUser app
ENV id 1525
RUN groupadd -g ${id} -r ${aUser} && useradd -u ${id} -r -g ${aUser} -d /home/${aUser} ${aUser}
WORKDIR /home/${aUser}
COPY build/libs/pb-integration.jar /home/${aUser}
CMD mkdir -p /home/${aUser}/logs
RUN chown -R ${aUser}:${aUser} /home/${aUser}
USER ${aUser}:${aUser}
CMD java -version
CMD java -Dserver.port=8080 $JAVA_OPTIONS -jar pb-integration.jar