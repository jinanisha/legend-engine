parser grammar CredentialParserGrammar;

import CoreParserGrammar;

options
{
    tokenVocab = CredentialLexerGrammar;
}

identifier:                      VALID_STRING
;



vaultCredential:          VAULT_CREDENTIAL
                                            BRACE_OPEN
                                                   (vaultReference)*
                                            BRACE_CLOSE
;

vaultReference:             VAULT_REFERENCE COLON STRING SEMI_COLON
;