/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.ext.asyncsql;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.config.DownloadConfig;
import com.wix.mysql.config.MysqldConfig;

import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_6_latest;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MySQL {

  public synchronized static MySQL start(int port) throws Exception {
    MysqldConfig cfg = aMysqldConfig(v5_6_latest)
      .withUser("vertx", "password")
      .withPort(port)
      .build();
    EmbeddedMysql.Builder builder = new EmbeddedMysql.Builder(cfg, new DownloadConfig.Builder().build())
      .addSchema("testdb");
    return new MySQL(builder.start());
  }

  private EmbeddedMysql process;

  public MySQL(EmbeddedMysql process) {
    this.process = process;
  }

  public synchronized void stop() throws Exception {
    if (process != null) {
      try {
        process.stop();
      } finally {
        process = null;
      }
    }
  }
}
