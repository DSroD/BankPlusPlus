package dez.fortexx.bankplusplus.configuration;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Configuration
public class ItemConfig {
    @Comment("Material name")
    private String material = Material.DIAMOND.toString();
    @Comment("Amount of the given item")
    private int amount = 5;

    private ItemConfig(Material material, int amount) {
        this.material = material.toString();
        this.amount = amount;
    }

    private ItemConfig() {
    }

    public static ItemConfig of(Material m, int amount) {
        return new ItemConfig(m, amount);
    }

    public ItemStack toItemStack() {
        return new ItemStack(Material.matchMaterial(material), amount);
    }
}
