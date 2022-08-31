package com.gempukku.lotro.common;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class);
    private static Properties _properties;
    private static File _root;

    private synchronized static Properties getProperties() {
        if (_properties == null) {
            Properties props = new Properties();
            try {
                props.load(AppConfig.class.getResourceAsStream("/gemp-lotr.properties"));
                String gempPropertiesOverride = System.getProperty("gemp-lotr.override");
                if (gempPropertiesOverride != null)
                    props.load(AppConfig.class.getResourceAsStream(gempPropertiesOverride));
                _properties = props;
            } catch (Exception exp) {
                LOGGER.error("Can't load application configuration", exp);
                throw new RuntimeException("Unable to load application configuration", exp);
            }
        }
        return _properties;
    }

    public static String getProperty(String property) {
        return getProperties().getProperty(property);
    }


    private static boolean AppInIDE() {
        String classPath = System.getProperty("java.class.path");
        return classPath.contains("idea_rt.jar");
    }

    private static boolean AppInUnitTest() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }
    public static String getResourcePath() throws IOException {
        if(AppInIDE())
            return getProperties().getProperty("dev.resources.path");

        if(AppInUnitTest())
            return getProperties().getProperty("test.resources.path");

        return getProperties().getProperty("resources.path");
    }

    public static File getResourceFile(String subPath) throws IOException {
        String path = Paths.get(getResourcePath(), subPath).toString();
        return new File(path);
    }

    public static FileInputStream getResourceStream(String subPath) throws IOException {
        String path = Paths.get(getResourcePath(), subPath).toString();
        return new FileInputStream(path);
    }

    public static String getWebPath() { return getProperty("web.path"); }

}
