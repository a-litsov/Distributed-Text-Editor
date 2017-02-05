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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.UUID;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import server.presenter.IServerPresenter;

/**
 *
 * @author al1as
 */
public class ServerModel implements IServerModel {
    JTable table;
    ServerSocket ss;
    Hashtable<UUID, FileElement> allClient = new Hashtable<UUID, FileElement>();
    Hashtable<String, ArrayList> allFiles = new Hashtable<String, ArrayList>();
    ArrayList Names = null;
    String message = "null";
    ArrayList<IServerPresenter> list_p = new ArrayList();
    
    public ServerModel(JTable table) {
        this.table = table;
    }  
    
    @Override
    public synchronized void addToTable(String filename, FileElement element) {
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        int row_num = getRowByValue(model, element.username);
        Object[] row = { filename, Integer.toString(element.range.getStart()) + "-" +
                Integer.toString(element.range.getEnd()), element.username, element.getUUID().toString()
                };
        if(row_num == -1)  
            model.addRow(row);
        else {
            model.removeRow(row_num);
            model.insertRow(row_num, row);
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
        DefaultTableModel model = (DefaultTableModel)table.getModel();
        int row_num = getRowByValue(model, element.username);
        Object[] row = { filename, Integer.toString(element.range.getStart()) + "-" +
                Integer.toString(element.range.getEnd()), element.username, element.getUUID().toString()
                };
        if(row_num == -1)  
            System.out.println("Error: user not found!");
        else 
            model.removeRow(row_num);
    }
    
    @Override
    public synchronized String putFileElement(String filename, FileElement element) {
        ArrayList clients = allFiles.get(filename);
        boolean f = true;
        if(clients == null)
        {
            clients = new ArrayList();
            clients.add(element);
            allFiles.put(filename, clients);    
        } else {
            int start = element.getStart();
            int end = element.getEnd();
            for(int i = 0; i < clients.size(); i++)
            {
                FileElement tmp = (FileElement)clients.get(i);
                if(!((start < tmp.getStart() && end < tmp.getStart()) ||
                        (start > tmp.getEnd() && end > tmp.getEnd())))
                {
                    f = false;
                    break;
                }              
            }
            if(f)
                clients.add(element);
        }
        if(f)
        {
            System.out.println("range is:" + element.getStart() + "-" + element.getEnd());
            this.addToTable(filename, element);
            return "Ranges was set successfully";
        } else {
            System.out.println("Error with setting ranges");
            return "Error with setting ranges";
        }
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
    }
    
    @Override
    public String GetFromDB(String Name) {
        Connection c = null;
        Statement stmt = null;
        String short_filename = "Unknown";
        try {
          Class.forName("org.sqlite.JDBC");
          c = DriverManager.getConnection("jdbc:sqlite:FilenamesDB");
          c.setAutoCommit(false);
          System.out.println("Opened database successfully");

          stmt = c.createStatement();
          ResultSet rs = stmt.executeQuery( "SELECT * FROM Filename WHERE Username='" + Name + "';" );
          if( rs.next() ) {
            short_filename = rs.getString("Filename");
          }
          rs.close();
          stmt.close();
          c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Operation done successfully");
        return short_filename;
}
    
    @Override
    public void SendToDB(String username, String filename) {
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:FilenamesDB");
            c.setAutoCommit(false);
            System.out.println("Opened database successfully");

            stmt = c.createStatement();
            String sql = "INSERT OR REPLACE INTO Filename (Username, Filename) " +
                        "VALUES ('" + username + "','" + filename + "') ";
            stmt.executeUpdate(sql);

            stmt.close();
            c.commit();
            c.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("Records created or updated successfully");    
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
        String fullname = "Shared/" + FileName;
        FileReader fr = null;
        BufferedReader textReader = null;
        String result = "";
        try {
            fr = new FileReader(fullname);
            textReader = new BufferedReader(fr);
            String str = "";
            while ((str = textReader.readLine()) != null) {
                result = result + str + "\n";
            }
            result = result.substring(0, result.length() - 1);
        } catch (IOException e) {
        } finally {
            try {
                textReader.close();
                fr.close();
            } catch (Exception ex) {
            }
        }
        return result;
    }
    
    @Override
    public void Save(String LockedContent, String filename, FileElement element) {
        String fullname = "Shared/" + filename;
        String FileContent = getFileContent(filename);
        String[] ArrayContent = FileContent.split("\n");
        String[] TextData = new String[2];
        for (int i = 0; i < 2; i++) {
            TextData[i] = "";
        }
        int switcher = 0;
        for (int i = 0; i < ArrayContent.length; i++) {
            if (i == element.getStart() - 1) {
                switcher++;
                i += element.getEnd() - element.getStart() + 1;
                // When lock is located at the end of file
                if (i >= ArrayContent.length)
                    break;
            }
            TextData[switcher] += ArrayContent[i] + "\n";
        }
        if (!TextData[1].isEmpty())
            TextData[1] = TextData[1].substring(0, TextData[1].length() - 1);
        String outContent = TextData[0] + LockedContent + TextData[1];
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
}
