package io.github.anjoismysign.blobloots.director.manager;

import io.github.anjoismysign.blobloots.configuration.LootsConfiguration;
import io.github.anjoismysign.blobloots.director.LootsManager;
import io.github.anjoismysign.blobloots.director.LootsManagerDirector;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LootsConfigurationManager extends LootsManager {
    private LootsConfiguration configuration;

    public LootsConfigurationManager(LootsManagerDirector managerDirector) {
        super(managerDirector);
        reload();
    }

    @Override
    public void reload() {
        configuration = reloadYamlFile("config", LootsConfiguration.class);
    }

    /**
     * Loads (or reloads) a YAML file from the plugin's data folder and maps it
     * onto a new instance of the given bean class using SnakeYAML.
     * <p>
     * The file is resolved as {@code <name>.yml} inside the plugin's data folder.
     * If it does not exist, the bundled resource of the same name is copied there
     * first via {@link org.bukkit.plugin.Plugin#saveResource(String, boolean)}
     * (without replacing). Afterwards, the manager director's
     * {@code detachAsset} is invoked for the file, and the file is parsed with a
     * {@link Constructor} rooted at {@code clazz}.
     * <p>
     * {@code clazz} must be a bean-style class: it needs an accessible no-argument
     * constructor and properties exposed through getters/setters. Keys in the
     * YAML that don't match a property will cause SnakeYAML to throw.
     *
     * @param name  the file name without extension (e.g. {@code "config"} for
     *              {@code config.yml})
     * @param clazz the class the YAML document is deserialized into
     * @param <T>   the type of the deserialized object
     * @return the deserialized object, or {@code null} if the file is empty
     * @throws RuntimeException                          if the file cannot be read
     *                                                   (wrapping the {@link IOException})
     * @throws org.yaml.snakeyaml.error.YAMLException    if the YAML is malformed or
     *                                                   doesn't match {@code clazz}
     */
    private <T> T reloadYamlFile(String name, Class<T> clazz){
        File pluginDataFolder = getPlugin().getDataFolder();
        String fileName = name+".yml";
        File yamlFile = new File(pluginDataFolder, fileName);
        if (!yamlFile.exists()) {
            getPlugin().saveResource(fileName, false);
        }
        getManagerDirector().detachAsset(fileName, false, pluginDataFolder);
        Constructor constructor = new Constructor(clazz, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try (FileInputStream inputStream = new FileInputStream(yamlFile)) {
            return yaml.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public LootsConfiguration getConfiguration() {
        return configuration;
    }
}