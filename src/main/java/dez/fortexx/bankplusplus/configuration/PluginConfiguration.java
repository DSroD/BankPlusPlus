package dez.fortexx.bankplusplus.configuration;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dez.fortexx.bankplusplus.configuration.configurator.FromFile;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@FromFile("config.yml")
public class PluginConfiguration {
        @Comment("MySQL configuration")
        private MysqlConfig mysql = new MysqlConfig();
        @Comment("Fee configuration")
        private FeeConfig fees = new FeeConfig();
        @Comment("Decimal precision of computations")
        private int decimalPrecision = 2;
        @Comment("Bank levels")
        private List<BankLevelConfig> bankLevels = List.of(
                BankLevelConfig.of(new BigDecimal("50000"), "Level 1"),
                BankLevelConfig.of(
                        new BigDecimal("200000"), "Level 2",
                        UpgradeRequirementConfig.of(
                                new BigDecimal("60000"), List.of(
                                        ItemConfig.of(Material.DIAMOND, 2),
                                        ItemConfig.of(Material.EMERALD, 1)
                                ))),
                BankLevelConfig.of(new BigDecimal("1000000"), "Level 3", UpgradeRequirementConfig.of(
                        new BigDecimal("300000"), List.of()
                ))
        );

        public MysqlConfig getMysql() {
                return mysql;
        }

        public FeeConfig getFees() {
                return fees;
        }

        public int getDecimalPrecision() {
                return decimalPrecision;
        }

        public List<BankLevelConfig> getBankLevels() {
                return bankLevels;
        }
}
