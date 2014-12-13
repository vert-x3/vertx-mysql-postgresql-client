# Vert.x MySQL PostgreSQL Service

This Vert.x module uses the https://github.com/mauricio/postgresql-async drivers to support a fully async module for 
MySQL and PostgreSQL.

## Requirements

* Vert.x 3
* A working PostgreSQL or MySQL server
* For testing PostgreSQL: A `testdb` database on a local PostgreSQL install and a user called `vertx`
* For testing MySQL: A `testdb` database on a local MySQL install and a user called `root`

## Installation

???

## Usage

To use this service, deploy it like other services, using `io.vertx:mysql-postgresql-service` as the verticle id:

    vertx.deployVerticle("service:io.vertx:mysql-postgresql-service", deployOptions, resultHandler);

Keep in mind that the service is only ready as soon as the resultHandler got called. If you try to use it before it 
was called, you might get errors. 

### Configuration

    {
      "postgresql" : {
        "address" : <event-bus-address-to-listen-on>,
        "host" : <your-host>,
        "port" : <your-port>,
        "maxPoolSize" : <maximum-number-of-open-connections>,
        "username" : <your-username>,
        "password" : <your-password>,
        "database" : <name-of-your-database>
      }
    }

The configuration you put into the deployOptions object should have a `postgresql` or `mysql` object in it. Both have 
the same configuration options:

* `address` - The address this module should register on the event bus. Defaults to `vertx.postgresql` or `vertx.mysql` 
respectively. If you provide both PostgreSQL and MySQL, use different addresses for them - otherwise you'll end up 
with strange errors.
* `host` - The host of the database. Defaults to `localhost`.
* `port` - The port of the database. Defaults to `5432` for PostgreSQL and `3306` for MySQL.
* `maxPoolSize` - The number of connections that may be kept open. Defaults to `10`.
* `username` - The username to connect to the database. Defaults to `postgres` for PostgreSQL and `root` for MySQL.
* `password` - The password to connect to the database. Default is not set, i.e. it uses no password.
* `database` - The name of the database you want to connect to. Defaults to `test`.

### Commands

There are only a few commands available currently, but in theory you should be able to invoke any command on the 
database using the `raw` action.

The database will always reply to your requests in the `replyHandler` provided with every message.

The module will expose a service proxy that you can use with your favorite language. Just create it with the same 
address as you provided in the configuration, for example in Java for PostgreSQL:

    PostgresqlService service = PostgresqlService.createEventBusProxy(vertx, address)

On the service, you have various methods that you can use to talk with the database.

#### Connections

To get a connection and use it, you need to use the `take()` method on the `MysqlService` or `PostgresqlService` 
instance.

#### Transactions

If you don't need the flexibility of a connection and just to make a single transaction, you can use the `begin()`
method. When you're done and `commit` or `rollback`, the transaction will be closed and the underlying connection will 
be made available for other purposes again.

#### Direct access via service

If you don't need any fancy transactions or ordering for your query, you can use the commands provided by the
service directly. Just call the commands as you would on a connection or transaction.
