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
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.text.BadLocationException;
import javax.swing.text.Utilities;
import server.model.Range;

/**
 *
 * @author al1as
 */
class MainFrame extends javax.swing.JFrame implements IObserver {
    private JTextPane mainTextPane;
    private JLabel idLabel, statusLabel, previousFilenameLabel;
    private JMenuItem openMenuItem, saveMenuItem, lockMenuItem, unlockMenuItem, refreshMenuItem;
    private StyledDocumentWithLocks mainDocument = new StyledDocumentWithLocks();
    private boolean isLocked = false; // Current state of document
    private int startSymbolNumber, endSymbolNumber;
    private int symbolsCount;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        this.setSize(700, 400); // Setting frame size

        createNumberedTextPane();
        createMenu();
        createBottomLabels();

        // Adds current frame to model observers list
        IClientModel clientModel = BClientModel.build();
        clientModel.addObserver(this);

        // Connects current client to server
        IClientController clientController = BClientController.build();
        clientController.connect();

        this.addWindowListener(new WindowAdapter() {
            // Invoked when a window has been opened.
            public void windowOpened(WindowEvent e) {
                System.out.println("Window opened event, showing user login dialog");
                showLoginDialog();
            }
        });

    }

    private void createNumberedTextPane() {
        mainTextPane = new JTextPane();
        // adding locked document
        mainTextPane.setDocument(mainDocument);
        mainTextPane.setText("Empty document");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(mainTextPane, BorderLayout.CENTER);

        JScrollPane paneScrollPane = new JScrollPane(panel);
        paneScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        TextLineNumber tln = new TextLineNumber(mainTextPane);
        paneScrollPane.setRowHeaderView(tln);
        this.getContentPane().add(paneScrollPane, BorderLayout.CENTER);
    }

    private void createBottomLabels() {
        idLabel = new JLabel("Identifier");
        statusLabel = new JLabel("Status");
        previousFilenameLabel = new JLabel("Previous filename");
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setVerticalAlignment(JLabel.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(idLabel, BorderLayout.WEST);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        bottomPanel.add(previousFilenameLabel, BorderLayout.EAST);

        this.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private String MD5(String md5) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(md5.getBytes());
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.out.println("Problem with md5 alghorithm occured. Null string returned.");
        }
        return null;
    }

    private void showLoginDialog() {
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
        String passHash;
        IClientController controller;
        switch (result) {
            case JOptionPane.YES_OPTION:
                passHash = MD5(new String(password.getPassword()));
                System.out.println("Authorization. User entered login:" + login.getText() + ", password hash:" + passHash);
                // Sending to server
                controller = BClientController.build();
                controller.loginUser(login.getText(), passHash);
                break;
            case JOptionPane.NO_OPTION:
                passHash = MD5(new String(password.getPassword()));
                System.out.println("Registration. User entered login:" + login.getText() + ", password hash:" + passHash);
                // Sending to server
                controller = BClientController.build();
                controller.registerUser(login.getText(), passHash);
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
            IClientController clientController = BClientController.build();
            clientController.sendFileContentRequest(fileChosen);
        } else {
            System.out.println("User canceled / closed the dialog, result: " + result);
        }
    }

    private void createMenu() {
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
        // Adds action for Open menu item - sends file list request to server
        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                clientController.sendFileListRequest();
            }
        });
        menu.add(openMenuItem);

        // Same actions for Save menu item
        saveMenuItem = new JMenuItem("Save");
        saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, shortcut));
        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    IClientController clientController = BClientController.build();
                    int startSymbol = clientController.getStartSymbolRange();
                    int endSymbol = clientController.getEndSymbolRange();

                    String content = "";
                    content = mainDocument.getText(startSymbol, endSymbol - startSymbol + 1);

                    clientController.sendSaveRequest(content);
                    System.out.println("Modified content sent, here it is:\n" + content);
                } catch (BadLocationException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Modified content parsing/sending error.");
                }
            }
        });
        menu.add(saveMenuItem);
        
        // Same actions for Refresh menu item
        refreshMenuItem = new JMenuItem("Refresh");
        refreshMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut));
        refreshMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                clientController.refreshFileContent();
            }
        });
        menu.add(refreshMenuItem);
        
        //Build the Lock menu in menubar
        menu = new JMenu("Lock");
        menuBar.add(menu);
        
        // Lock menu item
        lockMenuItem = new JMenuItem("Lock selected lines");
        lockMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, shortcut));
        lockMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //  Here's going selection ranges analyzing part
                System.out.println("Getting selected lines range:\n");
                try {
                    startSymbolNumber = Utilities.getRowStart(mainTextPane, mainTextPane.getSelectionStart());
                    endSymbolNumber = Utilities.getRowEnd(mainTextPane, mainTextPane.getSelectionEnd());
                    // By default JTextPane adds extra newline charater at the end of each paragraph
                    if (endSymbolNumber == symbolsCount) {
                        endSymbolNumber--;
                    }
                    IClientController clientController = BClientController.build();
                    clientController.sendRangesAndLock(startSymbolNumber, endSymbolNumber);
                    System.out.println("Lock was pased and sended to server.");
                } catch (BadLocationException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("Error while parsing/sending lock.");
                }
            }
        });
        menu.add(lockMenuItem);
        
        // Unlock menu item
        unlockMenuItem = new JMenuItem("Unlock");   
        unlockMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcut));
        unlockMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                clientController.sendUnlocking();
                System.out.println("Unlocking message was sent and file content in Text pane updated.");
            }
        });
        menu.add(unlockMenuItem);
        
        this.setJMenuBar(menuBar);
    }
    
    @Override
    public void updateId() {
        IClientModel model = BClientModel.build();
        idLabel.setText(model.getId());
        statusLabel.setText("Successfully connected!");
    }

    @Override
    public void updatePrevFilename() {
        IClientModel model = BClientModel.build();
        previousFilenameLabel.setText(model.getPrevFilename());
        statusLabel.setText("Log-in successful! Previous filename loaded.");
        
        openMenuItem.setEnabled(true);
        saveMenuItem.setEnabled(false);
        refreshMenuItem.setEnabled(false);
        lockMenuItem.setEnabled(false);
        unlockMenuItem.setEnabled(false);
    }

    @Override
    public void updateFileList() {
        IClientModel clientModel = BClientModel.build();
        String fileString = clientModel.getFileList();
        System.out.println(fileString);
        String[] filenames = fileString.split("\\r?\\n");
        showFileOpenDialog(filenames);
    }

    @Override
    public void updateFileContent() {
        try {
            IClientModel clientModel = BClientModel.build();
            String content = clientModel.getFileContent();
            ArrayList<TextFragment> fragments = clientModel.getTextFragments();
            mainDocument.loadFileContent(fragments);
            mainTextPane.setEditable(false);
            symbolsCount = content.length();
            statusLabel.setText("File content successfully loaded!");
            System.out.println("File content successfully loaded!");
            
            lockMenuItem.setEnabled(true);
        } catch (BadLocationException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
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
            IClientModel clientModel = BClientModel.build();
            Range tmp = new Range(startSymbolNumber, endSymbolNumber);
            ArrayList<Range> ranges = new ArrayList<Range>();
            ranges.add(tmp);
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
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void invalidUsername() {
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
        IClientModel model = BClientModel.build();
        String username = model.getUsername();
        statusLabel.setText("Registration successful! Your login:" + username);
        
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        // if user prefers macOS, our jmenu will look native
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                new MainFrame().setVisible(true);
            }
        });
    }
}
