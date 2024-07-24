package io.unitycatalog.server.persist.utils;

import io.unitycatalog.server.service.credential.azure.ALDSStorageConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ServerPropertiesUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerPropertiesUtils.class);
  @Getter private static final ServerPropertiesUtils instance = new ServerPropertiesUtils();
  private final Properties properties;

  private ServerPropertiesUtils() {
    properties = new Properties();
    loadProperties();
  }

  // Load properties from a configuration file
  private void loadProperties() {
    try (InputStream input = Files.newInputStream(Paths.get("etc/conf/server.properties"))) {
      properties.load(input);
      LOGGER.debug("Properties loaded successfully");
    } catch (IOException ex) {
      LOGGER.error("Exception during loading properties", ex);
    }
  }

  // Get a property value by key
  public String getProperty(String key) {
    if (System.getProperty(key) != null) return System.getProperty(key);
    if (System.getenv().containsKey(key)) return System.getenv(key);
    return properties.getProperty(key);
  }

  public Map<String, S3BucketConfig> getS3Configurations() {
    Map<String, S3BucketConfig> s3BucketConfigMap = new HashMap<>();
    int i = 0;
    while (true) {
      String bucketPath = properties.getProperty("s3.bucketPath." + i);
      String accessKey = properties.getProperty("s3.accessKey." + i);
      String secretKey = properties.getProperty("s3.secretKey." + i);
      String sessionToken = properties.getProperty("s3.sessionToken." + i);
      if (bucketPath == null || accessKey == null || secretKey == null || sessionToken == null) {
        break;
      }
      S3BucketConfig s3BucketConfig =
        new S3BucketConfig(bucketPath, accessKey, secretKey, sessionToken);
      s3BucketConfigMap.put(bucketPath, s3BucketConfig);
      i++;
    }

    return s3BucketConfigMap;
  }

  public Map<String, String> getGcsConfigurations() {
    Map<String, String> gcsConfigMap = new HashMap<>();
    int i = 0;
    while (true) {
      String bucketPath = properties.getProperty("gcs.bucketPath." + i);
      String jsonKeyFilePath = properties.getProperty("gcs.jsonKeyFilePath." + i);
      if (bucketPath == null || jsonKeyFilePath == null) {
        break;
      }
      gcsConfigMap.put(bucketPath, jsonKeyFilePath);
      i++;
    }

    return gcsConfigMap;
  }

  public Map<String, ALDSStorageConfig> getAdlsConfigurations() {
    Map<String, ALDSStorageConfig> gcsConfigMap = new HashMap<>();
    int i = 0;
    while (true) {
      String containerPath = properties.getProperty("adls.containerPath." + i);
      String tenantId = properties.getProperty("adls.tenantId." + i);
      String clientId = properties.getProperty("adls.clientId." + i);
      String clientSecret = properties.getProperty("adls.clientSecret." + i);
      if (containerPath == null || tenantId == null || clientId == null || clientSecret == null) {
        break;
      }
      gcsConfigMap.put(containerPath, ALDSStorageConfig.builder().containerPath(containerPath)
        .tenantId(tenantId).clientId(clientId).clientSecret(clientSecret).build());
      i++;
    }

    return gcsConfigMap;
  }

  public S3BucketConfig getS3BucketConfig(String s3Path) {
    return getS3BucketConfig(URI.create(s3Path));
  }

  public S3BucketConfig getS3BucketConfig(URI s3Uri) {
    String bucketPath = s3Uri.getScheme() + "://" + s3Uri.getHost();
    return (S3BucketConfig) properties.get(bucketPath);
  }

  // Get a property value by key with a default value
  public String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class S3BucketConfig {
    private final String bucketPath;
    private final String accessKey;
    private final String secretKey;
    private final String sessionToken;
  }
}
