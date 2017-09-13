package org.dbrain.tags.impl;

import org.dbrain.tags.config.Configuration;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

public class ConfigurationTest {

  /**
   * Test default configuration.
   * @throws Exception
   */
  @Test
  public void testDefaultConfiguration() throws Exception {
    Configuration config = new Configuration();

    Assert.assertNotNull(config.getExternalTags());
    Assert.assertEquals(config.getExternalTags(), new HashSet<String>());
  }

  @Test
  public void testLoadFile() throws Exception {
    Configuration config = new Configuration();
    Configuration.load(config, getClass().getResourceAsStream("/sampleConfig.properties"));

    Assert.assertEquals(
        new HashSet<>(
            Arrays.asList(
                "org.dbrain.tags.samples.external.ExternalTag",
                "org.dbrain.tags.samples.external.ExternalTag2")),
        config.getExternalTags());
  }
}
