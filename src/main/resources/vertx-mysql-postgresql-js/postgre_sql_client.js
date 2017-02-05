/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module vertx-mysql-postgresql-js/postgre_sql_client */
var utils = require('vertx-js/util/utils');
var AsyncSQLClient = require('vertx-mysql-postgresql-js/async_sql_client');
var SQLConnection = require('vertx-sql-js/sql_connection');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JPostgreSQLClient = Java.type('io.vertx.ext.asyncsql.PostgreSQLClient');

/**
 Represents an PostgreSQL client

 @class
*/
var PostgreSQLClient = function(j_val) {

  var j_postgreSQLClient = j_val;
  var that = this;
  AsyncSQLClient.call(this, j_val);

  /**
   Close the client and release all resources.
   Call the handler when close is complete.

   @public
   @param whenDone {function} handler that will be called when close is complete 
   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_postgreSQLClient["close()"]();
    }  else if (__args.length === 1 && typeof __args[0] === 'function') {
      j_postgreSQLClient["close(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        __args[0](null, null);
      } else {
        __args[0](null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
   connection when you are done, so it is returned to the pool.

   @public
   @param handler {function} the handler which is called when the <code>JdbcConnection</code> object is ready for use. 
   */
  this.getConnection = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_postgreSQLClient["getConnection(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(utils.convReturnVertxGen(SQLConnection, ar.result()), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_postgreSQLClient;
};

PostgreSQLClient._jclass = utils.getJavaClass("io.vertx.ext.asyncsql.PostgreSQLClient");
PostgreSQLClient._jtype = {
  accept: function(obj) {
    return PostgreSQLClient._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(PostgreSQLClient.prototype, {});
    PostgreSQLClient.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
PostgreSQLClient._create = function(jdel) {
  var obj = Object.create(PostgreSQLClient.prototype, {});
  PostgreSQLClient.apply(obj, arguments);
  return obj;
}
/**
 Create a PostgreSQL client which maintains its own pool.

 @memberof module:vertx-mysql-postgresql-js/postgre_sql_client
 @param vertx {Vertx} the Vert.x instance 
 @param config {Object} the configuration 
 @return {AsyncSQLClient} the client
 */
PostgreSQLClient.createNonShared = function(vertx, config) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(AsyncSQLClient, JPostgreSQLClient["createNonShared(io.vertx.core.Vertx,io.vertx.core.json.JsonObject)"](vertx._jdel, utils.convParamJsonObject(config)));
  } else throw new TypeError('function invoked with invalid arguments');
};

/**
 Create a PostgreSQL client which shares its pool with any other MySQL clients created with the same pool name.

 @memberof module:vertx-mysql-postgresql-js/postgre_sql_client
 @param vertx {Vertx} the Vert.x instance 
 @param config {Object} the configuration 
 @param poolName {string} the pool name 
 @return {AsyncSQLClient} the client
 */
PostgreSQLClient.createShared = function() {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null)) {
    return utils.convReturnVertxGen(AsyncSQLClient, JPostgreSQLClient["createShared(io.vertx.core.Vertx,io.vertx.core.json.JsonObject)"](__args[0]._jdel, utils.convParamJsonObject(__args[1])));
  }else if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'string') {
    return utils.convReturnVertxGen(AsyncSQLClient, JPostgreSQLClient["createShared(io.vertx.core.Vertx,io.vertx.core.json.JsonObject,java.lang.String)"](__args[0]._jdel, utils.convParamJsonObject(__args[1]), __args[2]));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = PostgreSQLClient;