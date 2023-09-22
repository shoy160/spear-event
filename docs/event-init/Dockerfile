FROM golang:alpine
LABEL maintainer="event-init<shoy160@qq.com>"
WORKDIR /event-init
COPY . /event-init

ENV GO111MODULE=on
ENV GOPROXY="https://goproxy.io"
ENV event_ARGS="-h"
ENV event_HOST="http://localhost:8080"
ENV event_TOKEN=""
ENV event_DB=""
ENTRYPOINT exec go run main.go -host="${event_HOST}" -token="${event_TOKEN}" -db="${event_DB}" ${event_ARGS}