package web.beans;

public class MyAuctionsObject {
    private String auctionid;
    private String articlecode;
    private String title;

    public MyAuctionsObject(String auctionid, String articlecode, String title) {
        this.auctionid = auctionid;
        this.articlecode = articlecode;
        this.title = title;
    }

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }

    public String getArticlecode() {
        return articlecode;
    }

    public void setArticlecode(String articlecode) {
        this.articlecode = articlecode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
