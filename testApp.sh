#!/bin/sh
./gradlew assembleDebugAndroidTest && 
	adb push app/build/outputs/apk/app-debug-androidTest-unaligned.apk /data/local/tmp/de.tum.in.tumcampusapp.test && 
	adb shell pm install -r "/data/local/tmp/de.tum.in.tumcampusapp.test" && 
	adb shell am instrument -w -r   -e package de.tum.in.tumcampusapp -e debug false de.tum.in.tumcampusapp.test/android.support.test.runner.AndroidJUnitRunner