package dez.fortexx.bankplusplus.bank.upgrade;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public final class ItemUpgradeRequirement implements IUpgradeRequirement {
    private final ItemStack item;

    public ItemUpgradeRequirement(ItemStack item) {
        this.item = item;
    }

    @Override
    public boolean has(Player p) {
        final var numInInventory = Arrays.stream(p.getInventory().getStorageContents())
                .filter(item::isSimilar)
                .map(ItemStack::getAmount)
                .reduce(Integer::sum);
        return numInInventory.map(n -> n >= item.getAmount()).orElse(false);
    }

    @Override
    public boolean takeFrom(Player p) {
        final var didNotRemove = p.getInventory().removeItem(item.clone());
        return (didNotRemove.isEmpty());
    }
}
