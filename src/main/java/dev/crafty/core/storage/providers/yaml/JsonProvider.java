package dev.crafty.core.storage.providers.yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.crafty.core.CraftyCore;
import dev.crafty.core.log.Logger;
import dev.crafty.core.storage.StorageKey;
import dev.crafty.core.storage.StorageUtils;
import dev.crafty.core.storage.providers.EmptyConfigSchema;
import dev.crafty.core.storage.providers.InternalStorageProvider;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class JsonProvider extends InternalStorageProvider<EmptyConfigSchema> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private File dataFile;

    @Inject
    private Logger logger;
    @Inject
    private CraftyCore core;

    public JsonProvider() {
        super("file", new EmptyConfigSchema());
    }

    @Override
    public void init() {
        dataFile = new File(core.getDataFolder(), "data.json");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (Exception e) {
                logger.logAndDigest(e);
            }
        }
    }

    @Override
    public <T> CompletableFuture<T> fetch(StorageKey<T> key) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching entry for key: " + key.getKey().asString(), false);
            try {
                if (dataFile.length() == 0) {
                    logger.warn("Data file is empty!", false);
                    return null;
                }

                JsonNode root = objectMapper.readTree(dataFile);
                String keyString = key.getKey().asString();

                JsonNode valueNode = root.get(keyString);
                if (valueNode.isMissingNode() || valueNode.isNull()) {
                    logger.warn("Value is missing for key: " + keyString, false);
                    return null;
                }

                if (StorageUtils.isPrimitive(key.getType())) {
                    return (T) valueNode.asText();
                }

                return (T) objectMapper.treeToValue(valueNode, key.getType());
            } catch (IOException e) {
                logger.logAndDigest(e);
                return null;
            }
        });
    }

    @Override
    public <T> void store(StorageKey<T> key, T value) {
        CompletableFuture.runAsync(() -> {
            logger.info("Storing entry for key: " + key.getKey().asString(), false);
            try {
                ObjectMapper mapper = new ObjectMapper();

                JsonNode rootNode;
                if (!dataFile.exists() || dataFile.length() == 0) {
                    rootNode = mapper.createObjectNode();
                } else {
                    rootNode = mapper.readTree(dataFile);
                    if (!rootNode.isObject()) {
                        rootNode = mapper.createObjectNode();
                    }
                }

                ObjectNode objectNode = (ObjectNode) rootNode;

                JsonNode valueNode;
                if (value == null) {
                    valueNode = null;
                } else if (StorageUtils.isPrimitive(value)) {
                    valueNode = mapper.valueToTree(value);
                } else {
                    // complex
                    valueNode = mapper.valueToTree(value);
                }

                String keyString = key.getKey().toString();
                objectNode.set(keyString, valueNode);

                mapper.writerWithDefaultPrettyPrinter().writeValue(dataFile, rootNode);
            } catch (Exception e) {
                logger.logAndDigest(e);
            }
        });
    }
}
