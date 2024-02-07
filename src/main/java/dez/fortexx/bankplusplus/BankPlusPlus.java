package dez.fortexx.bankplusplus;

import dez.fortexx.bankplusplus.api.economy.IBalanceManager;
import dez.fortexx.bankplusplus.async.BlockingScope;
import dez.fortexx.bankplusplus.async.SchedulerScope;
import dez.fortexx.bankplusplus.bank.BankManager;
import dez.fortexx.bankplusplus.bank.fees.PercentageFeeProvider;
import dez.fortexx.bankplusplus.bank.levels.BankLimit;
import dez.fortexx.bankplusplus.commands.BalanceCommand;
import dez.fortexx.bankplusplus.commands.DepositCommand;
import dez.fortexx.bankplusplus.commands.UpgradeCommand;
import dez.fortexx.bankplusplus.commands.WithdrawCommand;
import dez.fortexx.bankplusplus.commands.api.CommandDispatcher;
import dez.fortexx.bankplusplus.commands.api.arguments.validator.BasicValidator;
import dez.fortexx.bankplusplus.configuration.PluginConfiguration;
import dez.fortexx.bankplusplus.configuration.configurator.Configurator;
import dez.fortexx.bankplusplus.events.BukkitEventCaller;
import dez.fortexx.bankplusplus.handlers.PlayerJoinHandler;
import dez.fortexx.bankplusplus.localization.Localization;
import dez.fortexx.bankplusplus.persistence.IScheduledPersistence;
import dez.fortexx.bankplusplus.persistence.cache.LRUBankStoreCache;
import dez.fortexx.bankplusplus.persistence.hikari.HikariBankStore;
import dez.fortexx.bankplusplus.persistence.hikari.HikariBankStoreConfig;
import dez.fortexx.bankplusplus.persistence.utils.Failure;
import dez.fortexx.bankplusplus.persistence.wal.BankStoreWAL;
import dez.fortexx.bankplusplus.scheduler.BukkitScheduler;
import dez.fortexx.bankplusplus.scheduler.IScheduler;
import dez.fortexx.bankplusplus.utils.CurrencyFormatter;
import dez.fortexx.bankplusplus.vault.VaultEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public final class BankPlusPlus extends JavaPlugin {
    private IScheduler scheduler;
    private IScheduledPersistence bankPersistence;

    @Override
    public void onEnable() {
        /*
         * Bukkit specific and other plugin interop
         */
        final var eventCaller = new BukkitEventCaller();
        final var scheduler = new BukkitScheduler(this);
        this.scheduler = scheduler;

        final var economy = getEconomy();
        if (economy == null) {
            throw new RuntimeException("Could not load Vault!");
        }
        final var balanceManager = new VaultEconomy(
                economy
        );

        /*
         * CONFIG
         */

        final var config = Configurator.readConfiguration(this, PluginConfiguration.class);
        final var localization = Configurator.readConfiguration(this, Localization.class);

        /*
         * Bank store
         */
        final var bankStoreCache = new LRUBankStoreCache(200);
        final var bankWAL = new BankStoreWAL();

        final var bankStore = new HikariBankStore(
                bankStoreCache,
                bankWAL,
                HikariBankStoreConfig.fromMysqlConfig(config.getMysql())
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
                config.getDecimalPrecision()
        );

        /*
         * BANK MANAGER
         */
        final Function<IBalanceManager, @Unmodifiable List<BankLimit>> bankLevelSupplier = (bankManager) ->
                config.getBankLevels()
                        .stream()
                        .map(c ->
                                c.toBankLimit(
                                        List.of(
                                                balanceManager,
                                                bankManager
                                        )
                                )
                        ).toList();
        final var bankManager = new BankManager(
                bankLevelSupplier,
                balanceManager,
                bankStore,
                taxProvider,
                eventCaller,
                config
        );

        /*
         * EVENTS
         */
        this.getServer().getPluginManager().registerEvents(
                new PlayerJoinHandler(bankStore, scheduler), this
        );

        /*
         * COMMANDS
         */
        final var currencyFormatter = new CurrencyFormatter(localization);
        final var argumentValidator = new BasicValidator();
        final var bankCommandDispatcher = new CommandDispatcher(
                "bank",
                List.of(
                    new DepositCommand(bankManager, localization, argumentValidator, currencyFormatter),
                    new WithdrawCommand(bankManager, localization, argumentValidator, currencyFormatter),
                    new BalanceCommand(bankManager, localization, argumentValidator, currencyFormatter),
                    new UpgradeCommand(bankManager, localization, argumentValidator, currencyFormatter)
                ),
                localization
        );
        bankCommandDispatcher.register(this);

        /*
         * Scheduled tasks
         */
        scheduler.runTimerAsync(
                () -> SchedulerScope.from(scheduler).runAsync(bankStore::persistAsync),
                Duration.ofMinutes(1),
                Duration.ofMinutes(1)
        );
    }

    @Nullable
    private Economy getEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return null;
        }
        return rsp.getProvider();
    }

    @Override
    public void onDisable() {
        bankPersistence.persistAsync(BlockingScope.instance);
        scheduler.cancelAll();
    }
}
