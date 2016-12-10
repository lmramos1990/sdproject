package web.beans;

public class Auction {
    String auctionid;
    String title;
    String description;
    String deadline;
    String amount;
    String code;

    public Auction(String auctionid, String title, String description, String deadline, String amount, String code) {
        this.auctionid = auctionid;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.amount = amount;
        this.code = code;
    }

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
