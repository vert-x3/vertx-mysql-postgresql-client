#!/usr/bin/env bash

export POSTGRES_DB=testdb
export POSTGRES_USER=vertx
export POSTGRES_PASSWORD=password

docker run -d \
		-e POSTGRES_USER \
		-e POSTGRES_PASSWORD \
		-e POSTGRES_DB \
		--name "some-postgres-ssl" \
    -v $(pwd)/src/test/resources/server.crt:/srv/server.crt \
    -v $(pwd)/src/test/resources/server.key:/srv/server.key \
    -v $(pwd)/src/test/resources/ssl-docker.sh:/docker-entrypoint-initdb.d/init.sh \
		-p 5432:54321 \
		"postgres:9.4.4"

