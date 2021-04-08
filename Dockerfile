FROM gradle:6.5-jdk8 AS BUILDER

ENV JVM_OPTS -Xmx2g -Xms2g -XX:MaxPermSize=1024m

# provide a tmp/cache dir
VOLUME /tmp

# all following commands will be executed in /app
WORKDIR /workdir
# copy the sources to image (except .dockerignore)
ADD . /workdir
RUN gradle -x test :platform-rest:bootJar :platform-rest:prepareDocker -x :platform-rest:asciidoctor


# Start a new docker stage here, and only copy the finished build artefacts.
FROM openjdk:8-jdk-alpine

# add the gradle dependencies and own artificats in a docker-friendly way
COPY --from=BUILDER /workdir/platform-rest/build/dependency/BOOT-INF/classes /app
COPY --from=BUILDER /workdir/platform-rest/build/dependency/BOOT-INF/lib     /app/lib
COPY --from=BUILDER /workdir/platform-rest/build/dependency/META-INF         /app/META-INF

# start app
ENTRYPOINT ["java","-cp","app:app/lib/*","org.ossiaustria.platform.RestApplicationKt"]

