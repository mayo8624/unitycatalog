package io.unitycatalog.server.service.credential;

import com.google.auth.oauth2.AccessToken;
import io.unitycatalog.server.exception.BaseException;
import io.unitycatalog.server.exception.ErrorCode;
import io.unitycatalog.server.model.AwsCredentials;
import io.unitycatalog.server.model.AzureUserDelegationSAS;
import io.unitycatalog.server.model.GcpOauthToken;
import io.unitycatalog.server.model.GenerateTemporaryTableCredentialResponse;
import io.unitycatalog.server.model.GenerateTemporaryVolumeCredentialResponse;
import io.unitycatalog.server.model.TableInfo;
import io.unitycatalog.server.model.VolumeInfo;
import io.unitycatalog.server.service.credential.aws.AwsCredentialVendor;
import io.unitycatalog.server.service.credential.azure.AzureCredential;
import io.unitycatalog.server.service.credential.azure.AzureCredentialVendor;
import io.unitycatalog.server.service.credential.gcp.GcpCredentialVendor;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.services.sts.model.Credentials;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static io.unitycatalog.server.utils.Constants.URI_SCHEME_ABFS;
import static io.unitycatalog.server.utils.Constants.URI_SCHEME_ABFSS;
import static io.unitycatalog.server.utils.Constants.URI_SCHEME_GS;
import static io.unitycatalog.server.utils.Constants.URI_SCHEME_S3;

public class CredentialOperations {

  private final AwsCredentialVendor awsCredentialVendor;
  private final AzureCredentialVendor azureCredentialVendor;
  private final GcpCredentialVendor gcpCredentialVendor;

  public CredentialOperations() {
    this.awsCredentialVendor = new AwsCredentialVendor();
    this.azureCredentialVendor = new AzureCredentialVendor();
    this.gcpCredentialVendor = new GcpCredentialVendor();
  }

  public GenerateTemporaryTableCredentialResponse vendCredentialForTable(TableInfo table) {
    String tableStorageLocation = table.getStorageLocation();
    if (tableStorageLocation == null || tableStorageLocation.isEmpty()) {
      throw new BaseException(ErrorCode.FAILED_PRECONDITION, "Table storage location not found.");
    }

    URI storageLocationUri = URI.create(tableStorageLocation);

    // FIXME!! Update privileges when access controls are implemented
    CredentialContext credentialContext = CredentialContext.builder().storageScheme(storageLocationUri.getScheme()).storageBasePath(storageLocationUri.getPath())
      .privileges(Set.of(CredentialContext.Privilege.SELECT, CredentialContext.Privilege.UPDATE)).locations(List.of(table.getStorageLocation())).build();

    return vendCredential(credentialContext).toTableCredentialResponse();
  }

  public GenerateTemporaryVolumeCredentialResponse vendCredentialForVolume(VolumeInfo volume) {
    String volumePath = volume.getStorageLocation();
    if (volumePath == null || volumePath.isEmpty()) {
      throw new BaseException(ErrorCode.FAILED_PRECONDITION, "Volume storage location not found.");
    }

    URI storageLocationUri = URI.create(volumePath);

    // FIXME!! Update privileges when access controls are implemented
    CredentialContext credentialContext = CredentialContext.builder().storageScheme(storageLocationUri.getScheme()).storageBasePath(storageLocationUri.getPath())
      .privileges(Set.of(CredentialContext.Privilege.SELECT, CredentialContext.Privilege.UPDATE)).locations(List.of(volume.getStorageLocation())).build();

    return vendCredential(credentialContext).toVolumeCredentialResponse();
  }

  public CredentialResponse vendCredential(CredentialContext context) {
    CredentialResponse.CredentialResponseBuilder builder = new CredentialResponse.CredentialResponseBuilder();

    switch (context.storageScheme) {
      case URI_SCHEME_ABFS, URI_SCHEME_ABFSS -> {
        AzureCredential azureCredential = vendAzureCredential(context);
        builder.azureUserDelegationSas(new AzureUserDelegationSAS().sasToken(azureCredential.getSasToken()))
          .expirationTime(azureCredential.getExpirationTimeInEpochMillis());
      }
      case URI_SCHEME_GS -> {
        AccessToken gcpToken = vendGcpToken(context);
        builder.gcpOauthToken(new GcpOauthToken().oauthToken(gcpToken.getTokenValue()))
          .expirationTime(gcpToken.getExpirationTime().getTime());
      }
      case URI_SCHEME_S3 -> {
        Credentials awsSessionCredentials = vendAwsCredential(context);
        builder.awsTempCredentials(new AwsCredentials()
          .accessKeyId(awsSessionCredentials.accessKeyId())
          .secretAccessKey(awsSessionCredentials.secretAccessKey())
          .sessionToken(awsSessionCredentials.sessionToken()));
      }
    }

    return builder.build();
  }

  public Credentials vendAwsCredential(CredentialContext context) {
    return awsCredentialVendor.vendAwsCredentials(context);
  }

  public AzureCredential vendAzureCredential(CredentialContext context) {
    return azureCredentialVendor.vendAzureCredential(context);
  }

  public AccessToken vendGcpToken(CredentialContext context) {
    return gcpCredentialVendor.vendGcpToken(context);
  }

}
