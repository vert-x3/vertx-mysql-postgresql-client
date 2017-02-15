/*
 *  Copyright 2015 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/**
 * = Vert.x MySQL / PostgreSQL client
 *
 * The Async MySQL / PostgreSQL Client is responsible for providing an
 * interface for Vert.x applications that need to interact with a MySQL or PostgreSQL database.
 *
 * It uses Mauricio Linhares https://github.com/mauricio/postgresql-async[async driver] to interact with the MySQL
 * or PostgreSQL databases in a non blocking way.
 *
 * == Using the MySQL and PostgreSQL client
 *
 * This section describes how to configure your project to be able to use the MySQL / PostgreSQL client in your
 * application.
 *
 * === In a regular application
 *
 * To use this client, you need to add the following jar to your `CLASSPATH`:
 *
 * * ${maven.artifactId} ${maven.version} (the client)
 * * scala-library 2.11.4
 * * the postgress-async-2.11 and mysdql-async-2.11 from https://github.com/mauricio/postgresql-async
 * * joda time
 *
 * All these jars are downloadable from Maven Central.
 *
 * === In an application packaged in a fat jar
 *
 * If you are building a _Fat-jar_ using Maven or Gradle, just add the following dependencies:
 *
 * * Maven (in your `pom.xml`):
 *
 * [source,xml,subs="+attributes"]
 * ----
 * <dependency>
 *   <groupId>${maven.groupId}</groupId>
 *   <artifactId>${maven.artifactId}</artifactId>
 *   <version>${maven.version}</version>
 * </dependency>
 * ----
 *
 * * Gradle (in your `build.gradle` file):
 *
 * [source,groovy,subs="+attributes"]
 * ----
 * compile '${maven.groupId}:${maven.artifactId}:${maven.version}'
 * ----
 *
 * === In an application using a vert.x distributions
 *
 * If you are using a vert.x distribution, add the jar files listed above to the `$VERTX_HOME/lib` directory.
 *
 * Alternatively, you can edit the `vertx-stack.json` file located in `$VERTX_HOME`, and set `"included": true`
 * for the `vertx-mysql-postgresql-client` dependency. Once done, launch: `vertx resolve --dir=lib --stack=
 * ./vertx-stack.json`. It downloads the client and its dependencies.
 *
 * == Creating a client
 *
 * There are several ways to create a client. Let's go through them all.
 *
 * === Using default shared pool
 *
 * In most cases you will want to share a pool between different client instances.
 *
 * E.g. you scale your application by deploying multiple instances of your verticle and you want each verticle instance
 * to share the same pool so you don't end up with multiple pools
 *
 * You do this as follows:
 *
 * [source,java]
 * ----
 * {@link examples.Examples#exampleCreateDefault}
 * ----
 *
 * The first call to {@link io.vertx.ext.asyncsql.MySQLClient#createShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject)}
 * or {@link io.vertx.ext.asyncsql.PostgreSQLClient#createShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject)}
 * will actually create the data source, and the specified config will be used.
 *
 * Subsequent calls will return a new client instance that uses the same data source, so the configuration won't be used.
 *
 * === Specifying a pool name
 *
 * You can create a client specifying a pool name as follows
 *
 * [source,java]
 * ----
 * {@link examples.Examples#exampleCreatePoolName}
 * ----
 *
 * If different clients are created using the same Vert.x instance and specifying the same pool name, they will
 * share the same data source.
 *
 * The first call to {@link io.vertx.ext.asyncsql.MySQLClient#createShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject, String)}
 * or {@link io.vertx.ext.asyncsql.PostgreSQLClient#createShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject, String)}
 * will actually create the data source, and the specified config will be used.
 *
 * Subsequent calls will return a new client instance that uses the same pool, so the configuration won't be used.
 *
 * Use this way of creating if you wish different groups of clients to have different pools, e.g. they're
 * interacting with different databases.
 *
 * === Creating a client with a non shared data source
 *
 * In most cases you will want to share a pool between different client instances.
 * However, it's possible you want to create a client instance that doesn't share its pool with any other client.
 *
 * In that case you can use {@link io.vertx.ext.asyncsql.MySQLClient#createNonShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject)}
 * or {@link io.vertx.ext.asyncsql.PostgreSQLClient#createNonShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject)}
 *
 * [source,java]
 * ----
 * {@link examples.Examples#exampleCreateNonShared}
 * ----
 *
 * This is equivalent to calling {@link io.vertx.ext.asyncsql.MySQLClient#createShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject, String)}
 * or {@link io.vertx.ext.asyncsql.PostgreSQLClient#createShared(io.vertx.core.Vertx, io.vertx.core.json.JsonObject, String)}
 * with a unique pool name each time.
 *
 * == Closing the client
 *
 * You can hold on to the client for a long time (e.g. the life-time of your verticle), but once you have finished with
 * it, you should close it using {@link io.vertx.ext.asyncsql.AsyncSQLClient#close(io.vertx.core.Handler)} or
 * {@link io.vertx.ext.asyncsql.AsyncSQLClient#close()}
 *
 * == Getting a connection
 *
 * Use {@link io.vertx.ext.asyncsql.AsyncSQLClient#getConnection(io.vertx.core.Handler)} to get a connection.
 *
 * This will return the connection in the handler when one is ready from the pool.
 *
 * [source,java]
 * ----
 * {@link examples.Examples#example4}
 * ----
 *
 * Once you've finished with the connection make sure you close it afterwards.
 *
 * The connection is an instance of {@link io.vertx.ext.sql.SQLConnection} which is a common interface used by
 * other SQL clients.
 *
 * You can learn how to use it in the http://vertx.io/docs/vertx-sql-common/$lang/[common sql interface] documentation.
 *
 * === Note about date and timestamps
 *
 * Whenever you get dates back from the database, this service will implicitly convert them into ISO 8601
 * (`yyyy-MM-ddTHH:mm:ss.SSS`) formatted strings. MySQL usually discards milliseconds, so you will regularly see `.000`.
 *
 * === Note about last inserted ids
 *
 * When inserting new rows into a table, you might want to retrieve auto-incremented ids from the database. The JDBC API
 * usually lets you retrieve the last inserted id from a connection. If you use MySQL, it will work the way it does like
 * the JDBC API. In PostgreSQL you can add the
 * http://www.postgresql.org/docs/current/static/sql-insert.html["RETURNING" clause] to get the latest inserted ids. Use
 * one of the `query` methods to get access to the returned columns.
 *
 * === Note about stored procedures
 *
 * The `call` and `callWithParams` methods are not implemented currently.
 *
 * == Configuration
 *
 * Both the PostgreSql and MySql clients take the same configuration:
 *
 * ----
 * {
 *   "host" : <your-host>,
 *   "port" : <your-port>,
 *   "maxPoolSize" : <maximum-number-of-open-connections>,
 *   "username" : <your-username>,
 *   "password" : <your-password>,
 *   "database" : <name-of-your-database>,
 *   "charset" : <name-of-the-character-set>,
 *   "queryTimeout" : <timeout-in-milliseconds>
 * }
 * ----
 *
 * `host`:: The host of the database. Defaults to `localhost`.
 * `port`:: The port of the database. Defaults to `5432` for PostgreSQL and `3306` for MySQL.
 * `maxPoolSize`:: The number of connections that may be kept open. Defaults to `10`.
 * `username`:: The username to connect to the database. Defaults to `postgres` for PostgreSQL and `root` for MySQL.
 * `password`:: The password to connect to the database. Default is not set, i.e. it uses no password.
 * `database`:: The name of the database you want to connect to. Defaults to `testdb`.
 * `charset`:: The name of the character set you want to use for the connection. Defaults to `UTF-8`.
 * `queryTimeout`:: The timeout to wait for a query in milliseconds. Defaults to `10000` (= 10 seconds).
 */
@Document(fileName = "index.adoc")
@ModuleGen(name = "vertx-mysql-postgresql", groupPackage = "io.vertx") package io.vertx.ext.asyncsql;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
