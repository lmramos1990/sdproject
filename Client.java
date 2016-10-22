import java.io.*;
import java.net.*;
import java.util.*;

class Client {
    static Socket clientSocket;

    static DataInputStream dataInputStream;
    static DataOutputStream dataOutputStream;
    static InputStreamReader input;
    static BufferedReader reader;

    public static void main(String[] args) {
        System.out.println("\t------ HELLO IAM AN AWESOME CLIENT INTERFACE ------");

        if(args.length > 0) {
            System.out.println("ERROR: USAGE IS java Client");
            return;
        }

        InetAddress serverAddress = getHost();
        int port = getPort();

        try {
            clientSocket = new Socket(serverAddress, port);
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }

        try {
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            return;
        }


        input = new InputStreamReader(System.in);
        reader = new BufferedReader(input);

        int choice = 0;
        while (choice == 0) {
            choice = loginRegisterMenu();
        }

        return;
    }

    private static int loginRegisterMenu() {
        int choice = 0;

        while(choice != 3) {
            System.out.println("[1] - Login\n[2] - Register\n[3] - Exit");
            Scanner reader = new Scanner(System.in);
            try {
                System.out.print("CHOOSE AN OPTION: ");
                choice = reader.nextInt();
                if(choice <= 0 || choice >= 4) {
                    System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                    choice = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                choice = 0;
            }

            if(choice == 1 || choice == 2) {
                choice = loginRegister(choice);
            }
        }

        return choice;
    }

    private static int mainMenu() {
        int choice = 0;

        while(choice >= 0 && choice < 9) {
            System.out.println("[1] - Create a new auction");
            System.out.println("[2] - Search auction by article");
            System.out.println("[3] - Auction details");
            System.out.println("[4] - See my auctions");
            System.out.println("[5] - Bid in an auction");
            System.out.println("[6] - Edit an auction");
            System.out.println("[7] - Comment on an auction");
            System.out.println("[8] - List online users");
            System.out.println("[9] - Logout");

            Scanner reader = new Scanner(System.in);
            try {
                System.out.print("CHOOSE AN OPTION: ");
                choice = reader.nextInt();

                if(choice <= 0 || choice >= 10) {
                    System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                    choice = 0;
                }

                switch(choice) {
                    case 1:
                        choice = createAuction();
                        break;
                    case 2:
                        choice = searchAuction();
                        break;
                    case 3:
                        choice = auctionDetails();
                        break;
                    case 4:
                        choice = myAuctions();
                        break;
                    case 5:
                        choice = makeBid();
                        break;
                    case 6:
                        choice = editAuction();
                        break;
                    case 7:
                        choice = commentInAuction();
                        break;
                    case 8:
                        choice = listOnlineUsers();
                        break;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID OPTION");
                choice = 0;
            }
        }

        return choice;
    }

    private static int loginRegister(int choice) {

        String username = getUsername();
        String password = getPassword();

        if(choice == 1) {
            String request = new String();

            request = request.concat("type: login, username: ");
            request = request.concat(username);
            request = request.concat(", password: ");
            request = request.concat(password);

            String reply = sendRequest(clientSocket, request);

            if(reply.equals("type: login, ok: true")) {
                choice = mainMenu();
            } else {
                reply = "LOGIN WASN'T SUCCESSFULL";
            }

        } else {
            String request = new String();
            request = request.concat("type: register, username: ");
            request = request.concat(username);
            request = request.concat(", password: ");
            request = request.concat(password);

            String reply = sendRequest(clientSocket, request);

            if(reply.equals("type: register, ok: true")) {
                choice = mainMenu();
            } else {
                reply = "REGISTER WASN'T SUCCESSFULL";
            }

            System.out.println("[SERVER] " + reply);
        }

        return choice;
    }

    private static int createAuction() {

        String code = getCode();
        String title = getTitle();
        String description = getDescription();

        String year = getYear();
        String month = getMonth();
        String day = getDay(year, month);
        String hour = getHour();
        String minutes = getMinutes();
        String amount = getAmount();

        System.out.println("------ INSERT THE DATE OF THE DEADLINE ------");

        String deadline = new String();
        deadline = deadline.concat(year);
        deadline = deadline.concat("-");
        deadline = deadline.concat(month);
        deadline = deadline.concat("-");
        deadline = deadline.concat(day);
        deadline = deadline.concat(" ");
        deadline = deadline.concat(hour);
        deadline = deadline.concat(":");
        deadline = deadline.concat(minutes);

        String request = new String();

        request = request.concat("type: create_auction, code: ");
        request = request.concat(code);
        request = request.concat(", title: ");
        request = request.concat(title);
        request = request.concat(", description: ");
        request = request.concat(description);
        request = request.concat(", deadline: ");
        request = request.concat(deadline);
        request = request.concat(", amount: ");
        request = request.concat(amount);

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        if(reply.equals("type: create_auction, ok: true")) {
            System.out.println("CREATE AUCTION TRUE");
        } else {
            System.out.println("CREATE AUCTION FALSE");
        }

        return 0;
    }

    private static int searchAuction() {
        String code = getCode();

        String request = new String();

        request = request.concat("type: search_auction, code: ");
        request = request.concat(code);

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        if(reply.equals("type: search_auction, items_count: 0")) {
            System.out.println("SEARCH AUCTION FALSE");
        } else {
            System.out.println("SEARCH AUCTION TRUE");
        }

        return 0;
    }

    private static int auctionDetails() {

        String id = getId();
        String request = new String();

        request = request.concat("type: detail_auction, id: ");
        request = request.concat(id);

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        if(reply.equals("type: detail_auction, items_count: 0")) {
            System.out.println("AUCTION DETAILS FALSE");
        } else {
            System.out.println("AUCTION DETAILS TRUE");
        }

        return 0;
    }

    private static int myAuctions() {
        String request = new String();

        request = request.concat("type: my_auctions");

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        return 0;
    }

    private static int makeBid() {

        String id = getId();
        String amount = getAmount();

        String request = new String();

        request = request.concat("type: bid, id: ");
        request = request.concat(id);
        request = request.concat(", amount: ");
        request = request.concat(amount);

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        return 0;
    }

    private static int editAuction() {
        String id = getId();
        String year = getYear();
        String month = getMonth();
        String day = getDay(year, month);
        String hour = getHour();
        String minutes = getMinutes();

        System.out.println("------ INSERT THE DATE OF THE DEADLINE ------");

        String deadline = new String();
        deadline = deadline.concat(year);
        deadline = deadline.concat("-");
        deadline = deadline.concat(month);
        deadline = deadline.concat("-");
        deadline = deadline.concat(day);
        deadline = deadline.concat(" ");
        deadline = deadline.concat(hour);
        deadline = deadline.concat(":");
        deadline = deadline.concat(minutes);

        String request = new String();

        request = request.concat("type: edit_auction, id: ");
        request = request.concat(id);
        request = request.concat(", deadline: ");
        request = request.concat(deadline);

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        return 0;
    }

    private static int commentInAuction() {
        String id = getId();
        String text = getText();

        String request = new String();

        request = request.concat("type: message, id: ");
        request = request.concat(id);
        request = request.concat(", text: ");
        request = request.concat(text);

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        if(reply.equals("type: message, ok: true")) {
            System.out.println("MESSAGE TRUE");
        } else {
            System.out.println("MESSAGE FALSE");
        }

        return 0;
    }

    private static int listOnlineUsers() {
        String request = new String();

        request = request.concat("type: online_users");

        String reply = sendRequest(clientSocket, request);

        System.out.println("[SERVER] " + reply);

        return 0;
    }

    private static String getUsername() {
        Scanner reader = new Scanner(System.in);

        System.out.print("INSERT USERNAME: ");
        String username = reader.nextLine();

        return username;
    }

    private static String getPassword() {
        Console console = System.console();
        String password = new String();

        if(console == null) {
            System.out.println("ERROR: CONSOLE DOES NOT EXIST");
            return null;
        }

        char[] pwd = console.readPassword("PASSWORD: ");

        for(int i = 0; i < pwd.length; i++) {
            password = password.concat(Character.toString(pwd[i]));
        }

        return password;
    }

    private static String getCode() {
        int code = 0;
        Scanner sc = new Scanner(System.in);

        while(code == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE CODE OF THE ARTICLE: ");
                code = reader.nextInt();
                String scode = Integer.toString(code);

                if(scode.length() == 0 || scode.length() >= 12) {
                    System.out.println("ERROR: THIS IS NOT A VALID CODE");
                    code = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID CODE");
                code = 0;
            }
        }

        return Integer.toString(code);
    }

    private static String getTitle() {
        Scanner reader = new Scanner(System.in);

        System.out.print("INSERT THE TITLE OF THE AUCTION: ");
        String title = reader.nextLine();

        return title;
    }

    private static String getDescription() {
        Scanner reader = new Scanner(System.in);

        System.out.print("INSERT A DESCRIPTION ABOUT THE AUCTION: ");
        String description = reader.nextLine();

        return description;
    }

    private static String getYear() {
        int year = 0;

        while(year == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE YEAR: ");
                year = reader.nextInt();

                if(year < 2016) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    year = 0;
                }

                String syear = Integer.toString(year);

                if(syear.length() == 0 || syear.length() > 4) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    year = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                year = 0;
            }
        }

        return Integer.toString(year);
    }

    private static String getMonth() {
        int month = 0;

        while(month == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE MONTH: ");
                month = reader.nextInt();

                if(month <= 0 || month > 12) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    month = 0;
                }

                String smonth = Integer.toString(month);

                if(smonth.length() == 0 || smonth.length() > 2) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    month = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                month = 0;
            }
        }

        return Integer.toString(month);
    }

    private static String getDay(String syear, String smonth) {
        int day = 0;
        int year = Integer.parseInt(syear);
        int month = Integer.parseInt(smonth);

        while(day == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE DAY: ");
                day = reader.nextInt();

                if((month == 4 || month == 6 || month == 9 || month == 11) && day > 30) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    day = 0;
                } else if((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) && day > 31) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    day = 0;
                } else if(month == 2) {
                    if(year % 4 == 0 || year % 100 == 0 || year % 400 == 0) {
                        if(day > 29) {
                            System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                            day = 0;
                        }
                    } else {
                        if(day > 28) {
                            System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                            day = 0;
                        }
                    }
                }

                String sday = Integer.toString(day);

                if(sday.length() == 0 || sday.length() > 2) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    day = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                day = 0;
            }
        }

        return Integer.toString(day);
    }

    private static String getHour() {
        int hour = -1;

        while(hour == -1) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE HOUR: ");
                hour = reader.nextInt();

                if(hour <= 0 || hour > 24) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    hour = -1;
                }

                String shour = Integer.toString(hour);

                if(shour.length() == 0 || shour.length() > 2) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    hour = -1;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                hour = -1;
            }
        }

        return Integer.toString(hour);
    }

    private static String getMinutes() {
        int minutes = -1;

        while(minutes == -1) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE MINUTES: ");
                minutes = reader.nextInt();

                if(minutes < 0 && minutes > 60) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    minutes = -1;
                }

                String sminutes = Integer.toString(minutes);

                if(sminutes.length() == 0 || sminutes.length() > 2) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    minutes = -1;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                minutes = -1;
            }
        }

        return Integer.toString(minutes);
    }

    private static String getAmount() {
        int amount = 0;
        while(amount == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE AMOUNT OF ITEMS TO SELL: ");
                amount = reader.nextInt();

                if(amount <= 0) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    amount = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                amount = 0;
            }
        }

        return Integer.toString(amount);
    }

    private static String getId() {
        int id = 0;

        while(id == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE ID OF THE AUCTION: ");
                id = reader.nextInt();
                String sid = Integer.toString(id);

                if(sid.length() == 0 || sid.length() >= 12) {
                    System.out.println("ERROR: THIS IS NOT A VALID ID");
                    id = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID ID");
                id = 0;
            }
        }

        return Integer.toString(id);
    }

    private static String getText() {
        Scanner reader = new Scanner(System.in);

        System.out.print("INSERT THE TEXT: ");
        String text = reader.nextLine();

        return text;
    }

    private static String sendRequest(Socket socket, String request) {
        String data = new String();
        byte[] message = request.getBytes();
        System.out.println("THIS IS A REQUEST: " + request);

        byte[] buffer = new byte[1024];

        try {
            dataOutputStream.write(message);

            dataInputStream.read(buffer);
            data = new String(buffer);

        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return data;
    }

    private static InetAddress getHost() {
        String host = new String();
        Scanner reader = new Scanner(System.in);
        InetAddress serverAddress;

        System.out.print("INSERT THE HOST: ");
        host = reader.nextLine();
        try {
            serverAddress = InetAddress.getByName(host);
            return serverAddress;
        } catch(Exception e) {
            System.out.println("ERROR: " + e.getMessage());
            serverAddress = getHost();
        }
        return serverAddress;
    }

    private static int getPort() {
        int port = 0;

        while(port <= 1024) {
            System.out.print("INSERT PORT: ");
            try {
                Scanner reader = new Scanner(System.in);
                port = reader.nextInt();

                if(port <= 1024) {
                    System.out.println("THIS IS NOT A VALID VALUE FOR THE PORT");
                    port = 0;
                }
            } catch(Exception e) {
                System.out.println("THIS IS NOT A VALID VALUE FOR THE PORT");
                port = 0;
            }
        }

        return port;
    }
}
