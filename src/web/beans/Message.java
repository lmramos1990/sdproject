package web.beans;

public class Message {
    String username;
    String auctionid;
    String text;

    public Message(String username, String auctionid, String text) {
        this.username = username;
        this.auctionid = auctionid;
        this.text = text;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
