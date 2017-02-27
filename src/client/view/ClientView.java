/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

/**
 *
 * @author al1as
*/
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import client.model.BClientModel;
import client.model.IClientModel;
import client.controller.BClientController;
import client.controller.IClientController;
import client.controller.ILockObserver;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import server.model.Range;

/**
 *
 * @author al1as
 */
public class ClientView extends javax.swing.JFrame implements IObserver, IClientView {
    private JTextPane mainTextPane;
    private JLabel idLabel, statusLabel, usernameLabel;
    private JMenuItem openMenuItem, saveMenuItem, lockMenuItem, unlockMenuItem, refreshMenuItem;
    private StyledDocumentWithLocks mainDocument;
    private boolean isLocked = false; // Current state of document
    private int startSymbolNumber, endSymbolNumber;
    private int symbolsCount;
	private IClientController clientController;
	private IClientModel clientModel;

    /**
     * Creates new form MainFrame
     */
    public ClientView(IClientModel clientModel, IClientController clientController) {
        initComponents();
        this.clientController = clientController;
		this.clientModel = clientModel;
		this.setSize(700, 400); // Setting frame size
		mainDocument = new StyledDocumentWithLocks(clientController);
        // Adds current frame to model observers list
        clientModel.addObserver(this);
    }
	
	@Override
	public void showForm() {
		this.setVisible(true);
	}

	@Override
    public void createNumberedTextPane() {
        mainTextPane = new JTextPane();
        // adding locked document
        mainTextPane.setDocument(mainDocument);
//        mainTextPane.setText("Empty document");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mainTextPane, BorderLayout.CENTER);

        JScrollPane paneScrollPane = new JScrollPane(panel);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        TextLineNumber tln = new TextLineNumber(mainTextPane);
        paneScrollPane.setRowHeaderView(tln);
        this.getContentPane().add(paneScrollPane, BorderLayout.CENTER);
    }

	@Override
    public void createBottomLabels() {
        idLabel = new JLabel("Identifier");
        statusLabel = new JLabel("Status");
        usernameLabel = new JLabel("Username");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setVerticalAlignment(JLabel.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(idLabel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(usernameLabel, BorderLayout.EAST);

        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

	@Override
	public void setStatus(String status) {
		statusLabel.setText(status);
	}
	
	@Override
    public void showLoginDialog() {
        // Creates dialog's components and shows them
        JTextField login = new JTextField(10);
        JPasswordField password = new JPasswordField(10);
        final JComponent[] inputs = new JComponent[]{
            new JLabel("Login:"),
            login,
            new JLabel("Password:"),
            password
        };
        Object[] options = {"Login", "Register"};
        String dialogName = "Login or register before usage, please";
        JPanel panel = new JPanel();
        for (int i = 0; i < inputs.length; i++) {
            panel.add(inputs[i]);
        }
        // Shows dialog, variable result stores selected option
        int result = JOptionPane.showOptionDialog(null, panel, dialogName, JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
        String pass;
        switch (result) {
            case JOptionPane.YES_OPTION:
                pass = new String(password.getPassword());
                System.out.println("Authorization. User entered login:" + login.getText() + ", password hash:" + pass);
                // Sending to server
                clientController.loginUser(login.getText(), pass);
                break;
            case JOptionPane.NO_OPTION:
                pass = new String(password.getPassword());
                System.out.println("Registration. User entered login:" + login.getText() + ", password hash:" + pass);
                // Sending to server
                clientController.registerUser(login.getText(), pass);
                break;
            default:
                System.out.println("User closed login window, program stopped");
                System.exit(1);
                break;
        }
    }

    private void showFileOpenDialog(String[] data) {
        // Creates dialog's components and shows them; data - set of filenames sent from server
        JList fileList = new JList(data);
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        String dialogName = "Select one file, please";
        // Shows dialog, variable result stores selected option
        int result = JOptionPane.showConfirmDialog(null, scrollPane, dialogName, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String fileChosen = fileList.getSelectedValue().toString();
            System.out.println("User selected file: " + fileChosen);
            // Sending to server
            clientController.sendFileContentRequest(fileChosen);
        } else {
            System.out.println("User canceled / closed the dialog, result: " + result);
        }
    }

	@Override
    public void createMenu() {
        JMenuBar menuBar;
        JMenu menu;

        // Creates the menu bar
        menuBar = new JMenuBar();

        // Builds the File menu
        menu = new JMenu("File");
        menuBar.add(menu);

        // Cross-platform way of handling shortcuts for swing
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

        // A group of JMenuItems for File menu
        // Creates menu item for opening file
        openMenuItem = new JMenuItem("Open");
        // Adds cmd+O(ctrl+O for win users) shortcut for Open menu item
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcut));
        menu.add(openMenuItem);

        // Same actions for Save menu item
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
        menu.add(saveMenuItem);
        
        // Same actions for Refresh menu item
        refreshMenuItem = new JMenuItem("Refresh");
        refreshMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
        menu.add(refreshMenuItem);
        
        //Build the Lock menu in menubar
        menu = new JMenu("Lock");
        menuBar.add(menu);
        
        // Lock menu item
        lockMenuItem = new JMenuItem("Lock selected lines");
        lockMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
        menu.add(lockMenuItem);
        
        // Unlock menu item
        unlockMenuItem = new JMenuItem("Unlock");   
        unlockMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcut));
        menu.add(unlockMenuItem);
        
        this.setJMenuBar(menuBar);
    }
    
    @Override
    public void updateId() {
        idLabel.setText(clientModel.getId());
        statusLabel.setText("Successfully connected!");
		usernameLabel.setText("Please, authorize");
		showLoginDialog();
    }

    @Override
    public void updateFileList() {
        String fileString = clientModel.getFileList();
        System.out.println(fileString);
        String[] filenames = fileString.split("\\r?\\n");
        showFileOpenDialog(filenames);
    }

    @Override
    public void updateFileContent() {
        try {
            String content = clientModel.getFileContent();
            ArrayList<TextFragment> fragments = clientModel.getTextFragments();
            mainDocument.loadFileContent(fragments);
            mainTextPane.setEditable(false);
            symbolsCount = content.length();
            statusLabel.setText("File content successfully loaded!");
            System.out.println("File content successfully loaded!");
            
            lockMenuItem.setEnabled(true);
			refreshMenuItem.setEnabled(true);
        } catch (BadLocationException ex) {
            Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error while file content loading.");
        }
    }

    @Override
    public void updateSavingState() {
        statusLabel.setText("Current file sucessfully saved!");
        System.out.println("File saved on server");
        
        openMenuItem.setEnabled(true);
        saveMenuItem.setEnabled(false);
        refreshMenuItem.setEnabled(true);
        lockMenuItem.setEnabled(true);
        unlockMenuItem.setEnabled(false);
    }

    @Override
    public void updateRangesState() {
        // Getting all document from model and updating textPane
        try {
            mainTextPane.setEditable(true);
            mainDocument.loadFileContent(clientModel.getTextFragments());
            statusLabel.setText("Your lock successfully applied!");
            System.out.println("Your lock successfully applied!");
            
            openMenuItem.setEnabled(false);
            saveMenuItem.setEnabled(true);
            refreshMenuItem.setEnabled(false);
            lockMenuItem.setEnabled(false);
            unlockMenuItem.setEnabled(true);
        } catch (BadLocationException ex) {
            System.out.println("Error while lock applying.");
            Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void invalidLogin() {
        System.out.println("Invalid authorization data.");
        statusLabel.setText("Your login or/and password incorrect :(");
        showLoginDialog();
    }

    @Override
    public void invalidRange() {
        System.out.println("Invalid ranges.");
	statusLabel.setText("Invalid lock. Please, select unlocked range");
    }

    @Override
    public void invalidRegistration() {
        System.out.println("Invalid registration.");
        statusLabel.setText("Your registration login or/and password incorrect :(");
        showLoginDialog();
    }

    @Override
    public void updateRegistrationStatus() {
        System.out.println("Registration successful.");
        String username = clientModel.getUsername();
        statusLabel.setText("Registration successful!");
		usernameLabel.setText(username);
        
        openMenuItem.setEnabled(true);
        saveMenuItem.setEnabled(false);
        refreshMenuItem.setEnabled(false);
        lockMenuItem.setEnabled(false);
        unlockMenuItem.setEnabled(false);
    }

    @Override
	public void updateLoginStatus() {
		System.out.println("Login successful.");
		String username = clientModel.getUsername();
		statusLabel.setText("Login successful!");
		usernameLabel.setText(username);

		openMenuItem.setEnabled(true);
		saveMenuItem.setEnabled(false);
		refreshMenuItem.setEnabled(false);
		lockMenuItem.setEnabled(false);
		unlockMenuItem.setEnabled(false);
	}
	
    @Override
    public void updateUnlockingState() {
        statusLabel.setText("Successfully unlocked!");
        
        openMenuItem.setEnabled(true);
        saveMenuItem.setEnabled(false);
        refreshMenuItem.setEnabled(true);
        lockMenuItem.setEnabled(true);
        unlockMenuItem.setEnabled(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pack();
    }// </editor-fold>                        

	@Override
	public void addOpenListener(ActionListener openListener) {
		openMenuItem.addActionListener(openListener);
	}

	@Override
	public void addSaveListener(ActionListener saveListener) {
		saveMenuItem.addActionListener(saveListener);
	}

	@Override
	public void addRefreshListener(ActionListener refreshListener) {
		refreshMenuItem.addActionListener(refreshListener);
	}

	@Override
	public void addLockListener(ActionListener lockListener) {
		lockMenuItem.addActionListener(lockListener);
	}
	
	@Override
	public void addUnlockListener(ActionListener unlockListener) {
		unlockMenuItem.addActionListener(unlockListener);
	}

	@Override
	public String getSavingContent() {
		String content = "null";
		try {
			int startSymbol = clientController.getStartSymbolRange();
			int endSymbol = clientController.getEndSymbolRange();

			content = mainDocument.getText(startSymbol, endSymbol - startSymbol + 1);
			return content;
		} catch (BadLocationException ex) {
			Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("Modified content parsing/sending error.");
		}
		return content;
	}

	@Override
	public int getStartLockPos() {
		int startLockPos = -1;
		try {
			startLockPos =  Utilities.getRowStart(mainTextPane, mainTextPane.getSelectionStart());
		} catch (BadLocationException ex) {
			Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("Start lock position parsing error!");
		}
		return startLockPos;
	}

	@Override
	public int getEndLockPos() {
		int endLockPos = -1;
		try {
			endLockPos = Utilities.getRowEnd(mainTextPane, mainTextPane.getSelectionEnd());
			// By default JTextPane adds extra newline charater at the end of each paragraph
			if (endLockPos == symbolsCount) {
				endLockPos--;
			}
		} catch (BadLocationException ex) {
			Logger.getLogger(ClientView.class.getName()).log(Level.SEVERE, null, ex);
			System.out.println("End lock position parsing error!");
		}
		return endLockPos;
	}

	@Override
	public void addLockObserver(ILockObserver obs) {
		mainDocument.addLockObserver(obs);
	}
}