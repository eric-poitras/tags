package org.dbrain.tags.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

/**
 * Tags configuration class.
 */
public class Configuration {

    /**
     * Property that defines external tags to track. In the form of myapp.Class1;myapp.Class2;myapp.class3
     */
    public static final String EXTERNAL_TAGS = "externalTags";

    private Set<String> externalTags;

    /**
     * @return The list of external tags class names that should
     */
    public Set<String> getExternalTags() {
        if (externalTags == null) {
            externalTags = new HashSet<>();
        }
        return externalTags;
    }

    public void setExternalTags(Set<String> externalTags) {
        this.externalTags = externalTags;
    }


    /**
     * Load a configuration from an input stream, if provided. else return a default configuration.
     */
    public static void load(Configuration config, InputStream is ) throws IOException {
        if (is != null) {
            Properties props = new Properties();
            props.load(is);

            // Process the external tags parameter.
            String externalTags = props.getProperty(EXTERNAL_TAGS);
            if (externalTags != null) {
                Scanner scanner = new Scanner(externalTags);
                scanner.useDelimiter(";");
                while (scanner.hasNext()) {
                    config.getExternalTags().add(scanner.next());
                }
            }

        }
    }
}
