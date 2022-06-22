package org.feup.cpd.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Membership extends Remote {
    void join() throws RemoteException;
    void leave() throws RemoteException;
}
