package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

public class OAuthAuthentication extends Authentication
{
    public String grantType;
    public String clientId;
    public String clientSecretVaultReference;
    public String authServerUrl;
}
