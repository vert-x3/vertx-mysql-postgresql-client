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

/** @module vertx-mysql-postgresql-js/mysql_service */
var utils = require('vertx-js/util/utils');
var DatabaseCommands = require('vertx-mysql-postgresql-js/database_commands');
var MysqlConnection = require('vertx-mysql-postgresql-js/mysql_connection');
var BaseSqlService = require('vertx-mysql-postgresql-js/base_sql_service');
var MysqlTransaction = require('vertx-mysql-postgresql-js/mysql_transaction');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMysqlService = io.vertx.ext.asyncsql.mysql.MysqlService;
var SelectOptions = io.vertx.ext.asyncsql.SelectOptions;

/**

 @class
*/
var MysqlService = function(j_val) {

  var j_mysqlService = j_val;
  var that = this;
  BaseSqlService.call(this, j_val);
  DatabaseCommands.call(this, j_val);

  /**

   @public
   @param whenDone {function} 
   */
  this.start = function(whenDone) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlService.start(function(ar) {
      if (ar.succeeded()) {
        whenDone(null, null);
      } else {
        whenDone(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**

   @public
   @param whenDone {function} 
   */
  this.stop = function(whenDone) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlService.stop(function(ar) {
      if (ar.succeeded()) {
        whenDone(null, null);
      } else {
        whenDone(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Sends a raw command to the database.

   @public
   @param command {string} 
   @param resultHandler {function} 
   */
  this.raw = function(command, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_mysqlService.raw(command, function(ar) {
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
      j_mysqlService.insert(table, fields, utils.convParamListJsonArray(values), function(ar) {
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
      j_mysqlService.select(table, options != null ? new SelectOptions(new JsonObject(JSON.stringify(options))) : null, function(ar) {
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
      j_mysqlService.prepared(statement, utils.convParamJsonArray(values), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Begins a transaction.

   @public
   @param transaction {function} 
   */
  this.begin = function(transaction) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlService.begin(function(ar) {
      if (ar.succeeded()) {
        transaction(new MysqlTransaction(ar.result()), null);
      } else {
        transaction(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Gets a connection and frees it on close.

   @public
   @param connection {function} 
   */
  this.take = function(connection) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_mysqlService.take(function(ar) {
      if (ar.succeeded()) {
        connection(new MysqlConnection(ar.result()), null);
      } else {
        connection(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_mysqlService;
};

/**

 @memberof module:vertx-mysql-postgresql-js/mysql_service
 @param vertx {Vertx} 
 @param config {Object} 
 @return {MysqlService}
 */
MysqlService.create = function(vertx, config) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object') {
    return new MysqlService(JMysqlService.create(vertx._jdel, utils.convParamJsonObject(config)));
  } else utils.invalidArgs();
};

/**

 @memberof module:vertx-mysql-postgresql-js/mysql_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {MysqlService}
 */
MysqlService.createEventBusProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return new MysqlService(JMysqlService.createEventBusProxy(vertx._jdel, address));
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = MysqlService;