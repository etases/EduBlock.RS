package io.github.etases.edublock.rs;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public class ServerBuilder {
    private final List<Consumer<JavalinConfig>> configList;
    private final List<Consumer<Javalin>> handlerList;

    ServerBuilder() {
        configList = new LinkedList<>();
        handlerList = new LinkedList<>();
    }

    public ServerBuilder addConfig(Consumer<JavalinConfig> config) {
        configList.add(config);
        return this;
    }

    public ServerBuilder addHandler(Consumer<Javalin> handler) {
        handlerList.add(handler);
        return this;
    }

    public Javalin build() {
        Javalin server = Javalin.create(config -> configList.forEach(c -> c.accept(config)));
        handlerList.forEach(h -> h.accept(server));
        return server;
    }
}
