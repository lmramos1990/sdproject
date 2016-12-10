package web.beans;

public class SearchAuctionObject {
    private String articlecode;
    private String title;
    private String auctionid;

    public SearchAuctionObject(String articlecode, String title, String auctionid) {
        this.articlecode = articlecode;
        this.title = title;
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

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }
}
