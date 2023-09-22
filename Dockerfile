FROM registry.cn-hangzhou.aliyuncs.com/shay/gradle:7.1-jdk11 AS builder
LABEL maintainer="spear-event<shoy160@qq.com>"
WORKDIR /spear-event
COPY . /spear-event
RUN gradle clean event-web:bootJar -x test --stacktrace

FROM registry.cn-hangzhou.aliyuncs.com/shay/openjdk:11-jre
WORKDIR /app
COPY --from=builder /spear-event/event-web/build/libs/event-web-*.jar /app/spear-event.jar
EXPOSE 8080
ENV SPEAR_MODE "test"
ENV JAVA_OPTS "-Xss559K -Xmx1066M -XX:MaxMetaspaceSize=164M -Xms1066M -XX:MetaspaceSize=164M -XX:MaxDirectMemorySize=204M"
ENV DEF_JAVA_OPTS "-Djava.security.egd=file:/dev/./urandom -Duser.timezone=Asia/Shanghai -Dfile.encoding=utf-8"
ENTRYPOINT exec java $JAVA_OPTS $DEF_JAVA_OPTS -jar /app/spear-event.jar --spring.profiles.active=${SPEAR_MODE}
