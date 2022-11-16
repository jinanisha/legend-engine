// Copyright 2021 Goldman Sachs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.finos.legend.engine.language.pure.compiler.toPureGraph;

import org.eclipse.collections.api.tuple.Pair;
import org.eclipse.collections.impl.tuple.Tuples;
import org.finos.legend.engine.protocol.pure.v1.model.context.EngineErrorType;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.store.service.connection.ServiceStoreConnection;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Authentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.ApiKeyAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.UsernamePasswordAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OAuthAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.VaultCredential;
import org.finos.legend.pure.generated.*;
import org.finos.legend.pure.generated.Root_meta_external_store_service_metamodel_ServiceStore;
import org.finos.legend.engine.shared.core.operational.errorManagement.EngineException;
import org.finos.legend.engine.protocol.pure.v1.model.SourceInformation;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HelperAuthenticationBuilder
{
    public static List<Pair<String, ? extends Root_meta_pure_authentication_Authentication>> compileAuthentication(ServiceStoreConnection serviceStoreConnection,Root_meta_external_store_service_metamodel_ServiceStore pureServiceStore, CompileContext context)
    {
        return serviceStoreConnection.authSpecs.entrySet().stream().map(
                entry ->
        {
            String securitySchemeId = entry.getKey();
            Authentication authSpec = entry.getValue();

            validateSecurityScheme(securitySchemeId,authSpec,pureServiceStore,serviceStoreConnection.sourceInformation);

            if (authSpec instanceof UsernamePasswordAuthentication)
            {
                UsernamePasswordAuthentication usernamePasswordAuthentication = (UsernamePasswordAuthentication) authSpec;
                return Tuples.pair(securitySchemeId,
                        new  Root_meta_pure_authentication_UsernamePasswordAuthentication_Impl("")
                           ._username(usernamePasswordAuthentication.username)
                           ._password(new Root_meta_pure_authentication_VaultCredential_Impl("")
                                   ._vaultReference(((VaultCredential)usernamePasswordAuthentication.password).vaultReference))); //TODO: Validate Authenticaton  Credential Combos


            }
           else if (authSpec instanceof OAuthAuthentication)
           {
               OAuthAuthentication oAuthAuthentication = (OAuthAuthentication) authSpec;
               return Tuples.pair(securitySchemeId,
                       new Root_meta_pure_authentication_OauthAuthentication_Impl("")
                               ._credential ( new Root_meta_pure_authentication_OauthCredential_Impl("")
                          //._grantType(context.pureModel.getEnumValue("meta::external::store::service::metamodel::runtime::OauthGrantType", oAuthAuthentication.grantType.toString()))
                                ._grantType(oAuthAuthentication.credential.grantType)
                          ._clientId(oAuthAuthentication.credential.clientId)
                          ._clientSecretVaultReference(oAuthAuthentication.credential.clientSecretVaultReference)
                          ._authServerUrl(oAuthAuthentication.credential.authServerUrl)));
           }
           else if (authSpec instanceof ApiKeyAuthentication)
            {
                ApiKeyAuthentication apiKeyAuthentication = (ApiKeyAuthentication) authSpec;
                return Tuples.pair(securitySchemeId,
                        new Root_meta_pure_authentication_ApiKeyAuthentication_Impl("")
                                ._value(apiKeyAuthentication.value));
                               
            }
           else
           {
               throw new EngineException("Unsupported Authentication Type : " + authSpec.getClass().getSimpleName(), null, EngineErrorType.COMPILATION);
           }
        }).collect(Collectors.toList());
    }

    private static void validateSecurityScheme(String id, Authentication authSpec, Root_meta_external_store_service_metamodel_ServiceStore pureServiceStore, SourceInformation sourceInformation)
    {
        Root_meta_external_store_service_metamodel_SecurityScheme_Impl securityScheme = (Root_meta_external_store_service_metamodel_SecurityScheme_Impl) pureServiceStore._securitySchemes().getMap().get(id);
        if(securityScheme == null)
        {
            throw new EngineException("Security Scheme not defined in ServiceStore: " + id, sourceInformation, EngineErrorType.COMPILATION);
        }

        if (securityScheme instanceof Root_meta_external_store_service_metamodel_SimpleHttpSecurityScheme_Impl)
        {
            if (!(authSpec instanceof UsernamePasswordAuthentication))
            {
                throw new EngineException("securityScheme-Authentication combination is not supported. Only supported combinations are \n [Http, UsernamePasswordAuthentication], [ApiKey, ApiKeySpecification], [Oauth, OauthAuthentication]",sourceInformation,EngineErrorType.COMPILATION);
            }
        }
        else if (securityScheme instanceof Root_meta_external_store_service_metamodel_ApiKeySecurityScheme_Impl)
        {
            if (!(authSpec instanceof ApiKeyAuthentication))
            {
                throw new EngineException("securityScheme-Authentication combination is not supported. Only supported combinations are \n [Http, UsernamePasswordAuthentication], [ApiKey, ApiKeySpecification], [Oauth, OauthAuthentication]",sourceInformation,EngineErrorType.COMPILATION);
            }
        }
        else if (securityScheme instanceof Root_meta_external_store_service_metamodel_OauthSecurityScheme_Impl)
        {
            if (!(authSpec instanceof OAuthAuthentication))
            {
                throw new EngineException("securityScheme-Authentication combination is not supported. Only supported combinations are \n [Http, UsernamePasswordAuthentication], [ApiKey, ApiKeySpecification], [Oauth, OauthAuthentication]",sourceInformation,EngineErrorType.COMPILATION);
            }
        }
        else
        {
            throw new EngineException("Unsupported Security Scheme type : " + id);
        }
    }

}
