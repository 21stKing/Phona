package com.kinotech.phona;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class Download
{
    private String DEBUG_TAG = "Getting HTTP Response from kinotech ";
    private static String IMAGE_TAG = "Image_Download_HTTP_CONNECTION";
    protected static String SPACE = "%20";
    private String contactSize;
    static ArrayList<Contact> conts = new ArrayList<Contact>();

    public Download(){

    }

    private void setContents(String content)
    {
        try{
            JSONObject  json =  new JSONObject(content);
            JSONArray jarray =  json.getJSONArray("contact");

            for(int i = 0;i < jarray.length();i++)
            {
                JSONObject json_data = jarray.getJSONObject(i);
                conts.add(new Contact(Integer.parseInt(json_data.getString("contactid")), json_data.getString("name"), "",""));
            }
        }catch (JSONException e){System.out.println(e);}
    }

    private void setSize(String content)
    {
        try
        {
            JSONObject json_data = new JSONObject(content.substring(content.indexOf("{"), content.lastIndexOf("}") + 1));
            contactSize = (json_data.getString("size"));
        }catch (JSONException e){System.out.println(e);}
    }

    public String getSize(){
        return this.contactSize;
    }
    public ArrayList<Contact> getContents(){
        return conts;
    }

    public static InputStream downloadFile(String fileUrl) throws IOException
    {
        InputStream is = null;

        try
        {
            URL url = new URL(fileUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(IMAGE_TAG, "The response is: " + response);
            is = conn.getInputStream();
        }finally{if (is != null){}}

        return is;
    }

    public String download(String myurl, String task) throws IOException
    {
        InputStream is = null;
        String status;
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            int response = conn.getResponseCode();
            is = conn.getInputStream();
            Log.d(DEBUG_TAG, "The response is: " + response);
            String contentAsString = GetText(is);
            if(response == 200){
                status = "good";
                if(task == "size"){
                    setSize(contentAsString);
                }else{
                    setContents(contentAsString);  //contact
                }
            }else{
                status = "bad";
            }
            return status;
        }finally{
            if(is != null)
            {is.close();}
        }
    }

    private static String GetText(InputStream in)
    {
        String text = "";
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();
        String line = null;

        try{
            while ((line = reader.readLine()) != null)
            { sb.append(line + "\n");}
            text = sb.toString();
        }
        catch(Exception ex){ }
        finally{
            try{in.close();}
            catch(Exception ex){}
        }
        return text;
    }

    public static String splitFunction(String splitSearchIn)
    {
        String newSearchVal = "";
        String [] splitArray = splitSearchIn.split(" ");

        if(splitArray.length == 0)
        {
            newSearchVal = "";
        }
        else if(splitArray.length == 1)
        {
            newSearchVal = splitArray[0];
        }
        else if(splitArray.length == 2)
        {
            newSearchVal = splitArray[0] + SPACE + splitArray[1];
        }
        else if(splitArray.length == 3)
        {
            newSearchVal = splitArray[0] + SPACE + splitArray[1] + SPACE + splitArray[2];
        }
        else if(splitArray.length == 4)
        {
            newSearchVal = splitArray[0] + SPACE + splitArray[1] + SPACE + splitArray[2] + SPACE + splitArray[3];
        }
        else if(splitArray.length == 5)
        {
            newSearchVal = splitArray[0] + SPACE + splitArray[1] + SPACE + splitArray[2] + SPACE + splitArray[3] + SPACE + splitArray[4];
        }
        else
        {
            newSearchVal = splitSearchIn;

        }

        return newSearchVal;
    }
}

