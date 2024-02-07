package dez.fortexx.bankplusplus.configuration.configurator;

import de.exlll.configlib.YamlConfigurations;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

public class Configurator {
    public static <T> T readConfiguration(Plugin p, Class<T> cls) {
        assert cls.isAnnotationPresent(FromFile.class);
        final var dataFolder = p.getDataFolder().toPath();
        final var filename = cls.getDeclaredAnnotation(FromFile.class).value();

        final var configPath = dataFolder.resolve(filename);

        if (!configPath.toFile().exists()) {
            try {
                saveDefault(cls, configPath);
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        return YamlConfigurations.load(configPath, cls);
    }

    private static <T> void saveDefault(Class<T> cls, Path p) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        final var instance = cls.getConstructor().newInstance();

        YamlConfigurations.save(p, cls, instance);
    }
}
