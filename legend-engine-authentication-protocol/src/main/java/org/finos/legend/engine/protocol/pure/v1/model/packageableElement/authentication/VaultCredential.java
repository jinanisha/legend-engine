package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;
public class VaultCredential extends Credential{

    public String vaultReference;
    public SourceInformation sourceInformation;
}
