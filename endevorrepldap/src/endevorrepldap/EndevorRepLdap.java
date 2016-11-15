package endevorrepldap;


//import com.ca.harvest.jhsdk.hutils.*;

import java.sql.*;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.*;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;

import javax.naming.*;
import javax.naming.directory.*;

import org.eclipse.core.runtime.*;

import junit.framework.*;


import commonldap.CommonLdap;
import commonldap.JCaData;
import commonldap.JCaContainer;

public class EndevorRepLdap {
	void EndevorRepLdap() {
		
	}

	public static void main(String[] args) {
		int iParms = args.length;
		int iReturnCode = 0;
		String sOutputFile = "";
		String sBCC = "";
		String sLogPath = "endevorrepldap.log";
		
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
		
	}
}
