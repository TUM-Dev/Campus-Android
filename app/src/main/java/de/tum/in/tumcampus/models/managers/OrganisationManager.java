package de.tum.in.tumcampus.models.managers;

import android.content.Context;

import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;

import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.FileUtils;
import de.tum.in.tumcampus.auxiliary.Utils;

/**
 * Event Manager, handles database stuff, external imports
 */

public class OrganisationManager {

    /**
	 * Constructor, open/create database, create table if necessary
	 *
	 * @param context Context
	 */
	public OrganisationManager(Context context) { }

	/**
	 * Download xml File of the full Orgtree
	 *
	 * @param force True to force download over normal sync period, else false
	 * @param token Token for the XML-Request
	 * @throws Exception
	 */
	public void downloadFromExternal(boolean force, String token) throws Exception {

		// cancel, if it has been updated in the last 5 days
		if (!force && !this.needSync()) {
			return;
		}

		DefaultHttpClient client = new DefaultHttpClient();

		// build url
		String url = "https://campus.tum.de/tumonline/wbservicesbasic.orgBaum?pToken=" + token; //TODO let TUMOnlineRequest do this

		// delete and create new file if there is already an old one existing
		File xmlOrgFile = FileUtils.getFileOnSD(Const.ORGANISATIONS, "org.xml");
		if (xmlOrgFile.exists()) {
			xmlOrgFile.delete();
			xmlOrgFile.createNewFile();
		}

		// get xml data and write it into file
		String xmlText = FileUtils.sendGetRequest(client, url);
		FileUtils.writeFile(xmlOrgFile, xmlText);
		Utils.logv("Import: org.xml file has been new downloaded and saved.");
	}

	/**
	 * Look if the xml-File has to get updated
	 * 
	 * @return true (if xml-File is older than 1 week or doesn't exist), false
	 *         (if xml-File is newer than 1 week)
	 */
    boolean needSync() {

		File xmlOrgFile;
		try {
			xmlOrgFile = FileUtils.getFileOnSD("organisations", "org.xml");
		} catch (Exception e) {
			Utils.log(e, "No SD-card!");
			return true;
		}

		// does file exist? is it a file? is it a xml token error or the real
		// wanted file?
		if (!xmlOrgFile.exists() || !xmlOrgFile.isFile() || !(xmlOrgFile.length() > 100000)) {
			return true;
		}

		Long lastModified = xmlOrgFile.lastModified();

		// if older than 5 days => return true
        /* Maximal file age in milliseconds (here: (3600*1000*24*5) = 5 Days) */
        int MAX_FILE_AGE = 432000000;
        return (lastModified + MAX_FILE_AGE) < System.currentTimeMillis();
    }
}