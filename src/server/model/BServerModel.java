/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.model;

import javax.swing.table.DefaultTableModel;

/**
 *
 * @author al1as
 */
public class BServerModel {
    static IServerModel model;
    public static  IServerModel build(DefaultTableModel tableModel) {
        model = new ServerModel(tableModel);
        return model;
    }
}
