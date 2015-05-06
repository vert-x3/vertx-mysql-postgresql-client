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

/** @module vertx-mysql-postgresql-js/async_sql_client */
var utils = require('vertx-js/util/utils');
var SQLConnection = require('vertx-sql-js/sql_connection');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JAsyncSQLClient = io.vertx.ext.asyncsql.AsyncSQLClient;

/**

 Represents an asynchronous SQL client

 @class
*/
var AsyncSQLClient = function(j_val) {

  var j_asyncSQLClient = j_val;
  var that = this;

  /**
   Close the client and release all resources.
   Call the handler when close is complete.

   @public
   @param whenDone {function} handler that will be called when close is complete 
   */
  this.close = function() {
    var __args = arguments;
    if (__args.length === 0) {
      j_asyncSQLClient["close()"]();
    }  else if (__args.length === 1 && typeof __args[0] === 'function') {
      j_asyncSQLClient["close(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        __args[0](null, null);
      } else {
        __args[0](null, ar.cause());
      }
    });
    } else utils.invalidArgs();
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
      j_asyncSQLClient["getConnection(io.vertx.core.Handler)"](function(ar) {
      if (ar.succeeded()) {
        handler(new SQLConnection(ar.result()), null);
      } else {
        handler(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_asyncSQLClient;
};

// We export the Constructor function
module.exports = AsyncSQLClient;