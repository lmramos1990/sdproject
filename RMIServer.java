class RMIServer {
    public static void main(String[] args) {
        System.out.println("Hello im the RMIServer!");

        RMIRequestListener requestlistener = new RMIRequestListener();
        KeepAlive keepalive = new KeepAlive();


        requestlistener.run();
        keepalive.run();
    }

}

// Class that handles the requests of the clients
class RMIRequestListener implements Runnable {
    public RMIRequestListener() {
        System.out.println("This is the RMIRequestListener constructor");
    }

    public void run() {

    }
}

// Class that is supposed to be keeping the servers running at all times
class KeepAlive implements Runnable {
    public KeepAlive() {
        System.out.println("This is the KeepAlive constructor");
    }

    public void run() {

    }
}

// Class that handles the connection to the database
class DBConnection implements Runnable {
    public DBConnection() {
        System.out.println("This is the DBConnection constructor");
    }

    public void run() {

    }
}
