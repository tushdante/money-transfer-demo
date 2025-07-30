FROM golang:1.23-alpine AS builder

WORKDIR /app

COPY ../go/go.mod ../go/go.sum ./
RUN go mod download

COPY ../go/ .
RUN CGO_ENABLED=0 GOOS=linux go build -o worker ./worker

FROM alpine:latest

RUN apk --no-cache add ca-certificates
WORKDIR /root/

COPY --from=builder /app/worker .

CMD ["./worker"]
