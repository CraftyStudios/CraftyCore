package dev.crafty.core.storage.providers.postgres;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.crafty.core.log.Logger;
import dev.crafty.core.storage.StorageKey;
import dev.crafty.core.storage.StorageUtils;
import dev.crafty.core.storage.providers.DefaultDBConfigSchema;
import dev.crafty.core.storage.providers.InternalStorageProvider;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

public class PostgresProvider extends InternalStorageProvider<DefaultDBConfigSchema> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private HikariDataSource dataSource;

    @Inject
    private Logger logger;

    public PostgresProvider() {
        super("postgres", new DefaultDBConfigSchema());
    }

    @Override
    public <T> CompletableFuture<T> fetch(StorageKey<T> key) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching entry for key: " + key.getKey().asString());
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM data WHERE key = ?");
                stmt.setString(1, key.getKey().asString());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        Object value = rs.getObject(1);

                        if (StorageUtils.isPrimitive(value)) {
                            return (T) value;
                        }

                        // complex; object mapper
                        return (T) objectMapper.readValue(value.toString(), key.getType());
                    }

                    return null;
                }

            } catch (Exception e) {
                logger.logAndDigest(e);
                return null;
            }
        });
    }

    @Override
    public <T> void store(StorageKey<T> key, T value) {
        CompletableFuture.runAsync(() -> {
            logger.info("Storing entry for key: " + key.getKey().asString());
            try (Connection conn = dataSource.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO data (key, value) VALUES (?, ?)");
                stmt.setString(1, key.getKey().asString());

                if (StorageUtils.isPrimitive(value)) {
                    stmt.setObject(2, value);
                } else {
                    stmt.setObject(2, objectMapper.writeValueAsString(value));
                }

                stmt.executeUpdate();
           } catch (Exception e) {
               logger.logAndDigest(e);
           }
        });
    }

    @Override
    public void init() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(config.username);
        hikariConfig.setPassword(config.password);
        hikariConfig.setJdbcUrl("jdbc:postgresql://" + config.host + ":" + config.port + "/" + config.database);

        this.dataSource = new HikariDataSource(hikariConfig);
    }

    @Override
    public void shutdown() {
        dataSource.close();
        logger.info("Shutting down Postgres provider");
    }
}
