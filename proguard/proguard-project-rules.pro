# Add project specific ProGuard rules here.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontobfuscate

-keepattributes *Annotation*
-keepattributes Signature

-keep class de.tum.in.tumcampusapp.models.**{*;}


-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}


-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}