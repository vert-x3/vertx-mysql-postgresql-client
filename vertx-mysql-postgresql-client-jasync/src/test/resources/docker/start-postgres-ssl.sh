#!/usr/bin/env bash

export POSTGRES_DB=testdb
export POSTGRES_USER=vertx
export POSTGRES_PASSWORD=password

docker run -d \
		-e POSTGRES_USER \
		-e POSTGRES_PASSWORD \
		-e POSTGRES_DB \
		--name "some-postgres-ssl" \
    -v $(pwd)/src/test/resources/ssl-docker/server.crt:/docker-entrypoint-initdb.d/server.crt \
    -v $(pwd)/src/test/resources/ssl-docker/server.key:/docker-entrypoint-initdb.d/server.key \
    -v $(pwd)/src/test/resources/ssl-docker/init.sh:/docker-entrypoint-initdb.d/init.sh \
		-p 54321:5432 \
		"postgres:9.4.4"

