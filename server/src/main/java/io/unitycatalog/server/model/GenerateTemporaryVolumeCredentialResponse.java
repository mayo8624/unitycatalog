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

package io.unitycatalog.server.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Objects;

/** GenerateTemporaryVolumeCredentialResponse */
@JsonPropertyOrder({
  GenerateTemporaryVolumeCredentialResponse.JSON_PROPERTY_AWS_TEMP_CREDENTIALS,
  GenerateTemporaryVolumeCredentialResponse.JSON_PROPERTY_EXPIRATION_TIME
})
@jakarta.annotation.Generated(
    value = "org.openapitools.codegen.languages.JavaClientCodegen",
    comments = "Generator version: 7.5.0")
public class GenerateTemporaryVolumeCredentialResponse {
  public static final String JSON_PROPERTY_AWS_TEMP_CREDENTIALS = "aws_temp_credentials";
  private AwsCredentials awsTempCredentials;

  public static final String JSON_PROPERTY_EXPIRATION_TIME = "expiration_time";
  private Long expirationTime;

  public GenerateTemporaryVolumeCredentialResponse() {}

  public GenerateTemporaryVolumeCredentialResponse awsTempCredentials(
      AwsCredentials awsTempCredentials) {

    this.awsTempCredentials = awsTempCredentials;
    return this;
  }

  /**
   * Get awsTempCredentials
   *
   * @return awsTempCredentials
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_AWS_TEMP_CREDENTIALS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public AwsCredentials getAwsTempCredentials() {
    return awsTempCredentials;
  }

  @JsonProperty(JSON_PROPERTY_AWS_TEMP_CREDENTIALS)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setAwsTempCredentials(AwsCredentials awsTempCredentials) {
    this.awsTempCredentials = awsTempCredentials;
  }

  public GenerateTemporaryVolumeCredentialResponse expirationTime(Long expirationTime) {

    this.expirationTime = expirationTime;
    return this;
  }

  /**
   * Server time when the credential will expire, in epoch milliseconds. The API client is advised
   * to cache the credential given this expiration time.
   *
   * @return expirationTime
   */
  @jakarta.annotation.Nullable
  @JsonProperty(JSON_PROPERTY_EXPIRATION_TIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public Long getExpirationTime() {
    return expirationTime;
  }

  @JsonProperty(JSON_PROPERTY_EXPIRATION_TIME)
  @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
  public void setExpirationTime(Long expirationTime) {
    this.expirationTime = expirationTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenerateTemporaryVolumeCredentialResponse generateTemporaryVolumeCredentialResponse =
        (GenerateTemporaryVolumeCredentialResponse) o;
    return Objects.equals(
            this.awsTempCredentials, generateTemporaryVolumeCredentialResponse.awsTempCredentials)
        && Objects.equals(
            this.expirationTime, generateTemporaryVolumeCredentialResponse.expirationTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(awsTempCredentials, expirationTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenerateTemporaryVolumeCredentialResponse {\n");
    sb.append("    awsTempCredentials: ").append(toIndentedString(awsTempCredentials)).append("\n");
    sb.append("    expirationTime: ").append(toIndentedString(expirationTime)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
