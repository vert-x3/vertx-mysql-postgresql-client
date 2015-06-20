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

/** @module vertx-mysql-postgresql-js/my_sql_client */
var utils = require('vertx-js/util/utils');
var AsyncSQLClient = require('vertx-mysql-postgresql-js/async_sql_client');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JMySQLClient = io.vertx.ext.asyncsql.MySQLClient;

/**

 Represents an asynchronous MySQL client

 @class
*/
var MySQLClient = function(j_val) {

  var j_mySQLClient = j_val;
  var that = this;
  AsyncSQLClient.call(this, j_val);

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_mySQLClient;
};

/**
 Create a MySQL client which maintains its own pool.

 @memberof module:vertx-mysql-postgresql-js/my_sql_client
 @param vertx {Vertx} the Vert.x instance 
 @param config {Object} the configuration 
 @return {AsyncSQLClient} the client
 */
MySQLClient.createNonShared = function(vertx, config) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object') {
    return utils.convReturnVertxGen(JMySQLClient["createNonShared(io.vertx.core.Vertx,io.vertx.core.json.JsonObject)"](vertx._jdel, utils.convParamJsonObject(config)), AsyncSQLClient);
  } else utils.invalidArgs();
};

/**
 Create a MySQL client which shares its data source with any other MySQL clients created with the same
 data source name

 @memberof module:vertx-mysql-postgresql-js/my_sql_client
 @param vertx {Vertx} the Vert.x instance 
 @param config {Object} the configuration 
 @param poolName {string} the pool name 
 @return {AsyncSQLClient} the client
 */
MySQLClient.createShared = function() {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object') {
    return utils.convReturnVertxGen(JMySQLClient["createShared(io.vertx.core.Vertx,io.vertx.core.json.JsonObject)"](__args[0]._jdel, utils.convParamJsonObject(__args[1])), AsyncSQLClient);
  }else if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object' && typeof __args[2] === 'string') {
    return utils.convReturnVertxGen(JMySQLClient["createShared(io.vertx.core.Vertx,io.vertx.core.json.JsonObject,java.lang.String)"](__args[0]._jdel, utils.convParamJsonObject(__args[1]), __args[2]), AsyncSQLClient);
  } else utils.invalidArgs();
};

// We export the Constructor function
module.exports = MySQLClient;