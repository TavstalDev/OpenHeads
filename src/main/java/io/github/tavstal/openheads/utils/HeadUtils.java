package io.github.tavstal.openheads.utils;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openheads.OpenHeads;
import io.github.tavstal.openheads.models.HeadCategory;
import io.github.tavstal.openheads.models.HeadData;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// Regex Save
// Tags:\s*"([^"]*)"\s*"([^"]*)"
// Texture:\s*"([^"]*)"\s*"([^"]*)"\s*"([^"]*)"

/**
 * Utility class for handling head categories and YAML configuration.
 */
public class HeadUtils {
    private static PluginLogger _logger;
    private static List<HeadCategory> _headCategories;
    /**
     * Gets the list of head categories.
     *
     * @return the list of head categories
     */
    public static List<HeadCategory> getHeadCategories() {
        return _headCategories;
    }

    /**
     * Creates a new Yaml instance with custom configuration.
     *
     * @return the configured Yaml instance
     */
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

    /**
     * Loads the head categories from the YAML configuration file.
     *
     * @return true if the categories were loaded successfully, false otherwise
     */
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
        if (yamlObject instanceof List) {
            try {
                List<?> tempList = (List<?>) yamlObject;
                _headCategories = new ArrayList<>();
                for (Object obj : tempList) {
                    if (obj instanceof Map) {
                        Map<String, Object> data = (Map<String, Object>) obj;
                        HeadCategory category = new HeadCategory(
                                (String) data.get("Name"),
                                (String) data.get("DisplayNameKey"),
                                (String) data.get("DescriptionKey"),
                                (Boolean) data.get("RequirePermission"),
                                (String) data.get("Permission"),
                                data.get("Price") instanceof Number ? ((Number) data.get("Price")).doubleValue() : 0.0,
                                (String) data.get("File"),
                                (String) data.get("Texture")
                        );
                        _headCategories.add(category);
                    } else {
                        _logger.Warn("Element in yamlObject list is not a Map<String, Object>.");
                    }
                }
            } catch (Exception ex) {
                _logger.Warn("Failed to cast the yamlObject.");
                _logger.Error(ex.getMessage());
            }
        } else {
            _logger.Warn("yamlObject is not a List.");
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

    /**
     * Retrieves a head category by its name.
     *
     * @param categoryName the name of the category to search for
     * @return the HeadCategory object if found, null otherwise
     */
    public static HeadCategory getCategory(String categoryName) {
        for (var cat : _headCategories) {
            if (cat.Name.equals(categoryName)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Retrieves the head data for a specific head within a specified category.
     *
     * @param categoryName the name of the category to search in
     * @param headName the name of the head to retrieve
     * @return the HeadData object if found, null otherwise
     */
    public static HeadData getHead(String categoryName, String headName) {
        for (var cat : _headCategories) {
            if (!cat.Name.equals(categoryName)) {
                continue;
            }

            for (var head : cat.getHeads()) {
                if (head.Name.equals(headName)) {
                    return head;
                }
            }

            break;
        }
        return null;
    }

    /**
     * Calculates the total number of heads across all categories.
     *
     * @return the total count of heads
     */
    public static int getHeadCount() {
        int count = 0;
        for (var cat : _headCategories) {
            count += cat.getHeads().size();
        }
        return count;
    }
}
