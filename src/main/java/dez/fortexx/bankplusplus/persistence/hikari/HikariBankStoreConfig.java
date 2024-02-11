package dez.fortexx.bankplusplus.persistence.hikari;

import dez.fortexx.bankplusplus.configuration.MysqlConfig;

public record HikariBankStoreConfig(
        String jdbcUrl,
        String username,
        String password,
        String tableName
) {
    public static HikariBankStoreConfig fromMysqlConfig(MysqlConfig s) {
        return new HikariBankStoreConfig(
                "jdbc:mysql://" + s.getHost() + ":" + s.getPort() + "/" + s.getDatabase(),
                s.getUsername(),
                s.getPassword(),
                s.getTableName()
        );
    }
}
