FROM amazoncorretto:17-alpine3.13
EXPOSE 10080
ENV aUser app
ENV id 1525
    RUN apk update &&\
    apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Europe/Kiev /etc/localtime && \
    echo "Europe/Kiev" > /etc/timezone && \
    addgroup -g ${id} ${aUser} && adduser -u ${id} -G ${aUser} -h /home/${aUser} -D ${aUser} && \
    mkdir -p /home/${aUser}/logs && \
    chown -R ${aUser}:${aUser} /home/${aUser}

WORKDIR /home/${aUser}
USER ${aUser}:${aUser}
COPY build/libs/sync-app.jar /home/${aUser}
CMD java -Dserver.port=8080 $JAVA_OPTIONS -jar sync-app.jar