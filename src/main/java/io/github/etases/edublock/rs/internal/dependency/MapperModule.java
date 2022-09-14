package io.github.etases.edublock.rs.internal.dependency;

import com.google.inject.AbstractModule;
import org.modelmapper.ModelMapper;

public class MapperModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ModelMapper.class).asEagerSingleton();
    }
}
