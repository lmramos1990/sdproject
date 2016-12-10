package web.beans;

public class Bid {
    String auctionid;
    String username;
    String amount;

    public Bid(String auctionid, String username, String amount) {
        this.auctionid = auctionid;
        this.username = username;
        this.amount = amount;
    }

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
