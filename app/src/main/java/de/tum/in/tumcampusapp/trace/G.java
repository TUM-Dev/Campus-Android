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

package de.tum.in.tumcampusapp.trace;

import android.content.Context;
import android.content.SharedPreferences;

public class G {
    // Since the exception handler doesn't have access to the context, or anything really, the library prepares these values for when the handler needs them.
    public static String filesPath = null;
    public static String appVersion = "unknown";
    public static String appPackage = "unknown";
    public static int appVersionCode = -1;
    public static final String tag = "TCA Error Reporting";
    public static String androidVersion = null;
    public static String phoneModel = null;
    public static String deviceId = "unknown";

    public static final int MAX_TRACES = 5;

    public static final String traceVersion = "1.1";

    public static Context context = null;

    public static SharedPreferences preferences = null;
    public static final Boolean bugReportDefault = true;
}