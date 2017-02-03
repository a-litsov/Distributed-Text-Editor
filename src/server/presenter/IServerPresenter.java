/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.presenter;

import java.util.UUID;

/**
 *
 * @author al1as
 */
public interface IServerPresenter {
    public UUID getUUID();
    public void update();
}
