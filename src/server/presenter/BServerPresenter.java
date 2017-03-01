/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.presenter;

import server.model.IServerModel;
import server.view.IServerView;
/**
 *
 * @author al1as
 */
public class BServerPresenter {
    static public IServerPresenter build(IServerModel m, IServerView v)
    {
        return new ServerPresenter(m, v);
    }    
}
