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
            System.out.println("ERROR: USAGE IS java TCPClient");
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
                        choice = searchAuctionByArticle();
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

        Scanner reader = new Scanner(System.in);
        String username = new String();
        String password = new String();
        Console console = System.console();

        if(console == null) {
            System.out.println("ERROR: Console does not exist");
            return 0;
        }

        System.out.print("USERNAME: ");
        username = reader.nextLine();
        char[] pwd = console.readPassword("PASSWORD: ");

        for(int i = 0; i < pwd.length; i++) {
            password = password.concat(Character.toString(pwd[i]));
        }

        if(choice == 1) {
            String a = "type: login, ";
            String b = "username: " + username + ", ";
            String c = "password: " + password;

            String request = a + b + c;

            String reply = new String();

            reply = sendRequest(clientSocket, request);

            System.out.println(reply);

            if(reply.equals("type: login, ok: true")) {
                choice = mainMenu();
            } else {
                reply = "LOGIN WASN'T SUCCESSFULL";
            }

            System.out.println("[SERVER] " + reply);
        } else {
            String a = "type: register, ";
            String b = "username: " + username + ", ";
            String c = "password: " + password;

            String request = a + b + c;
            String reply = new String();

            reply = sendRequest(clientSocket, request);

            System.out.println(reply);

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
        System.out.println("Create a new auction");
        int code = 0;
        Scanner sc = new Scanner(System.in);

        // SINGLE ARTICLE STUFF
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
            }
        }

        System.out.print("INSERT THE TITLE OF THE AUCTION: ");
        String title = sc.nextLine();

        System.out.print("INSERT A DESCRIPTION ABOUT THE AUCTION: ");
        String description = sc.nextLine();

        System.out.println("------ INSERT THE DATE OF THE DEADLINE ------");
        int year = 0, month = 0, day = 0, hour = -1, minutes = -1;

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

        while(hour == -1) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE HOUR: ");
                hour = reader.nextInt();

                if(hour < 0 || hour > 24) {
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

        String deadline = new String();
        deadline = deadline.concat(Integer.toString(year));
        deadline = deadline.concat("-");
        deadline = deadline.concat(Integer.toString(month));
        deadline = deadline.concat("-");
        deadline = deadline.concat(Integer.toString(day));
        deadline = deadline.concat(" ");
        deadline = deadline.concat(Integer.toString(hour));
        deadline = deadline.concat(":");
        deadline = deadline.concat(Integer.toString(minutes));

        int amount = 0;
        while(amount == 0) {
            try {
                Scanner reader = new Scanner(System.in);
                System.out.print("INSERT THE AMOUNT OF ITEMS TO SELL: ");
                amount = reader.nextInt();

                if(amount < 0) {
                    System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                    amount = 0;
                }
            } catch(Exception e) {
                System.out.println("ERROR: THIS IS NOT A VALID VALUE");
                amount = 0;
            }
        }

        String request = new String();

        request = request.concat("type: create_auction, code: ");
        request = request.concat(Integer.toString(code));
        request = request.concat(", title: ");
        request = request.concat(title);
        request = request.concat(", description: ");
        request = request.concat(description);
        request = request.concat(", deadline: ");
        request = request.concat(deadline);
        request = request.concat(", amount: ");
        request = request.concat(Integer.toString(amount));

        System.out.println(request);

        String reply = new String();

        reply = sendRequest(clientSocket, request);

        if(reply.equals("type: create_auction, ok: true")) {
            System.out.println("true");
        } else {
            System.out.println("false");
        }

        return 0;
    }

    private static int searchAuctionByArticle() {
        System.out.println("Search auction by article");
        return 0;
    }

    private static int auctionDetails() {
        System.out.println("Auction details");
        return 0;
    }

    private static int myAuctions() {
        System.out.println("See my auctions");
        return 0;
    }

    private static int makeBid() {
        System.out.println("Bid in an auction");
        return 0;
    }

    private static int editAuction() {
        System.out.println("Edit an auction");
        return 0;
    }

    private static int commentInAuction() {
        System.out.println("Comment on an auction");
        return 0;
    }

    private static int listOnlineUsers() {
        System.out.println("List online users");
        return 0;
    }

    private static String sendRequest(Socket socket, String request) {
        String data = new String();
        System.out.println("THIS IS A REQUEST: " + request);

        try {
            dataOutputStream.writeUTF(request);

            data = dataInputStream.readUTF();
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
