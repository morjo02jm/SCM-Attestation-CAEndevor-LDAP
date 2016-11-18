package endevorrepldap;


//import com.ca.harvest.jhsdk.hutils.*;

import java.sql.*;

import java.util.*;

import java.io.*;

import commonldap.CommonLdap;
import commonldap.JCaContainer;

public class EndevorRepLdap {
	private static int iReturnCode = 0;
	private static CommonLdap frame;
		
	private static void readDBToRepoContainer(JCaContainer cRepoInfo, 
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
			//sqlStmt = frame.readTextResource("EndevorDBQuery.txt", "= 'VIEW'", "", "");
			sqlStmt = frame.readTextResource("EndevorDBQuery.txt", "IS NOT NULL", "", "");
			pstmt=conn.prepareStatement(sqlStmt); 
			rSet = pstmt.executeQuery();
			
			while (rSet.next()) {
				String sAuthType = rSet.getString("AUTHTYPE").trim();
				String sRoleID = (sAuthType.equalsIgnoreCase("R"))? rSet.getString("ROLEID"): "";
				cRepoInfo.setString("APP",           rSet.getString("APP").trim(),                  iIndex);
				cRepoInfo.setString("APP_INSTANCE",  rSet.getString("APP_INSTANCE").trim(),         iIndex);
				cRepoInfo.setString("PRODUCT",       rSet.getString("PRODUCT").trim(),              iIndex);
				cRepoInfo.setString("AUTHTYPE",      sAuthType,                                     iIndex);
				cRepoInfo.setString("ROLEID",        sRoleID.trim(),                                iIndex);
				cRepoInfo.setString("RESMASK",       rSet.getString("RESMASK").trim(),              iIndex);
				cRepoInfo.setString("ADMINISTRATOR", rSet.getString("ADMINISTRATOR").toLowerCase().trim(),iIndex);
				cRepoInfo.setString("RESOURCE_OWNER",rSet.getString("RESOURCE_OWNER").toLowerCase().trim(),iIndex);
				cRepoInfo.setString("USERID",        rSet.getString("USERID").toLowerCase().trim(), iIndex);
				cRepoInfo.setString("ACC_READ",      rSet.getString("acc_read").trim(),             iIndex);
				cRepoInfo.setString("ACC_WRITE",     rSet.getString("acc_write").trim(),            iIndex);
				cRepoInfo.setString("ACC_UPDATE",    rSet.getString("acc_update").trim(),           iIndex);
				cRepoInfo.setString("ACC_ALL",       rSet.getString("acc_all").trim(),              iIndex);
				cRepoInfo.setString("ACC_NONE",      rSet.getString("acc_none").trim(),             iIndex);
				cRepoInfo.setString("ACC_CREATE",    rSet.getString("acc_create").trim(),           iIndex);
				cRepoInfo.setString("ACC_FETCH",     rSet.getString("acc_fetch").trim(),            iIndex);
				cRepoInfo.setString("ACC_SCRATCH",   rSet.getString("acc_scratch").trim(),          iIndex);
				cRepoInfo.setString("ACC_CONTROL",   rSet.getString("acc_control").trim(),          iIndex);
				cRepoInfo.setString("ACC_INQUIRE",   rSet.getString("acc_inquire").trim(),          iIndex);
				cRepoInfo.setString("ACC_SET",       rSet.getString("acc_set").trim(),              iIndex);			
				iIndex++;
			} // loop over record sets
			
			frame.printLog(">>>:"+iIndex+" Records Read From DB2.");

		} catch (ClassNotFoundException e) {
			iReturnCode = 101;
		    System.err.println(sqlError);
		    System.err.println(e);			
		    System.exit(iReturnCode);
		} catch (SQLException e) {     
			iReturnCode = 102;
		    System.err.println(sqlError);
		    System.err.println(e);			
		    System.exit(iReturnCode);
		}	
	}
	
	private static void writeCSVFileFromListGeneric( JCaContainer cList, String sOutputFileName, char sep)
	{
		File fout = new File(sOutputFileName);
		
		try {
			FileOutputStream fos = new FileOutputStream(fout);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
			String[] keylist = cList.getKeyList();
			int[] ord = new int[] {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19};
			
			String line = "";
			for (int i=0; i<keylist.length; i++) {
				if (!line.isEmpty()) 
					line += sep;
				line += keylist[ord[i]];
			}
			bw.write(line);
			bw.newLine();
			
			for (int i=0; i < cList.getKeyElementCount(keylist[0]); i++) {
				if (!cList.getString("APP", i).isEmpty()) 
				{
					line = "";
					for (int j=0; j<keylist.length; j++) {
						if (!line.isEmpty())
							line += sep;
						line += cList.getString(keylist[ord[j]], i);
					}
					bw.write(line);
					bw.newLine();
				}
			}
		 
			bw.close();
		} catch (FileNotFoundException e) {             
			iReturnCode = 201;
		    System.err.println(e);			
		    System.exit(iReturnCode);
		} catch (IOException e) {             
			iReturnCode = 202;
		    System.err.println(e);			
		    System.exit(iReturnCode);
		}
	}	
	
	private static void writeDBFromRepoContainer(JCaContainer cRepoInfo, String sImagDBPassword) {
		PreparedStatement pstmt = null; 
		String sqlStmt;
		int iResult;
		
		String sqlError = "";
		String sJDBC = "jdbc:sqlserver://AWS-UQAPA6ZZ:1433;databaseName=GMQARITCGISTOOLS;user=gm_tools_user;password="+sImagDBPassword+";";
		String sqlStmt0 = "insert into GITHUB_REVIEW "+
	              "( Application, ApplicationLocation, EntitlementOwner1, EntitlementOwner2, EntitlementName, EntitlementAttributes, ContactEmail, User_ID, UserAttributes) values ";
		
		String sEntitlement2 = "";
		String sContactEmail = "";
		String sEntitlementAttrs = "";
		String sUserAttrs = "";
		String sValues = "";
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Connection conn = DriverManager.getConnection(sJDBC);
	
			String sApp = "CA Endevor";

			sqlError = "DB. Error deleting previous records.";
			sqlStmt = "delete from GITHUB_REVIEW where Application='"+ sApp +"'";
			pstmt=conn.prepareStatement(sqlStmt);  
			iResult = pstmt.executeUpdate();
			if (iResult > 0) 
				frame.printLog(">>>:"+iResult+" Previous IMAG Feed Records Deleted.");
			
			sqlError = "DB. Error inserting record.";
			int nRecordsWritten = 0;
			int nBlock = 100;
			
			for (int iIndex=0,nRecords=0; iIndex<cRepoInfo.getKeyElementCount("APP"); iIndex++) {
				if (!cRepoInfo.getString("APP", iIndex).isEmpty()) { 
					if (nRecords%nBlock == 0)
						sqlStmt = sqlStmt0;
					else 
						sqlStmt += " , ";
					
					sEntitlement2 = (cRepoInfo.getString("AUTHTYPE", iIndex).equalsIgnoreCase("U"))? "User" : "Role:"+cRepoInfo.getString("ROLEID", iIndex);
					sContactEmail = cRepoInfo.getString("ADMINISTRATOR", iIndex) + "@ca.com";
					sEntitlementAttrs = "resowner:"+ cRepoInfo.getString("RESOURCE_OWNER", iIndex);
					sUserAttrs = "read=" + (cRepoInfo.getString("ACC_READ", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "write=" + (cRepoInfo.getString("ACC_WRITE", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "update=" + (cRepoInfo.getString("ACC_UPDATE", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "all=" + (cRepoInfo.getString("ACC_ALL", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "none=" + (cRepoInfo.getString("ACC_READ", iIndex).equalsIgnoreCase("A")? "Y" : "N") ;
					
					sValues = "('" + cRepoInfo.getString("APP", iIndex) + "',"+
							  "'"   + cRepoInfo.getString("APP_INSTANCE", iIndex) + "',"+
							  "'"   + cRepoInfo.getString("PRODUCT", iIndex) + "',"+
							  "'"   + sEntitlement2 + "',"+
							  "'"   + cRepoInfo.getString("RESMASK", iIndex) + "',"+
							  "'"   + sEntitlementAttrs + "',"+
							  "'"   + sContactEmail + "',"+
							  "'"   + cRepoInfo.getString("USERID", iIndex) + "',"+
							  "'"   + sUserAttrs + "')";
					
				    sqlStmt += sValues;
				    
				    if (nRecords%nBlock == (nBlock-1)) {
						pstmt=conn.prepareStatement(sqlStmt);  
						iResult = pstmt.executeUpdate();
						if (iResult > 0) 
							nRecordsWritten += iResult;	
						sqlStmt = "";
				    }
					nRecords++;	
				}
			} // loop over records
			
			if (!sqlStmt.isEmpty()) {
				pstmt=conn.prepareStatement(sqlStmt);  
				iResult = pstmt.executeUpdate();
				if (iResult > 0) 
					nRecordsWritten += iResult;					
			}
			frame.printLog(">>>:"+nRecordsWritten+" Inserted Records Made to DB.");
		
		} catch (ClassNotFoundException e) {
			iReturnCode = 301;
		    System.err.println(sqlError);
		    System.err.println(e);			
		    System.exit(iReturnCode);
		} catch (SQLException e) {     
			iReturnCode = 302;
		    System.err.println(sqlError);
		    System.err.println(e);			
		    System.exit(iReturnCode);
		}
	} // writeDBFromRepoContainer		
	
	
	
	
	
	
	public static void main(String[] args) {
		int iParms = args.length;
		int iReturnCode = 0;
		String sOutputFile = "";
		String sBCC = "";
		String sLogPath = "endevorrepldap.log";
		String sDB2Password = "";
		String sImagDBPassword = "";	
		String sProblems = "";
		
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
		frame = new CommonLdap("endeavorrepldap",
        		                sLogPath,
        		                sBCC,
        		                cLDAP);
			
		try {	
			Map<String, String> environ = System.getenv();
	        for (String envName : environ.keySet()) {
	        	if (envName.equalsIgnoreCase("ENDEAVOR_DB_PASSWORD"))        
	        		sDB2Password = frame.AESDecrypt(environ.get(envName));
	        	if (envName.equalsIgnoreCase("IMAG_DB_PASSWORD"))        
	        		sImagDBPassword = frame.AESDecrypt(environ.get(envName));
	        }
			// Write out processed records to database
			JCaContainer cRepoInfo = new JCaContainer();
			readDBToRepoContainer(cRepoInfo, sDB2Password);
			
			// a. Loop over records collapsing APP_INSTANCEs
			String sEntitlementLast = "";
			String sInstanceLast = "";
			int iIndexLast = -1;
			
			for (int iIndex=0; iIndex<cRepoInfo.getKeyElementCount("APP"); iIndex++) {
				if (!cRepoInfo.getString("APP", iIndex).isEmpty()) {
					String sInstance = cRepoInfo.getString("APP_INSTANCE", iIndex);
					String sEntitlement = cRepoInfo.getString("PRODUCT", iIndex)+"/"+
										  cRepoInfo.getString("AUTHTYPE", iIndex)+"/"+
				                          cRepoInfo.getString("ROLEID", iIndex)+"/"+
							              cRepoInfo.getString("RESMASK", iIndex)+"/"+
							              cRepoInfo.getString("USERID", iIndex);
					
					if ( sEntitlement.equalsIgnoreCase(sEntitlementLast) && 
					    !sInstance.equalsIgnoreCase(sInstanceLast)) {
						cRepoInfo.setString("APP", "", iIndex);
						String sNewInstance = sInstanceLast+';'+sInstance;
						cRepoInfo.setString("APP_INSTANCE", sNewInstance, iIndexLast);
					}
					sEntitlementLast = sEntitlement;
					sInstanceLast = sInstance;
					iIndexLast = iIndex;
				}
			}
			
			// Write out processed repository in organization file
			if (!sOutputFile.isEmpty()) {
				writeCSVFileFromListGeneric(cRepoInfo, sOutputFile, ',');					
			}
			
			// Write out processed records to database
			writeDBFromRepoContainer(cRepoInfo, sImagDBPassword);
			
			if (!sProblems.isEmpty()) {
				sProblems+="</ul>\n";
				String email = "faudo01@ca.com";
				String sSubject, sScope;
				
				sSubject = "Notification of Problematic CA Endevor Contacts";
				sScope = "SourceMinder DB2 Database";
				
		        String bodyText = frame.readTextResource("Notification_of_Noncompliant_GitHub_Contacts.txt", sScope, sProblems, sProblems);								        								          
		        frame.sendEmailNotification(email, sSubject, bodyText, true);
			} // had some notifications
			
	     } catch (Exception e) {
				iReturnCode = 1;
			    System.err.println(e);			
			    System.exit(iReturnCode);		    	    	 
	     }	// try/catch blocks         
	}
}
