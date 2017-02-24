/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;
import client.model.*;
import client.controller.*;
import client.view.*;

/**
 *
 * @author al1as
 */
public class ClientMVC {
	public static void main(String[] args) {
		// if user prefers macOS, our jmenu will look native
        if (System.getProperty("os.name").contains("Mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
		IClientModel model = BClientModel.build();
		IClientView view = new client.view.ClientView();
		IClientController controller = new ClientController();
	}
}
