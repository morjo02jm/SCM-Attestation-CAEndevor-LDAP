package endevorrepldap;


//import com.ca.harvest.jhsdk.hutils.*;

import java.sql.*;

//import java.util.Date;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;

import java.util.*;

//import java.io.*;
//import java.net.URL;
//import java.nio.charset.Charset;

//import javax.naming.*;
//import javax.naming.directory.*;

//import org.eclipse.core.runtime.*;

//import junit.framework.*;


import commonldap.CommonLdap;
//import commonldap.JCaData;
import commonldap.JCaContainer;

public class EndevorRepLdap {
	private static int iReturnCode = 0;
	
	void EndevorRepLdap() {
		
	}
	
	private static void readDBToRepoContainer(CommonLdap frame,
			                                  JCaContainer cRepoInfo, 
			                                  String sDB2Password) {
		PreparedStatement pstmt = null; 
		String sqlStmt;
		int iIndex = 0;
		ResultSet rSet;
		
		String sqlError = "DB2. Unable to execute query.";
		String sJDBC = "jdbc:db2://CA31:5122/DA0GPTIB:retrieveMessagesFromServerOnGetMessage=true;emulateParameterMetaDataForZCalls=1;;user=ATTAUT1;password="+sDB2Password+";";
		
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			Connection conn = DriverManager.getConnection(sJDBC);
	
			sqlError = "DB2. Error reading Endevor records from CIA database.";
			sqlStmt = frame.readTextResource("EndeavorDBQuery", "", "", "");
			pstmt=conn.prepareStatement(sqlStmt); 
			rSet = pstmt.executeQuery();
			
			while (rSet.next()) {
				String sAuthType = rSet.getString("AUTHTYPE");
				String sRoleID = (sAuthType.equalsIgnoreCase("R"))? rSet.getString("ROLEID"): "";
				cRepoInfo.setString("APP",          rSet.getString("APP"),          iIndex);
				cRepoInfo.setString("APP_INSTANCE", rSet.getString("APP_INSTANCE"), iIndex);
				cRepoInfo.setString("PRODUCT",      rSet.getString("PRODUCT"),      iIndex);
				cRepoInfo.setString("AUTHTYPE",     sAuthType,                      iIndex);
				cRepoInfo.setString("ROLEID",       sRoleID,                        iIndex);
				cRepoInfo.setString("RESMASK",      rSet.getString("RESMASK"),      iIndex);
				cRepoInfo.setString("MANAGER",      rSet.getString("MANAGER"),      iIndex);
				cRepoInfo.setString("USERID",       rSet.getString("USERID"),       iIndex);
				cRepoInfo.setString("ACC_READ",     rSet.getString("acc_read"),     iIndex);
				cRepoInfo.setString("ACC_WRITE",    rSet.getString("acc_write"),    iIndex);
				cRepoInfo.setString("ACC_UPDATE",   rSet.getString("acc_update"),   iIndex);
				cRepoInfo.setString("ACC_ALL",      rSet.getString("acc_all"),      iIndex);
				cRepoInfo.setString("ACC_NONE",     rSet.getString("acc_none"),     iIndex);
				cRepoInfo.setString("ACC_CREATE",   rSet.getString("acc_create"),   iIndex);
				cRepoInfo.setString("ACC_FETCH",    rSet.getString("acc_fetch"),    iIndex);
				cRepoInfo.setString("ACC_SCRATCH",  rSet.getString("acc_scratch"),  iIndex);
				cRepoInfo.setString("ACC_CONTROL",  rSet.getString("acc_control"),  iIndex);
				cRepoInfo.setString("ACC_INQUIRE",  rSet.getString("acc_inquire"),  iIndex);
				cRepoInfo.setString("ACC_SET",      rSet.getString("acc_set"),      iIndex);			
				iIndex++;
			} // loop over record sets
			
			frame.printLog(">>>:"+iIndex+" Records Read From DB2.");

		} catch (ClassNotFoundException e) {
			iReturnCode = 1;
		    System.err.println(sqlError);
		    System.err.println(e);			
		    System.exit(iReturnCode);
		} catch (SQLException e) {     
			iReturnCode = 2;
		    System.err.println(sqlError);
		    System.err.println(e);			
		    System.exit(iReturnCode);
		}	
	}

	public static void main(String[] args) {
		int iParms = args.length;
		int iReturnCode = 0;
		String sOutputFile = "";
		String sBCC = "";
		String sLogPath = "endevorrepldap.log";
		String sDB2Password = "";
		String sIMAGPassword = "";
		
		// check parameters
		for (int i = 0; i < iParms; i++)
		{					
			if (args[i].compareToIgnoreCase("-outputfile") == 0 )
			{
				sOutputFile = args[++i];
			}			
			else if (args[i].compareToIgnoreCase("-bcc") == 0 )
			{
				sBCC = args[++i];
			}			
			else if (args[i].compareToIgnoreCase("-log") == 0 )
			{
				sLogPath = args[++i];
			}	
			else {
				System.out.println("Argument: "+args[i]);
				System.out.println("Usage: endevorrepldap \n"+
				                   "                     -outputfile textfile \n"+
				                   "                     [-bcc emailadress] [-log textfile] [-h |-?]");
				System.out.println(" -inputfile option specifies the attestation input file to validate (tsv)");
				System.out.println(" -outputfile option specifies the attestation output file (csv)");
				System.out.println(" -bcc option specifies an email address to bcc on notifications sent to users");
				System.out.println(" -log option specifies location log file.");
				System.exit(iReturnCode);
			}
		} // end for
		
		JCaContainer cLDAP = new JCaContainer();
		CommonLdap frame = new CommonLdap("endeavorrepldap",
        		                           sLogPath,
        		                           "Team-GIS-ToolsSolutions-Global@ca.com",
        		                           cLDAP);
			
		try {	
			Map<String, String> environ = System.getenv();
	        for (String envName : environ.keySet()) {
	        	if (envName.equalsIgnoreCase("ENDEAVOR_DB_PASSWORD"))        
	        		sDB2Password = frame.AESDecrypt(environ.get(envName));
	        	if (envName.equalsIgnoreCase("IMAG_DB_PASSWORD"))        
	        		sIMAGPassword = frame.AESDecrypt(environ.get(envName));
	        }
			// Write out processed records to database
			JCaContainer cRepoInfo = new JCaContainer();
			readDBToRepoContainer(frame, cRepoInfo, sDB2Password);
	     } catch (Exception e) {
	    	 
	     }	// try/catch blocks         
	}
}
