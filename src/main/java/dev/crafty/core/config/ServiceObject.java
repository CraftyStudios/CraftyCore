package dev.crafty.core.config;

import java.util.List;

public abstract class ServiceObject<T> {
    public final List<String> servicePaths; // DO NOT CHANGE FIELD NAME

    protected ServiceObject(List<String> servicePaths) {
        this.servicePaths = servicePaths;
    }

    protected abstract void onLoad(ServiceObjectMetadata<T> value);

}
