package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import itstep.learning.services.stream.StringReader;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class DbModule extends AbstractModule {
   private Connection connection = null;
   private Driver mysqlDriver = null;
   private final StringReader reader;

   public DbModule(StringReader reader) {
       this.reader = reader;
   }

   @Override
    protected void configure() {

    }

    @Provides
    private Connection getConnection() {
        if (this.connection == null) {
            try{
                Map<String, String> ini = new HashMap<>();;
                String fileName = "db.ini";

                try(InputStream rs = this.getClass().getClassLoader().getResourceAsStream(fileName))
                {
                    String[] lines = reader.read(rs).split("\n");
                    for(String line : lines){
                        String[] parts = line.split("=");
                        ini.put(parts[0].trim(), parts[1].trim());
                    }

                }catch (IOException ignore){
                    return connection;
                }

                // create new driver DBMS
                this.mysqlDriver = new com.mysql.cj.jdbc.Driver();
                // register it
                DriverManager.registerDriver(this.mysqlDriver);

                connection = DriverManager.getConnection(
                        String.format("jdbc:%s://%s:%s/%s?useUnicode=true&characterEncoding=utf8",
                                ini.get("dbms"),
                                ini.get("host"),
                                ini.get("port"),
                                ini.get("schema")
                                ),

                        ini.get("user"),
                        ini.get("password")
                        );
                // different between ADO and JDBC connected when create
            }
            catch (SQLException ex){
                System.out.println("DbModule::getConnection " + ex.getMessage());
            }
        }
        return  this.connection;
    }
}
