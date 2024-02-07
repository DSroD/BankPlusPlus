package dez.fortexx.bankplusplus.configuration;

import de.exlll.configlib.Configuration;
import dez.fortexx.bankplusplus.api.economy.IBalanceManager;
import dez.fortexx.bankplusplus.bank.upgrade.IUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.ItemUpgradeRequirement;
import dez.fortexx.bankplusplus.bank.upgrade.MoneyUpgradeRequirement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class UpgradeRequirementConfig {
    private BigDecimal moneyRequired = null;
    private List<ItemConfig> itemsRequired;

    private UpgradeRequirementConfig() {
    }

    private UpgradeRequirementConfig(BigDecimal moneyRequired, List<ItemConfig> itemsRequired) {
        this.moneyRequired = moneyRequired;
        this.itemsRequired = itemsRequired;
    }

    public static UpgradeRequirementConfig of(@Nullable BigDecimal money, @NotNull List<ItemConfig> items) {
        return new UpgradeRequirementConfig(money, items);
    }

    public Set<IUpgradeRequirement> toUpgradeRequirementsSet(List<IBalanceManager> balanceManagers) {
        final var moneyStream = Stream.of(moneyRequired)
                .filter(Objects::nonNull)
                .filter(Predicate.not(BigDecimal.ZERO::equals))
                .map(cost -> new MoneyUpgradeRequirement(cost, balanceManagers));
        final var itemRequirementStream = itemsRequired
                .stream()
                .map(ItemConfig::toItemStack)
                .map(ItemUpgradeRequirement::new);

        final var merged = Stream.concat(moneyStream, itemRequirementStream);
        return merged.collect(Collectors.toSet());
    }
}
