/*
* Copyright 2014 Red Hat, Inc.
*
* Red Hat licenses this file to you under the Apache License, version 2.0
* (the "License"); you may not use this file except in compliance with the
* License. You may obtain a copy of the License at:
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
* WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
* License for the specific language governing permissions and limitations
* under the License.
*/

package io.vertx.ext.asyncsql.mysql;

import io.vertx.ext.asyncsql.mysql.MysqlService;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.Vertx;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;
import java.util.ArrayList;import java.util.HashSet;import java.util.List;import java.util.Map;import java.util.Set;import io.vertx.serviceproxy.ProxyHelper;
import io.vertx.core.Vertx;
import io.vertx.ext.asyncsql.SelectOptions;
import io.vertx.ext.asyncsql.mysql.MysqlConnection;
import io.vertx.ext.asyncsql.BaseSqlService;
import io.vertx.ext.asyncsql.mysql.MysqlService;
import io.vertx.core.json.JsonArray;
import java.util.List;
import io.vertx.ext.asyncsql.DatabaseCommands;
import io.vertx.core.json.JsonObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.asyncsql.mysql.MysqlTransaction;

/*
  Generated Proxy code - DO NOT EDIT
  @author Roger the Robot
*/
public class MysqlServiceVertxEBProxy implements MysqlService {

  private Vertx _vertx;
  private String _address;
  private boolean closed;

  public MysqlServiceVertxEBProxy(Vertx vertx, String address) {
    this._vertx = vertx;
    this._address = address;
  }

  public void start(Handler<AsyncResult<Void>> whenDone) {
    if (closed) {
      whenDone.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "start");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        whenDone.handle(Future.failedFuture(res.cause()));
      } else {
        whenDone.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void stop(Handler<AsyncResult<Void>> whenDone) {
    if (closed) {
      whenDone.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "stop");
    _vertx.eventBus().<Void>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        whenDone.handle(Future.failedFuture(res.cause()));
      } else {
        whenDone.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void raw(String command, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("command", command);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "raw");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void insert(String table, List<String> fields, List<JsonArray> values, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("table", table);
    _json.put("fields", new JsonArray(fields));
    _json.put("values", new JsonArray(values));
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "insert");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void select(String table, SelectOptions options, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("table", table);
    _json.put("options", options.toJson());
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "select");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void prepared(String statement, JsonArray values, Handler<AsyncResult<JsonObject>> resultHandler) {
    if (closed) {
      resultHandler.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    _json.put("statement", statement);
    _json.put("values", values);
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "prepared");
    _vertx.eventBus().<JsonObject>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        resultHandler.handle(Future.failedFuture(res.cause()));
      } else {
        resultHandler.handle(Future.succeededFuture(res.result().body()));
      }
    });
  }

  public void begin(Handler<AsyncResult<MysqlTransaction>> transaction) {
    if (closed) {
      transaction.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "begin");
    _vertx.eventBus().<MysqlTransaction>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        transaction.handle(Future.failedFuture(res.cause()));
      } else {
        String addr = res.result().headers().get("proxyaddr");
        transaction.handle(Future.succeededFuture(ProxyHelper.createProxy(MysqlTransaction.class, _vertx, addr)));
      }
    });
  }

  public void take(Handler<AsyncResult<MysqlConnection>> connection) {
    if (closed) {
      connection.handle(Future.failedFuture(new IllegalStateException("Proxy is closed")));
      return;
    }
    JsonObject _json = new JsonObject();
    DeliveryOptions _deliveryOptions = new DeliveryOptions();
    _deliveryOptions.addHeader("action", "take");
    _vertx.eventBus().<MysqlConnection>send(_address, _json, _deliveryOptions, res -> {
      if (res.failed()) {
        connection.handle(Future.failedFuture(res.cause()));
      } else {
        String addr = res.result().headers().get("proxyaddr");
        connection.handle(Future.succeededFuture(ProxyHelper.createProxy(MysqlConnection.class, _vertx, addr)));
      }
    });
  }


  private List<Character> convertToListChar(JsonArray arr) {
    List<Character> list = new ArrayList<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      list.add((char)jobj.intValue());
    }
    return list;
  }

  private Set<Character> convertToSetChar(JsonArray arr) {
    Set<Character> set = new HashSet<>();
    for (Object obj: arr) {
      Integer jobj = (Integer)obj;
      set.add((char)jobj.intValue());
    }
    return set;
  }

  private <T> Map<String, T> convertMap(Map map) {
    return (Map<String, T>)map;
  }
  private <T> List<T> convertList(List list) {
    return (List<T>)list;
  }
  private <T> Set<T> convertSet(List list) {
    return new HashSet<T>((List<T>)list);
  }
}