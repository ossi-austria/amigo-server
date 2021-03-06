FROM gradle:7-jdk11 AS BUILDER

ENV JVM_OPTS -Xmx2g -Xms1g -XX:MaxPermSize=512m

# provide a tmp/cache dir
VOLUME /tmp

# all following commands will be executed in /app
WORKDIR /workdir
# copy the sources to image (except .dockerignore)
ADD . /workdir
RUN gradle -x test :platform-domain:jar :platform-rest:bootJar -x :platform-rest:asciidoctor

# Start a new docker stage here, and only copy the finished build artefacts.
FROM openjdk:11-jre-slim-stretch
RUN addgroup --system amigo && adduser --system amigo --ingroup amigo
USER amigo:amigo
COPY --from=BUILDER /workdir/platform-rest/build/libs/platform-rest-*.jar /app.jar

# start app
ENTRYPOINT ["java","-jar","/app.jar"]
