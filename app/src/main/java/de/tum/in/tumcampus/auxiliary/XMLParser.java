package de.tum.in.tumcampus.auxiliary;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Parses through XML input
 */
public class XMLParser {

    /**
     * Getting XML DOM element
     *
     * @param xml string
     */
    public Document getDomElement(String xml) {
        Document doc;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource(new StringReader(xml));
            doc = db.parse(is);

        } catch (ParserConfigurationException e) {
            Utils.log(e);
            return null;
        } catch (SAXException e) {
            Utils.log(e);
            return null;
        } catch (IOException e) {
            Utils.log(e);
            return null;
        }

        return doc;
    }

    /**
     * Getting node value
     *
     * @param elem element
     */
    public final String getElementValue(Node elem) {
        Node child;
        if (elem != null) {
            if (elem.hasChildNodes()) {
                for (child = elem.getFirstChild(); child != null; child = child
                        .getNextSibling()) {
                    if (child.getNodeType() == Node.TEXT_NODE) {
                        return child.getNodeValue();
                    }
                }
            }
        }
        return "";
    }

    /**
     * Getting node value
     *
     * @param item node
     * @param str  string
     */
    public String getValue(Element item, String str) {
        NodeList n = item.getElementsByTagName(str);
        String value = getElementValue(n.item(0));

        // TODO Fix in API itself
        // Workarounds for bug in RoomFinder API
        value = value.replace("H?rsaal", "Hörsaal");
        value = value.replace("B?hne", "Bühne");
        value = value.replace("Hochbr?ck", "Hochbrück");
        value = value.replace("StammgelÃ¤nde", "Stammgelände");
        value = value.replace("M??bauer", "Mößbauer");
        value = value.replace("F?ppl", "Föppl");
        value = value.replace("Sch?nleutner", "Schönleutner");
        value = value.replace("f?r", "für");
        value = value.replace("Einf?hrung", "Einführung");
        value = value.replace("Tutor?bung", "Tutorübung");
        value = value.replace("Tutor-?bung", "Tutor-übung");
        value = value.replace("er?te", "eräte");
        value = value.replace("Ma?", "Maß");
        value = value.replace("Prop?deutikum", "Propädeutikum");
        value = value.replace("geb?ude", "gebäude");
        return value;
    }

    /**
     * Getting XML from URL making HTTP request
     *
     * @param url string
     */
    public String getXmlFromUrl(String url) {
        String xml = null;

        try {
            //Normal http request
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = NetUtils.execute(httpGet);

            //Get the Response
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                // do something with the response
                xml = EntityUtils.toString(httpEntity, HTTP.UTF_8);
            }
        } catch (IOException e) {
            Utils.log(e);
        }

        // return XML
        return xml;
    }

}
