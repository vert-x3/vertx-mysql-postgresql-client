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

/** @module vertx-mysql-postgresql-js/mysql_connection */
var utils = require('vertx-js/util/utils');
var DatabaseCommands = require('vertx-mysql-postgresql-js/database_commands');
var ConnectionCommands = require('vertx-mysql-postgresql-js/connection_commands');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMysqlConnection = io.vertx.ext.asyncsql.mysql.MysqlConnection;
var SelectOptions = io.vertx.ext.asyncsql.SelectOptions;

/**

 @class
*/
var MysqlConnection = function(j_val) {

  var j_mysqlConnection = j_val;
  var that = this;
  ConnectionCommands.call(this, j_val);
  DatabaseCommands.call(this, j_val);

  /**
   Sends a raw command to the database.

   @public
   @param command {string} 
   @param resultHandler {function} 
   */
  this.raw = function(command, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_mysqlConnection.raw(command, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Inserts new values into a table. Use this action to insert new rows into a table. You need to specify a table, the
   fields to insert and an array of rows to insert. The rows itself are an array of values.

   @public
   @param table {string} 
   @param fields {Array.<string>} 
   @param values {Array.<todo>} 
   @param resultHandler {function} 
   */
  this.insert = function(table, fields, values, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1] instanceof Array && typeof __args[2] === 'object' && __args[2] instanceof Array && typeof __args[3] === 'function') {
      j_mysqlConnection.insert(table, fields, utils.convParamListJsonArray(values), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Reads data from a table. The select action creates a SELECT statement to get a projection from a table. You can
   filter the columns by providing a fields array. If you omit the fields array, it selects every column available in
   the table.

   @public
   @param table {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.select = function(table, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && typeof __args[2] === 'function') {
      j_mysqlConnection.select(table, options != null ? new SelectOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Creates a prepared statement and lets you fill the ? with values.

   @public
   @param statement {string} 
   @param values {todo} 
   @param resultHandler {function} 
   */
  this.prepared = function(statement, values, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1] instanceof Array && typeof __args[2] === 'function') {
      j_mysqlConnection.prepared(statement, utils.convParamJsonArray(values), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Starts a transaction on this connection.

   @public
   @param handler {function} 
   */
  this.startTransaction = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlConnection.startTransaction(function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Commits a transaction.

   @public
   @param resultHandler {function} 
   */
  this.commit = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlConnection.commit(function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Rolls back a transaction.

   @public
   @param resultHandler {function} 
   */
  this.rollback = function(resultHandler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlConnection.rollback(function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Frees the connection and puts it back into the pool.

   @public
   @param handler {function} 
   */
  this.close = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlConnection.close(function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_mysqlConnection;
};

// We export the Constructor function
module.exports = MysqlConnection;