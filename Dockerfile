FROM openjdk:11-jdk-slim

ENV TZ UTC

RUN groupadd --gid 1000 -r app && \
    useradd --uid 1000 --no-log-init -g app app
WORKDIR /home/app
RUN chown -R app:app .
USER app

COPY app/target/service.jar .

ENTRYPOINT exec java \
            -jar \
            -Djava.rmi.server.hostname=127.0.0.1 \
            -Dcom.sun.management.jmxremote.ssl=false \
            -Dcom.sun.management.jmxremote.authenticate=false \
            -Dcom.sun.management.jmxremote.port=1099 \
            -Dcom.sun.management.jmxremote.rmi.port=1099 \
            -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/home/app \
            -XX:+UseStringDeduplication \
            -Xms128M -Xmx128M \
            ${JAVA_OPTS} \
            service.jar
