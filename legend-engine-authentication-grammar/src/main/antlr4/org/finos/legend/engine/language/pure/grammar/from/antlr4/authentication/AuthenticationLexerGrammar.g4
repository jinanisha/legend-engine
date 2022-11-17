lexer grammar AuthenticationLexerGrammar;

import CoreLexerGrammar;

OAUTH_AUTHENTICATION:                                       'OauthAuthentication';
TOKEN:                                                      'token';

BASIC_AUTHENTICATION:                                       'UsernamePasswordAuthentication';
USERNAME:                                                   'username';
PASSWORD:                                                   'password';

API_KEY_AUTHENTICATION:                                     'ApiKeyAuthentication';
VALUE:                                                      'value';

BRACKET_OPEN:                                               '[';
BRACKET_CLOSE:                                              ']';

// -------------------------------------- ISLAND ---------------------------------------
PAREN_OPEN:                    '(' -> pushMode (CREDENTIAL_ISLAND_MODE);


mode CREDENTIAL_ISLAND_MODE;
CREDENTIAL_ISLAND_OPEN: '(' -> pushMode (CREDENTIAL_ISLAND_MODE);
CREDENTIAL_ISLAND_CLOSE: ')' -> popMode;
CREDENTIAL_CONTENT: (~[()])+;