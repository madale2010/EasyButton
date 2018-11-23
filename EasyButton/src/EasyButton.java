import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class EasyButton extends JFrame implements ActionListener{
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	JFrame popUpFrame = new JFrame();
	private String [] csixmlLabels= {"FiberAddAccountServiceRequest","FiberAddAccountServiceResponse",
	"FiberInquireAccountDetailsServiceRequest","FiberInquireAccountDetailsServiceResponse",
	"FiberInquireCrossProductPackagesServiceRequest","FiberInquireCrossProductPackagesServiceResponse",
	"FiberInquireProductDetailsServiceRequest","FiberInquireProductDetailsServiceResponse",
	"FiberValidateProductDetailsServiceRequest","FiberValidateProductDetailsServiceResponse",
	"FiberInquireQuotationServiceRequestRequest","FiberInquireQuotationServiceRequestResponse",
	"FInqAsgndProdDetailsServiceRequest", "FInqAsgndProdDetailsServiceResponse"};
	private String totalResults, accountNumber="", orderID, fileName;
	private ArrayList<String> csixmlData;
	private ArrayList<String> ftpFileList;
	private  File orderFolder;
	private int confirmResults =1;

	
	/**
	 * Create main to show driver
	 * @param args
	 */
	public static void main(String[] args) {
		new EasyGUI();		
	}
	public EasyButton() {
		//First check to make sure plink is setup and read
		File plinkRegKeys= new File("Plink Host Keys.reg");
		File plinkHostBatch = new File("PlinkHostKey.bat");
		if(plinkRegKeys.exists()){
			JOptionPane.showMessageDialog(null, "Setup will configure now",
					  "First Time Setup", JOptionPane.INFORMATION_MESSAGE);
			BatchEngine.checkPlinkHostKey();
			//Perform cleanup
			//Give time to process
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(plinkHostBatch.exists()){
				plinkHostBatch.delete();
			}

			if(plinkRegKeys.exists()){
				plinkRegKeys.delete();
			}
		}
	}

	public void actionPerformed(ActionEvent arg0) {

		
		//Comment out for testing uses
		if(arg0.getActionCommand().equals("TEST")){
			
		}
		if(arg0.getActionCommand().equals("Close")){
			ShowPopUpWindow.closeWindow();
		}
		if(arg0.getActionCommand().equals("!EASY!")){			  
			//Lets make sure no frames are open
			popUpFrame.dispose();
			boolean debugFlag = false;
			//Delete all files created once finished
			checkForExistingFiles(debugFlag);
			//Clear all variables
			accountNumber="";
			totalResults="";
			orderID="";

			ftpFileList = new ArrayList<String>();
			csixmlData = new ArrayList<String>();
			String flow =EasyGUI.getFlowTypeComboBox();
			String orderType = EasyGUI.getOrderTypeComboBox();
			String autoResults = "", autoTechResults="";
			//First do a check on the order number
			//Lets make sure its only digits at x length
			orderID = EasyGUI.getOrderIDTextField();
			if(checkOrderIDFormat(orderID)){
				
			//checkForExistingFiles();
			// Get order id from user and pass it in here
			String envirVariable="", hostName="";
			if(EasyGUI.getEnvirTypeComboBox().equals("FST1")){
				envirVariable="1";
				hostName="b2cfst";
			}
			if(EasyGUI.getEnvirTypeComboBox().equals("FST2")){
				envirVariable="2";
				hostName="b2cfst";
			}
			if(EasyGUI.getEnvirTypeComboBox().equals("FST3")){
				envirVariable="3";
				hostName="b2cfst";
			}
			if(EasyGUI.getEnvirTypeComboBox().equals("FST5")){
				envirVariable="5";
				hostName="b2cfst";
			}
			if(EasyGUI.getEnvirTypeComboBox().equals("FST7")){
				envirVariable="7";
				hostName="b2cfst";
			}		 
			if(EasyGUI.getEnvirTypeComboBox().equals("DEV3")){
				envirVariable="03";
				hostName="b2cimp";
			}
			BatchEngine.createConvIDLogBatch(orderID, hostName, envirVariable, flow);
			  try {
				  //NOTE 
				  //When placing commands for batch params are only seprated by space and may not go past 9.
				  
				  String convIDCommand = "cmd /C  start /MIN EasyConvID.bat "
				  +hostName	  
				  +" "+envirVariable
				  +" "+flow
				  +" "+orderID;
				  Runtime convIDRunTime = Runtime.getRuntime();
				  Process convIDProcess = convIDRunTime.exec(convIDCommand);

				//Call special timer here if this process is running for more 
			    //than 60 seconds kill it.
				  startTimeOutSession(60);

				  int convIDPause=0;
				  int banPause=0;
				  int ftpPause=0;
				  File convIDResults = new File("results.txt");
				  File banResults = new File("results2.txt");
				  File ftpResults = new File("results3.txt");
				  String convIDStr="";

				  while (convIDPause==0) {
					  
					  if(convIDResults.exists()){						  
						  convIDProcess.destroy();
						  try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}        
							convIDStr=BatchEngine.getConversationID();		
							autoResults=BatchEngine.getAutomationResults(convIDStr);
							//special automation call for Jira tickets and developers to debug
							autoTechResults=BatchEngine.getAutomationResultsForText();
							convIDPause=1;
					  }  
				  }
				  
				  //Check to make sure system did not run to fast
                  System.out.println("Conversation ID grab is "+convIDStr);
				  if(convIDStr.equals("ECHO is off.")|| convIDStr.equals("")){
					  ShowPopUpWindow.showPopUp(orderID,"Not Found","Not Found","\nCSIXMLS Found:\nN/A",autoResults, "", null);

						EasyGUI.setOrderIDTextField("");
				  }

				  else {
					  accountNumber=BatchEngine.getAccountNumber();
					//Run second batch job
					  if(EasyGUI.isCSIXMLSelected()){
					  BatchEngine.createBanBatch(convIDStr, getMessageID());
					  
					  String banCommand = "cmd /C start /MIN EasyBan.bat "
						  +hostName	 
						  +" "+envirVariable
						  +" "+flow
						  +" "+orderID;
					  Runtime banRunTime = Runtime.getRuntime();
					  Process banProcess = banRunTime.exec(banCommand);
				    
					  //Create a timer to wait for x amount of time before killing process
					  //This will fix hang in system, if error found in plink
					  csixmlData.clear();
					  while (banPause==0){
						  if(banResults.exists()){
							  banProcess.destroy();
							  banResults.delete();
							  //Start reading in output to get BAN and CSIXML
							  BufferedReader line = new BufferedReader(new FileReader("output2.txt"));

							  String bufBanStr; 
							  String fiberAddAccountServiceResponse="",fiberAddAccountServiceRequest="";
							  String fiberInquireAccountDetailsServiceResponse="",fiberInquireAccountDetailsServiceRequest="";
							  String fiberInquireCrossProductPackagesServiceResponse="", fiberInquireCrossProductPackagesServiceRequest=""; 
							  String fiberInquireProductDetailsServiceResponse="",fiberInquireProductDetailsServiceRequest="";
							  String fiberValidateProductDetailsServiceResponse="",fiberValidateProductDetailsServiceRequest="";
							  String fiberInquireQuotationServiceResponse="",fiberInquireQuotationServiceRequest="";
							  String fInqAsgndProdDetailsServiceResponse="",fInqAsgndProdDetailsServiceRequest="";
							  
							    while ((bufBanStr = line.readLine()) != null) {
							    	if(bufBanStr.contains("FiberAddAccountServiceResponse")){
							    		fiberAddAccountServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberAddAccountServiceRequest")){
							    		fiberAddAccountServiceRequest=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireAccountDetailsServiceResponse")){
							    		fiberInquireAccountDetailsServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireAccountDetailsServiceRequest")){
							    		fiberInquireAccountDetailsServiceRequest=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireCrossProductPackagesServiceResponse")){
							    		fiberInquireCrossProductPackagesServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireCrossProductPackagesServiceRequest")){
							    		fiberInquireCrossProductPackagesServiceRequest=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireProductDetailsServiceResponse")){
							    		fiberInquireProductDetailsServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireProductDetailsServiceRequest")){
							    		fiberInquireProductDetailsServiceRequest=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberValidateProductDetailsServiceResponse")){
							    		fiberValidateProductDetailsServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberValidateProductDetailsServiceRequest")){
							    		fiberValidateProductDetailsServiceRequest=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireQuotationServiceResponse")){
							    		fiberInquireQuotationServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FiberInquireQuotationServiceRequest")){
							    		fiberInquireQuotationServiceRequest=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FInqAsgndProdDetailsServiceResponse")){
							    		fInqAsgndProdDetailsServiceResponse=bufBanStr;
							    	}
							    	if(bufBanStr.contains("FInqAsgndProdDetailsServiceRequest")){
							    		fInqAsgndProdDetailsServiceRequest=bufBanStr;
							    	}
							    }		    

							    if(orderType.equals("Provide")){
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberAddAccountServiceResponse, 1));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberAddAccountServiceRequest, 0));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireAccountDetailsServiceResponse, 3));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireAccountDetailsServiceRequest, 2));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireCrossProductPackagesServiceResponse, 5));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireCrossProductPackagesServiceRequest, 4));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireProductDetailsServiceResponse,7));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireProductDetailsServiceRequest, 6));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberValidateProductDetailsServiceResponse,9));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberValidateProductDetailsServiceRequest,8));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireQuotationServiceResponse,11));
							    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireQuotationServiceRequest,10));
							    }
							    else {
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireAccountDetailsServiceResponse, 3));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireAccountDetailsServiceRequest, 2));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fInqAsgndProdDetailsServiceResponse,13));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fInqAsgndProdDetailsServiceRequest,12));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberValidateProductDetailsServiceResponse,9));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberValidateProductDetailsServiceRequest,8));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireQuotationServiceResponse,11));
								    csixmlData.add(checkLogForEmptyCSIXMLFileNames(fiberInquireQuotationServiceRequest,10));
							    }
							    
							    totalResults=gatherResults(csixmlData);
							    banPause=1;
							    line.close();
							    
						  }
						  
					  }
					  confirmResults = JOptionPane.showConfirmDialog(null,"Would you like the xml files copied to your system?\n","Copy Results",JOptionPane.YES_NO_OPTION);
				  }
					  if(totalResults==null){
						  totalResults="CSIXMLS\r\nN/A";
					  }
					  //Create directory for results found an put text file in that folder
					 orderFolder = new File(orderID);
					  if(orderFolder.exists()){
						  orderFolder.delete();
					  }
					  else{
						  orderFolder.mkdir();
					  }
					   fileName="Order ID "+orderID+".txt";
					  	FileWriter fstream = new FileWriter(orderFolder+"/"+fileName);
						BufferedWriter out = new BufferedWriter(fstream);
						out.write("Conversation ID is:"+"       "+convIDStr
			    				+"\r\nBan is:                             "+accountNumber
			    				+"\r\nOrder is:                          "+orderID
			    				+"\r\n"+totalResults
			    				+"\r\n\r\n\r\nAUTOMATION RESULTS:\r\n\r\n"+autoTechResults
								);
						out.close();
					  if(confirmResults==0){

							  for(int i=0; i<csixmlData.size();i++){
								  if(!csixmlData.get(i).contains("File Not Found")){
									  ftpFileList.add(csixmlData.get(i));
								  }
							  }
							  //Before we run batch job check for old files and delete to make room for new files
							  for(int i=0; i<ftpFileList.size();i++)
							  {
								  deleteFiles(ftpFileList.get(i), orderFolder);
							  }
							  //Create the batch job for files found
							  BatchEngine.getFtpFiles(ftpFileList, hostName, envirVariable, flow);
							  //Run batch job
							  String ftpCommand = "cmd /C start /MIN EasyFtpFileCopy.bat ";
							  Runtime ftpRunTime = Runtime.getRuntime();
							  Process ftpProcess = ftpRunTime.exec(ftpCommand);
							  //Move files to correct directory
							  while (ftpPause==0) {
								  if(ftpResults.exists()){						  
									  ftpProcess.destroy();
									  for(int i=0; i<ftpFileList.size();i++)
									  {
										  moveFiles(ftpFileList.get(i), orderFolder);
									  }
									  JOptionPane.showMessageDialog(null, "Your files have been copied",
											  "Finished", JOptionPane.INFORMATION_MESSAGE);
								  ftpPause+=1;
								  }
							}
					  }
					  ShowPopUpWindow.showPopUp(orderID, convIDStr, accountNumber,totalResults, autoResults, fileName.toString(), orderFolder);
						//Delete all files created once finished
						checkForExistingFiles(debugFlag);
						EasyGUI.setOrderIDTextField("");
				  }
			} catch (IOException e) {
				// Catch error and print stack
				e.printStackTrace();
			}
			//End if statement for digit check
			}
			//End if statement for listener

		}
		
	}
	/**
	 * Method takes the command and executes the process by
	 * creating a generic runtime and process to execute.
	 * @param command
	 */
	private void runBatchJob(String command){
		  Runtime genericRuntime = Runtime.getRuntime();
		  try {
			Process genericProcess = genericRuntime.exec(command);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Check for existing xml files and delete
	 * @param fileName
	 * @param orderFolder
	 */
	private void deleteFiles(String filename, File folder) {
	// Check for file in folder
		File checkFile = new File(folder+"/"+filename);
		if(checkFile.exists()){
			checkFile.delete();
		}
	}
	/**
	 * Take all the xml files and move them to the correct folders
	 * @param fileName
	 * @param orderFolder
	 */
	private void moveFiles(String fileName, File folder) {
		// File (or directory) to be moved
		File file = new File(fileName);

		// Move file to new directory
		file.renameTo(new File(folder, file.getName()));
	}
	/**
	 * Check to make sure Order is in the correct format
	 * @param orderID2
	 * @return
	 */
	private boolean checkOrderIDFormat(String orderID2) {
		Pattern digitPattern = Pattern.compile("\\d+");
		Matcher digitMatcher = digitPattern.matcher(orderID2);
		if(orderID2.length()!=10){
			JOptionPane.showMessageDialog(null, "Please enter correct order number lenght.\nEx. 5777870132", "Format Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(!digitMatcher.matches()){
			JOptionPane.showMessageDialog(null, "Please enter digits only.\nEx. 5777870132", "Format Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
	//Everything looks ok return true.
	return true;
	}
	/**
	 * Get all results 
	 * @param csixmlData2 
	 */
	private String gatherResults(ArrayList<String> csixmlData2) {
	//Get all results and format.
		String results="\r\n\r\nCSIXML RESULTS:\r\n\r\n";

			for(int i=0; i<csixmlData2.size();i++){

					results+=csixmlData2.get(i)+" \r\n";
			}
	return results;
	}
	/**
	 * Check for existing files and then delete them if they exist.
	 */
	public void checkForExistingFiles(Boolean debug) {
		  File result1Text = new File("results.txt");
		  File result2Text = new File("results2.txt");
		  File result3Text = new File("results3.txt");
		  File command1Text = new File("commands.txt");
		  File command2Text = new File("commands2.txt");
		  File command3Text = new File("ftpCommands.txt");
		  File output1Text = new File("output.txt");
		  File output2Text = new File("output2.txt");
		  File bat1File = new File("EasyConvID.bat");
		  File bat2File = new File("EasyBan.bat");
		  File bat3File = new File("EasyFtpFileCopy.bat");
		  if(!debug){
			  if(result1Text.exists()){
				  result1Text.delete();
			  }
			  if(result2Text.exists()){
				  result2Text.delete();
			  }
			  if(result3Text.exists()){
				  result3Text.delete();
			  }
			  if(bat1File.exists()){
				  bat1File.delete();
			  }
			  if(bat2File.exists()){
				  bat2File.delete();
			  }
			  if(bat3File.exists()){
				  bat3File.delete();
			  }
			  if(command1Text.exists()){
				  command1Text.delete();
			  }
			  if(command2Text.exists()){
				  command2Text.delete();
			  }
			  if(command3Text.exists()){
				  command3Text.delete();
			  }
			  if(output1Text.exists()){
				  output1Text.delete();	  
			  }
			  if(output2Text.exists()){
				  output2Text.delete();
			  }
		  }
		  
	  }
	/**
	 * Read in the message ID for other batch files
	 * @return 
	 */
	public static String getMessageID(){
		String messageIDFound="";

			try {
				FileReader input = new FileReader("output.txt");
				BufferedReader line = new BufferedReader(input);
				 String messageIDStr;
				while ((messageIDStr = line.readLine()) != null) {
					 //Strip out the message id
					 if(messageIDStr.contains("<cng:messageId>")){
						 messageIDFound = messageIDStr.split("</cng:messageId>")[0].split("<cng:messageId>")[1];
						 //messageIDFound= messageIDFound.substring(0, messageIDFound.length()-6);
					 }
				 }
				input.close();
				line.close();

			}catch (IOException e) {
				
			}
		
		return messageIDFound;
	}
	/**
	 * Check to see if file found if not display error
	 */
	public String checkLogForEmptyCSIXMLFileNames(String xml, int nameTagID){
		if(xml.equals("")){
			xml="File Not Found............................. "+csixmlLabels[nameTagID];
		}
		return xml;
	}
	/**
	 * Will start a timer to kill process after x amount of time
	 * @param convIDProcess 
	 * @return
	 */
	public void startTimeOutSession(int sessionTimeout){
	  
	    Timer timer = new Timer();      
		timer.schedule(new TimerTask(){
	          public void run(){
	            System.out.println("This process is killed.");

	  		  String killCommand = "cmd /C  taskkill /F /IM plink.exe ";
				  Runtime convIDRunTime = Runtime.getRuntime();
				  try {
					Process convIDProcess = convIDRunTime.exec(killCommand);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	          }
	        },sessionTimeout*1000);
	}
}