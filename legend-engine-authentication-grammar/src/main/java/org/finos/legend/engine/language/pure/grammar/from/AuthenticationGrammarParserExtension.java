// Copyright 2020 Goldman Sachs
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

package org.finos.legend.engine.language.pure.grammar.from;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.collections.api.factory.Lists;
import org.finos.legend.engine.language.pure.grammar.from.extensions.IAuthenticationGrammarParserExtension;
import org.finos.legend.engine.language.pure.grammar.from.authentication.AuthenticationParseTreeWalker;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.AuthenticationLexerGrammar;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.AuthenticationParserGrammar;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.CredentialLexerGrammar;
import org.finos.legend.engine.language.pure.grammar.from.antlr4.authentication.CredentialParserGrammar;
import org.finos.legend.engine.language.pure.grammar.from.authentication.AuthenticationSourceCode;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Authentication;
import org.finos.legend.engine.protocol.pure.v1.model.packageableElement.authentication.Credential;
import org.finos.legend.engine.language.pure.grammar.from.authentication.CredentialParseTreeWalker;
import org.finos.legend.engine.language.pure.grammar.from.authentication.AuthenticationSourceCode;
import org.finos.legend.engine.language.pure.grammar.from.authentication.CredentialSourceCode;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class AuthenticationGrammarParserExtension implements IAuthenticationGrammarParserExtension
{
    public static final String NAME = "Authentication";

    public List<Function<AuthenticationSourceCode, Authentication>> getExtraAuthenticationParsers()
    {
        return Collections.singletonList(code ->
        {
            SourceCodeParserInfo parserInfo = getAuthenticationParserInfo(code);
            AuthenticationParseTreeWalker walker = new AuthenticationParseTreeWalker(parserInfo.walkerSourceInformation);
            switch (code.getType())
            {
                case "ApiKeyAuthentication":
                    return parseAuthentication(code, p -> walker.visitApiKeyAuthentication(code,p.apiKeyAuthentication()));
                case "UsernamePasswordAuthentication":
                    return parseAuthentication(code, p -> walker.visitUsernamePasswordAuthentication(code, p.basicAuthentication()));
                case "OauthAuthentication":
                    return parseAuthentication(code, p -> walker.visitOAuthAuthentication(code, p.oauthAuthentication()));
                default:
                    return null;
            }
        });
    }

    public List<Function<CredentialSourceCode, Credential>> getExtraCredentialParsers()
    {
        return Collections.singletonList(code ->
        {
            SourceCodeParserInfo parserInfo = getCredentialParserInfo(code);
            CredentialParseTreeWalker walker = new CredentialParseTreeWalker(parserInfo.walkerSourceInformation);
            switch (code.getType())
            {
                case "VaultCredential":
                    return parseCredential(code, p -> walker.visitVaultCredential(code,p.vaultCredential()));
               default:
                    return null;
            }
        });
    }

    private Authentication parseAuthentication(AuthenticationSourceCode code, Function<AuthenticationParserGrammar, Authentication> func)
    {
        CharStream input = CharStreams.fromString(code.getCode());
        ParserErrorListener errorListener = new ParserErrorListener(code.getWalkerSourceInformation());
        AuthenticationLexerGrammar lexer = new AuthenticationLexerGrammar(input);
        AuthenticationParserGrammar parser = new AuthenticationParserGrammar(new CommonTokenStream(lexer));

        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return func.apply(parser);
    }

    private Credential parseCredential(CredentialSourceCode code, Function<CredentialParserGrammar, Credential> func)
    {
        CharStream input = CharStreams.fromString(code.getCode());
        ParserErrorListener errorListener = new ParserErrorListener(code.getWalkerSourceInformation());
        CredentialLexerGrammar lexer = new CredentialLexerGrammar(input);
        CredentialParserGrammar parser = new CredentialParserGrammar(new CommonTokenStream(lexer));

        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);

        return func.apply(parser);
    }

    private static SourceCodeParserInfo getAuthenticationParserInfo(AuthenticationSourceCode authenticationSourceCode)
    {
        CharStream input = CharStreams.fromString(authenticationSourceCode.code);
        ParserErrorListener errorListener = new ParserErrorListener(authenticationSourceCode.walkerSourceInformation);
        AuthenticationLexerGrammar lexer = new AuthenticationLexerGrammar(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        AuthenticationParserGrammar parser = new AuthenticationParserGrammar(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return new SourceCodeParserInfo(authenticationSourceCode.code, input, authenticationSourceCode.sourceInformation, authenticationSourceCode.walkerSourceInformation, lexer, parser, null);
    }

    private static SourceCodeParserInfo getCredentialParserInfo(CredentialSourceCode credentialSourceCode)
    {
        CharStream input = CharStreams.fromString(credentialSourceCode.code);
        ParserErrorListener errorListener = new ParserErrorListener(credentialSourceCode.walkerSourceInformation);
        CredentialLexerGrammar lexer = new CredentialLexerGrammar(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        CredentialParserGrammar parser = new CredentialParserGrammar(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        return new SourceCodeParserInfo(credentialSourceCode.code, input, credentialSourceCode.sourceInformation, credentialSourceCode.walkerSourceInformation, lexer, parser, null);
    }
}
