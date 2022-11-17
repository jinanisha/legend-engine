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

package org.finos.legend.engine.language.pure.grammar.from.authentication;

import org.finos.legend.engine.language.pure.grammar.from.PureGrammarParserUtility;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.AuthenticationParserGrammar;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.OauthCredential;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.VaultCredential;
import org.finos.legend.engine.language.pure.grammar.from.ParseTreeWalkerSourceInformation;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.CredentialParserGrammar;

public class CredentialParseTreeWalker
{
    private final ParseTreeWalkerSourceInformation walkerSourceInformation;

    public CredentialParseTreeWalker(ParseTreeWalkerSourceInformation walkerSourceInformation)
    {
        this.walkerSourceInformation = walkerSourceInformation;
    }
    public VaultCredential visitVaultCredential(CredentialSourceCode code, CredentialParserGrammar.VaultCredentialContext ctx)
    {
        VaultCredential v = new VaultCredential();
        v.sourceInformation = code.getSourceInformation();

        CredentialParserGrammar.VaultReferenceContext vaultReferenceContext = PureGrammarParserUtility.validateAndExtractRequiredField(ctx.vaultReference(),"vautReference", v.sourceInformation);
        v.vaultReference = PureGrammarParserUtility.fromGrammarString(vaultReferenceContext.STRING().getText(), true);

        return v;
    }

    public OauthCredential visitOauthCredential(CredentialSourceCode code, CredentialParserGrammar.OauthCredentialContext oauthCredentialContext)
    {
        OauthCredential o = new OauthCredential();
        o.sourceInformation = code.getSourceInformation();

        CredentialParserGrammar.GrantTypeContext grantTypeContext = PureGrammarParserUtility.validateAndExtractRequiredField(oauthCredentialContext.grantType(), "grantType", o.sourceInformation);
        //o.grantType = OauthGrantType.valueOf(PureGrammarParserUtility.fromGrammarString(grantTypeContext.STRING().getText(), true));
        o.grantType = PureGrammarParserUtility.fromGrammarString(grantTypeContext.STRING().getText(), true);

        CredentialParserGrammar.ClientIdContext clientIdContext = PureGrammarParserUtility.validateAndExtractRequiredField(oauthCredentialContext.clientId(), "clientId", o.sourceInformation);
        o.clientId = PureGrammarParserUtility.fromGrammarString(clientIdContext.STRING().getText(), true);

        CredentialParserGrammar.ClientSecretContext clientSecretContext = PureGrammarParserUtility.validateAndExtractOptionalField(oauthCredentialContext.clientSecret(), "clientSecret", o.sourceInformation);
        if (clientSecretContext != null)
        {
            o.clientSecretVaultReference = PureGrammarParserUtility.fromGrammarString(clientSecretContext.STRING().getText(), true);
        }

        CredentialParserGrammar.AuthServerUrlContext authServerUrlContext = PureGrammarParserUtility.validateAndExtractRequiredField(oauthCredentialContext.authServerUrl(), "authServerUrl", o.sourceInformation);
        o.authServerUrl = PureGrammarParserUtility.fromGrammarString(authServerUrlContext.STRING().getText(), true);

        return o;
    }
}
