FROM amazoncorretto:17-alpine
EXPOSE 10080
ENV aUser app
ENV id 1525
RUN echo "http://dl-cdn.alpinelinux.org/alpine/v3.15/main" > /etc/apk/repositories &&\
    apk update &&\
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