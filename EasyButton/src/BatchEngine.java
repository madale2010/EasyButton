import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class BatchEngine {
	/** 
	 * Method will use plink once to make sure we have at 
	 * host key stored on the users system.
	 * 
	 */
	public static void checkPlinkHostKey(){
		FileWriter fstreamBatch;
		try {
			fstreamBatch = new FileWriter("PlinkHostKey.bat");

			BufferedWriter outBatch = new BufferedWriter(fstreamBatch);
			outBatch.write(
							"regedit.exe /c /s \"Plink Host Keys.reg\"\n"+
							"exit\n"
			);
			outBatch.close();
			  String hostCommand = "cmd /C  start /MIN PlinkHostKey.bat ";
			  Runtime hostRunTime = Runtime.getRuntime();
			  Process hostProcess = hostRunTime.exec(hostCommand);
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 *  Method creates batch job that will find the convID.
	 * @param flow 
	 * @param envirVariable 
	 * @param hostName 
	 */
	public static void createConvIDLogBatch(String orderID, String hostName, String envirVariable, String flow) {
	   try{

		//Create the commands file that will be used by the batch
	    FileWriter fstreamCommands = new FileWriter("commands.txt");
		BufferedWriter outCommands = new BufferedWriter(fstreamCommands);
		outCommands.write(
				"cd /\r\n"+
				"cd sites/servers/"+hostName+envirVariable+flow+"/logs\r\n"+
				"conv=`grep "+orderID+" dynamo.log | grep SetupAccount | grep CONVERSATIONID |cut -c142-212`\r\n"+
				"clear\r\n"+
				"conv=${conv%%]*}\r\n"+
				"echo $conv\r\n"+
				"grep "+orderID+" dynamo.log | grep automation\r\n"+
				"cd csixmls\r\n"+
				"if [[ -n $conv ]]; then grep messageId `find *FiberAddAccountServiceResponse.xml -type f -exec grep -l \"$conv\" {} \\;`; fi\r\n"+
				//finds the ban for given convID
				"if [[ -n $conv ]]; then grep accountNumber `find *FiberInquireAccountDetailsServiceResponse.xml -type f -exec grep -l \"$conv\" {} \\;`; fi\r\n"+
				//Debugging turned on
				//"if [[ -n $conv ]]; then grep \"$conv\" dynamo.log | grep AccountNumber ; fi\r\n"+
				"exit\r\n"
				);
		outCommands.close();
		   
		// Create batch job to write with all hard coded commands
		FileWriter fstreamBatch = new FileWriter("EasyConvID.bat");
		BufferedWriter outBatch = new BufferedWriter(fstreamBatch);
		outBatch.write(		    		
				"@echo off\n" +
				"setlocal EnableDelayedExpansion\n"+
				"goto :environment\n"+
				"REM Select Environment\n"+
				":environment\n"+
				"set hostName=%1\n"+
				"set env=%2\n"+
				//"echo Host selected is %1\n"+
				"REM Select Flow\n"+
				":flow\n"+
				"if %3==A set flow=A\n"+
				"if %3==B set flow=B\n"+
				//"echo Envir selected  is %2\n"+
				"REM Search for Order\n"+
				":order\n"+
				"set order=%4\n"+
				//"echo Flow is %3\n"+
				//"echo Order is %4\n"+
				"REM Open plink and execute proper script\n"+
				"plink -ssh -pw atgview atgview@%hostName%%env%.edc.cingular.net <\"commands.txt\" >>\"output.txt\"\n"+	
				"REM Takes the output file and finds the conv ID\n"+
				"echo Finished>>\"results.txt\"\n"+
				"echo Debugging Turned on\r\n"+
				//"pause\r\n"+
				"exit\n");
		//Close the output stream
		outBatch.close();
		}catch (Exception e){
		  System.err.println("Error: " + e.getMessage());
		}
	}
	/**
	 * Method takes the convID then process the BAN and xml files from another batch job.
	 * @param convID
	 * @param messageID 
	 */
	public static void createBanBatch(String convID, String messageID) {
		// Create batch job to write with all hard coded commands

		try {

			FileWriter fstream = new FileWriter("EasyBan.bat");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(
					"@echo off\n" +
					"setlocal EnableDelayedExpansion\n"+
					"set hostName=%1\n"+
					"set env=%2\n"+
					"if %3==A set flow=A\n"+
					"if %3==B set flow=B\n"+
					"REM Creates the list of commands to find Conversation ID in txt file\n"+
					"echo cd / >>\"commands2.txt\"\n"+
					"echo cd sites/servers/%hostName%%env%%flow%/logs/csixmls >>\"commands2.txt\"\n"+
					//Finds the Ban
					//"echo grep accountNumber `find *FiberInquireAccountDetailsServiceResponse.xml -type f -exec grep -l \"$conv\" {} \\;>>\"commands2.txt\"\n"+
					//"REM finds all xml files that have the convID in it\n"+
					"REM finds the csixmls files\n"+
					"echo find *Fiber*.xml -type f -exec grep -l \""+convID+"\" {} \\;>>\"commands2.txt\"\n"+
					"echo find *FiberAddAccountServiceRequest.xml -type f -exec grep -l \""+messageID+"\" {} \\;>>\"commands2.txt\"\n"+
					"echo exit >>\"commands2.txt\"\n"+
					"REM Open plink and execute proper script\n"+
					"plink -ssh -pw atgview atgview@%hostName%%env%.edc.cingular.net <\"commands2.txt\" >>\"output2.txt\"\n"+	
					"echo Finished BAN batch >>\"results2.txt\"\n"	+
					"exit\n"
			);
			out.close();
		}catch (Exception e){
		  System.err.println("Error: " + e.getMessage());
		}
		
	}
	/**
	 * Get the conversation ID 
	 * @throws IOException 
	 *
	 */
	public static String getConversationID() throws IOException{
		String convID="";
		BufferedReader line = new BufferedReader(new FileReader("output.txt"));
		    while ((convID = line.readLine()) != null) {
		    	if(convID.contains("buyonline")){
		    		line.close();
		    		return convID;
		    	}
		    }
		    line.close();
		
		return "";
	}
	/**
	 * Get the account Number  
	 * @throws IOException 
	 *
	 */
	public static String getAccountNumber() throws IOException{
		String accountNumber="";
		BufferedReader line = new BufferedReader(new FileReader("output.txt"));
		    while ((accountNumber = line.readLine()) != null) {
		    	if(accountNumber.contains("<accountNumber>")){
		    		line.close();
		    		return accountNumber.substring(15, 24);
		    	}
		    }
		    line.close();
		
		return "";
	}
	
	/**
	 * Get the automation results to display to user
	 * @param convIDStr 
	 * @throws IOException 
	 *
	 */
	public static String getAutomationResults(String convIDStr) throws IOException{
		String autoLine="", autoResults="";
		BufferedReader autoLineBuf = new BufferedReader(new FileReader("output.txt"));
		    while ((autoLine = autoLineBuf.readLine()) != null) {

		    	//Add extra space where needed
		    	if(autoLine.contains("Executing")){
		    		autoLine=autoLine.substring(0,autoLine.indexOf("Executing"))+" "+autoLine.substring(autoLine.indexOf("Executing"));
		    	}
		    	//Remove out the conversation ID we have this and it causes clutter
		    	if(autoLine.contains(convIDStr)&& autoLine.length()>convIDStr.length() && !convIDStr.equals("")){
		    		autoLine=autoLine.substring(0, autoLine.indexOf("[Order:")+17)+autoLine.substring(autoLine.indexOf(convIDStr)+convIDStr.length());
		    	}

		    	if(autoLine.contains("/automation/")){
		    		autoResults+=autoLine.substring(autoLine.indexOf("PST")-20,autoLine.indexOf("PST"))+autoLine.substring(autoLine.indexOf("[Order"),autoLine.length())+"\r\n";  		
		    	}

		    }
		    autoLineBuf.close();
		
		return autoResults;
	}
	
	/**
	 * Get the automation results to display in text, this will include all data found
	 * @param convIDStr 
	 * @throws IOException 
	 *
	 */
	public static String getAutomationResultsForText() throws IOException{
		String autoLine="", autoResults="";
		BufferedReader autoLineBuf = new BufferedReader(new FileReader("output.txt"));
		    while ((autoLine = autoLineBuf.readLine()) != null) {

		    	if(autoLine.contains("/automation/")){
		    		autoResults+=autoLine+"\r\n";  		
		    	}
		    }
		    autoLineBuf.close();
		
		return autoResults;
	}
	
	
	/**
	 * Go ahead and copy the files found over to the users host computer
	 * 
	 * @param ftpFileList
	 * @param flow 
	 * @param flow2 
	 * @param envirVarable 
	 */
	public static void getFtpFiles(ArrayList<String> ftpFileList, String hostName, String envir, String flow) {
		// TODO Auto-generated method stub
		try {

			
			//Create the batch that will copy over the files
			FileWriter fstream = new FileWriter("EasyFtpFileCopy.bat");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write("@echo off\n" +
					  "setlocal EnableDelayedExpansion\n"+
					  "echo cd />>\"ftpCommands.txt\"\n"+
					  "echo cd sites/servers/"+hostName+envir+flow+"/logs/csixmls>>\"ftpCommands.txt\"\n");
			for(int i=0; i< ftpFileList.size(); i++){
				out.write("echo get "+ftpFileList.get(i)+">>\"ftpCommands.txt\"\n");
			}
			out.write("echo bye>>\"ftpCommands.txt\"\n"+
					  "PSFTP -l atgview -pw atgview "+hostName+envir+".edc.cingular.net -b ftpCommands.txt\n"+
					  "echo Finished ftp batch >>\"results3.txt\"\n"	+
					  "exit\n"
					   );
			out.close();
			
			
		}catch (Exception e){
		  System.err.println("Error: " + e.getMessage());
		}
		
		
	}
}