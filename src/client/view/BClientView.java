/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client.view;

import client.controller.IClientController;
import client.model.IClientModel;

/**
 *
 * @author al1as
 */
public class BClientView {
	static IClientView clientView = null;

	public static IClientView build(IClientModel model, IClientController controller) {
		if(clientView == null)
			clientView = new ClientView(model, controller);
		return clientView;
	}
}
