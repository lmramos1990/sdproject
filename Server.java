import java.util.Scanner;

class Server {

    public static void main(String[] args) {
        String host = new String();
        int port = 0;

        if(args.length == 2) {
            host = args[0];
            port = getPort(args[1]);
        } else {
            System.out.println("FUCK YOU!")
        }



        System.out.println("HOST: " + host + " PORT: " + port);
    }

    private static int stringToInt(String string) {
        int integer = 0;

        try {
            
        } catch(Exception e) {
            System.out.println("FUCK MY LIFE!");
        }
    }

    private static int getPort(String portString) {

        int port = 0;

        while(port == 0) {
            try {
                port = Integer.parseInt(String.valueOf(portString));
            } catch(Exception e) {
                System.out.println("The 2nd Argument is not valid, please insert an integer");
                port = -1;
            }
        }

        while(port <= 1024) {
            Scanner reader = new Scanner(System.in);

            System.out.print("PLEASE INSERT A VALID VALUE FOR THE PORT [>1024] \nPORT [>1024]: ");
            try {
                port = reader.nextInt();
            } catch(Exception inputError) {
                System.out.println("This is not a valid value, please insert an integer higher than 1024");
            }
        }

        return port;
    }

    private static String getHost() {
        System.out.prinln("SUCK MY HAIRY DICK!")
    }
}

// Class that manages the incoming client requests
class ServerRequestListener implements Runnable {
    public ServerRequestListener() {

    }

    public void run() {

    }
}

// Class that balances the number of clients per server
class NetworkBalancer implements Runnable {
    public NetworkBalancer() {

    }

    public void run() {

    }
}
