#!/usr/bin/env bash

export MYSQL_DATABASE=testdb
export MYSQL_USER=vertx
export MYSQL_ROOT_PASSWORD=password
export MYSQL_PASSWORD=password

docker run -d \
		-e MYSQL_USER \
		-e MYSQL_PASSWORD \
		-e MYSQL_ROOT_PASSWORD \
		-e MYSQL_DATABASE \
		--name "some-mysql" \
		-p 3306:3306 \
		"mysql/mysql-server:5.6"



