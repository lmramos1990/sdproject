/*package web.socket;

import console.NotificationCenter;
import console.RequestObject;

import java.lang.reflect.Array;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.Session;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class SocketObject implements NotificationCenter {

    private ArrayList<RequestObject> requests = new ArrayList<>();
    private Set<SocketObject> sessions = new CopyOnWriteArraySet<>();
    private Session session;

    private SocketObject() {
        super();
    }

    public SocketObject(ArrayListsession) {
        SocketObject so = new SocketObject();
        so.session = session;
    }


    public synchronized boolean isUserOnline(String username) throws RemoteException {
    }

    public synchronized ArrayList getOnlineUsers() throws RemoteException {
    }

    public synchronized void sendNotificationToUser(String username, String message) throws RemoteException {
    }

    public synchronized void updateRequest(String uuid) throws RemoteException {
        for(RequestObject request : requests) {
            if(uuid.equals(request.getUUID())) {
                request.setModified(1);
            }
        }
    }

    public synchronized int requestStatus(String uuid) throws RemoteException {
        for(RequestObject request : requests) {
            if(uuid.equals(request.getUUID())) {
                return request.getModified();
            }
        }

        return -1;
    }
}*/
