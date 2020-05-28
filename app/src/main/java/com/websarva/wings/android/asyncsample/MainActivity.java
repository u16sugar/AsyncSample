package com.websarva.wings.android.asyncsample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 画面部品ListViewを取得
        ListView lvCityList = findViewById(R.id.lvCityList);
        // Simple Adapterで使用するListオブジェクトを用意
        List<Map<String, String>> cityList = new ArrayList<>();
        // 都市データを格納するMapオブジェクトの用意とCityListへのデータ登録
        Map<String, String> city = new HashMap<>();
//        city = new HashMap<>();
        city.put("name", "福島");
        city.put("id", "070010");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "さいたま");
        city.put("id", "110010");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "東京");
        city.put("id", "130010");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "大阪");
        city.put("id", "270000");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "銚子");
        city.put("id", "120020");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "館山");
        city.put("id", "120030");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "八丈島");
        city.put("id", "130030");
        cityList.add(city);
        city = new HashMap<>();
        city.put("name", "父島");
        city.put("id", "130040");
        cityList.add(city);


        String[] from = {"name"};
        int[] to = {android.R.id.text1};
        // Simple Adapterを生成
        SimpleAdapter adapter = new SimpleAdapter(MainActivity.this, cityList, android.R.layout.simple_expandable_list_item_1, from, to);
        // ListViewにSimple Adapterを設定
        lvCityList.setAdapter(adapter);
        // ListViewにリスナを設定
        lvCityList.setOnItemClickListener(new ListItemClickListener());
    }

    /**
     * リストが選択されたときの処理が記述されたメンバクラス
     */
    private class ListItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // ListViewでタップされた行の都市名と都市IDを取得
            Map<String, String> item = (Map<String, String>) parent.getItemAtPosition(position);
            String cityName = item.get("name");
            String cityId = item.get("id");
            // 取得した都市名をtvCityNameに設定
            TextView tvCityName = findViewById(R.id.tvCityName);
            tvCityName.setText(cityName + "の天気：");
            // 天気情報を表示するTextViewを取得
            TextView tvWeatherTelop = findViewById(R.id.tvWeatherTelop);
            // 天気詳細情報を表示するTextViewを取得
            TextView tvWeatherDesc = findViewById(R.id.tvWeatherDesc);
            // WeatherInfoReceiverをnew。引数として上で取得したTextViewを渡す
            WeatherInfoReceiver receiver = new WeatherInfoReceiver(tvWeatherTelop, tvWeatherDesc);
            // WeatherInfoReceiverを実行
            receiver.execute(cityId);
        }
    }

    private class WeatherInfoReceiver extends AsyncTask<String, String, String> {
        /**
         * 現在の天気を表示する画面部品フィールド
         */
        private TextView _tvWeatherTelop;
        /**
         * 天気の詳細を表示する画面部品フィールド
         */
        private TextView _tvWeatherDesc;

        /**
         * コンストラクタ
         * お天気情報を表示する画面部品をあらかじめ取得してフィールドに格納している。
         */
        public WeatherInfoReceiver(TextView tvWeatherTelop, TextView tvWeatherDesc) {
            // 引数をそれぞれのフィールドに格納
            _tvWeatherTelop = tvWeatherTelop;
            _tvWeatherDesc = tvWeatherDesc;
        }

        @Override
        public String doInBackground(String... params) {
            // 可変長引数の１個目（インデックス０）を取得。これが都市ID
            String id = params[0];
            // 都市IDを使って接続URL文字列を生成
            String urlStr = "http://weather.livedoor.com/forecast/webservice/json/v1?city=" + id;
            // 天気情報サービスから取得したJSON文字列。天気情報が格納されている。
            String result = "";
            // HTTP接続を行うHttpURLConnectionオブジェクトを宣言。finallyで確実に解放するためにtry外で宣言。
            HttpURLConnection con = null;
            // HTTP接続のレスポンスデータとして取得するInputStreamオブジェクトを宣言。同じくtry外で宣言。
            InputStream is = null;
            try {
                // URLオブジェクトを生成
                URL url = new URL(urlStr);
                // URLオブジェクトからHttpURLConnectionオブジェクトを取得
                con = (HttpURLConnection) url.openConnection();
                // HTTP接続メソッドを設定
                con.setRequestMethod("GET");
                // 接続
                con.connect();
                // HttpURLConnectionオブジェクトからレスポンスデータを取得
                is = con.getInputStream();
                // レスポンスデータであるInputStreamオブジェクトを文字列に変換
                result = is2String(is);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                // HttpURLConnectionオブジェクトがnullでないなら解放
                if(con != null) {
                    con.disconnect();
                }
                // InputStreamオブジェクトがnullでないなら解放
                if(is != null) {
                    try {
                        is.close();
                    } catch (IOException ex) {
//                        ex.printStackTrace();
                    }
                }
            }

            return result;
        }

        @Override
        public void onPostExecute(String result) {
            // 天気情報用文字列変数を用意
            String telop = "";
            String desc = "";

            // TODO: 2020/05/27 ここに天気情報
            try {
                // JSON文字列からJSONObjectオブジェクトを生成。これをルートJSONオブジェクトとする。
                JSONObject rootJSON = new JSONObject(result);
                // ルートJSON直下の「description」JSONオブジェクトを取得
                JSONObject descriptionJSON = rootJSON.getJSONObject("description");
                // 「description」プロパティ直下の「text」文字列（天気概況文）を取得
                desc = descriptionJSON.getString("text");
                // ルートJSON直下の「forecasts」JSON配列を取得
                JSONArray forecasts = rootJSON.getJSONArray("forecasts");
                // 「forecasts」JSON配列の１つ目（インデックス０）のJSONオブジェクトを取得
                JSONObject forecastNow = forecasts.getJSONObject(0);
                // 「forecasts」１つ目のJSONオブジェクトから「telop」文字列（天気）を取得
                telop = forecastNow.getString("telop");
            }
            catch (JSONException ex) {
            }

            // 天気情報用文字列をTextViewにセット
            _tvWeatherTelop.setText(telop);
            _tvWeatherDesc.setText(desc);
        }

        private String is2String(InputStream is) throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuffer sb = new StringBuffer();
            char[] b = new char[1024];
            int line;
            while (0 <= (line = reader.read(b))) {
                sb.append(b, 0, line);
            }
            return sb.toString();
        }

    }

}
