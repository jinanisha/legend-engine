CREATE OR REPLACE PROCEDURE APP_OPERATIONS.INSTALL_BUSINESS_OBJECTS(CONSUMER_SCHEMA_NAME VARCHAR, CONSUMER_ROLE_NAME VARCHAR)
RETURNS VARCHAR NOT NULL
LANGUAGE JAVASCRIPT
AS
$$
    var consumerSchemaName = CONSUMER_SCHEMA_NAME;
    var currentRoleName = CONSUMER_ROLE_NAME;
    var sourceObjectFqn = 'N/A';
    var sqlquery = 'select current_account()';
    var tmp_rs = snowflake.execute({sqlText: sqlquery});
    tmp_rs.next();
    var currentAccountName = tmp_rs.getColumnValue(1);

    var objectQuerySQL = 'select distinct business_object_fqn , bu_name, bu_snow_account, producer_schema_name_shared from APP_OPERATIONS.VENTITLEMENTS where BU_SNOW_ACCOUNT= ' + '\'' + currentAccountName + '\'';

    var stmt = snowflake.createStatement( {sqlText: objectQuerySQL } );
    var resultSet = stmt.execute();

    while(resultSet.next()){


        var businessObjectFqn = resultSet.getColumnValue(1);
        var buName = resultSet.getColumnValue(2);
        var buSnowAccount = resultSet.getColumnValue(3);
        var producerSchemaNameShared = resultSet.getColumnValue(4);

        var wrapperFunctionName = consumerSchemaName + '.' + businessObjectFqn ;

//Get the schema first for the secure funtion
        var producerSecureFunction = producerSchemaNameShared + '.' + 'SECURE_FX_' + businessObjectFqn;
        var tempTableName = consumerSchemaName + '.' + 'TEMP_' + businessObjectFqn;
        tempTableSQL = 'CREATE OR REPLACE TABLE ' + tempTableName + ' as select * from table(' + producerSecureFunction + '());'

        var stmtCreateTempTable = snowflake.createStatement( {sqlText: tempTableSQL } );
        stmtCreateTempTable.execute();

        var grantSQL = 'GRANT SELECT ON TABLE ' + tempTableName + ' TO DATABASE ROLE ' + currentRoleName;

        var stmtgrant = snowflake.createStatement( {sqlText: grantSQL });
        stmtgrant.execute();

        var descSQL = 'DESC table ' + tempTableName;
        var stmtDesc = snowflake.createStatement( {sqlText: descSQL });
        var result = stmtDesc.execute();

        var columnName = '';
        var columnType = '';
        var columnNameAndType ='';

        while (result.next())  {
             if (columnNameAndType == '')
              {
                columnNameAndType = result.getColumnValue(1) + ' ' + result.getColumnValue(2);
}
              else
               {
                columnNameAndType = columnNameAndType + ',' + result.getColumnValue(1)+ ' ' + result.getColumnValue(2);
}

              }

         consumerWrapperName = consumerSchemaName + '.' + businessObjectFqn;
         consumerWrapperSQL = 'CREATE OR REPLACE SECURE FUNCTION ' + consumerWrapperName + '() RETURNS TABLE (' + columnNameAndType + ') as \$\$select distinct a.* from table(' + producerSecureFunction + '()) a JOIN APP_OPERATIONS.VENTITLEMENTS b on b.BU_SNOW_ACCOUNT = ' + '\'' + currentAccountName + '\'' + ' and b.ALLOWED_IND = ' + '\'Y\' and b.BUSINESS_OBJECT_FQN = ' + '\'' + businessObjectFqn + '\'\$\$';

         var createConsumerWrapper = snowflake.createStatement( {sqlText: consumerWrapperSQL } );
         createConsumerWrapper.execute();

         var grantFunctionSQL = 'GRANT USAGE ON FUNCTION ' + consumerWrapperName + '() TO DATABASE ROLE ' + currentRoleName;
         var stmtgrantFunction = snowflake.createStatement( {sqlText: grantFunctionSQL });
         stmtgrantFunction.execute();

//now drop the temporary table created for schema
         snowflake.execute( {sqlText: 'DROP TABLE IF EXISTS ' + tempTableName});

}
    return 'Done';
$$;
