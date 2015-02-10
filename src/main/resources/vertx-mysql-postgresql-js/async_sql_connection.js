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

/** @module vertx-mysql-postgresql-js/async_sql_connection */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JAsyncSqlConnection = io.vertx.ext.asyncsql.AsyncSqlConnection;

/**

 @class
*/
var AsyncSqlConnection = function(j_val) {

  var j_asyncSqlConnection = j_val;
  var that = this;

  /**
   Sets the auto commit flag for this connection. True by default. Set to false if you want to start a transaction.
   <p>
   If you change autoCommit from false to true, it will commit the running transaction. If you change it from false to
   true, it will start a new transaction. If the autoCommit flag doesn't change, it will just call the resultHandler
   with a success.

   @public
   @param autoCommit {boolean} the autoCommit flag, true by default. 
   @param resultHandler {function} The handler which is called once this operation completes. 
   @return {AsyncSqlConnection}
   */
  this.setAutoCommit = function(autoCommit, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] ==='boolean' && typeof __args[1] === 'function') {
      j_asyncSqlConnection.setAutoCommit(autoCommit, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Executes the given SQL statement.

   @public
   @param sql {string} The SQL to execute. For example <code>CREATE TABLE IF EXISTS table ...</code> 
   @param resultHandler {function} The handler which is called once this operation completes. 
   @return {AsyncSqlConnection}
   */
  this.execute = function(sql, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_asyncSqlConnection.execute(sql, function(ar) {
      if (ar.succeeded()) {
        resultHandler(null, null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Executes the given SQL <code>SELECT</code> statement which returns the results of the query.

   @public
   @param sql {string} The SQL to execute. For example <code>SELECT * FROM mytable</code>. 
   @param resultHandler {function} The handler which is called once the operation completes. It will return a list of <code>JsonObject</code>'s which represent the ResultSet. So column names are keys, and values are of course values. 
   @return {AsyncSqlConnection}
   */
  this.query = function(sql, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_asyncSqlConnection.query(sql, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result().toJson()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Executes the given SQL <code>SELECT</code> statement which returns the results of the query. It will use a prepared
   statement to pass the parameters.

   @public
   @param sql {string} The SQL to execute. For example <code>SELECT * FROM mytable WHERE id=?</code>. 
   @param params {todo} These are the parameters to fill the statement. 
   @param resultHandler {function} The handler which is called once the operation completes. It will return a list of <code>JsonObject</code>'s which represent the ResultSet. So column names are keys, and values are of course values. 
   @return {AsyncSqlConnection}
   */
  this.queryWithParams = function(sql, params, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1] instanceof Array && typeof __args[2] === 'function') {
      j_asyncSqlConnection.queryWithParams(sql, utils.convParamJsonArray(params), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result().toJson()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Executes the given SQL statement which may be an <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code>
   statement.

   @public
   @param sql {string} The SQL to execute. For example <code>INSERT INTO table ...</code> 
   @param resultHandler {function} The handler which is called once the operation completes. 
   @return {AsyncSqlConnection}
   */
  this.update = function(sql, resultHandler) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'string' && typeof __args[1] === 'function') {
      j_asyncSqlConnection.update(sql, function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result().toJson()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Executes the given SQL statement which may be an <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code>
   statement.

   @public
   @param sql {string} The SQL to execute. For example <code>INSERT INTO mytable ('name', 'age') VALUES (?, ?)</code> 
   @param params {todo} These are the parameters to fill the statement. 
   @param resultHandler {function} The handler which is called once the operation completes. 
   @return {AsyncSqlConnection}
   */
  this.updateWithParams = function(sql, params, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && typeof __args[1] === 'object' && __args[1] instanceof Array && typeof __args[2] === 'function') {
      j_asyncSqlConnection.updateWithParams(sql, utils.convParamJsonArray(params), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result().toJson()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Closes the connection. Important to always close the connection when you are done so it's returned to the pool.

   @public
   @param handler {function} The handler which is called once the operation completes. 
   */
  this.close = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_asyncSqlConnection.close(function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  /**
   Commits all changes made since the previous commit/rollback.

   @public
   @param handler {function} The handler which is called once the operation completes. 
   @return {AsyncSqlConnection}
   */
  this.commit = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_asyncSqlConnection.commit(function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  /**
   Rolls back all changes made since the previous commit/rollback.

   @public
   @param handler {function} The handler which is called once the operation completes. 
   @return {AsyncSqlConnection}
   */
  this.rollback = function(handler) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_asyncSqlConnection.rollback(function(ar) {
      if (ar.succeeded()) {
        handler(null, null);
      } else {
        handler(null, ar.cause());
      }
    });
      return that;
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_asyncSqlConnection;
};

// We export the Constructor function
module.exports = AsyncSqlConnection;