import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;



public class ShowPopUpWindow {
	
	private static JFrame popUpFrame;
	private static EasyButton actionListener = new EasyButton();
	
	public ShowPopUpWindow(){
		
	}
	
	/**
	 * Creates the results window based on what was returned from search
	 * @param string
	 */
	public static void showPopUp(String orderID, String convID, String ban, String csixml, String auto, String fileName, File orderFolder) {
		// TODO Auto-generated method stub
		popUpFrame = new JFrame();
		JTextArea resultsArea = new JTextArea(17, 50);
		JScrollPane scrollPane = new JScrollPane(resultsArea);
		JTextArea autoResultsArea = new JTextArea(10, 25);
		JScrollPane autoScrollPane = new JScrollPane(autoResultsArea);
		JPanel popUpPanel = new JPanel();
        JButton close = new JButton("Close");

        //Only if something was found will we print this label
        JLabel savedFileLabel, savedPathLabel, savedPathNameLabel;
        popUpFrame.setTitle("Results Found");
        popUpFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        popUpFrame.setVisible(true);
        popUpFrame.setSize(200, 200);
        popUpFrame.setLocation(100,25);
		popUpFrame.setResizable(false);
		


		//Configure text here
		resultsArea.setText("Order ID:                   "+orderID+"\n"
						   +"Conversation ID:     "+convID+"\n"
						   +"Ban:                           "+ban+"\n"
						   +csixml
		);
		resultsArea.setEditable(false);
		resultsArea.setBorder(null);
		resultsArea.setForeground(UIManager.getColor("Label.foreground"));
		//resultsArea.setBackground(getBackground());
		resultsArea.setFont(UIManager.getFont("Label.font"));
		resultsArea.setCaretPosition(0);
		//Configure auto text here
		
		autoResultsArea.setText("AUTOMATION RESULTS:        \n\n"+auto);
		autoResultsArea.setEditable(false);
		autoResultsArea.setBorder(null);
		autoResultsArea.setForeground(UIManager.getColor("Label.foreground"));
		//autoResultsArea.setBackground(getBackground());
		autoResultsArea.setFont(UIManager.getFont("Label.font"));
		autoResultsArea.setCaretPosition(0);
		
		GridBagConstraints mainC = new GridBagConstraints();  	 
		popUpPanel.setLayout(new GridBagLayout());
        mainC.fill = GridBagConstraints.HORIZONTAL;
		mainC.gridx = 0;
        mainC.gridy = 0; 
		popUpPanel.add(scrollPane, mainC);
		
		mainC.gridy = 1;  
		mainC.insets = new Insets(20,0,0,0);
		popUpPanel.add(autoScrollPane, mainC);
		mainC.insets = new Insets(0,0,0,0);
		
		if(convID.equals("Not Found")){
			mainC.gridy = 2;
			mainC.insets = new Insets(20,240,10,240);
			popUpPanel.add(close, mainC);
		}
		else
		{
			mainC.gridy = 2;
			mainC.insets = new Insets(10,0,0,0);
			savedFileLabel = new JLabel("Your results have been saved as \""+fileName+"\"");
			popUpPanel.add(savedFileLabel, mainC);
			mainC.insets = new Insets(0,0,0,0);
			mainC.gridy = 3; 
			savedPathLabel= new JLabel("All files have been saved to: ");
			popUpPanel.add(savedPathLabel, mainC);
			
			mainC.gridy = 4; 
			savedPathNameLabel= new JLabel(orderFolder.getAbsolutePath());
			popUpPanel.add(savedPathNameLabel, mainC);
			
			mainC.gridy = 5;
			mainC.insets = new Insets(20,240,10,240);
			popUpPanel.add(close, mainC);
		}
		

		close.addActionListener(actionListener);
		popUpFrame.add(popUpPanel);
		popUpFrame.pack();
		
	}
public static void closeWindow(){
	popUpFrame.dispose();
}
}