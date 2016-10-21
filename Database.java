import java.sql.*;

public class Database {

    String url, name, username, pass, port;

    private Connection conn;

    public Database() {
        //should all of this shit be on the file of configurations?????
        url = "127.0.0.1";
        name = "iBei";
        user = pass = "root";
        port = "10000";
    }

    public boolean connect() {


        return true;

    }

    public int doQuery(String query) {


    }

    public int doUpdate(String query) {

    }
}
