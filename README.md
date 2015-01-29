# Vert.x MySQL PostgreSQL Service

This Vert.x module uses the https://github.com/mauricio/postgresql-async drivers to support a fully async module for 
MySQL and PostgreSQL.

## Requirements

* Vert.x 3
* A working PostgreSQL or MySQL server
* For testing PostgreSQL: A `testdb` database on a local PostgreSQL install and a user called `vertx`
* For testing MySQL: A `testdb` database on a local MySQL install and a user called `vertx` with password `password`

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

After starting or deploying the SQL service, you need to grab a connection from the pool. You can do that by using the
`service.getConnection()` method, which gives you a connection back asynchronously.

On the connection, you have a few commands you can use to interact with your MySQL or PostgreSQL database.

#### execute

To execute a simple database action without caring for the result, use `execute`. For example to create or drop tables.

#### update

To insert or update an entry in a database table, use one of the `update` methods. You'll receive the number of affected
rows.

#### updateWithParams

Use this method if you need to pass parameters to your update statement. Just add strings or numbers into a JsonArray.

#### query

To query the database and fetch results from it, use one of the `query` methods. You'll receive a `ResultSet` which
consists of a list of column names and a list of JsonArrays that contain the values.

#### queryWithParams

Use this method if you need to pass parameters to your query statement. Just add strings or numbers into a JsonArray.

#### commit

To commit a transaction, you can use this method. You can think of this method as an alias for `execute("COMMIT")`. As
transactions are not created implicitly by the underlying driver, you need to explicitly begin a new one by starting it
via `execute("BEGIN")`.

#### rollback

To roll back a transaction, you can use this method. You can think of this method as an alias for `execute("ROLLBACK")`.
As transactions are not created implicitly by the underlying driver, you need to explicitly begin a new one by starting
it via `execute("BEGIN")`.

#### close

When you're done dealing with the database, you need to close the connection. It will free it and put it back into the
pool of connections.
