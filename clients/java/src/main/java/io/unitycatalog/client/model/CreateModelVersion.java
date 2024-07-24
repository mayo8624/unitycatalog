/*
 * Unity Catalog API
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * The version of the OpenAPI document: 0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */


package io.unitycatalog.client.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * CreateModelVersion
 */
@JsonPropertyOrder({
  CreateModelVersion.JSON_PROPERTY_MODEL_NAME,
  CreateModelVersion.JSON_PROPERTY_CATALOG_NAME,
  CreateModelVersion.JSON_PROPERTY_SCHEMA_NAME,
  CreateModelVersion.JSON_PROPERTY_SOURCE,
  CreateModelVersion.JSON_PROPERTY_RUN_ID,
  CreateModelVersion.JSON_PROPERTY_STORAGE_LOCATION,
  CreateModelVersion.JSON_PROPERTY_COMMENT
})
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "Generator version: 7.5.0")
public class CreateModelVersion {
  public static final String JSON_PROPERTY_MODEL_NAME = "model_name";
  private String modelName;

  public static final String JSON_PROPERTY_CATALOG_NAME = "catalog_name";
  private String catalogName;

  public static final String JSON_PROPERTY_SCHEMA_NAME = "schema_name";
  private String schemaName;

  public static final String JSON_PROPERTY_SOURCE = "source";
  private String source;

  public static final String JSON_PROPERTY_RUN_ID = "run_id";
  private String runId;

  public static final String JSON_PROPERTY_STORAGE_LOCATION = "storage_location";
  private String storageLocation;

  public static final String JSON_PROPERTY_COMMENT = "comment";
  private String comment;

  public CreateModelVersion() { 
  }

  public CreateModelVersion modelName(String modelName) {
    this.modelName = modelName;
    return this;
  }

   /**
   * Name of model, relative to parent schema.
   * @return modelName
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_MODEL_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getModelName() {
    return modelName;
  }


  @JsonProperty(JSON_PROPERTY_MODEL_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }


  public CreateModelVersion catalogName(String catalogName) {
    this.catalogName = catalogName;
    return this;
  }

   /**
   * Name of parent catalog.
   * @return catalogName
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_CATALOG_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getCatalogName() {
    return catalogName;
  }


  @JsonProperty(JSON_PROPERTY_CATALOG_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }


  public CreateModelVersion schemaName(String schemaName) {
    this.schemaName = schemaName;
    return this;
  }

   /**
   * Name of parent schema relative to its parent catalog.
   * @return schemaName
  **/
  @jakarta.annotation.Nonnull
  @JsonProperty(JSON_PROPERTY_SCHEMA_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)

  public String getSchemaName() {
    return schemaName;
  }


  @JsonProperty(JSON_PROPERTY_SCHEMA_NAME)
  @JsonInclude(value = JsonInclude.Include.ALWAYS)
  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }


  public CreateModelVersion source(String source) {
    this.source = source;
    return this;
  }

   /**
   * URI indicating the location of the source model artifacts.
   * @return source
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_SOURCE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getSource() {
    return source;
  }


  @JsonProperty(JSON_PROPERTY_SOURCE)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setSource(String source) {
    this.source = source;
  }


  public CreateModelVersion runId(String runId) {
    this.runId = runId;
    return this;
  }

   /**
   * The run id used by the ML package that generated the model.
   * @return runId
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_RUN_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getRunId() {
    return runId;
  }


  @JsonProperty(JSON_PROPERTY_RUN_ID)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setRunId(String runId) {
    this.runId = runId;
  }


  public CreateModelVersion storageLocation(String storageLocation) {
    this.storageLocation = storageLocation;
    return this;
  }

   /**
   * Storage root URL for model (for **MANAGED**, **EXTERNAL** models)
   * @return storageLocation
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_STORAGE_LOCATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getStorageLocation() {
    return storageLocation;
  }


  @JsonProperty(JSON_PROPERTY_STORAGE_LOCATION)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setStorageLocation(String storageLocation) {
    this.storageLocation = storageLocation;
  }


  public CreateModelVersion comment(String comment) {
    this.comment = comment;
    return this;
  }

   /**
   * User-provided free-form text description.
   * @return comment
  **/
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_COMMENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

  public String getComment() {
    return comment;
  }


  @JsonProperty(JSON_PROPERTY_COMMENT)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setComment(String comment) {
    this.comment = comment;
  }


  /**
   * Return true if this CreateModelVersion object is equal to o.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateModelVersion createModelVersion = (CreateModelVersion) o;
    return Objects.equals(this.modelName, createModelVersion.modelName) &&
        Objects.equals(this.catalogName, createModelVersion.catalogName) &&
        Objects.equals(this.schemaName, createModelVersion.schemaName) &&
        Objects.equals(this.source, createModelVersion.source) &&
        Objects.equals(this.runId, createModelVersion.runId) &&
        Objects.equals(this.storageLocation, createModelVersion.storageLocation) &&
        Objects.equals(this.comment, createModelVersion.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(modelName, catalogName, schemaName, source, runId, storageLocation, comment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateModelVersion {\n");
    sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
    sb.append("    catalogName: ").append(toIndentedString(catalogName)).append("\n");
    sb.append("    schemaName: ").append(toIndentedString(schemaName)).append("\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    runId: ").append(toIndentedString(runId)).append("\n");
    sb.append("    storageLocation: ").append(toIndentedString(storageLocation)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

  /**
   * Convert the instance into URL query string.
   *
   * @return URL query string
   */
  public String toUrlQueryString() {
    return toUrlQueryString(null);
  }

  /**
   * Convert the instance into URL query string.
   *
   * @param prefix prefix of the query string
   * @return URL query string
   */
  public String toUrlQueryString(String prefix) {
    String suffix = "";
    String containerSuffix = "";
    String containerPrefix = "";
    if (prefix == null) {
      // style=form, explode=true, e.g. /pet?name=cat&type=manx
      prefix = "";
    } else {
      // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
      prefix = prefix + "[";
      suffix = "]";
      containerSuffix = "]";
      containerPrefix = "[";
    }

    StringJoiner joiner = new StringJoiner("&");

    // add `model_name` to the URL query string
    if (getModelName() != null) {
      joiner.add(String.format("%smodel_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getModelName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `catalog_name` to the URL query string
    if (getCatalogName() != null) {
      joiner.add(String.format("%scatalog_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getCatalogName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `schema_name` to the URL query string
    if (getSchemaName() != null) {
      joiner.add(String.format("%sschema_name%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSchemaName()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `source` to the URL query string
    if (getSource() != null) {
      joiner.add(String.format("%ssource%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getSource()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `run_id` to the URL query string
    if (getRunId() != null) {
      joiner.add(String.format("%srun_id%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getRunId()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `storage_location` to the URL query string
    if (getStorageLocation() != null) {
      joiner.add(String.format("%sstorage_location%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getStorageLocation()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    // add `comment` to the URL query string
    if (getComment() != null) {
      joiner.add(String.format("%scomment%s=%s", prefix, suffix, URLEncoder.encode(String.valueOf(getComment()), StandardCharsets.UTF_8).replaceAll("\\+", "%20")));
    }

    return joiner.toString();
  }
}

