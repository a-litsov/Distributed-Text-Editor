/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

/**
 *
 * @author al1as
 */
public class BClientController {
    static ClientController clientController = new ClientController();
    
    public static IClientController build() {
        return clientController;
    }
}
