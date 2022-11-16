package org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OauthCredential.class, name = "oauth"),
        @JsonSubTypes.Type(value = VaultCredential.class, name = "vault")
})
public class Credential{

    public SourceInformation sourceInformation;
}
