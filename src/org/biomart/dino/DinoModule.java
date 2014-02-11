package org.biomart.dino;

import org.apache.commons.lang.StringUtils;
import org.biomart.api.Portal;
import org.biomart.api.Query;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.dino.annotations.EnrichmentConfig;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HypgRunner;
import org.biomart.dino.configreader.ConfigReader;
import org.biomart.dino.configreader.AttributesConfig;
import org.biomart.dino.configreader.ShellCommandReader;
import org.biomart.dino.dinos.enrichment.GuiResponseCompiler;
import org.biomart.dino.querybuilder.JavaQueryBuilder;
import org.biomart.dino.querybuilder.QueryBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

public class DinoModule extends AbstractModule {

    @Override
    protected void configure() {
        String s = System.getProperty("file.separator"),
               p = StringUtils.join(new String[] {
                       System.getProperty("biomart.basedir"),
                       System.getProperty("file.separator"),
                       "conf", "dinos", "EnrichmentDino.json"
               }, s);
        
        bind(QueryBuilder.class)
            .annotatedWith(Names.named("JavaApi"))
            .to(JavaQueryBuilder.class);

        bind(HypgRunner.class);
        bind(HypgCommand.class);

        bind(String.class)
            .annotatedWith(Names.named("Enrichment File Config Path"))
            .toInstance(p);
        
        bind(GuiResponseCompiler.class);

    }
                

    @Provides
    Portal providePortal(MartRegistryFactory factory) {
        return new Portal(factory);
    }
    
    @Provides
    Query provideQuery(MartRegistryFactory factory) {
        return new Query(new Portal(factory));
    }
    
    @Provides @EnrichmentConfig
    ConfigReader provideEnrichmentDinoConfig(
            @Named("Enrichment File Config Path") String configPath) {
        
        ConfigReader attrsReader = new AttributesConfig(),
                     binReader = new ShellCommandReader(attrsReader);
        
        return binReader.setConfigFile(configPath);
    }
    
}
