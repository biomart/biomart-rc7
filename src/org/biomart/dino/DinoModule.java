package org.biomart.dino;

import org.biomart.api.Portal;
import org.biomart.api.Query;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.dino.command.CommandRunner;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HypgRunner;
import org.biomart.dino.querybuilder.JavaQueryBuilder;
import org.biomart.dino.querybuilder.QueryBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

public class DinoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(QueryBuilder.class)
            .annotatedWith(Names.named("JavaApi"))
            .to(JavaQueryBuilder.class);

        bind(HypgRunner.class);
        bind(HypgCommand.class);

    }

    @Provides
    Query providePortal(MartRegistryFactory factory) {
        return new Query(new Portal(factory));
    }
}
