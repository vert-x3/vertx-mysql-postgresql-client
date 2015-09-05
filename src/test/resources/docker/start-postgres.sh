#!/usr/bin/env bash

export POSTGRES_DB=testdb
export POSTGRES_USER=vertx
export POSTGRES_PASSWORD=password

docker run -d \
		-e POSTGRES_USER \
		-e POSTGRES_PASSWORD \
		-e POSTGRES_DB \
		--name "some-postgres" \
		-p 5432:5432 \
		"postgres:9.4.4"



