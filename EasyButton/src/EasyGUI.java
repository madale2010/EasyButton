import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class EasyGUI {
	private JFrame frame = new JFrame();
	//Create all panel componants 
	private JPanel mainPanel = new JPanel();
	private JCheckBox orderHandoffCheck = new JCheckBox("Do you need orderHandoff?");
	private JLabel enterOrderIDLabel = new JLabel("Please enter orderID: ");
	private JLabel enterEnvLabel = new JLabel("Please select which environment: ");
	private JLabel enterFlowLabel = new JLabel("Please slect which flow: ");
	private JLabel enterOrderLabel = new JLabel("Please slect which order type: ");
	//private JButton submit = new JButton("TEST");
	private JButton submit = new JButton("!EASY!");
	
	private static JTextField orderIDTextField = new JTextField(10);
	private static JCheckBox csixmlCheck = new JCheckBox("Do you need CSIXML files?");
	private static String [] flowType = {"A","B"};
	private static String [] envirType = {"FST1","FST2", "FST3", "FST5", "FST7", "DEV3"};
	private static String [] orderType = {"Provide", "Modify"};
	private static JComboBox flowTypeComboBox = new JComboBox(flowType);
	private static JComboBox envirTypeComboBox = new JComboBox(envirType);
	private static JComboBox orderTypeComboBox = new JComboBox(orderType);
	private EasyButton actionListener = new EasyButton();
	public EasyGUI() {
	//Create frames and size for main program
	  	
	  	frame.setTitle("EasyButton by CGI (eCommerce) Version 1.0.5.2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.setSize(400, 400);
		frame.setLocation(100,200);
		frame.setResizable(false);
		
		GridBagConstraints mainC = new GridBagConstraints();  	 
        mainPanel.setLayout(new GridBagLayout());
        mainC.fill = GridBagConstraints.HORIZONTAL;
        
        mainC.gridx = 0;
        mainC.gridy = 0;      
        mainPanel.add(enterEnvLabel, mainC);        
        mainC.gridx = 2;
        mainC.gridy = 0;
        mainC.insets = new Insets(0,30,10,0);
        mainPanel.add(envirTypeComboBox, mainC);
        envirTypeComboBox.addActionListener(actionListener);
        mainC.gridx = 0;
        mainC.gridy = 1;
        mainC.insets = new Insets(0,0,10,0);      
        mainPanel.add(enterFlowLabel, mainC);
        mainC.gridx = 2;
        mainC.gridy = 1;
        mainC.insets = new Insets(0,30,10,0);
        mainPanel.add(flowTypeComboBox, mainC); 
        flowTypeComboBox.addActionListener(actionListener);
        
        mainC.gridx = 0;
        mainC.gridy = 2;
        mainC.insets = new Insets(0,0,10,0);
        mainPanel.add(enterOrderLabel, mainC);
        mainC.gridx = 2;
        mainC.gridy = 2;
        mainC.insets = new Insets(0,30,10,0);
        mainPanel.add(orderTypeComboBox, mainC);
        orderTypeComboBox.addActionListener(actionListener);
  
        mainC.gridx = 0;
        mainC.gridy = 3;
        mainC.insets = new Insets(0,0,10,0);
        mainPanel.add(enterOrderIDLabel, mainC);
        mainC.gridx = 2;
        mainC.gridy = 3;
        mainC.insets = new Insets(0,30,10,0);
        mainPanel.add(orderIDTextField, mainC);
        orderIDTextField.addActionListener(actionListener);
 
        mainC.gridx = 0;
        mainC.gridy = 4;
        mainC.insets = new Insets(0,0,10,0);
        mainPanel.add(csixmlCheck, mainC);
        //csixmlCheck.setSelected(true);
        csixmlCheck.addActionListener(actionListener);
        mainC.gridx = 2;
        mainC.gridy = 4;
        mainC.insets = new Insets(0,30,10,0);
        mainPanel.add(orderHandoffCheck, mainC);
        orderHandoffCheck.addActionListener(actionListener);
        mainC.gridx = 1;
        mainC.gridy = 5;
        mainC.fill = GridBagConstraints.CENTER;
        mainPanel.add(submit, mainC);  
        submit.addActionListener(actionListener);
        
        //Finish frame layout
        frame.add(mainPanel);
        frame.pack();
		frame.setVisible(true);
	}
	/**
	 * Gets the value of the environment type.
	 * @return
	 */
	public static String getEnvirTypeComboBox(){
		return envirTypeComboBox.getSelectedItem().toString();
	}
	/**
	 * Gets the order ID value.
	 * @return
	 */
	public static String getOrderIDTextField(){
		return orderIDTextField.getText();
	}
	/**
	 * Sets the order ID fields based on text input.
	 * @param text
	 */
	public static void setOrderIDTextField(String text){
		orderIDTextField.setText(text);
	}
	/**
	 * Gets the order type value.
	 * 
	 * @return
	 */
	public static String getOrderTypeComboBox(){
		return orderTypeComboBox.getSelectedItem().toString();
	}
	/**
	 * Gets the value of the Flow type.
	 * 
	 * @return
	 */
	public static String getFlowTypeComboBox(){
		return flowTypeComboBox.getSelectedItem().toString();
	}
	/**
	 * Gets the value of the CSIXML flag and returns
	 * true or false.
	 * @return
	 */
	public static boolean isCSIXMLSelected(){
		if(csixmlCheck.isSelected()){
			return true;
		}
		else {
			return false;
		}
	}
}