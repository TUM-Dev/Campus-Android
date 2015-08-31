# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

-dontobfuscate

-keepattributes *Annotation*
-keepattributes Signature
-keep class com.bea.xml.stream.**{*;}
-keep class com.squareup.okhttp.internal.**{*;}
-keep class okio.**{*;}
-keep class org.simpleframework.xml.**{*;}
-keep class retrofit.**{*;}
-keep class de.tum.in.tumcampus.models.**{*;}

#Don't warn about our various dependencies, they should work
-dontwarn com.bea.xml.stream.**
-dontwarn com.squareup.okhttp.internal.**
-dontwarn okio.**
-dontwarn org.simpleframework.xml.stream.**
-dontwarn retrofit.**
-dontwarn com.google.android.gms.**

-keep class * extends java.util.ListResourceBundle {
    protected Object[][] getContents();
}

-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
    public static final *** NULL;
}

-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}

-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}


# Allow obfuscation of android.support.v7.internal.view.menu.**
# to avoid problem on Samsung 4.2.2 devices with appcompat v21
# see https://code.google.com/p/android/issues/detail?id=78377
-keep class !android.support.v7.internal.view.menu.**,android.support.** {*;}