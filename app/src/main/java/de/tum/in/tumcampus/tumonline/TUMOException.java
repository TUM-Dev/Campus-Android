package de.tum.in.tumcampus.tumonline;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.simpleframework.xml.core.Persister;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tum.in.tumcampus.R;
import de.tum.in.tumcampus.auxiliary.Const;
import de.tum.in.tumcampus.auxiliary.NetUtils;
import de.tum.in.tumcampus.auxiliary.Utils;
import de.tum.in.tumcampus.models.managers.CacheManager;
import de.tum.in.tumcampus.models.managers.TumManager;

public class TUMOException extends Exception {
    public final String errorMessage;

    public TUMOException(String errorMessage){
        this.errorMessage = errorMessage;
    }
}
