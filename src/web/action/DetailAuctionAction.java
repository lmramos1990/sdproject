package web.action;

import com.opensymphony.xwork2.Action;
import org.apache.struts2.interceptor.SessionAware;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import web.beans.Bean;
import web.beans.DetailAuctionObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class DetailAuctionAction implements SessionAware {
    private Map<String, Object> session;
    private String auctionid;
    private String findingapikey;

    public String execute() {
        if(auctionid == null) return "stay";
        session.remove("notifications");

        Bean myBean = new Bean();
        myBean.setAuctionid(getAuctionid());
        myBean.setDetailAuctionObject(new DetailAuctionObject());

        String reply = myBean.detailauction();

        if(reply.equals(Action.SUCCESS)) {

            readProperties();

            String articlecode = myBean.getArticleCodeFromAuctionId();
            String lowestprice = getEbayLink(articlecode);

            session.put("detailauctionbean", myBean);
            session.put("lowestprice", lowestprice);
        }

        return reply;
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }

    public String getAuctionid() {
        return auctionid;
    }

    public void setAuctionid(String auctionid) {
        this.auctionid = auctionid;
    }

    private void readProperties() {
        InputStream inputStream = null;

        try {
            Properties prop = new Properties();

            inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            prop.load(inputStream);
            findingapikey = prop.getProperty("findingApiKey");
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert inputStream != null;
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getEbayLink(String articlecode) {
        String myurl = "http://svcs.ebay.com/services/search/FindingService/v1?OPERATION-NAME=findItemsByProduct&SERVICE-VERSION=1.0.0&SECURITY-APPNAME=" + findingapikey + "&RESPONSE-DATA-FORMAT=JSON&productId.@type=ISBN&productId=" + articlecode;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            JSONObject json = (JSONObject) new JSONParser().parse(rd);

            rd.close();

            JSONArray cenas = (JSONArray) json.get("findItemsByProductResponse");

            JSONObject aux = (JSONObject) cenas.get(0);

            JSONArray searchResult = (JSONArray) aux.get("searchResult");

            aux = (JSONObject) searchResult.get(0);

            JSONArray items = (JSONArray) aux.get("item");
            float currentValue;
            float lowestValue = (float) -1.0;

            for(Object obj : items) {
                JSONObject item = (JSONObject) obj;
                JSONArray sellingstatus = (JSONArray) item.get("sellingStatus");
                aux = (JSONObject) sellingstatus.get(0);
                JSONArray currentprice = (JSONArray) aux.get("currentPrice");
                aux = (JSONObject) currentprice.get(0);
                currentValue = Float.parseFloat(aux.get("__value__").toString());
                if(lowestValue == -1.0) lowestValue = currentValue;
                else if(lowestValue > currentValue) lowestValue = currentValue;
            }

            return "the lowest price on ebay for this item is: " + String.valueOf(lowestValue);
        } catch (NullPointerException | IOException | ParseException ignored) {}

        return "no item on ebay corresponds to this code";
    }
}
