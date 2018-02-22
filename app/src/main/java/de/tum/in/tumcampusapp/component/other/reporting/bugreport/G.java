/*
Copyright (c) 2009 nullwire aps

Permission is hereby granted, free of charge, to any person
obtaining a copy of this software and associated documentation
files (the "Software"), to deal in the Software without
restriction, including without limitation the rights to use,
copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Contributors: 
Mads Kristiansen, mads.kristiansen@nullwire.com
Glen Humphrey
Evan Charlton
Peter Hewitt
*/

package de.tum.in.tumcampusapp.component.other.reporting.bugreport;

import android.content.SharedPreferences;

public final class G {
    public static final String UNKNOWN = "unknown";
    public static final boolean BUG_REPORT_DEFAULT = true;
    public static final String TAG = "TCA Error Reporting";
    public static final int MAX_TRACES = 5;

    // Since the exception handler doesn't have access to the context, or anything really, the library prepares these values for when the handler needs them.
    public static String filesPath;
    public static String appVersion = UNKNOWN;
    public static String appPackage = UNKNOWN;
    public static int appVersionCode = -1;
    public static String androidVersion;

    public static String phoneModel;

    public static String deviceId = UNKNOWN;

    public static SharedPreferences preferences;

    private G() {
        // G is a utility class
    }
}