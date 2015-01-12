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

/** @module vertx-mysql-postgresql-js/base_sql_service */
var utils = require('vertx-js/util/utils');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JBaseSqlService = io.vertx.ext.asyncsql.BaseSqlService;

/**

 @class
*/
var BaseSqlService = function(j_val) {

  var j_baseSqlService = j_val;
  var that = this;

  /**

   @public
   @param whenDone {function} 
   */
  this.start = function(whenDone) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'function') {
      j_baseSqlService.start(function(ar) {
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
      j_baseSqlService.stop(function(ar) {
      if (ar.succeeded()) {
        whenDone(null, null);
      } else {
        whenDone(null, ar.cause());
      }
    });
    } else utils.invalidArgs();
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_baseSqlService;
};

// We export the Constructor function
module.exports = BaseSqlService;