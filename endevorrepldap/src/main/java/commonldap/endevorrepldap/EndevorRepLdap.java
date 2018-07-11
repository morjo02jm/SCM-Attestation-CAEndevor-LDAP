package commonldap.endevorrepldap;

import java.sql.*;
import java.util.*;
import java.io.*;

import commonldap.commonldap.CommonLdap;
import commonldap.commonldap.JCaContainer;
import commonldap.commonldap.SDTicket;

public class EndevorRepLdap {
	private static int iReturnCode = 0;
	private static CommonLdap frame;
	private static String sProblems = "";
	private static List<String> ticketProblems = new ArrayList<String>();

// Repository container headings	
	private static String sTagProject = "PRODUCT";
	private static String sTagContact = "CONTACT";
	private static String sTagApp     = "APP";
	private static String sTagProduct = "PRODUCTNAME";
	
	// LDAP columns
	private static String sTagPmfkey  = "sAMAccountName";
	
	// Notification
	static String tagUL = "<ul> ";
	
	// JDBC
	private static String sDB2 = "jdbc:db2://usilca31.ca.com:5220/PP0ADB2"; //"jdbc:db2://CA31:5122/DA0GPTIB"; //  
	
	EndevorRepLdap() {
		// Leaving empty		
	}
			
	private static void readDBToRepoContainer(JCaContainer cRepoInfo, 
			                                  String sDB2Password) {
		PreparedStatement pstmt = null; 
		String sqlStmt;
		int iIndex = 0;
		ResultSet rSet;
		
		String sqlError = "DB2. Unable to execute query.";
		String sJDBC = sDB2+":retrieveMessagesFromServerOnGetMessage=true;emulateParameterMetaDataForZCalls=1;;user=ATTAUT1;password="+sDB2Password+";";
		
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			Connection conn = DriverManager.getConnection(sJDBC);
	
			sqlError = "DB2. Error reading Endevor records from CIA database.";
			//sqlStmt = frame.readTextResource("EndevorDBQuery.txt", "= 'VIEW'", "", "", "");
			sqlStmt = frame.readTextResource("EndevorDBQuery.txt", "IS NOT NULL", "", "", "");
			pstmt=conn.prepareStatement(sqlStmt); 
			rSet = pstmt.executeQuery();
			
			while (rSet.next()) {
				String sAuthType = rSet.getString("AUTHTYPE").trim();
				String sRoleID = sAuthType.equalsIgnoreCase("R")? rSet.getString("ROLEID"): "";
				
				cRepoInfo.setString("APP",           rSet.getString("APP").trim(),                         iIndex);
				cRepoInfo.setString("APP_INSTANCE",  rSet.getString("APP_INSTANCE").trim(),                iIndex);
				cRepoInfo.setString("PRODUCT",       rSet.getString("PRODUCT").trim(),                     iIndex);
				cRepoInfo.setString("AUTHTYPE",      sAuthType,                                            iIndex);
				cRepoInfo.setString("ROLEID",        sRoleID.trim(),                                       iIndex);
				cRepoInfo.setString("RESMASK",       rSet.getString("RESMASK").trim(),                     iIndex);
				//cRepoInfo.setString("CONTACT",       rSet.getString("ADMINISTRATOR").toLowerCase().trim(), iIndex);
				cRepoInfo.setString("CONTACT",       "",                                                   iIndex);
				cRepoInfo.setString("ADMINISTRATOR", rSet.getString("ADMINISTRATOR").toLowerCase().trim(), iIndex);
				cRepoInfo.setString("DEPARTMENT",    rSet.getString("RESOURCE_OWNER").toLowerCase().trim(),iIndex);
				cRepoInfo.setString("USERID",        rSet.getString("USERID").toLowerCase().trim(),        iIndex);
				//cRepoInfo.setString("USERNAME",      rSet.getString("FULLNAME").trim().replace(',', '|'),  iIndex);
				cRepoInfo.setString("USERNAME",      rSet.getString("FULLNAME").trim(),                    iIndex);
				cRepoInfo.setString("ACC_READ",      rSet.getString("acc_read").trim(),                    iIndex);
				cRepoInfo.setString("ACC_WRITE",     rSet.getString("acc_write").trim(),                   iIndex);
				cRepoInfo.setString("ACC_UPDATE",    rSet.getString("acc_update").trim(),                  iIndex);
				cRepoInfo.setString("ACC_ALL",       rSet.getString("acc_all").trim(),                     iIndex);
				cRepoInfo.setString("ACC_NONE",      rSet.getString("acc_none").trim(),                    iIndex);
				cRepoInfo.setString("ACC_CREATE",    rSet.getString("acc_create").trim(),                  iIndex);
				cRepoInfo.setString("ACC_FETCH",     rSet.getString("acc_fetch").trim(),                   iIndex);
				cRepoInfo.setString("ACC_SCRATCH",   rSet.getString("acc_scratch").trim(),                 iIndex);
				cRepoInfo.setString("ACC_CONTROL",   rSet.getString("acc_control").trim(),                 iIndex);
				cRepoInfo.setString("ACC_INQUIRE",   rSet.getString("acc_inquire").trim(),                 iIndex);
				cRepoInfo.setString("ACC_SET",       rSet.getString("acc_set").trim(),                     iIndex);			
				cRepoInfo.setString("PRODUCTNAME",   "",                                                   iIndex);
				iIndex++;
			} // loop over record sets
			
			frame.printLog(">>>:"+iIndex+" Records Read From DB2.");

		} catch (ClassNotFoundException e) {
			iReturnCode = 101;
		    frame.printErr(sqlError);
		    frame.printErr(e.getLocalizedMessage());			
		    System.exit(iReturnCode);
		} catch (SQLException e) {     
			iReturnCode = 102;
		    frame.printErr(sqlError);
		    frame.printErr(e.getLocalizedMessage());			
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
			String sApp2 = "MF20SCM";

			sqlError = "DB. Error deleting previous records.";
			sqlStmt = "delete from GITHUB_REVIEW where Application in ('"+ sApp +"','"+ sApp2 + "')";
			pstmt=conn.prepareStatement(sqlStmt);  
			iResult = pstmt.executeUpdate();
			if (iResult > 0) 
				frame.printLog(">>>:"+iResult+" Previous IMAG Feed Records Deleted.");
			
			sqlError = "DB. Error inserting record.";
			int nRecordsWritten = 0;
			int nBlock = 100;
			
			for (int iIndex=0,nRecords=0; iIndex<cRepoInfo.getKeyElementCount(sTagApp); iIndex++) {
				if (!cRepoInfo.getString(sTagApp, iIndex).isEmpty()) { 
					if (nRecords%nBlock == 0)
						sqlStmt = sqlStmt0;
					else 
						sqlStmt += " , ";
					
					sEntitlement2 = cRepoInfo.getString("AUTHTYPE", iIndex).equalsIgnoreCase("U")? "User" : "Role:"+cRepoInfo.getString("ROLEID", iIndex);
					sContactEmail = "";
					String[] aContacts = frame.readAssignedApprovers(cRepoInfo.getString(sTagContact, iIndex));
					for (int j=0; j<aContacts.length; j++) {
						String sContactNext = "";
						if (aContacts[j].equalsIgnoreCase("toolsadmin"))
							sContactNext= "Toolsadmin@ca.com";
						else
							sContactNext= aContacts[j]+"@ca.com";
						
						if ((sContactEmail.length()+sContactNext.length()+1) > 80) continue;
						
						if (!sContactEmail.isEmpty())
							sContactEmail += ";";
						sContactEmail += sContactNext;
					}
					sEntitlementAttrs = "resowner="+ cRepoInfo.getString("DEPARTMENT", iIndex)+";"+
							            "adminby=" + cRepoInfo.getString("ADMINISTRATOR", iIndex)+";"+
							            "product=" + cRepoInfo.getString("PRODUCTNAME", iIndex);
					sUserAttrs = "username=" +  cRepoInfo.getString("USERNAME", iIndex) + ";" +
							     "read="     + (cRepoInfo.getString("ACC_READ", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "write="    + (cRepoInfo.getString("ACC_WRITE", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "update="   + (cRepoInfo.getString("ACC_UPDATE", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "all="      + (cRepoInfo.getString("ACC_ALL", iIndex).equalsIgnoreCase("A")? "Y" : "N") + ";" +
							     "none="     + (cRepoInfo.getString("ACC_READ", iIndex).equalsIgnoreCase("A")? "Y" : "N") ;
					
					sValues = "('"  + sApp + "',"+
							  "'"   + cRepoInfo.getString("APP_INSTANCE", iIndex) + "',"+
							  "'"   + cRepoInfo.getString(sTagProject, iIndex) + "',"+
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
		    frame.printErr(sqlError);
		    frame.printErr(e.getLocalizedMessage());			
		    System.exit(iReturnCode);
		} catch (SQLException e) {     
			iReturnCode = 302;
		    frame.printErr(sqlError);
		    frame.printErr(e.getLocalizedMessage());			
		    System.exit(iReturnCode);
		}
	} // writeDBFromRepoContainer		
	
	
	
	private static void readEndevorDepartmentContacts(JCaContainer cDCA, ArrayList<String> aDept, String sDB2Password) {
		PreparedStatement pstmt = null; 
		String sqlStmt;
		int iIndex = 0;
		ResultSet rSet;
		
		String sqlError = "DB2. Unable to execute query.";
		String sJDBC = sDB2+":retrieveMessagesFromServerOnGetMessage=true;emulateParameterMetaDataForZCalls=1;;user=ATTAUT1;password="+sDB2Password+";";

		String sIn = "";
		for (int i=0; i<aDept.size(); i++) {
			if (!sIn.isEmpty())
				sIn += ", ";
			sIn += "'"+aDept.get(i)+"'";
		}
		
		try {
			Class.forName("com.ibm.db2.jcc.DB2Driver");
			Connection conn = DriverManager.getConnection(sJDBC);
	
			sqlError = "DB2. Error reading DCA/VCA records from CIA database.";
			sqlStmt = frame.readTextResource("EndeavorDCAQuery.txt", sIn, "", "", "");
			pstmt=conn.prepareStatement(sqlStmt); 
			rSet = pstmt.executeQuery();
			while (rSet.next()) {
				String sResult= rSet.getString("USERMASK").trim() + "/" +
								rSet.getString("SCOPEID").trim() + "/" +
								rSet.getString("TYPE").trim();
				cDCA.setString("USERID", rSet.getString("USERMASK").trim(), iIndex);
				cDCA.setString("DEPARTMENT", rSet.getString("SCOPEID").trim(), iIndex);
				cDCA.setString("TYPE", rSet.getString("TYPE").trim(), iIndex);
				iIndex++;
			} // loop over record sets
		} catch (ClassNotFoundException e) {
			iReturnCode = 101;
		    frame.printErr(sqlError);
		    frame.printErr(e.getLocalizedMessage());			
		    System.exit(iReturnCode);
		} catch (SQLException e) {     
			iReturnCode = 102;
		    frame.printErr(sqlError);
		    frame.printErr(e.getLocalizedMessage());			
		    System.exit(iReturnCode);
		}			
	} //readEndevorDepartmentContacts
	
	
	
	
	public static void main(String[] args) {
		int iParms = args.length;
		int iReturnCode = 0;
		String sOutputFile = "";
		String sBCC = "";
		String sLogPath = "endevorrepldap.log";
		String sMapFile = "tss_user_mapping.csv";
		String sContactFile = "";
		String sDB2Password = "";
		String sImagDBPassword = "";	
		boolean bShowTerminated = false;
		
		// check parameters
		for (int i = 0; i < iParms; i++)
		{					
			if (args[i].compareToIgnoreCase("-outputfile") == 0 )
			{
				sOutputFile = args[++i];
			}			
			else if (args[i].compareToIgnoreCase("-mapfile") == 0 )
			{
				sMapFile = args[++i];
			}			
			else if (args[i].compareToIgnoreCase("-contactfile") == 0 )
			{
				sContactFile = args[++i];
			}			
			else if (args[i].compareToIgnoreCase("-bcc") == 0 )
			{
				sBCC = args[++i];
			}			
			else if (args[i].compareToIgnoreCase("-log") == 0 )
			{
				sLogPath = args[++i];
			}	
			else if (args[i].compareToIgnoreCase("-showterminated") == 0 )
			{
				bShowTerminated = true;
			}	
			else {
				System.out.println("Argument: "+args[i]);
				System.out.println("Usage: endevorrepldap \n"+
				                   "                     -outputfile textfile \n"+
				                   "                     [-bcc emailadress] [-log textfile] [-h |-?]");
				System.out.println(" -inputfile option specifies the attestation input file to validate (tsv)");
				System.out.println(" -outputfile option specifies the attestation output file (csv)");
				System.out.println(" -showterminated option creates notifications for terminated users");
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
/*
			// Write out processed repository in organization file
			if (!sOutputFile.isEmpty()) {
				frame.writeCSVFileFromListGeneric(cRepoInfo, sOutputFile, '\t');					
			}
*/			
			// a. Loop over records collapsing APP_INSTANCEs
			String sEntitlementLast = "";
			String sInstanceLast = "";
			int iIndexLast = -1;
			
			for (int iIndex=0; iIndex<cRepoInfo.getKeyElementCount(sTagApp); iIndex++) {
				if (!cRepoInfo.getString(sTagApp, iIndex).isEmpty()) {
					String sInstance = cRepoInfo.getString("APP_INSTANCE", iIndex);
					String sEntitlement = cRepoInfo.getString(sTagProject, iIndex)+"/"+
										  cRepoInfo.getString("AUTHTYPE", iIndex)+"/"+
				                          cRepoInfo.getString("ROLEID", iIndex)+"/"+
							              cRepoInfo.getString("RESMASK", iIndex)+"/"+
							              cRepoInfo.getString("USERID", iIndex);
					
					if ( sEntitlement.equalsIgnoreCase(sEntitlementLast) && 
					    !sInstance.equalsIgnoreCase(sInstanceLast)) {
						cRepoInfo.setString(sTagApp, "", iIndex);
						String sNewInstance = sInstanceLast+';'+sInstance;
						cRepoInfo.setString("APP_INSTANCE", sNewInstance, iIndexLast);
					}
					sEntitlementLast = sEntitlement;
					sInstanceLast = sInstance;
					iIndexLast = iIndex;
				}
			}
			
			// b. Apply any mapping table for contacts.
			JCaContainer cContact = new JCaContainer();
			if (!sContactFile.isEmpty())
				frame.sContactFile = sContactFile;
			frame.readSourceMinderContacts(cContact, "Endevor", cLDAP);
			// Apply contact information for records
			// a. from SourceMinder Contacts
			for (int iIndex=0; iIndex<cContact.getKeyElementCount("Approver"); iIndex++) {
				String sLocation = cContact.getString("Location", iIndex).replace("\"", "");
				String sProduct = cContact.getString("Product", iIndex);
				String[] sProjects = frame.readAssignedBrokerProjects(sLocation, "");
				String[] sApprovers = frame.readAssignedApprovers(cContact.getString("Approver", iIndex));
				boolean bActive = cContact.getString("Active", iIndex).contentEquals("Y");
				//String sReleases = cContact.getString("Release", iIndex);
				
				if (sProjects.length > 0) {
					String sApprover = "";
					for (int jIndex=0; jIndex<sApprovers.length; jIndex++) {
						if (!sApprover.isEmpty()) 
							sApprover += ";";
						sApprover += sApprovers[jIndex];
					}

					
					for (int k=0; k<sProjects.length; k++) {
						String sProject = sProjects[k].toUpperCase();
						sProject.replace("**", "!!");
						sProject.replace("*", "");
						sProject.replace("!!", "**");

						int[] iProjects = cRepoInfo.find(sTagProject, sProject);
						
						for (int kIndex=0; kIndex<iProjects.length; kIndex++) {
							if (cRepoInfo.getString(sTagContact, iProjects[kIndex]).isEmpty())
								//cRepoInfo.setString(sTagContact, bActive? sApprover : "toolsadmin", iProjects[kIndex]);
								// Endevor has no concept of inactive project/view, so SCO says we need to disregard project state.
								cRepoInfo.setString(sTagContact, sApprover, iProjects[kIndex]);
							String sThis = cRepoInfo.getString(sTagProduct, iProjects[kIndex]);
							if (sThis.isEmpty())
								cRepoInfo.setString(sTagProduct, sProduct, iProjects[kIndex]);
						}
					} // loop over project prefixes
				} 	// broker record exists in contact info					
			} // loop over contact records

			// Process all end of life projects (make them inactive projects in Harvest)
			for (int k=0; k<cRepoInfo.getKeyElementCount(sTagProject); k++) {
				String sProject = cRepoInfo.getString(sTagProject, k);
				if (cRepoInfo.getString(sTagContact, k).equalsIgnoreCase("toolsadmin") &&
					!cRepoInfo.getString(sTagApp, k).isEmpty() ) {
		    		if (sProblems.isEmpty()) 
		    			sProblems = tagUL;
		    		sProblems+= "<li>The product, <b>"+sProject+"</b> is End of Life.</li>\n";
		    		
					int[] iProjects = cRepoInfo.find(sTagProject, sProject);
					for (int iIndex=0; iIndex<iProjects.length; iIndex++) {
						cRepoInfo.setString(sTagApp, "", iProjects[iIndex]);
					}
					
				} // end of life entry					
			} //loop over broker entries
			
			
			// c. Loop through the Container to check for invalid contacts			
			for (int iIndex=0; iIndex<cRepoInfo.getKeyElementCount(sTagApp); iIndex++) {
				if (!cRepoInfo.getString(sTagApp, iIndex).isEmpty()) {
					String sApprovers = cRepoInfo.getString(sTagContact, iIndex);
					String[] sID = frame.readAssignedApprovers(sApprovers);
					
					String sNewApprovers = "";
					
					for (int m=0; m<sID.length; m++) {
						int[] iLDAP = cLDAP.find(sTagPmfkey, sID[m]);
						if (iLDAP.length > 0) {
							if (!sNewApprovers.isEmpty())
								sNewApprovers += ";";
							sNewApprovers += sID[m];
						}
					} // loop over Approvers
		    		int[] iContacts = cRepoInfo.find(sTagContact, sApprovers); 	
		    		
					for (int i=0; i<iContacts.length; i++) {
						String sApp = cRepoInfo.getString(sTagApp, iContacts[i]);
						if (!sApp.isEmpty()) {	
							if (sNewApprovers.isEmpty()) {								
								String sView = cRepoInfo.getString(sTagProject, iContacts[i]);
								
					    		if (sProblems.isEmpty()) 
					    			sProblems = tagUL;			    		
					    		sProblems+= "<li>The Endeavor product, <b>"+sView+"</b>, does not have a valid contact.</li>\n";
					    		
					    		for (int j=i+1; j<iContacts.length; j++) {
					    			if (sView.contentEquals(cRepoInfo.getString(sTagProject, iContacts[j]))) {
					    				cRepoInfo.setString(sTagApp, "", iContacts[j]);
					    			}
					    		}
								cRepoInfo.setString(sTagApp, "", iContacts[i]);
								
							}
						} 
					} // loop over repo records with particular contact
				}
			}
		
			// d. Look for terminated users
			JCaContainer cUsers = new JCaContainer();
			frame.readInputListGeneric(cUsers, sMapFile, ',');
			
			for (int iIndex=0; iIndex<cRepoInfo.getKeyElementCount(sTagApp); iIndex++) {
				if (!cRepoInfo.getString(sTagApp, iIndex).isEmpty()) {
					boolean bLocalGeneric=false;
					boolean bTerminated=false;

					String sID  = cRepoInfo.getString("USERID", iIndex);
					
					if (sID.contains("?")) 
						sID = sID.substring(0, sID.indexOf('?'));
					String sRealID = sID;
					String sUseID  = sID;

					int[] iRepl = cUsers.find("TOPSECRET", sID.toLowerCase());
					
					boolean bUnmapped = false;
					if (iRepl.length > 0) {
						sRealID = cUsers.getString("CADOMAIN", iRepl[0]);
						if (sRealID.equals("Generic")) {
							bLocalGeneric = true;
						}
						else {
							sUseID = sRealID;
						}
					}
					else {
						iRepl = cUsers.find("CADOMAIN",sID.toLowerCase());
						if (iRepl.length == 0)
							bUnmapped = true;
					}
					
					int[] iLDAP = cLDAP.find(sTagPmfkey, sUseID);				
					if (iLDAP.length == 0 && !bLocalGeneric) {						
			    		int[] iUsers = cRepoInfo.find("USERID", sID); 	

			    		if (!bLocalGeneric) {
			    			String sProduct, sAuthtype, sResmask;
							for (int i=0; i<iUsers.length; i++) {
								String sApp = cRepoInfo.getString(sTagApp, iUsers[i]);
								if (!sApp.isEmpty()) {									
									if (bUnmapped) {
							    		if (sProblems.isEmpty()) 
							    			sProblems = tagUL;			    		
							    		sProblems+= "<li>The Endeavor user id, <b>"+sID+"</b>, references an unmapped user.</li>\n";									
									}
									else {				
										if (bShowTerminated) {											
								    		if (sProblems.isEmpty()) 
								    			sProblems = tagUL;			    		
								    		sProblems+= "<li>The Endeavor user id, <b>"+sID+"</b>, references a terminated user.</li>\n";									
										}
									}									
									
						    		String sSysIdArr = "{";
						    		for (int j=i; j<iUsers.length; j++) {
						    			String sSysId = cRepoInfo.getString("APP_INSTANCE", iUsers[j]);
						    			if (!sSysIdArr.contains(sSysId)) {
						    				sSysIdArr += (i==j?"":";") + sSysId;
						    			}
						    			cRepoInfo.setString(sTagApp, "", iUsers[j]);
						    		}
						    		sSysIdArr += "}";
						    		
					    			if (bShowTerminated) {
					    				ticketProblems.add("USERTSS.ASUSPEND should be set for TSS user id, "+sID+", with SYSIDs, "+sSysIdArr+".");
					    			}
								} // unprocessed user records with no corporate id
							} // loop over records where user has no corporate id
			    		}
					} 
					else if (bLocalGeneric || !sID.equalsIgnoreCase(sRealID)){
			    		int[] iUsers = cRepoInfo.find("USERID", sID); 	
			    		for (int i=0; i<iUsers.length; i++) {
			    			cRepoInfo.setString("USERID", 
			    					            (bLocalGeneric? sUseID+"?" : sUseID),
			    					            iUsers[i]);
			    		}
					}
					
					if (bUnmapped && !bLocalGeneric && iLDAP.length > 0) {
						int cIndex = cUsers.getKeyElementCount("CADOMAIN");
						cUsers.setString("TOPSECRET", sID, cIndex);
						cUsers.setString("CADOMAIN", sUseID.toLowerCase(), cIndex);
					}
				}
			}
			
			//e. Create notifications regarding approvers not in list of DCA/VCAs
			// retrieve a list of departments
			if (bShowTerminated) {				
				ArrayList<String> aDept = new ArrayList<String>();
				for (int iIndex=0; iIndex<cRepoInfo.getKeyElementCount(sTagApp); iIndex++) {
					String sDept = cRepoInfo.getString("DEPARTMENT", iIndex).toUpperCase();
					if (!aDept.contains((Object)sDept))
						aDept.add(sDept.toUpperCase());
				}
				Collections.sort(aDept);
				
				JCaContainer cDCA = new JCaContainer();
				readEndevorDepartmentContacts(cDCA, aDept, sDB2Password);
				
				for (int iIndex=0; iIndex<aDept.size(); iIndex++) {
					String sDept = aDept.get(iIndex);
					int[] iDept = cDCA.find("DEPARTMENT", sDept.toUpperCase());
					if (iDept.length > 0) {
						ArrayList<String> aContact = new ArrayList<String>();
						for (int j=0; j<iDept.length; j++) {
							String sContact = cDCA.getString("USERID", iDept[j]).toLowerCase();
							// need to translate from TOPSECRET to CADOMAIN
							int[] iContact = cUsers.find("TOPSECRET", sContact);
							if (iContact.length > 0)
								sContact = cUsers.getString("CADOMAIN", iContact[0]);
							
							if (!sContact.equalsIgnoreCase("generic") &&
								!aContact.contains(sContact))
								aContact.add(sContact.toLowerCase());
						}
							
						// look at the repo info for matching departments; check if approver is in DCA/VCA lists
						int[] iRepo = cRepoInfo.find("DEPARTMENT", sDept.toLowerCase());
						String sLastProduct = "";
						for (int k=0; k<iRepo.length; k++) {
							if (cRepoInfo.getString(sTagApp, iRepo[k]).isEmpty()) continue;
							
							String sProduct = cRepoInfo.getString(sTagProduct, iRepo[k]);
							if (!sProduct.equalsIgnoreCase(sLastProduct)) {
								String aApprovers = cRepoInfo.getString(sTagContact, iRepo[k]);
								int eIndex=-1;
								do {
									String sApprover;
									eIndex = aApprovers.indexOf(';');
									if (eIndex>0) {
										sApprover = aApprovers.substring(0, eIndex);
										aApprovers = aApprovers.substring(eIndex+1);
									}
									else
										sApprover = aApprovers;
									if (!aContact.contains(sApprover)) {
							    		if (sProblems.isEmpty()) sProblems = tagUL;			    		
							    		sProblems+= "<li>The approver,<b>"+sApprover+"</b>, for Endeavor product, <b>"+sProduct+"</b>, is not in the list of DCAs/VCAs for the product's department/division, <b>"+sDept+"</b>.</li>\n";									
									}								
								}
								while (eIndex>0);
							}
							sLastProduct = sProduct;
						}
					}
					else {
			    		if (sProblems.isEmpty()) 
			    			sProblems = tagUL;			    		
			    		sProblems+= "<li>The department/division, <b>"+sDept+"</b>, has no DCA/VCA/ZCA value.</li>\n";														
					}
				}
			}
			
			
			
			// Write out tss mapping file with changes
			if (!cUsers.isEmpty()) {
				frame.writeCSVFileFromListGeneric(cUsers, sMapFile, ',', null, false);
			}
			
			// Write out processed repository in organization file
			if (!sOutputFile.isEmpty()) {
				frame.writeCSVFileFromListGeneric(cRepoInfo, sOutputFile, '\t', cLDAP);					
			}
			
			// Write out processed records to database
			writeDBFromRepoContainer(cRepoInfo, sImagDBPassword);
			
			if (!sProblems.isEmpty()) {
				String email = frame.expandDistributionListforEmail("cn=Team - GIS - githubcom - Tools Services - Contacts,ou=self service groups,ou=groups", cLDAP);
				String sSubject, sTicket, sScope;
				if (email.startsWith(";"))
					email = email.substring(1);
				
				if (sProblems.contains("terminated user") ||
					sProblems.contains("DCA")) {
					email = email+";bigag01@ca.com"; //Team-GIS-Mainframe-PlatformManagement-Security?
				}
				
				sSubject = "Notification of CA Endevor Governance Problems and Changes";
				sScope   = "CIA DB2 Database";
				sTicket  = "Mainframe:System Endeavor SCM User Access";
				
		        //create a service desk ticket from ticketProblem
				frame.createServiceTicket(sProblems, sTicket, ticketProblems, "GIS-STO-Mainframe-Management-L2", "");
				sProblems+="</ul>\n";				
		        String bodyText = frame.readTextResource("Notification_of_Noncompliant_Endevor_Contacts.txt", sScope, sProblems, "", "");								        								          
		        frame.sendEmailNotification(email, sSubject, bodyText, true);
			} // had some notifications
			
	     } catch (Exception e) {
				iReturnCode = 1;
			    frame.printErr(e.getLocalizedMessage());			
			    System.exit(iReturnCode);		    	    	 
	     }	// try/catch blocks         
	}
}
