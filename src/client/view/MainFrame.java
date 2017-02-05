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
import javax.swing.JList;
/**
 *
 * @author al1as
 */
public class MainFrame extends javax.swing.JFrame implements IObserver {
    JTextPane mainTextPane;
    JLabel idLabel, statusLabel, previousFilenameLabel;
    JMenuItem openMenuItem;
    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        
        initComponents();
        this.setSize(600, 400); // Setting frame size
        
        createNumberedTextPane();          
        createMenu();
        createBottomLabels();
        
        appendString("", mainTextPane);
        
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
        JTextField firstName = new JTextField();
        JTextField lastName = new JTextField();
        JPasswordField password = new JPasswordField();
        final JComponent[] inputs = new JComponent[]{
            new JLabel("First"),
            firstName,
            new JLabel("Last"),
            lastName,
            new JLabel("Password"),
            password
        };
        String dialogName = "Login before usage, please";
        int result = JOptionPane.showConfirmDialog(null, inputs, dialogName, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            System.out.println("You entered "
                    + firstName.getText() + ", "
//                    + lastName.getText() + ", "
                    + password.getPassword());
            IClientController controller = BClientController.build();
            controller.sendName(firstName.getText());
        } else {
            System.out.println("User canceled / closed the dialog, result = " + result);
            System.exit(1);
        }
    }
    
    private void showFileOpenDialog(String[] data) {
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
            IClientController clientController = BClientController.build();
            clientController.sendFileContentRequest(fileList.getSelectedValue().toString());
        } else {
            System.out.println("User canceled / closed the dialog, result = " + result);
        }
    }
    
    private void appendString(String str, JTextPane textPane) {
        StyledDocument doc = textPane.getStyledDocument();

//  Define a keyword attribute
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        StyleConstants.setForeground(keyWord, Color.GRAY);
//        StyleConstants.setBackground(keyWord, Color.YELLOW);
//        StyleConstants.setBold(keyWord, true);

//  Add some text
        try {
            doc.insertString(0, "Start of text\n", keyWord);
            doc.insertString(doc.getLength(), "\nEnd of text", null);
        } catch (Exception e) {
            System.out.println(e);
        }
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
        mainTextPane.setText(content);
        mainTextPane.setEditable(false);
    }

    @Override
    public void updateSavingState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateRangesState() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
