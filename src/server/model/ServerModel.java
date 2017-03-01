/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import server.presenter.IServerPresenter;

/**
 *
 * @author al1as
 */
public class ServerModel implements IServerModel {
    DefaultTableModel tableModel;
    ServerSocket ss;
    Hashtable<UUID, FileElement> allClient = new Hashtable<UUID, FileElement>();
    Hashtable<String, ArrayList> allFiles = new Hashtable<String, ArrayList>();
    ArrayList Names = null;
    String message = "null";
    ArrayList<IServerPresenter> list_p = new ArrayList();
    
    public ServerModel(DefaultTableModel tableModel) {
        this.tableModel = tableModel;
    }  
    
    @Override
    public synchronized void addToTable(String filename, FileElement element) {
        int row_num = getRowByValue(tableModel, element.username);
        Object[] row = { filename, Integer.toString(element.range.getStart()) + "-" +
                Integer.toString(element.range.getEnd()), element.username, element.getUUID().toString()
                };
        if(row_num == -1)  
            tableModel.addRow(row);
        else {
            tableModel.removeRow(row_num);
            tableModel.insertRow(row_num, row);
        }
    }
    
    int getRowByValue(TableModel model, Object value) {
        for (int i = model.getRowCount() - 1; i >= 0; --i) {
            for (int j = model.getColumnCount() - 1; j >= 0; --j) {
                if(model.getValueAt(i, j) == null)
                    continue;
                if (model.getValueAt(i, j).equals(value)) {
                    // what if value is not unique?
                    return i;
                }
            }
        }
        return -1;
    }
    
    @Override
    public synchronized void delFromTable(String filename, FileElement element) {
		// Is a better way to store here not JTable, but AbstractTableModel, do this things and then fire 
		// tableModel.fireTableDataChanged(); or fireTableRowsDeleted and fireTableRowsInserted(it is better way, ok)
        int row_num = getRowByValue(tableModel, element.username);
        Object[] row = { filename, Integer.toString(element.range.getStart()) + "-" +
                Integer.toString(element.range.getEnd()), element.username, element.getUUID().toString()
                };
        if(row_num == -1)  
            System.out.println("Error: user not found!");
        else 
            tableModel.removeRow(row_num);
    }
    
    @Override
    public synchronized boolean putFileElement(String filename, FileElement element) {
        ArrayList clients = allFiles.get(filename);
        if(clients == null) {
            clients = new ArrayList();
            clients.add(element);
            allFiles.put(filename, clients); 
            allClient.put(element.getUUID(), element);
        } else {
            int start = element.getStart();
            int end = element.getEnd();
            for(int i = 0; i < clients.size(); i++)
            {
                FileElement tmp = (FileElement)clients.get(i);
                if(!((start < tmp.getStart() && end < tmp.getStart()) ||
                        (start > tmp.getEnd() && end > tmp.getEnd())))
                {
                    return false;
                }              
            }
            clients.add(element);
            allClient.put(element.getUUID(), element);
        }
        this.addToTable(filename, element);
        return true;
    }
    
    @Override
    public synchronized void extractFileElement(String filename, FileElement element) {
        ArrayList clients = allFiles.get(filename);
        int index = clients.indexOf(element);
        clients.remove(index);//works
        if(clients.size() == 0) {
            allFiles.remove(filename);
        }
        delFromTable(filename, element);
        allClient.remove(element.getUUID());
    }
    
    @Override
    public synchronized boolean addName(String username) {
        boolean res = true;
        if(Names == null) {
            Names = new ArrayList();
            Names.add(username);
        } else 
        if(Names.indexOf(username) == -1) {
            Names.add(username);
        } else 
            res = false;
        return res;
    }
    
    @Override
    public void removeName(String username) {
        Names.remove(username);
    }
    
    @Override
    public String listFilesForFolder(final File folder) {
        String result = "";
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listFilesForFolder(fileEntry);
            } else {
                result += fileEntry.getName() + "\n";
            }
        }
        return result;
    }

    @Override
    public String getFileContent(String FileName) {
        String s = "";
        try {
            String fullname = "Shared/" + FileName;
            FileReader fr = new FileReader(new File(fullname));
            
            int c;
            while ((c = fr.read()) >= 0) {
                s += (char) c;
            }
        } catch (IOException ex) {
            Logger.getLogger(ServerModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }
    
    @Override
    public ArrayList getFileRanges(String filename) {
        ArrayList<Range> result = new ArrayList<Range>();
        ArrayList<FileElement> fileUsers = allFiles.get(filename);
        if (fileUsers == null)
            return result;
        for(int i = 0; i < fileUsers.size(); i++) {
            FileElement tmpFileElement = (FileElement)fileUsers.get(i);
            result.add(tmpFileElement.range);
        }
        // Sorting ranges by start
        Collections.sort(result, new Comparator<Range>() {
            @Override
            public int compare(Range left, Range right) {
                if (left.getStart() > right.getStart()) {
                    return 1;
                }
                if (left.getStart() < right.getStart()) {
                    return -1;
                }
                return 0;
            }
        });
        return result;
    }
    
    @Override
    public void Save(String lockedContent, String filename, UUID id, int oldEnd) {
        FileElement element = allClient.get(id);
        String fullname = "Shared/" + filename;
        String fileContent = getFileContent(filename) + '\n';
        String outContent = "";
        int i, lineNumber = 0, k = 0;
        for(i = fileContent.indexOf('\n', 0); lineNumber < oldEnd-1; i = fileContent.indexOf('\n', i+1)) {
            if(lineNumber < element.getStart() - 1)
                outContent += fileContent.substring(k, i+1);
            k = i+1;
            lineNumber++;
        }
        outContent += lockedContent;
        if(i+1 < fileContent.length())
            outContent += fileContent.substring(i+1, fileContent.length() - 1);
        i = outContent.length()-1;
        while(outContent.charAt(i) == '\n') {
            outContent = outContent.substring(0, i);
            i--;
        }
        try {
            PrintWriter printer = new PrintWriter(fullname);
            printer.print(outContent);
            printer.close();
        } catch (IOException e) {
        }
    }
    
    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public void sendMessage(String message) {
        this.message = message;
        refresh();
    }
    
    @Override
    public void refresh() {
        for (IServerPresenter presenter : list_p) {
            presenter.update();
        }
    }
    
    @Override
    public void addPresenter(IServerPresenter p) {
        list_p.add(p);
    }

    @Override
    public void updateRanges(int value, String filename, int end, UUID id) {
        // When user deletes or inserts line in his locked range we should shift other
	// users range
        ArrayList<FileElement> elements = allFiles.get(filename);
        for(int i = 0; i < elements.size(); i++) {
            FileElement curElement = elements.get(i);
            if(curElement.getUUID().equals(id)) // Change own
                curElement.setEnd(curElement.getEnd() + value);
            else if(curElement.getStart() >= end) { // Change others
                curElement.setStart(curElement.getStart() + value);
                curElement.setEnd(curElement.getEnd() + value);
            }
        }
    }

    @Override
    public boolean registerUser(String login, String pass) {
        Connection c = null;
        PreparedStatement stmt = null;
        String dbFilename = "users.db";
        boolean result = false;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbFilename);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            // Checking if user already exists
            stmt = c.prepareStatement("select * from users where Login=?");
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                result = true;
            }
            rs.close();
            stmt.close();
            
            // if not then adding him in table
            if  (result) {
                stmt = c.prepareStatement("insert into users values(?,?)");
                stmt.setString(1, login);
                stmt.setString(2, pass);
                stmt.executeUpdate();
                stmt.close();
                c.commit();
            }
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Registration done");
        return result;
    }

    @Override
    public boolean loginUser(String login, String pass) {
        Connection c = null;
        PreparedStatement stmt = null;
        String dbFilename = "users.db";
        boolean result = false;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:" + dbFilename);
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            // Checking if user already exists
            stmt = c.prepareStatement("select * from users where Login=? and Pass=?");
            stmt.setString(1, login);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                result = true;
            }
            rs.close();
            stmt.close();

            c.commit();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        return result;
    }
}
