package io.github.tavstal.openheads.utils;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.models.HeadCategory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HeadUtils {
    private static PluginLogger _logger;
    private static List<HeadCategory> _headCategories;
    public static List<HeadCategory> getHeadCategories() {
        return _headCategories;
    }

    private static Yaml createYaml() {
        // Create LoaderOptions
        LoaderOptions loaderOptions = new LoaderOptions();

        // Set up Constructor with LoaderOptions
        Constructor constructor = new Constructor(loaderOptions);
        TypeDescription dimensionDataDescription = new TypeDescription(HeadCategory.class);
        constructor.addTypeDescription(dimensionDataDescription);

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);

        Representer representer = new Representer(options);
        representer.addClassTag(HeadCategory.class, Tag.MAP);

        return new Yaml(constructor, representer, options);
    }

    public static boolean Load() {
        _logger = OpenHeads.Logger().WithModule(HeadUtils.class);
        InputStream inputStream;
        boolean isFirstLaunch = false;
        _headCategories = new ArrayList<>();

        Path dirPath = Paths.get(OpenHeads.Instance.getDataFolder().getPath());
        Path filePath = Paths.get(dirPath.toString(), "categories.yml");
        if (!Files.exists(filePath)) {
            try {
                inputStream = OpenHeads.Instance.getResource("categories.yml");
                if (inputStream == null) {
                    _logger.Debug("Failed to get categories file from resources.");
                    return false;
                }
                Files.copy(inputStream, filePath);
                isFirstLaunch = true;
            } catch (IOException ex) {
                _logger.Warn("Failed to create categories file.");
                _logger.Error(ex.getMessage());
                return false;
            }
        }

        try {
            inputStream = new FileInputStream(filePath.toFile());
        } catch (FileNotFoundException ex) {
            _logger.Error(String.format("Failed to get categories file. Path: %s", filePath));
            return false;
        } catch (Exception ex) {
            _logger.Warn("Unknown error happened while reading categories file.");
            _logger.Error(ex.getMessage());
            return false;
        }

        _logger.Debug("Loading yaml file...");
        Yaml yaml = createYaml();
        Object yamlObject = yaml.load(inputStream);
        _logger.Debug("Casting yamlObject to list...");
        try {
            //noinspection unchecked
            _headCategories = (List<HeadCategory>) yamlObject;
        } catch (Exception ex) {
            _logger.Warn("Failed to cast the yamlObject.");
            _logger.Error(ex.getMessage());
        }

        for (var category : _headCategories) {
            if (isFirstLaunch) {
                if (!category.CopyFromResource()) {
                    _logger.Warn(String.format("Failed to copy head data file for category '%s'.", category.Name));
                    continue;
                }
            }

            if (!category.Load())
                _logger.Warn(String.format("Failed to load head data file for category '%s'.", category.Name));
        }
        return true;
    }
}
