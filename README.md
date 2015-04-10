# Use GooglePlace API
google place auto complete

# Get Google API KEY
[Get Here : API KEY](https://console.developers.google.com/project)

![ScreenShot](http://sangcomz.cafe24.com/eximg/apikey.png)

API 및 인증에서 Google Maps Android API와 Places API를 사용으로 합니다.
(Google Maps Android API는 맵을 이용하지 않을경우에는 필요없습니다.)

>Android 애플리케이션용 키는 맵을 띄워주기 위해서 사용합니다.

>브라우저 애플리케이션용 키는 Place API를 위해서 발급받습니다.
# Add permission

    <permission
        android:name="com.javapapers.android.googleplacesdetail.permission.MAPS_RECEIVE"
        android:protectionLevel="signature" />
    <uses-permission android:name="com.javapapers.android.googleplacesdetail.permission.MAPS_RECEIVE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="com.google.android.providers.gsf.permission.READ_GSERVICES" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
# Add your API KEY
AndroidManifest.xml
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        <meta-data
            android:name="com.google.android.gms.version"
            android:value="@integer/google_play_services_version" />
        <meta-data
            android:name="com.google.android.maps.v2.API_KEY"
            android:value="Your Application API key" />

        <activity
            android:name=".AutoComplete"
            android:label="@string/title_activity_auto_complete" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
