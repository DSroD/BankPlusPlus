# BANK++

Bank plugin developed for FortexxGaming CraftingDead server

## Requirements

- Spigot server (or any other fork of Bukkit compatible with SpigotAPI)
    - Tested only on v1.18.2
    - Library downloading based on plugin.yml required
- Vault plugin

## Installation

Just drop the JAR from artifacts (TODO - github actions) into plugins folder in your sever root.

## Usage

### Commands

| Command                          | Permission required                                | Description                                                    |
|----------------------------------|----------------------------------------------------|----------------------------------------------------------------|
| `/bank info`                     | bankplusplus.use                                   | Information about bank account and upgrades                    |
| `/bank balance`                  | bankplusplus.use                                   | Balance of the bank account                                    |
| `/bank deposit [amount]`         | bankplusplus.use                                   | Deposit give amount to the bank                                |
| `/bank withdraw [amount]`        | bankplusplus.use                                   | Withdraws given amount from the bank                           |
| `/bank upgrade`                  | bankplusplus.use + see [Permissions](#permissions) | Upgrades bank level                                            |
| `/bank pbalance [player]`        | bankplusplus.admin.playerbalance                   | Balance of players bank account                                |
| `/bank give [player] [amount]`   | bankplusplus.admin.give                            | Gives money to the players account, considering limits         |
| `\bank take [player] [amount]`   | bankplusplus.admin.take                            | Takes money from the players account (can not go below zero)   |

### Permissions

| Permission                          | Description                                                       |
|-------------------------------------|-------------------------------------------------------------------|
| `bankplusplus.use`                  | Allows usage of all basic commands                                |
| `bankplusplus.fees.bypass`          | User ignores deposit and withdraw fee                             |
| `bankplusplus.fees.bypass.deposit`  | User ignores deposit fee                                          |
| `bankplusplus.fees.bypass.withdraw` | User ignores withdraw fee                                         |
| `bankplusplus.upgrade.[level_name]` | Allows user to upgrade bank to the specified level                |
| `bankplusplus.admin`                | Allows use of all admin commands (see respective sub-permissions) |
| `bankplusplus.admin.playerbalance`  | Allows use of pbalance command                                    |
| `bankplusplus.admin.give`           | Allows use of give command                                        |
| `bankplusplus.admin.take`           | Allows use of take command                                        |

#### Notes:

- `bank.upgrade.[level_name]` permission is available for all levels except the first level
  (first bank level in the list in configuration)
- *level_name* in `bankplusplus.upgrade.[level_name]` is always lowercase, spaces are replaced with underscore (`_`)

### Bank fees

Withdraws and deposits have configurable fees. In order for fees to work properly, the percentage number needs to
be between `0.0` and `1.0`.

Withdrawals are done in such a way the player actually receives the amount asked for - actual change in the bank account is
*larger* then `amount` argument.

Deposits are done in the opposite way - fee is deducted from the `amount` argument - actual change in the bank account is
*smaller* than `amount` argument.

## Placeholders

This plugin implements placeholder extension.
You can use following placeholders if PlaceholderAPI is installed
on the server.

| Placeholder                 | Description                     |
|-----------------------------|---------------------------------|
| %bankplusplus_bank_balance% | Balance of players bank account |
| %bankplusplus_bank_level%   | Level of players bank account   |   

## Building from source

Use gradle tasks to build the plugin from source. Shadow gradle plugin is
used to incorporate dependencies not available from Maven central - use
shadowJar task to build the actual jar file.

## TODO

- Admin commands for managing bank - setting and inspecting levels and bank balance of
players on the server
- Transactions for BankManager instead of *check and hope for the best* approach
- More persistence providers (such as file databases)
- Fee levels (i.e. based on permissions/configuration and bank level)
- More tests, especially integration tests
- Interest (online/offline based on permissions/configuration and bank level)
- Some cleanup and improvement on design choices

## Contributing

Feel free to open issues and PRs, items in [TODO](#todo) and fixes are welcomed the most.
PRs should follow existing code style (another TODO for me to specify this better!)
- Implement against interfaces and use [Dependency injection](http://www.jamesshore.com/v2/blog/2006/dependency-injection-demystified)
- Keep interfaces small (you can separate behaviour to several interfaces).
- Try to follow [SOLID](https://en.wikipedia.org/wiki/SOLID) principles
- Favor composition over inheritance
- Disregard DRY on implementation details
- Delegate APIs of libraries for better testability (i.e. VaultEconomy implmenting IEconomyManager so things depending on it can be tested against mock)

If you are not sure about something, consult [grug](https://grugbrain.dev/) first!

