package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.hash.ShaHashService;

public class ServicesModule extends AbstractModule {
    @Override
    protected void configure() {
        bind( HashService.class )
                .annotatedWith( Names.named("digest") )
                .to( Md5HashService.class ) ;

        bind( HashService.class )
                .annotatedWith( Names.named("signature") )
                .to( ShaHashService.class ) ;

    }
}
