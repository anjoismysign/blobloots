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
        File pluginDataFolder = getPlugin().getDataFolder();
        getPlugin().saveResource("config.yml", false);
        getManagerDirector().detachAsset("config.yml", false, pluginDataFolder);
        File configurationFile = new File(pluginDataFolder, "config.yml");
        Constructor constructor = new Constructor(LootsConfiguration.class, new LoaderOptions());
        Yaml yaml = new Yaml(constructor);
        try (FileInputStream inputStream = new FileInputStream(configurationFile)) {
            configuration = yaml.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public LootsConfiguration getConfiguration() {
        return configuration;
    }
}