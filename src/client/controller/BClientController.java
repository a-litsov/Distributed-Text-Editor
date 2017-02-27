/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.controller;

import client.model.IClientModel;

/**
 *
 * @author al1as
 */
public class BClientController {
    static IClientController clientController = null;
    
    public static IClientController build(IClientModel model) {
		if(clientController == null)
			clientController = new ClientController(model);
        return clientController;
    }
}
