/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.model;

/**
 *
 * @author al1as
 */
public class BClientModel {
    static ClientModel clientModel = new ClientModel();
    
    public  static IClientModel build()
    {
        return clientModel;
    }
}
