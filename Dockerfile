FROM amazoncorretto:16.0.1-alpine
EXPOSE 10080
ENV aUser app
ENV id 1525
RUN apk update && \
    apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime && \
    echo "Europe/Kiev" > /etc/timezone && \
    addgroup -g ${id} ${aUser} && adduser -u ${id} -G ${aUser} -h /home/${aUser} -D ${aUser}

WORKDIR /home/${aUser}
COPY build/libs/pb-integration.jar /home/${aUser}
RUN chown -R ${aUser}:${aUser} /home/${aUser}
USER ${aUser}:${aUser}
CMD mkdir -p /home/${aUser}/logs && java -Dserver.port=8080 $JAVA_OPTIONS -jar pb-integration.jar