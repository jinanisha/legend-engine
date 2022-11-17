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

package org.finos.legend.engine.language.pure.grammar.to;

import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Authentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.UsernamePasswordAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.ApiKeyAuthentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OAuthAuthentication;

import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.convertString;
import static org.finos.legend.engine.language.pure.grammar.to.PureGrammarComposerUtility.getTabString;

public class HelperAuthenticationGrammarComposer
{
    public static String renderAuthentication(String securityScheme, Authentication a, int baseIndentation)
    {
        if (a instanceof OAuthAuthentication)
        {
            OAuthAuthentication spec = (OAuthAuthentication) a;
            return getTabString(baseIndentation) + securityScheme +
                    " : OauthAuthentication\n" +
                    getTabString(baseIndentation) + "{\n" +
                    getTabString(baseIndentation+1) + "token : OauthCredential\n" +
                    getTabString(baseIndentation+1) + "(\n" +
                    getTabString(baseIndentation + 2) + "grantType : " + convertString(spec.credential.grantType.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 2) + "clientId : " + convertString(spec.credential.clientId, true) + ";\n" +
                    getTabString(baseIndentation + 2) + "clientSecretVaultReference : " + convertString(spec.credential.clientSecretVaultReference, true) + ";\n" +
                    getTabString(baseIndentation + 2) + "authorizationServerUrl : " + convertString(spec.credential.authServerUrl, true) + ";\n" +
                    getTabString(baseIndentation + 1) + ");\n" +
                    getTabString(baseIndentation) + "}";
        }
        else if (a instanceof UsernamePasswordAuthentication)
        {
            UsernamePasswordAuthentication spec = (UsernamePasswordAuthentication) a;
            return  getTabString(baseIndentation) + securityScheme +
                    " : UsernamePasswordAuthentication\n" +
                    getTabString(baseIndentation) + "{\n" +
                    getTabString(baseIndentation + 1) + "username : " + convertString(spec.username.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + "password : VaultCredential" +
                    getTabString(baseIndentation + 1) + "(\n" +
                    getTabString(baseIndentation + 2) + "vaultReference : " + convertString(spec.password.toString(), true) + ";\n" +
                    getTabString(baseIndentation + 1) + ");\n" +
                    getTabString(baseIndentation) + "}";
        }
        else if (a instanceof ApiKeyAuthentication)
        {
            ApiKeyAuthentication spec = (ApiKeyAuthentication) a;
            return  getTabString(baseIndentation) + securityScheme +
                    " : ApiKeyAuthentication\n" +
                    getTabString(baseIndentation) + "{\n" +
                    getTabString(baseIndentation + 1) + "value : " + convertString(spec.value.toString(), true) + ";\n" +
                    getTabString(baseIndentation) + "}";
        }
        return null;
    }
}
