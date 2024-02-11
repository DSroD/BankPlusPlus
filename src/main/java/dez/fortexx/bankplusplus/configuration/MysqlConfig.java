package dez.fortexx.bankplusplus.configuration;

import de.exlll.configlib.Configuration;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Configuration
public class MysqlConfig {
        private String host = "localhost";
        private String port = "3306";
        private String database = "banks";
        private String username = "root";
        private String password = "";
        private String tableName = "bankplusplus";

        public String getHost() {
                return host;
        }

        public String getPort() {
                return port;
        }

        public String getDatabase() {
                return database;
        }

        public String getUsername() {
                return username;
        }

        public String getPassword() {
                return password;
        }

        public String getTableName() {
                return tableName;
        }
}
