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

import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.ICommandLinePostProcessor;
import de.flapdoodle.embed.process.store.IArtifactStore;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.File;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PostgreSQL {

  public synchronized static PostgreSQL start(int port) throws Exception {
    IRuntimeConfig config;
    String a = System.getProperty("target.dir");
    File targetDir;
    if (a != null && (targetDir = new File(a)).exists() && targetDir.isDirectory()) {
      config = EmbeddedPostgres.cachedRuntimeConfig(targetDir.toPath());
    } else {
      config = EmbeddedPostgres.defaultRuntimeConfig();
    }

    // SSL
    File sslKey = getResourceAsFile("ssl-docker/server.key");
    Files.setPosixFilePermissions(sslKey.toPath(), Collections.singleton(PosixFilePermission.OWNER_READ));
    File sslCrt = getResourceAsFile("ssl-docker/server.crt");

    EmbeddedPostgres process = new EmbeddedPostgres(V9_6);
    IRuntimeConfig sslConfig = new IRuntimeConfig() {
      @Override
      public ProcessOutput getProcessOutput() {
        return config.getProcessOutput();
      }
      @Override
      public ICommandLinePostProcessor getCommandLinePostProcessor() {
        ICommandLinePostProcessor commandLinePostProcessor = config.getCommandLinePostProcessor();
        return (distribution, args) -> {
          List<String> result = commandLinePostProcessor.process(distribution, args);
          if (result.get(0).endsWith("postgres")) {
            result = new ArrayList<>(result);
            result.add("--ssl=on");
            result.add("--ssl_cert_file=" + sslCrt.getAbsolutePath());
            result.add("--ssl_key_file=" + sslKey.getAbsolutePath());
          }
          return result;
        };
      }
      @Override
      public IArtifactStore getArtifactStore() {
        return config.getArtifactStore();
      }
      @Override
      public boolean isDaemonProcess() {
        return config.isDaemonProcess();
      }
    };
    process.start(sslConfig,
      "localhost",
      port,
      "testdb",
      "vertx",
      "password",
      Collections.emptyList());
    return new PostgreSQL(process);
  }

  private EmbeddedPostgres postgres;

  public PostgreSQL(EmbeddedPostgres postgres) {
    this.postgres = postgres;
  }

  public synchronized void stop() throws Exception {
    if (postgres != null) {
      try {
        postgres.stop();
      } finally {
        postgres = null;
      }
    }
  }

  private static File getResourceAsFile(String name) throws Exception {
    InputStream in = PostgreSQL.class.getClassLoader().getResourceAsStream(name);
    Path path = Files.createTempFile("vertx.pg.", ".tmp");
    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
    File file = path.toFile();
    file.deleteOnExit();
    return file;
  }
}
