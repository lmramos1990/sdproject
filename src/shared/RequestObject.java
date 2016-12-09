package shared;

public class RequestObject {
    private String uuid;
    private int modified;

    public RequestObject(String uuid, int modified) {
        this.uuid = uuid;
        this.modified = modified;
    }

    public String getUUID() {
        return uuid;
    }

    public int getModified() {
        return modified;
    }

    public void setModified(int modified) {
        this.modified = modified;
    }
}
