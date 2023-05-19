CREATE OR REPLACE PROCEDURE APP_OPERATIONS.CREATE_WRAPPERS(vendorDataSet varchar, executionMode varchar)
RETURNS VARCHAR
LANGUAGE JAVASCRIPT
EXECUTE AS OWNER
AS $$
  /* Description : This is the stored procedure which create will create wrapper 1 and 2 - wrapper 2 will be shared in the installer
                   Current wrapper creation mechanism doesnt support parameterized queries - this will be a future enhancement

     Date: 02-09-2023
     Author: ghonil
   */

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

             if (vendorDataSetName == "ALL")
                    {
                     var objectQuerySQL = 'select distinct a.business_object_fqn, a.producer_db_shared, a.producer_schema_name_referrence, a.producer_schema_name_shared, a.bu_name, a.bu_snow_account, a.external_share_role, b.sql_fragment from ' +mdmDb+ '.mdm_schema.ventitlements a left join ' +mdmDb+ '.legend_governance.business_objects b on a.business_object_fqn = b.business_object_fqn;';
}
                else
                    {
                    var objectQuerySQL = 'select distinct a.business_object_fqn, a.producer_db_shared, a.producer_schema_name_referrence, a.producer_schema_name_shared, a.bu_name, a.bu_snow_account, a.external_share_role, b.sql_fragment from '+mdmDb+ '.mdm_schema.ventitlements a left join ' +mdmDb+ '.legend_governance.business_objects b on a.business_object_fqn = b.business_object_fqn  where a.business_object_fqn like \'' + vendorDataSetName + '%\';';
}

            var stmt = snowflake.createStatement( {sqlText: objectQuerySQL } );
            var resultSet = stmt.execute();

            while(resultSet.next())
            {

                try{

                var businessObjectFqn = resultSet.getColumnValue(1);

                var producer_db_shared = resultSet.getColumnValue(2);
                var producer_schema_name_ref = resultSet.getColumnValue(3);
                var producer_schema_name_shared = resultSet.getColumnValue(4);

                var buName = resultSet.getColumnValue(5);
                var buSnowAccount = resultSet.getColumnValue(6);

//var viewN = businessObjectFqn.split(".");
                var viewName = businessObjectFqn;

//THIS IS WHERE WE GET THE LEGEND SQL STATEMENT
                  var legend_sql_fragment = resultSet.getColumnValue(8);

//Now we will re-engineer this SQL to get rid of Humanized names and keep pristine Vendor Names

                  var legend_sql_main_part = legend_sql_fragment.split('select ')[1].split('from ')[0];

                  var source_view_name = legend_sql_fragment.split('select ')[1].split('from ')[1].split('as ')[0].trim();

                  var legend_sql_fields_array = legend_sql_main_part.split(',');

//loop this legend array to re-engineer the SQL
                  let i = 0;
                  var f_name_array = [];
                  while (i < legend_sql_fields_array.length) {

                      f_name = legend_sql_fields_array[i];
                      f_name_extracted = f_name.split(' as ')[0].split('.')[1];

                      if(!f_name_extracted.includes('crux')){
                            f_name_extracted = f_name_extracted.replaceAll('"', '');
}

                      if(!f_name_array.includes(f_name_extracted)){
                          f_name_array.push(f_name_extracted);
}

                      i++;
}

                  var field_string = f_name_array.join(',');

                  var modified_legend_sql_fragment = 'select ' + field_string + ' from ' + source_view_name;

//create a temporary view to get all fields and their data types
                  tempViewName = producer_db_shared + '.' + producer_schema_name_ref  + '.' +'TEMP_VIEW_' + businessObjectFqn;
                  tempViewSQL = 'CREATE OR REPLACE VIEW ' + tempViewName + ' as ' + modified_legend_sql_fragment + ';';

                  var tempResultSet = (snowflake.createStatement({sqlText: tempViewSQL})).execute();

//Now get the schema of the view - because we have declare structure in Wrapper 1 and 2
                  var describe_statement_sql = "DESCRIBE VIEW " + tempViewName ;

                  var describeResultSet = (snowflake.createStatement({sqlText: describe_statement_sql})).execute();

                  var field_struc_string = "";
                  var field_data_struc_string = "";

//LOOP THROUGH THE DESCRIBE VIEW RESULTS TO CONSTRUCT THE STRING
                  while(describeResultSet.next()){

                    var field_name = describeResultSet.getColumnValue(1);

                    field_struc_string += field_name + ",";
                    field_data_struc_string += field_name + " " + describeResultSet.getColumnValue(2) + ",";
}

                  //remove comma from last character
                  //NOW OUR FIELD AND DATA TYPE STRUCTURE IS READY FOR WRAPPER
                  field_struc_string = field_struc_string.slice(0,-1);
                  field_data_struc_string = field_data_struc_string.slice(0,-1);

                  wrapper1FunctionName = producer_db_shared + '.' + producer_schema_name_ref + '.' + 'FX_' + businessObjectFqn;

//construct the wrapper 1 sql
                  var wrapper1_sql = 'CREATE OR REPLACE FUNCTION ' + wrapper1FunctionName + '() RETURNS TABLE (' + field_data_struc_string + ') as  \$\$' + modified_legend_sql_fragment + '\$\$;';

//create wrapper1
                  var createWrapper1 = snowflake.createStatement( {sqlText: wrapper1_sql } );
                  createWrapper1.execute();

//wrapper1 Read Access to developer role
                  var grantWrapper1SQL = 'Grant usage  on function ' + wrapper1FunctionName + '() TO ROLE ' + developerRole + ';';
                  var stmtgrantFunctionWrapper1 = snowflake.createStatement( {sqlText: grantWrapper1SQL });
                  stmtgrantFunctionWrapper1.execute();


//drop the temp view created for describe
                  dropTempViewSQL = 'DROP VIEW ' + tempViewName;
                  dropTempViewExecute = (snowflake.createStatement( {sqlText: dropTempViewSQL } )).execute();

//Now construct wrapper 2 SQL
                  wrapper2FunctionName = producer_db_shared + '.' + producer_schema_name_shared + '.SECURE_FX_' + businessObjectFqn;
                  wrapper2_sql = 'CREATE OR REPLACE SECURE FUNCTION ' + wrapper2FunctionName + '() RETURNS TABLE(' + field_data_struc_string + ') as \$\$select ' + field_struc_string + ' from table(' + wrapper1FunctionName + '())\$\$';


                  createWrapper2 = snowflake.createStatement( {sqlText: wrapper2_sql } );
                  createWrapper2.execute();

}
                catch (e) {
                    allErrorMessages += e.message;
}
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

                    var grantFunctionSQL = 'Grant usage  on function ' + wrapper2FunctionName + '() TO ROLE ' + dboRole + ' WITH GRANT OPTION;';
                    var stmtgrantFunction = snowflake.createStatement( {sqlText: grantFunctionSQL });
                    stmtgrantFunction.execute();

                    var grantFunctionSQL = 'Grant usage  on function ' + wrapper2FunctionName + '() TO ROLE ' + developerRole + ';';
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
