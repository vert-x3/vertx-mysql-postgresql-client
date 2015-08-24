require 'vertx-mysql-postgresql/async_sql_client'
require 'vertx/vertx'
require 'vertx/context'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.asyncsql.MySQLClient
module VertxMysqlPostgresql
  #  Represents an asynchronous MySQL client
  class MySQLClient < ::VertxMysqlPostgresql::AsyncSQLClient
    # @private
    # @param j_del [::VertxMysqlPostgresql::MySQLClient] the java delegate
    def initialize(j_del)
      super(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxMysqlPostgresql::MySQLClient] the underlying java delegate
    def j_del
      @j_del
    end
    #  Create a MySQL client which maintains its own pool.
    # @overload createNonShared(vertx,config)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [Hash{String => Object}] config the configuration
    # @overload createNonShared(vertx,context,config)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [::Vertx::Context] context the context
    #   @param [Hash{String => Object}] config the configuration
    # @return [::VertxMysqlPostgresql::AsyncSQLClient] the client
    def self.create_non_shared(param_1=nil,param_2=nil,param_3=nil)
      if param_1.class.method_defined?(:j_del) && param_2.class == Hash && !block_given? && param_3 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::MySQLClient.java_method(:createNonShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(param_1.j_del,::Vertx::Util::Utils.to_json_object(param_2)),::VertxMysqlPostgresql::AsyncSQLClient)
      elsif param_1.class.method_defined?(:j_del) && param_2.class.method_defined?(:j_del) && param_3.class == Hash && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::MySQLClient.java_method(:createNonShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCore::Context.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(param_1.j_del,param_2.j_del,::Vertx::Util::Utils.to_json_object(param_3)),::VertxMysqlPostgresql::AsyncSQLClient)
      end
      raise ArgumentError, "Invalid arguments when calling create_non_shared(param_1,param_2,param_3)"
    end
    #  Create a MySQL client which shares its data source with any other MySQL clients created with the same
    #  data source name
    # @overload createShared(vertx,config)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [Hash{String => Object}] config the configuration
    # @overload createShared(vertx,config,poolName)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [Hash{String => Object}] config the configuration
    #   @param [String] poolName the pool name
    # @overload createShared(vertx,context,config)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [::Vertx::Context] context the context
    #   @param [Hash{String => Object}] config the configuration
    # @overload createShared(vertx,context,config,poolName)
    #   @param [::Vertx::Vertx] vertx the Vert.x instance
    #   @param [::Vertx::Context] context the context
    #   @param [Hash{String => Object}] config the configuration
    #   @param [String] poolName the pool name
    # @return [::VertxMysqlPostgresql::AsyncSQLClient] the client
    def self.create_shared(param_1=nil,param_2=nil,param_3=nil,param_4=nil)
      if param_1.class.method_defined?(:j_del) && param_2.class == Hash && !block_given? && param_3 == nil && param_4 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::MySQLClient.java_method(:createShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(param_1.j_del,::Vertx::Util::Utils.to_json_object(param_2)),::VertxMysqlPostgresql::AsyncSQLClient)
      elsif param_1.class.method_defined?(:j_del) && param_2.class == Hash && param_3.class == String && !block_given? && param_4 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::MySQLClient.java_method(:createShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::java.lang.String.java_class]).call(param_1.j_del,::Vertx::Util::Utils.to_json_object(param_2),param_3),::VertxMysqlPostgresql::AsyncSQLClient)
      elsif param_1.class.method_defined?(:j_del) && param_2.class.method_defined?(:j_del) && param_3.class == Hash && !block_given? && param_4 == nil
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::MySQLClient.java_method(:createShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCore::Context.java_class,Java::IoVertxCoreJson::JsonObject.java_class]).call(param_1.j_del,param_2.j_del,::Vertx::Util::Utils.to_json_object(param_3)),::VertxMysqlPostgresql::AsyncSQLClient)
      elsif param_1.class.method_defined?(:j_del) && param_2.class.method_defined?(:j_del) && param_3.class == Hash && param_4.class == String && !block_given?
        return ::Vertx::Util::Utils.safe_create(Java::IoVertxExtAsyncsql::MySQLClient.java_method(:createShared, [Java::IoVertxCore::Vertx.java_class,Java::IoVertxCore::Context.java_class,Java::IoVertxCoreJson::JsonObject.java_class,Java::java.lang.String.java_class]).call(param_1.j_del,param_2.j_del,::Vertx::Util::Utils.to_json_object(param_3),param_4),::VertxMysqlPostgresql::AsyncSQLClient)
      end
      raise ArgumentError, "Invalid arguments when calling create_shared(param_1,param_2,param_3,param_4)"
    end
  end
end
