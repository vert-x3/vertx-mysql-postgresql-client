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

There are only a few commands available currently, but in theory you should be able to invoke any command on the 
database using the `raw` action.

The database will always reply to your requests in the `replyHandler` provided with every message.

The module will expose a service proxy that you can use with your favorite language. Just create it with the same 
address as you provided in the configuration, for example in Java for PostgreSQL:

    PostgresqlService service = PostgresqlService.createEventBusProxy(vertx, address)

You will always get a `JsonObject` back. Depending on the sent SQL query, you will get you will get a result and fields
back.

    {
      "rows" : 3,
      "fields" : ["id", "name"]
      "results" : [
        [1, "Albert"],
        [2, "Bertram"],
        [3, "Cornelius"]
      ]
    }

#### raw

The `raw(String sql, Handler<AsyncResult<JsonObject>> result)` command can be used to send an arbitrary command to the 
database. If you did a query that returns a result set, you will get the fields `results` and `fields` inside the 
returned `JsonObject`.

The results are shown in a `JsonArray` containing `JsonArray`s. The outer array are the rows that were selected and the 
inner array contains the values of each column. To find out which column is in which index, you can have a look into the
`fields` `JsonArray`. It gives you the names of the columns that were selected in the query you've sent. It is in the 
same order as the results provided in the `results` field.

#### prepared

With the `prepared(String sql, JsonArray values, Handler<AsyncResult<JsonObject>> result)` method, you can create 
prepared statements and run them directly. Put `?` into the sql String and use the `values` array to push values into 
it.

#### insert

To insert a new row into a table, you can use the method 
`insert(String table, JsonArray columns, JsonArray rows, Handler<AsyncResult<JsonObject>> result)`. The columns need 
to be in the same order as you put the values into the `rows` array. The `rows` are an array of arrays, so you can 
insert multiple rows with a single insert statement.

#### select

For basic select statements you can use the convenient method 
`select(String table, SelectOptions options, Handler<AsyncResult<JsonObject>> result)`. For the specified table, it will
collect the list of rows with the specified columns in the given `options`.

`fields` is a list containing the columns that you want to select. If you don't provide any, it will select all 
available fields / columns in the table.

`limit` is used when you want to select only a maximum amount of rows.

`offset` can be used to select only from a specific row. If you want to paginate your results, you need to use `limit`
and `offset`.

### Ways to access the commands

On the service, you have various ways that you can use to talk with the database. Each of these will let you call the 
above commands.

#### Connections

To get a connection and use it, you need to use the `take()` method on the `MysqlService` or `PostgresqlService` 
instance. To free the connection, call the `close()` method.

A connection provides three additional methods, to do transactions on it.

##### startTransaction

If you need to use transactions while on a specific connection, you can use the `startTransaction()` method to start 
one.

##### commit

To commit a previously started transaction, use the `commit()` method on the connection.

##### rollback

To roll back a previously started transaction, use the `rollback()` method on the connection.

#### Transactions

If you don't need the flexibility of a connection and just want to make a single transaction, you can use the `begin()`
method to start one.

A transaction provides the two methods `commit()` and `rollback()` to close the transaction again and free the 
underlying connection. If you commit, the transaction will commit everything and the database will make the changes 
according to your inputs on the transactions. If you roll back, the changes the transaction made will be rolled back and
all values in the database will be the same as they were before starting the transaction.

#### Direct access via service

If you don't need any ordering promises for your query or transactions, you can use the commands provided by the
service directly. Just call the commands as you would on a connection or transaction.
