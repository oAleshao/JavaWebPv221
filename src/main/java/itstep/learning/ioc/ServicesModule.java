package itstep.learning.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import itstep.learning.services.files.FileService;
import itstep.learning.services.files.LocalFileService;
import itstep.learning.services.formparse.FormParseService;
import itstep.learning.services.formparse.MixedFormParseService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.hash.ShaHashService;
import itstep.learning.services.stream.BaosStringReader;
import itstep.learning.services.stream.StringReader;

public class ServicesModule extends AbstractModule {
    private final StringReader reader;

    public ServicesModule(StringReader reader) {
        this.reader = reader;
    }

    @Override
    protected void configure() {
        bind( HashService.class )
                .annotatedWith( Names.named("digest") )
                .to( Md5HashService.class ) ;

        bind( HashService.class )
                .annotatedWith( Names.named("signature") )
                .to( ShaHashService.class ) ;

        bind( FormParseService.class )
                .annotatedWith( Names.named("formParse") )
                .to( MixedFormParseService.class ) ;

        bind(StringReader.class).toInstance(reader); ;

        bind(FileService.class).to(LocalFileService.class);
    }
}
