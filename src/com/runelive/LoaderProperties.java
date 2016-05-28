package com.runelive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class LoaderProperties {

    private static final boolean LOAD_FROM_URL = true;
    private static final String PROPERTIES_URL = "https://www.dropbox.com/s/z9foa3rps6iy9v2/loader.props?dl=0";

    public static void load() {
        Properties props = new Properties();
        try {
            if (!LOAD_FROM_URL) {
                props.load(new FileInputStream(new File("loader.props")));
            } else {
                props.load(new URL(PROPERTIES_URL).openStream());
            }
            String prop = props.getProperty("server.version");
            if (prop != null) {
                GameSettings.client_version = prop;
                System.out.println("Client version set to: " + prop);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}