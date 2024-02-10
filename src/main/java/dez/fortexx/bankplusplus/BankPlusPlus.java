package dez.fortexx.bankplusplus;

import dez.fortexx.bankplusplus.async.BlockingScope;
import dez.fortexx.bankplusplus.bank.BankManager;
import dez.fortexx.bankplusplus.bank.balance.BankEconomyManager;
import dez.fortexx.bankplusplus.bank.fees.PercentageFeeProvider;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.UpgradePermissionChecker;
import dez.fortexx.bankplusplus.commands.*;
import dez.fortexx.bankplusplus.commands.api.CommandDispatcher;
import dez.fortexx.bankplusplus.commands.api.arguments.validator.BasicValidator;
import dez.fortexx.bankplusplus.configuration.PluginConfiguration;
import dez.fortexx.bankplusplus.configuration.configurator.Configurator;
import dez.fortexx.bankplusplus.events.BukkitEventDispatcher;
import dez.fortexx.bankplusplus.handlers.PlayerJoinHandler;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.logging.BukkitPluginLogger;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.persistence.cache.LRUBankStoreCache;
import dez.fortexx.bankplusplus.persistence.hikari.HikariBankStore;
import dez.fortexx.bankplusplus.persistence.hikari.HikariBankStoreConfig;
import dez.fortexx.bankplusplus.persistence.api.Failure;
import dez.fortexx.bankplusplus.persistence.wal.BankStoreWAL;
import dez.fortexx.bankplusplus.scheduler.BukkitScheduler;
import dez.fortexx.bankplusplus.scheduler.IScheduler;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import dez.fortexx.bankplusplus.utils.TimeProvider;
import dez.fortexx.bankplusplus.utils.formatting.CurrencyFormatter;
import dez.fortexx.bankplusplus.utils.formatting.ICurrencyFormatter;
import dez.fortexx.bankplusplus.utils.formatting.IUpgradeRequirementFormatter;
import dez.fortexx.bankplusplus.utils.formatting.UpgradeRequirementFormatter;
import dez.fortexx.bankplusplus.vault.VaultEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class BankPlusPlus extends JavaPlugin {
    private IScheduler scheduler;
    private IScheduledPersistence bankPersistence;
    private UUID persistenceTaskId;

    @Override
    public void onEnable() {
        /*
         * CONFIG
         */
        final var config = Configurator.readConfiguration(this, PluginConfiguration.class);
        final var localization = Configurator.readConfiguration(this, Localization.class);

        /*
         * Bukkit specific and other plugin interop
         */
        final var eventCaller = new BukkitEventDispatcher();
        final var logger = new BukkitPluginLogger(this, config.getLogLevel());
        final var scheduler = new BukkitScheduler(this);
        this.scheduler = scheduler;

        final var economy = provideVaultEconomy();
        if (economy == null) {
            throw new RuntimeException("Could not load Vault!");
        }
        final var balanceManager = new VaultEconomy(
                economy
        );

        /*
         * UTILS
         */
        final var timeProvider = new TimeProvider();
        final var currencyFormatter = new CurrencyFormatter(localization);
        final var upgradeRequirementFormatter = new UpgradeRequirementFormatter(currencyFormatter);
        final ITransactionRounding rounding = (amount) ->
                amount.setScale(config.getDecimalPrecision(), RoundingMode.CEILING);

        /*
         * Bank store
         */
        final var bankStoreCache = new LRUBankStoreCache(getServer().getMaxPlayers() * 2, timeProvider);
        final var bankWAL = new BankStoreWAL();

        final var bankStore = new HikariBankStore(
                bankStoreCache,
                bankWAL,
                HikariBankStoreConfig.fromMysqlConfig(config.getMysql()),
                Duration.ofMinutes(2) // TODO: from config
        );
        this.bankPersistence = bankStore;

        // initialize synchronously
        final var initResult = bankStore.initialize();
        if (initResult instanceof Failure f) {
            throw new RuntimeException(f.message());
        }


        final var taxProvider = new PercentageFeeProvider(
                config.getFees().getDepositFeePercent(),
                config.getFees().getWithdrawFeePercent(),
                rounding
        );

        /*
         * BANK MANAGER
         */
        final var bankEconomyManager = new BankEconomyManager(bankStore);
        final var economyManagers = List.of(
                balanceManager,
                bankEconomyManager
        );
        final var bankUpgradePermissionChecker = new UpgradePermissionChecker();

        final var bankLevels = config.getBankLevels()
                .stream()
                .map(level -> level.toBankLimit(economyManagers))
                .toList();

        // Register upgrade permission node for each level (except the first one) of bank
        bankLevels.stream().skip(1).map(BankLimit::name)
                .forEach(
                        registerPermissionNode(
                                "bank.upgrade",
                                '_',
                                "Allows to upgrade to this bank level"
                        )
                );

        final var bankManager = new BankManager(
                bankLevels,
                balanceManager,
                bankEconomyManager,
                bankUpgradePermissionChecker,
                taxProvider,
                eventCaller,
                rounding
        );

        /*
         * EVENTS
         */
        this.getServer().getPluginManager().registerEvents(
                new PlayerJoinHandler(bankStore, scheduler, logger), this
        );

        /*
         * COMMANDS
         */
        final var bankCommandDispatcher = createCommandDispatcher(
                localization,
                bankManager,
                currencyFormatter,
                upgradeRequirementFormatter
        );
        bankCommandDispatcher.register(this);

        /*
         * Scheduled tasks
         */
        persistenceTaskId = scheduler.runTimer(
                bankStore.persistAsync().then(
                        (scope, result) -> {
                            if (result instanceof Failure f) {
                                scope.runSync(() -> this.getLogger().warning(f.message()));
                            }
                        }
                ),
                Duration.ofSeconds(30),
                Duration.ofSeconds(30)
        );
    }

    @NotNull
    private static CommandDispatcher createCommandDispatcher(
            Localization localization,
            BankManager bankManager,
            ICurrencyFormatter currencyFormatter,
            IUpgradeRequirementFormatter upgradeRequirementFormatter
    ) {
        final var argumentValidator = new BasicValidator();
        return new CommandDispatcher(
                "bank",
                List.of(
                    new DepositCommand(bankManager, localization, currencyFormatter),
                    new WithdrawCommand(bankManager, localization, currencyFormatter),
                    new BalanceCommand(bankManager, localization, currencyFormatter),
                    new UpgradeCommand(bankManager, localization, currencyFormatter, upgradeRequirementFormatter),
                    new InfoCommand(bankManager, localization, currencyFormatter, upgradeRequirementFormatter)
                ),
                localization,
                argumentValidator
        );
    }

    @Nullable
    private Economy provideVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        return rsp.getProvider();
    }

    private Consumer<String> registerPermissionNode(String parentNode, Character separator, String description) {
        return (s) -> {
            final var permission = new Permission(
                    parentNode + "." + s.replace(' ', separator),
                    description,
                    PermissionDefault.OP
            );
            Bukkit.getPluginManager().addPermission(permission);
        };
    }

    @Override
    public void onDisable() {
        bankPersistence.persistAsync().runInScope(BlockingScope.instance);
        scheduler.cancelAll();
    }
}
