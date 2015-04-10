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
            android:value="Android 애플리케이션용 키" />
        <activity
            android:name=".AutoComplete"
            android:label="@string/title_activity_auto_complete" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

AutoComplete.class

    private static final String API_KEY = "브라우저 애플리케이션용 키";

직접 입력해도 괜찮지만 편의정을 위해 입력해놓고 사용하는걸 추천합니다.

# Autocomplete


private ArrayList<AutoCompleteBean> autocomplete(String input) {

        ArrayList<AutoCompleteBean> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?input=" + URLEncoder.encode(input, "utf8"));
            sb.append("&sensor=true&key=" + API_KEY);
            
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());
            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");
            // Extract the Place descriptions from the results
            resultList = new ArrayList<AutoCompleteBean>(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(new AutoCompleteBean(predsJsonArray.getJSONObject(i).getString("description"), predsJsonArray.getJSONObject(i).getString("reference")));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cannot process JSON results", e);
        }
        return resultList;
    }
    
# Result Screen

![ScreenShot](http://sangcomz.cafe24.com/eximg/auto1.png)  ![ScreenShot](http://sangcomz.cafe24.com/eximg/auto2.png)
