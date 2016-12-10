package web.beans;

import java.util.ArrayList;

public class DetailAuctionObject {
    private String auctionid;
    private String title;
    private String description;
    private String deadline;

    private ArrayList<MessageObject> messages;
    private ArrayList<BidObject> bids;

    public DetailAuctionObject() {
        this.auctionid = "";
        this.title = "";
        this.description = "";
        this.deadline = "";
        this.messages = new ArrayList<>();
        this.bids = new ArrayList<>();
    }

    public DetailAuctionObject(String auctionid, String title, String description, String deadline, ArrayList<MessageObject> messages, ArrayList<BidObject> bids) {
        this.auctionid = auctionid;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.messages = messages;
        this.bids = bids;
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

    public ArrayList<MessageObject> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<MessageObject> messages) {
        this.messages = messages;
    }

    public ArrayList<BidObject> getBids() {
        return bids;
    }

    public void setBids(ArrayList<BidObject> bids) {
        this.bids = bids;
    }
}
