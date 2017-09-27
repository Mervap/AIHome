package com.example.valera.homeweatherstation.MySqlReader;

import com.example.valera.homeweatherstation.parser.JSONParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Valera on 23.03.2015.
 */
public class ReceiveData {

    //Экземпляр JSONParser'а
    static JSONParser jsonParser = new JSONParser();

    public static JSONObject ReceiveData(String url_get_data, String dateTimeFrom, String dateTimeTo){
        try {
            // Список параметров
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("dateTimeFrom", dateTimeFrom));
            params.add(new BasicNameValuePair("dateTimeTo", dateTimeTo));
            // получаем информацию по HTTP запросу
            JSONObject json = jsonParser.makeHttpRequest(url_get_data, "GET", params);
            return json;
        }
        catch (NullPointerException e) {
             return null;
        }
    }

    public static JSONObject ReceiveData(String url_get_data) {
        try {
            // Список параметров
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // получаем информацию по HTTP запросу
            JSONObject json = jsonParser.makeHttpRequest(url_get_data, "GET", params);
            return json;

        }
        catch (NullPointerException e) {
            return null;
        }
    }
}
