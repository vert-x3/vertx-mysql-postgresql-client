require 'vertx-sql/sql_connection'
require 'vertx/util/utils.rb'
# Generated from io.vertx.ext.asyncsql.AsyncSQLClient
module VertxMysqlPostgresql
  # 
  #  Represents an asynchronous SQL client
  class AsyncSQLClient
    # @private
    # @param j_del [::VertxMysqlPostgresql::AsyncSQLClient] the java delegate
    def initialize(j_del)
      @j_del = j_del
    end
    # @private
    # @return [::VertxMysqlPostgresql::AsyncSQLClient] the underlying java delegate
    def j_del
      @j_del
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
  end
end
