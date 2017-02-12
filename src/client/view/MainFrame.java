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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import client.model.BClientModel;
import client.model.IClientModel;
import client.controller.BClientController;
import client.controller.IClientController;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JList;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Utilities;
import server.model.Range;
/**
 *
 * @author al1as
 */
public class MainFrame extends javax.swing.JFrame implements IObserver {
    JTextPane mainTextPane;
    JLabel idLabel, statusLabel, previousFilenameLabel;
    JMenuItem openMenuItem, saveMenuItem, lockMenuItem, unlockMenuItem;
    StyledDocumentWithLocks mainDocument = new StyledDocumentWithLocks();
    boolean isLocked = false; // Current state of document
    int startLineNumber, endLineNumber;
    int startSymbolNumber, endSymbolNumber;
    int symbolsCount;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        
        initComponents();
        this.setSize(600, 400); // Setting frame size
        
        createNumberedTextPane();          
        createMenu();
        createBottomLabels();
        
        IClientModel clientModel = BClientModel.build();
        clientModel.addObserver(this);
        
        IClientController clientController = BClientController.build();
        clientController.connect();
        
        this.addWindowListener(new WindowAdapter() {
            //
            // Invoked when a window has been opened.
            //
            public void windowOpened(WindowEvent e) {
                System.out.println("Window Opened Event");
                showLoginDialog();
            }
        });
        
        
    }
    
    private void createNumberedTextPane() {
        mainTextPane = new JTextPane();
        // adding locked document
        mainTextPane.setDocument(mainDocument);
        mainTextPane.setText("Hello there, I'm Ernie!");

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
    
    private void showLoginDialog() {
        // Create dialog and show
        JTextField login = new JTextField(10);
        JPasswordField password = new JPasswordField(10);
        final JComponent[] inputs = new JComponent[]{
            new JLabel("Login"),
            login,
            new JLabel("Password"),
            password
        };
        Object[] options = { "Login", "Register" };
        String dialogName = "Login or register before usage, please";
        JPanel panel = new JPanel();
        for(int i = 0; i < 4; i++)
            panel.add(inputs[i]);
        int result = JOptionPane.showOptionDialog(null, panel, dialogName,
        JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE,
        null, options, options[0]);
        if (result == JOptionPane.YES_OPTION){
            System.out.println("You entered "
                    + login.getText() + ", "
                    //                    + lastName.getText() + ", "
                    + password.getPassword());
            // Sending to server
            IClientController controller = BClientController.build();
            controller.sendName(login.getText());
        } else {
            if(result == JOptionPane.NO_OPTION) {
                // add here method for registration
            } else {
                // user closed dialog
                System.out.println("User canceled / closed the dialog, result = " + result);
                System.exit(1);
            }
        }
    }
    
    private void showFileOpenDialog(String[] data) {
        // Create dialog's components and show it
        JList fileList = new JList(data);
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        final JComponent[] inputs = new JComponent[]{
            scrollPane
        };
        int result = JOptionPane.showConfirmDialog(null, inputs, "My custom dialog", JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            System.out.println("You entered "
                    + fileList.getSelectedValue());
            // Sending to server
            IClientController clientController = BClientController.build();
            clientController.sendFileContentRequest(fileList.getSelectedValue().toString());
        } else {
            System.out.println("User canceled / closed the dialog, result = " + result);
        }
    }
    
    private void appendLockedString(String str, JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();

//  Define a keyword attribute
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.GRAY);

//        StyleConstants.setBackground(keyWord, Color.GRAY);
//        StyleConstants.setBold(keyWord, true);

//  Add some text
        try {
            doc.insertString(doc.getLength(), str, keyWord);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Appended locked string to mainTextPane\n");
    }
 
    private void appendUnlockedString(String str, JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();

//  Define a keyword attribute
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.BLACK);
//        StyleConstants.setBackground(keyWord, Color.YELLOW);
//        StyleConstants.setBold(keyWord, true);

//  Add some text
        try {
            doc.insertString(doc.getLength(), str, keyWord);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Appended unlocked string to mainTextPane\n");
    }
    
    private int caretPositionToLineNumber(int caretPosition, JTextPane textPane) {
        int lineNumber = (caretPosition == 0) ? 1 : 0;
        try {
        for (int offset = caretPosition; offset > 0;) {
            offset = Utilities.getRowStart(textPane, offset) - 1;
            lineNumber++;
        }   } catch (BadLocationException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        System.out.println("Current line number: " + lineNumber);
        return lineNumber;
    }
    
    private void createMenu() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;

//Create the menu bar.
        menuBar = new JMenuBar();

//Build the first menu.
        menu = new JMenu("File");
        menuBar.add(menu);

//a group of JMenuItems
        openMenuItem = new JMenuItem("Open",
                KeyEvent.VK_O);
        menu.add(openMenuItem);
        
        openMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                clientController.sendFileListRequest();
            }
        });

        saveMenuItem = new JMenuItem("Save",
                KeyEvent.VK_O);
        menu.add(saveMenuItem);

        saveMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                String content = "";
                try {
                    int startSymbol = clientController.getStartSymbolRange();
                    int endSymbol = clientController.getEndSymbolRange();
                    content = mainTextPane.getDocument().getText(startSymbol, endSymbol - startSymbol + 1);
                } catch (BadLocationException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                System.out.println("Text to save sended, here it is:" + content);
                clientController.sendSaveRequest(content);
            }
        });
        
        JMenuItem closeMenuItem = new JMenuItem("Close",
            KeyEvent.VK_O);
        menu.add(closeMenuItem);
        
        closeMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                clientController.sendUnlocking();
                System.exit(0);
            }
        });
        
//Build second menu in the menu bar.
        menu = new JMenu("Lock");
        menuBar.add(menu);
        
        lockMenuItem = new JMenuItem("Lock selected lines",
                KeyEvent.VK_O);
        menu.add(lockMenuItem);
        
        lockMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //  Here's going selection ranges analyzing part
                System.out.println("Getting selected lines range:\n");
                try {
                    startSymbolNumber = Utilities.getRowStart(mainTextPane, mainTextPane.getSelectionStart());
                    endSymbolNumber = Utilities.getRowEnd(mainTextPane, mainTextPane.getSelectionEnd());
                    // By default JTextPane adds newline charater at the end of each paragraph
                    if (endSymbolNumber == symbolsCount)
                        endSymbolNumber--;
                    startLineNumber = caretPositionToLineNumber(mainTextPane.getSelectionStart(), mainTextPane);
                    endLineNumber = caretPositionToLineNumber(mainTextPane.getSelectionEnd(), mainTextPane);
                } catch (BadLocationException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
               
                IClientController clientController = BClientController.build();
                clientController.sendRangesAndLock(Integer.toString(startLineNumber), Integer.toString(endLineNumber), startSymbolNumber,
                        endSymbolNumber);

//                int start = mainTextPane.getSelectionStart();
//                int end = mainTextPane.getSelectionEnd();
//                System.out.println("Selection start: " + start + ", end: " + end);
                
//                IClientController clientController = BClientController.build();
//                clientController.sendFileListRequest();
            }
        });
        
        unlockMenuItem = new JMenuItem("Unlock",
                KeyEvent.VK_O);
        menu.add(unlockMenuItem);

        unlockMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IClientController clientController = BClientController.build();
                clientController.sendUnlocking();
                System.out.println("Send unlocking message");

            }
        });
        
        this.setJMenuBar(menuBar);
    }
    
    private void createMenuSample() {
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;

//Create the menu bar.
        menuBar = new JMenuBar();

//Build the first menu.
        menu = new JMenu("A Menu");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");
        menuBar.add(menu);

//a group of JMenuItems
        menuItem = new JMenuItem("A text-only menu item",
                KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_1, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
                "This doesn't really do anything");
        menu.add(menuItem);

        menuItem = new JMenuItem("Both text and icon",
                new ImageIcon("images/middle.gif"));
        menuItem.setMnemonic(KeyEvent.VK_B);
        menu.add(menuItem);

        menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
        menuItem.setMnemonic(KeyEvent.VK_D);
        menu.add(menuItem);

//a group of radio button menu items
        menu.addSeparator();
        ButtonGroup group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Another one");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

//a group of check box menu items
        menu.addSeparator();
        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
        cbMenuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        menu.add(cbMenuItem);

//a submenu
        menu.addSeparator();
        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_2, ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);
        menu.add(submenu);

//Build second menu in the menu bar.
        menu = new JMenu("Another Menu");
        menu.setMnemonic(KeyEvent.VK_N);
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu does nothing");
        menuBar.add(menu);
        
        this.setJMenuBar(menuBar);
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
        if (System.getProperty("os.name").contains("Mac"))
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    // End of variables declaration                   

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
        IClientModel clientModel = BClientModel.build();
        String content = clientModel.getFileContent();
        ArrayList<TextFragment> fragments = clientModel.getTextFragments();
        try {
            mainDocument.loadFileContent(fragments);
        } catch (BadLocationException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        mainTextPane.setEditable(false);
        symbolsCount = content.length();
    }

    @Override
    public void updateSavingState() {
        statusLabel.setText("Successufly saved current file!");
        System.out.println("File saved on server");
    }

    @Override
    public void updateRangesState() {
        // Getting all document from model and updating textPane
        IClientModel clientModel = BClientModel.build();
        String lockedPart1 = clientModel.getLockedPart1();
        String unlockedPart = clientModel.getUnlockedPart();
        String lockedPart2 = clientModel.getLockedPart2();
//        appendLockedString(clientModel.getLockedPart1(), mainTextPane);
//        appendUnlockedString(clientModel.getUnlockedPart(), mainTextPane);
//        appendLockedString(clientModel.getLockedPart2(), mainTextPane);
        
        String finalString = lockedPart1 + unlockedPart + lockedPart2;
        Range tmp = new Range(startSymbolNumber, endSymbolNumber);
        ArrayList<Range> ranges = new ArrayList<Range>();
        ranges.add(tmp);
        try {
            mainDocument.loadFileContent(clientModel.getTextFragments());
        } catch (BadLocationException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
        mainTextPane.setEditable(true);

    }

    @Override
    public void invalidUsername() {
        statusLabel.setText("Your login or/and passowrd incorrect :(");
        showLoginDialog();
    }

    @Override
    public void invalidRange() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
