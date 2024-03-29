FROM openjdk:8-jre-alpine

WORKDIR /home

ENV componentName "Administration"
ENV componentVersion 3.1.7-SNAPSHOT

# RUN apk --no-cache add \
# 	git \
# 	unzip \
# 	wget \
# 	bash \
#     && echo "Downloading $componentName $componentVersion" \
# 	&& wget "https://jitpack.io/com/github/symbiote-h2020/$componentName/$componentVersion/$componentName-$componentVersion-run.jar"
	COPY ./Administration-3.1.7-SNAPSHOT-run.jar Administration-3.1.7-SNAPSHOT-run.jar

EXPOSE 8250

# CMD java -DSPRING_BOOT_WAIT_FOR_SERVICES=symbiote-coreinterface:8100 -Xmx1024m -Duser.home=/home -Dspring.output.ansi.enabled=NEVER -jar $(ls *.jar)
CMD java $JAVA_HTTP_PROXY $JAVA_HTTPS_PROXY $JAVA_NON_PROXY_HOSTS -Xmx1024m -Duser.home=/home -Dspring.output.ansi.enabled=NEVER -jar $(ls *.jar)