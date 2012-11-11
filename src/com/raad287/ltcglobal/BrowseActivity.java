package com.raad287.ltcglobal;


import java.util.Arrays;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import android.widget.ListView;



public class BrowseActivity extends Activity {
	
	public static final String URL_API_TICKER = "http://www.litecoinglobal.com/api/ticker/";
	// take unsorted ticker string, return JSONObject
		public JSONObject parseTickersJSON(String sTickers)
		{

			JSONObject jTicker = new JSONObject();
			try {
				jTicker = new JSONObject(sTickers);
				
			} catch (JSONException e) {
				Log.i("LG", "JSONException:"+e.getMessage());
				return null;
			}
			
			return jTicker;
		}

	public class DownloadBrowse extends AsyncTask<String, Integer, JSONObject> 
	{
		TextView tv;
		@Override
		protected void onPreExecute() {
			LinearLayout ll = (LinearLayout)findViewById(R.id.browse_linearLayout);
			tv = new TextView(getApplicationContext());
			tv.setText("Downloading Tickers");
			ll.addView(tv);
			ll.invalidate();
			
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... url) {
			Log.i("LG", "BrowseActivity:DownloadBrowse:doInBackground: Executing download names");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_TICKER);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
			
		 	return parseTickersJSON(sb.toString());
		}

		@Override
		protected void onPostExecute(JSONObject jTickers) {
			LinearLayout ll = (LinearLayout)findViewById(R.id.browse_linearLayout);
			ll.removeView(tv);
			ll.invalidate();
			populateList(jTickers);
			super.onPostExecute(jTickers);
			
		}
		
	}
	
	public void populateList(JSONObject jTickers)
	{	
		String[] items;
		items = new String[jTickers.names().length()];
		for(int i=0; i<jTickers.names().length(); i++)
		{
			try {
				items[i]=jTickers.names().getString(i);
				Log.i("LG", "Item"+items[i]);
			} catch (JSONException e) { 
				// if populateList fails finish the activity
				Log.i("LG", "JSONException:"+e.getMessage()); 
				Intent returnIntent = new Intent();
		    	 setResult(RESULT_CANCELED,returnIntent);     
		    	 finish();
				
				}
		}
		Arrays.sort(items);
		Log.i("LG", "Items:"+items.toString());
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
		        android.R.layout.simple_list_item_1, items);
		ListView lv_tickers = (ListView) this.findViewById(R.id.browse_listView_tickers);
		lv_tickers.setAdapter(adapter);
		lv_tickers.invalidate();


	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_browse);
		super.onCreate(savedInstanceState);
		new DownloadBrowse().execute("");
		
		ListView lv_browse = (ListView) findViewById(R.id.browse_listView_tickers);
		lv_browse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> arg0, View view, int arg2,long itemID) {
		    	
		    	//return what the user selected to the calling activity
		    	 String item = ((TextView)view).getText().toString();
		    	 Intent returnIntent = new Intent();
		    	 returnIntent.putExtra("result",item);
		    	 setResult(RESULT_OK,returnIntent);     
		    	 finish();

		    }
		});
	}
}
