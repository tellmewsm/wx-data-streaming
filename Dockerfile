FROM registry.fit2cloud.com/fit2cloud2/fabric8-java-alpine-openjdk8-jre

MAINTAINER FIT2CLOUD <support@fit2cloud.com>

ARG MS_VERSION=dev

RUN mkdir -p /opt/apps

ADD target/data-streaming-1.4.jar /opt/apps

ENV JAVA_APP_JAR=/opt/apps/data-streaming-1.4.jar

ENV AB_OFF=true

ENV MS_VERSION=${MS_VERSION}

ENV JAVA_OPTIONS=-Dfile.encoding=utf-8

CMD ["/deployments/run-java.sh"]
