lexer grammar AuthenticationLexerGrammar;

import CoreLexerGrammar;

OAUTH_AUTHENTICATION:                                       'OauthAuthentication';
GRANT_TYPE:                                                 'grantType';
CLIENT_ID:                                                  'clientId';
CLIENT_SECRET_VAULT_REFERENCE:                              'clientSecretVaultReference';
AUTH_SERVER_URL:                                            'authorizationServerUrl';
TOKEN:                                                      'token';
OAUTH_CREDENTIAL:                                           'OauthCredential';

BASIC_AUTHENTICATION:                                       'UsernamePasswordAuthentication';
USERNAME:                                                   'username';
PASSWORD:                                                   'password';

API_KEY_AUTHENTICATION:                                     'ApiKeyAuthentication';
VALUE:                                                      'value';

BRACKET_OPEN:                                               '[';
BRACKET_CLOSE:                                              ']';

// -------------------------------------- ISLAND ---------------------------------------
BRACE_OPEN:                    '{' -> pushMode (CREDENTIAL_ISLAND_MODE);


mode CREDENTIAL_ISLAND_MODE;
CREDENTIAL_ISLAND_OPEN: '{' -> pushMode (CREDENTIAL_ISLAND_MODE);
CREDENTIAL_ISLAND_CLOSE: '}' -> popMode;
CREDENTIAL_CONTENT: (~[{}])+;