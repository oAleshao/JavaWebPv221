package itstep.learning.services.cacheMaster;

import com.google.inject.Inject;
import itstep.learning.services.stream.StringReader;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CacheMasterService implements CacheMaster {

    private final StringReader stringReader;
    private final Logger logger;

    @Inject
    public CacheMasterService(StringReader stringReader, Logger logger) {
        this.stringReader = stringReader;
        this.logger = logger;
    }

    @Override
    public int getMaxAge(String maxAgeName) {
        int maxAge = 0;
        try {
            Map<String, Object> tmpMap = this.stringReader.getDataFromFile("maxAges.ini");
            String[] tmpData = ((String) tmpMap.get("maxAge." + maxAgeName)).split("\\*");

            maxAge = Integer.parseInt(tmpData[0]);
            for (int i = 1; i < tmpData.length; i++) {
                int number = Integer.parseInt(tmpData[i]);
                maxAge *= number;
            }

            return maxAge;

        } catch (Exception ex) {
            logger.log(Level.WARNING,"WORK HERE " + ex.getMessage(), ex);
        }
        return 0;
    }
}
