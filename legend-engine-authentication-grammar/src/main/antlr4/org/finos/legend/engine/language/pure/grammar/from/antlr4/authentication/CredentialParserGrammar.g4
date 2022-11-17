parser grammar CredentialParserGrammar;

import CoreParserGrammar;

options
{
    tokenVocab = CredentialLexerGrammar;
}

identifier:                      VALID_STRING
;



vaultCredential:          VAULT_CREDENTIAL
                                            PAREN_OPEN
                                                   (vaultReference)*
                                            PAREN_CLOSE
;

vaultReference:             VAULT_REFERENCE COLON STRING SEMI_COLON
;


oauthCredential:                           OAUTH_CREDENTIAL
                                           PAREN_OPEN
                                            (
                                                   grantType
                                                   | clientId
                                                   | clientSecret
                                                   |  authServerUrl
                                            )*
                                            PAREN_CLOSE
;

grantType:                                 GRANT_TYPE COLON STRING SEMI_COLON
;

clientId:                                   CLIENT_ID COLON STRING SEMI_COLON
;

clientSecret:                               CLIENT_SECRET_VAULT_REFERENCE COLON STRING SEMI_COLON
;

authServerUrl:                              AUTH_SERVER_URL COLON STRING SEMI_COLON
;