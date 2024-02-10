package dez.fortexx.bankplusplus.configuration;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import dez.fortexx.bankplusplus.api.economy.IEconomyManager;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Configuration
public class BankLevelConfig {
    @Comment("Maximum money that can be stored in the bank")
    private BigDecimal moneyLimit = new BigDecimal("100000");
    @Comment("Bank level name")
    private String levelName = "default level";
    private UpgradeRequirementConfig requirements = UpgradeRequirementConfig.of(
            new BigDecimal("100000"), List.of()
    );

    private BankLevelConfig() {}

    private BankLevelConfig(BigDecimal moneyLimit, String levelName) {
        this(moneyLimit, levelName, null);
    }

    private BankLevelConfig(BigDecimal moneyLimit, String levelName, UpgradeRequirementConfig upgrades) {
        this.moneyLimit = moneyLimit;
        this.levelName = levelName;
        this.requirements = upgrades;
    }

    public BankLimit toBankLimit(List<IEconomyManager> balanceManagers) {
        final Set<IUpgradeRequirement> reqs = (this.requirements == null)
                ? Set.of()
                : requirements.toUpgradeRequirementsSet(balanceManagers);
        return new BankLimit(
                levelName,
                moneyLimit,
                reqs
        );
    }

    public static BankLevelConfig of(BigDecimal maximumMoney, String levelName) {
        return new BankLevelConfig(maximumMoney, levelName);
    }
    public static BankLevelConfig of(BigDecimal maximumMoney, String levelName, UpgradeRequirementConfig upgrades) {
        return new BankLevelConfig(maximumMoney, levelName, upgrades);
    }
}
