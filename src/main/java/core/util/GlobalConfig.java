package core.util;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import jdk.nashorn.internal.objects.Global;

/**
 * Created by xinszhou on 17/03/2017.
 */
public class GlobalConfig {
    public static Config getConfig = ConfigFactory.load();

    public static Config getConfig(String configPath)  {
        return ConfigFactory.load(configPath);
    }

}
