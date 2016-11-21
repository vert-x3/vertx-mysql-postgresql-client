require 'vertx-mysql-postgresql/async_sql_client'
require 'vertx-sql/sql_connection'
require 'vertx/vertx'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.asyncsql.PostgreSQLClient
module VertxMysqlPostgresql
  #  Represents an PostgreSQL client
  class PostgreSQLClient < ::VertxMysqlPostgresql::AsyncSQLClient
    # @private
    # @param j_del [::VertxMysqlPostgresql::PostgreSQLClient] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxMysqlPostgresql::PostgreSQLClient] the underlying java delegate
    def j_del
      @j_del
    end
    @@j_api_type = Object.new
    def @@j_api_type.accept?(obj)
      obj.class == PostgreSQLClient
    end
    def @@j_api_type.wrap(obj)
      PostgreSQLClient.new(obj)
    end
    def @@j_api_type.unwrap(obj)
      obj.j_del
    end
    def self.j_api_type
      @@j_api_type
    end
    def self.j_class
      Java::IoVertxExtAsyncsql::PostgreSQLClient.java_class
    end
    #  Close the client and release all resources.
    #  Call the handler when close is complete.
    # @yield handler that will be called when close is complete
    # @return [void]
    def close
      if !block_given?
        return @j_del.java_method(:close, []).call()
      elsif block_given?
        return @j_del.java_method(:close, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling close()"
    end
    #  Returns a connection that can be used to perform SQL operations on. It's important to remember to close the
    #  connection when you are done, so it is returned to the pool.
    # @yield the handler which is called when the <code>JdbcConnection</code> object is ready for use.
    # @return [void]
    def get_connection
      if block_given?
        return @j_del.java_method(:getConnection, [Java::IoVertxCore::Handler.java_class]).call((Proc.new { |ar| yield(ar.failed ? ar.cause : nil, ar.succeeded ? ::Vertx::Util::Utils.safe_create(ar.result,::VertxSql::SQLConnection) : nil) }))
      end
      raise ArgumentError, "Invalid arguments when calling get_connection()"
    end
    #  Create a PostgreSQL client which maintains its own pool.
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [Hash{String => Object}] config the configuration
    # @return [::VertxMysqlPostgresql::AsyncSQLClient] the client
    def self.create_non_shared(vertx=nil,config=nil)
      if vertx.class.method_defined?(:j_del) && config.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::PostgreSQLClient.java_method(:createNonShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(vertx.j_del,::Vertx::Util::Utils.to_json_object(config)),::VertxMysqlPostgresql::AsyncSQLClient)
      end
      raise ArgumentError, "Invalid arguments when calling create_non_shared(#{vertx},#{config})"
    end
    #  Create a PostgreSQL client which shares its pool with any other MySQL clients created with the same pool name.
    # @param [::Vertx::Vertx] vertx the Vert.x instance
    # @param [Hash{String => Object}] config the configuration
    # @param [String] poolName the pool name
    # @return [::VertxMysqlPostgresql::AsyncSQLClient] the client
    def self.create_shared(vertx=nil,config=nil,poolName=nil)
      if vertx.class.method_defined?(:j_del) && config.class == Hash && !block_given? && poolName == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::PostgreSQLClient.java_method(:createShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(vertx.j_del,::Vertx::Util::Utils.to_json_object(config)),::VertxMysqlPostgresql::AsyncSQLClient)
      elsif vertx.class.method_defined?(:j_del) && config.class == Hash && poolName.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::PostgreSQLClient.java_method(:createShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::java.lang.String.java_class]).call(vertx.j_del,::Vertx::Util::Utils.to_json_object(config),poolName),::VertxMysqlPostgresql::AsyncSQLClient)
      end
      raise ArgumentError, "Invalid arguments when calling create_shared(#{vertx},#{config},#{poolName})"
    end
  end
end
