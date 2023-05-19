CREATE OR REPLACE PROCEDURE APP_OPERATIONS.GRANT_DATABASE_ROLES_TO_WRAPPERS(vendorDataSet varchar, executionMode varchar)
RETURNS VARCHAR
LANGUAGE JAVASCRIPT
EXECUTE AS OWNER
AS $$
 try {
             var executionMode = EXECUTIONMODE;
             var vendorDataSetName = VENDORDATASET;
             var allErrorMessages = 'Execution Result: Done - Messages: ';
             var dboRole = 'NATIVE_APP_OWNER';
             var developerRole = 'SUMMIT_NATIVE_APP_DEVELOPER';

             if (executionMode == 'test')
             {
                mdmDb = 'SUMMIT_MDM_DATA';
}
             else
             {
                mdmDb = 'MDM_DATA';
}

            //We need to run a separate loop for Grants - because copy grants doesnt work for functions
            if (vendorDataSetName == "ALL")
                    {
                     var objectQuerySQL = 'select distinct business_object_fqn, producer_db_shared, producer_schema_name_shared, external_share_role from '+mdmDb+ '.mdm_schema.ventitlements ;';
}
                else
                    {
                    var objectQuerySQL = 'select distinct business_object_fqn, producer_db_shared, producer_schema_name_shared, external_share_role from ' +mdmDb+ '.mdm_schema.ventitlements where business_object_fqn like \'' + vendorDataSetName + '%\';';
}

            var stmt = snowflake.createStatement( {sqlText: objectQuerySQL } );
            var resultSet = stmt.execute();

            while(resultSet.next()){
                try{

                    businessObjectFqn = resultSet.getColumnValue(1);
                    producerDbShared = resultSet.getColumnValue(2);
                    producerSchemaNameShared = resultSet.getColumnValue(3);
                    externalShareRole = resultSet.getColumnValue(4);

                    wrapper2FunctionName = producerDbShared + '.' + producerSchemaNameShared + '.SECURE_FX_' + businessObjectFqn;

                    var grantFunctionSQL = 'Grant usage  on function ' + wrapper2FunctionName + '() TO DATABASE ROLE ' + externalShareRole + ';';
                    var stmtgrantFunction = snowflake.createStatement( {sqlText: grantFunctionSQL });
                    stmtgrantFunction.execute();
}
                catch (e) {
                    allErrorMessages += e.message;
}
            }


      }
      catch (err)  {
      result =  "Failed: Code: " + err.code + "\\n  State: " + err.state;
      result += "\\n  Message: " + err.message;
      result += "\\nStack Trace:\\n" + err.stackTraceTxt;
      result += "\\nbusinessObjectFqn:\\n" + businessObjectFqn;
return result;
}

      return allErrorMessages;

$$;
