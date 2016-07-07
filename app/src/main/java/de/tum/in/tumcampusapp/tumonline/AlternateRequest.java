package de.tum.in.tumcampusapp.tumonline;

import java.io.File;
import java.io.StringReader;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.xml.sax.InputSource;

import android.content.Context;
import android.os.AsyncTask;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import de.tum.in.tumcampusapp.R;
import de.tum.in.tumcampusapp.auxiliary.NetUtils;
import de.tum.in.tumcampusapp.auxiliary.Utils;
import de.tum.in.tumcampusapp.models.managers.CacheManager;
import de.tum.in.tumcampusapp.models.managers.TumManager;
import de.tum.in.tumcampusapp.trace.Util;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequest;
import de.tum.in.tumcampusapp.tumonline.TUMOnlineRequestFetchListener;

/**
 * Created by shifuddin on 7/6/2016.
 */
public class AlternateRequest {

    /*
     * NetUtils class for content download from Url
     */
    private final NetUtils net;
    /**
     * asynchronous task for interactive fetch
     */
    private AsyncTask<Void, Void, String> backgroundTask = null;

    private final Context mContext;
    private final CacheManager cacheManager;
    private final TumManager tumManager;
    // force to fetch data and fill cache
    private boolean fillCache = false;
    private String lastError = "";
    private List<String> result;

    /*
     * Required information which are going to be asked
     */
    private String contactName;
    private String email;
    private String phone;
    private String homepage;
    private String roomNumber;

    /*
     * Base URL to be fetched
     */
    private final String baseUrl = "https://campus.tum.de/tumonline/wborggruppen.gruppen_anonym_xml?";

    public AlternateRequest(Context context)
    {
        mContext = context;
        cacheManager = new CacheManager(context);
        tumManager = new TumManager(context);
        net = new NetUtils(context);
        this.fillCache = true;
    }

    public String fetch(String key, String value)
    {
            String xmlContent;
            String url = getRequestedURL(key, value);

            //Check for error lock
            String error = this.tumManager.checkLock(url);

            if (error != null) {
                Utils.log("aborting fetch URL (" + error + ") " + url);
                lastError = error;
                return null;
            }

            Utils.log("fetching URL " + url);
            boolean addToCache = false;
            try {
                xmlContent = cacheManager.getFromCache(url);
                if (xmlContent == null || fillCache) {
                    boolean isOnline = NetUtils.isConnected(mContext);
                    if (!isOnline) {
                        // not online, fetch does not make sense
                        return null;
                    }

                    xmlContent = net.downloadStringHttp(url);
                    addToCache = true;
                } else {
                    Utils.logv("loaded from cache " + url);
                }
            } catch (Exception e) {
                Utils.log(e, "FetchError");
                lastError = e.getMessage();
                return null;
            }
            try {
                getResult(xmlContent);

                // Only add to cache if data is valid
                if (addToCache) {
                    cacheManager.addToCache(url, xmlContent, CacheManager.VALIDITY_ONE_DAY, CacheManager.CACHE_TYP_DATA);
                    Utils.logv("added to cache " + url);
                }

                //Release any lock present in the database
                tumManager.releaseLock(url);
                return "Successful";
            } catch (Exception e) {
                //Serialisation failed - lock for a specific time, save the error message
                lastError = tumManager.addLock(url, xmlContent);
                return null;
            }
    }
    private void getResult(String xmlContent)
    {
        try
        {
        /*
         * Build document from String result and normalized to put all Text nodes in the full depth of the sub-tree underneath this Node
         */
        Document doc = loadXMLFromString(xmlContent);
        doc.getDocumentElement().normalize();

            /*
             * We are actually concern about data under member tag
             * For this, we took all the members under the group and retrieve information of first available member
             */
        NodeList memeberList = doc.getElementsByTagName("member");
        for(int i = 0; i < memeberList.getLength();i++)
        {
            Node member = memeberList.item(i);
            if (member.getNodeType() == Node.ELEMENT_NODE) {

                Element eElement = (Element) member;

                contactName = eElement.getElementsByTagName("title").item(0).getTextContent()+ " " +eElement.getElementsByTagName("givenName").item(0).getTextContent() + "  " + eElement.getElementsByTagName("surname").item(0).getTextContent();
                phone = eElement.getElementsByTagName("phone").item(0).getTextContent();
                email = eElement.getElementsByTagName("email").item(0).getTextContent();
                homepage = eElement.getElementsByTagName("wwwHomepage").item(0).getTextContent();


                NodeList roomList = ((Element) member).getElementsByTagName("room");
                for(int j = 0; j < roomList.getLength(); j++) {
                    Node room = memeberList.item(i);
                    if (room.getNodeType() == Node.ELEMENT_NODE) {
                        eElement = (Element) room;
                        roomNumber = eElement.getElementsByTagName("roomLong").item(0).getTextContent();
                        Utils.logv("Room : " + roomNumber);
                    }
                    break;

                }
            }
            if (contactName != null)
                break;
        }
    }
    catch (Exception e)        {
        Utils.logv("Error "+e);
    }

    }

    /**
     * this fetch method will fetch the data from the TUMOnline Request and will
     * address the listeners onFetch if the fetch succeeded, else the
     * onFetchError will be called
     *
     */
    public void fetchInteractive(final String key, final String value) {


        // fetch information in a background task and show progress dialog in
        // meantime
        backgroundTask = new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {
                // we are online, return fetch result
                return fetch(key, value);
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    Utils.logv("Received result <" + result + ">");
                } else {
                    Utils.log("No result available");
                }

                // Handles result
                if (!NetUtils.isConnected(mContext)) {
                    if (result == null) {
                        return;
                    } else {
                        Utils.showToast(mContext, R.string.no_internet_connection);
                    }
                }

            }

        };
        backgroundTask.execute();
    }
    /*
     * Helper method
     * Purpose: Build doc from string
     * param: Content of xml as string
     * return: Built doc
     */
    private  Document loadXMLFromString(String xml) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        return builder.parse(is);
    }

    private String getRequestedURL(String key, String value)
    {
        return baseUrl +key +"="+value;
    }

    /*
     * Getters
     */
    public String getContactName(){return contactName;}
    public String getContactEmail(){return email;}
    public String getContactPhone(){return phone;}
    public String getHomepage(){return homepage;}
    public String getRoomNumber(){return roomNumber;}
}
