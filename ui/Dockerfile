FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /opt/app
COPY . .
RUN apt update \
    && apt install -y gradle \
    && ./gradlew build
EXPOSE 7070
ENV TEMPORAL_ADDRESS=temporal:7233
ENV TEMPORAL_NAMESPACE=default
ENV TEMPORAL_CERT_PATH=""
ENV TEMPORAL_KEY_PATH=""
ENTRYPOINT [ "./startlocalwebui.sh" ]
# Encrypt the payload
CMD [ "false" ]
