package dez.fortexx.bankplusplus;

import dez.fortexx.bankplusplus.async.BlockingScope;
import dez.fortexx.bankplusplus.bank.BankTransactionManager;
import dez.fortexx.bankplusplus.bank.BankEconomyManager;
import dez.fortexx.bankplusplus.bank.fees.PercentageFeeProvider;
import dez.fortexx.bankplusplus.bank.limits.BankLimit;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.IUpgradePermissionManager;
import dez.fortexx.bankplusplus.bank.upgrade.permissions.UpgradePermissionManager;
import dez.fortexx.bankplusplus.commands.admin.*;
import dez.fortexx.bankplusplus.commands.api.CommandDispatcher;
import dez.fortexx.bankplusplus.commands.api.arguments.OfflinePlayerArgument;
import dez.fortexx.bankplusplus.commands.api.arguments.validator.BasicValidator;
import dez.fortexx.bankplusplus.commands.user.*;
import dez.fortexx.bankplusplus.configuration.PluginConfiguration;
import dez.fortexx.bankplusplus.configuration.configurator.Configurator;
import dez.fortexx.bankplusplus.events.BukkitEventDispatcher;
import dez.fortexx.bankplusplus.handlers.PlayerJoinHandler;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.logging.BukkitPluginLogger;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.persistence.api.Failure;
import dez.fortexx.bankplusplus.persistence.cache.LRUBankStoreCache;
import dez.fortexx.bankplusplus.persistence.hikari.HikariBankStore;
import dez.fortexx.bankplusplus.persistence.hikari.HikariBankStoreConfig;
import dez.fortexx.bankplusplus.persistence.wal.BankStoreWAL;
import dez.fortexx.bankplusplus.placeholders.BankPlusPlusPlaceholderExpansion;
import dez.fortexx.bankplusplus.scheduler.BukkitScheduler;
import dez.fortexx.bankplusplus.scheduler.IScheduler;
import dez.fortexx.bankplusplus.utils.ITransactionRounding;
import dez.fortexx.bankplusplus.utils.TimeProvider;
import dez.fortexx.bankplusplus.utils.formatting.CurrencyFormatter;
import dez.fortexx.bankplusplus.utils.formatting.UpgradeRequirementFormatter;
import dez.fortexx.bankplusplus.vault.VaultEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
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
        final var eventDispatcher = new BukkitEventDispatcher();
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

        final var upgradePermissionNode = "bankplusplus.upgrade";
        final var bankUpgradePermissionManager = new UpgradePermissionManager(upgradePermissionNode);

        /*
         * BANK MANAGER
         */
        final var bankEconomyManager = new BankEconomyManager(
                bankStore,
                bankUpgradePermissionManager,
                eventDispatcher,
                logger
        );

        final var economyManagers = List.of(
                balanceManager,
                bankEconomyManager
        );
        final var bankLevels =  config.getBankLevels()
                .stream()
                .map(level -> level.toBankLimit(economyManagers, rounding))
                .toList();

        bankEconomyManager.setBankLimits(bankLevels);



        // Register upgrade permission node for each level (except the first one) of bank
        final var registerUpgradeNode = registerUpgradePermissionNode(
                "Allows to upgrade to this bank level",
                bankUpgradePermissionManager
        );
        bankLevels.stream().skip(1)
                .forEach(registerUpgradeNode);

        final var bankTransactionManager = new BankTransactionManager(
                balanceManager,
                bankEconomyManager,
                taxProvider,
                eventDispatcher,
                rounding,
                logger
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
        final var argumentValidator = new BasicValidator();
        final var cachedOfflinePlayerArgument = new OfflinePlayerArgument(
                localization.getPlayer().toLowerCase(),
                this,
                timeProvider
        );
        final var bankCommandDispatcher = new CommandDispatcher(
                "bank",
                List.of(
                        new BalanceCommand(bankEconomyManager, localization, currencyFormatter),
                        new InfoCommand(bankEconomyManager, localization, currencyFormatter, upgradeRequirementFormatter),
                        new DepositCommand(bankTransactionManager, localization, currencyFormatter),
                        new WithdrawCommand(bankTransactionManager, localization, currencyFormatter),
                        new UpgradeCommand(bankEconomyManager, localization, currencyFormatter, upgradeRequirementFormatter),
                        new PlayerBalanceCommand(bankEconomyManager, localization, currencyFormatter, cachedOfflinePlayerArgument),
                        new GiveCommand(bankEconomyManager, localization, currencyFormatter, cachedOfflinePlayerArgument),
                        new TakeCommand(bankEconomyManager, localization, currencyFormatter, cachedOfflinePlayerArgument),
                        new PlayerUpgradeCommand(bankEconomyManager, localization, currencyFormatter, cachedOfflinePlayerArgument),
                        new PlayerDowngradeCommand(bankEconomyManager, localization, currencyFormatter, cachedOfflinePlayerArgument)
                ),
                localization,
                argumentValidator
        );
        bankCommandDispatcher.register(this);

        /*
         * Scheduled tasks
         */
        persistenceTaskId = scheduler.runTimer(
                bankStore.persistAsync().then(
                        (scope, result) -> {
                            if (result instanceof Failure f) {
                                scope.runSync(() -> logger.warn(f.message()));
                            }
                        }
                ),
                Duration.ofSeconds(30),
                Duration.ofSeconds(30)
        );

        /*
         * Placeholder API
         */
        if (this.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            final var expansion = new BankPlusPlusPlaceholderExpansion(this, bankStore, currencyFormatter);
            expansion.register();
        }
    }

    @Nullable
    private Economy provideVaultEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        return rsp.getProvider();
    }

    private Consumer<BankLimit> registerUpgradePermissionNode(String description, IUpgradePermissionManager permissionChecker) {
        return (s) -> {
            final var permission = new Permission(
                    permissionChecker.getLimitPermissionNode(s),
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
