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
public class BClientView {
	static IClientView clientView = new ClientView();

	public static IClientView build() {
		return clientView;
	}
}