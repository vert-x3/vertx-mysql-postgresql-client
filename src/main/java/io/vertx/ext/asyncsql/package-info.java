/*
 * Copyright (c) 2011-2015 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *     The Eclipse Public License is available at
 *     http://www.eclipse.org/legal/epl-v10.html
 *
 *     The Apache License v2.0 is available at
 *     http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

/**
 * = Vert.x MySQL / PostgreSQL client
 *
 * The {@link io.vertx.ext.asyncsql.AsyncSQLClient MySQL / PostgreSQL Client} is responsible for providing an
 * interface for Vert.x applications that need to interact with a MySQL or PostgreSQL database.
 *
 * It uses Mauricio Linhares https://github.com/mauricio/postgresql-async[open source driver] to interact with the MySQL
 * or PostgreSQL databases in a non blocking way
 *
 * == Setting up the client
 *
 * You can create an instance of the MySQL client as follows:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example2}
 * ----
 *
 * Or the PostgreSQL client like this:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example3}
 * ----
 *
 * If you create an instance this way you should make sure you start it with {@link io.vertx.ext.asyncsql.AsyncSQLClient#start(io.vertx.core.Handler)}
 * before you use it.
 *
 * Starting the client is asynchronous -the client will not be ready to use until the handler passed into the start
 * method is called.
 *
 * You can hold on to the client for a long time (e.g. the life-time of your verticle), but once you have finished with
 * it, you should stop it using {@link io.vertx.ext.asyncsql.AsyncSQLClient#stop(io.vertx.core.Handler)}
 *
 * == Getting a connection
 *
 * Use {@link io.vertx.ext.asyncsql.AsyncSQLClient#getConnection(io.vertx.core.Handler)} to get a connection.
 *
 * This will return the connection in the handler when one is ready from the pool.
 *
 * * [source,java]
 * ----
 * {@link examples.Examples#example4}
 * ----
 *
 * Once you've finished with the connection make sure you close it afterwards.
 *
 * The connection is an instance of {@link io.vertx.ext.sql.SQLConnection} which is a common interface used by
 * more than Vert.x sql service.
 *
 * You can learn how to use it in the http://foobar[common sql interface] documentation.
 *
 * === Note about date and timestamps
 *
 * Whenever you get dates back from the database, this service will implicitly convert them into ISO 8601
 * (`yyyy-MM-ddTHH:mm:ss.SSS`) formatted strings. MySQL usually discards milliseconds, so you will regularly see `.000`.
 *
 * == Configuration
 *
 * Both the PostgreSql and MySql services take the same configuration:
 *
 * ----
 * {
 *   "address" : <event-bus-address-to-listen-on>,
 *   "host" : <your-host>,
 *   "port" : <your-port>,
 *   "maxPoolSize" : <maximum-number-of-open-connections>,
 *   "username" : <your-username>,
 *   "password" : <your-password>,
 *   "database" : <name-of-your-database>
 * }
 * ----
 *
 * `address`:: The address this service should register on the event bus. Defaults to `vertx.postgresql` or `vertx.mysql`.
 * `host`:: The host of the database. Defaults to `localhost`.
 * `port`:: The port of the database. Defaults to `5432` for PostgreSQL and `3306` for MySQL.
 * `maxPoolSize`:: The number of connections that may be kept open. Defaults to `10`.
 * `username`:: The username to connect to the database. Defaults to `postgres` for PostgreSQL and `root` for MySQL.
 * `password`:: The password to connect to the database. Default is not set, i.e. it uses no password.
 * `database`:: The name of the database you want to connect to. Defaults to `test`.
 */
@Document(fileName = "index.adoc")
@GenModule(name = "vertx-mysql-postgresql") package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.GenModule;
import io.vertx.docgen.Document;
