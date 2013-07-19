package com.raad287.ltcglobal;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;




import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.widget.ListView;
import com.raad287.ltcglobal.Constants;

public class BrowseActivity extends Activity {
	
	Constants constants = new Constants();
	
	public class BrowseItem extends Object{
		private String itemTicker;
		private String itemLatest;
		private String item24hAvg;
		private int triangle;  // 0 = same -1 = down 1 = up
		
		public BrowseItem(String itemTicker, String itemLatest, String item24hAvg, int triangle)
		{
			// Init Ticker
			if(itemTicker!=null) { this.itemTicker=itemTicker; }
			else { this.itemTicker="Ticker: Error"; }

			if(itemLatest!=null) { this.itemLatest=itemLatest; }
			else { this.itemLatest="Latest: Error"; }
			
			if(triangle==0 || triangle == -1 || triangle==1 ) {
				this.triangle=triangle;
			} else { this.triangle=0; }
			
			if(item24hAvg!=null) {this.item24hAvg=item24hAvg; }
			else { this.item24hAvg="24h Avg:Error"; }
		
		}
		
		public String getItemTicker() {
			return itemTicker;
		}
		
		public void setItemTicker(String itemTicker) {
			this.itemTicker=itemTicker;
		}
		
		public String getItemLatest() {
			return itemLatest;
		}
		
		public void setItemLatest(String itemLatest) { 
			this.itemLatest=itemLatest;
		}
		
		public String get24hAvg()
		{
			return this.item24hAvg;
		}
		
		public void setItem24hAvg(String item24hAvg)
		{
			this.item24hAvg=item24hAvg;
		}
		
		public int getTriangle() {
			return triangle;
		}
		
		public void setTriangle(int triangle) {
			this.triangle=triangle;
		}

		public Object getText() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private class BrowseItemAdapter extends ArrayAdapter<BrowseItem> {

		Context context;
        public BrowseItemAdapter(Context context, int resourceId,
				List<BrowseItem> items) {
			super(context, resourceId, items);
			this.context=context;
			// TODO Auto-generated constructor stub
		}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) 
        {
        	View v = convertView;
        	if (v == null) {
        		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        		v = vi.inflate(R.layout.browse_row, null);
        	}

                BrowseItem browse_item = getItem(position);
                if (browse_item != null) {
                        TextView tv_ticker = (TextView) v.findViewById(R.id.tv_row_ticker);
                        TextView tv_latest = (TextView) v.findViewById(R.id.tv_row_latest);
                        TextView tv_24h_avg = (TextView) v.findViewById(R.id.tv_row_24h_avg);
                        ImageView img_icon = (ImageView) v.findViewById(R.id.img_row_icon);
                        if (tv_ticker != null) {
                              tv_ticker.setText(browse_item.getItemTicker());                            }
                        if(tv_latest != null){
                              tv_latest.setText("Latest: "+ browse_item.getItemLatest());
                        }
                        if(tv_24h_avg!= null)
                        {
                        	tv_24h_avg.setText("24h Avg:"+browse_item.get24hAvg());
                        }
                        if(img_icon!=null) {
                        	Bitmap mBitmap;
                        	switch (browse_item.getTriangle())
                        	{
                        		case -1: // down
                        			img_icon.setImageResource(R.drawable.ic_down);		
                        			break;
                        		case 0: // same
                        			img_icon.setImageResource(R.drawable.ic_same);	
                        			break;
                        		case 1:
                        			img_icon.setImageResource(R.drawable.ic_up);	
                        			break;
                        		default:
                        			img_icon.setImageResource(R.drawable.ic_same);	
                        			break;
                        	} 
                        } 
                }
                return v;
        }
}
	
	
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
			HttpGet http_get = new HttpGet(constants.URL_API_TICKER);
			
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
		browseItems=new ArrayList<BrowseItem>();
		
		// get all ticker names
		String[] sorted_names= new String[jTickers.names().length()];
		
		for(int i=0; i<jTickers.names().length(); i++)
		{
			try {
				sorted_names[i]=jTickers.names().getString(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.i("LG", "Error: failed to sort ticker name array");
			}
		}
		
		// sort the array
		Arrays.sort(sorted_names);
		
		for(int i=0; i<jTickers.names().length(); i++)
		{
			try {
				String ticker_name = sorted_names[i];
				//Log.i("LG", "ticker_name:"+ticker_name);
				String ticker_latest = jTickers.getJSONObject(ticker_name).getString("latest");
				//Log.i("LG", "ticker_latest:"+ticker_latest);
				String ticker_24h_avg = jTickers.getJSONObject(ticker_name).getString("24h_avg");
				//Log.i("LG", "ticker_24h_avg:"+ticker_24h_avg);
				int triangle;
				if(ticker_latest.split("@").length>1)
				{
				ticker_latest=ticker_latest.split("@")[1];
				}
				//Log.i("LG", "ticker_latest after split:"+ticker_24h_avg);
				
				try {
				// going up
				if( Double.parseDouble(ticker_latest) > Double.parseDouble(ticker_24h_avg) )
				{
					triangle=1;
				}
				// going down
				else if(Double.parseDouble(ticker_latest) < Double.parseDouble(ticker_24h_avg) )
				{
					triangle=-1;
				}
				else { triangle=0; }
				} catch (NumberFormatException nfe) { 
					Log.i("LG", "NumberFormatException:"+nfe.getMessage());
					triangle=0;
				}
				
				BrowseItem item = new BrowseItem(ticker_name, ticker_latest, ticker_24h_avg, triangle);
				browseItems.add(item);
				
			} catch (JSONException e) { 
				// if populateList fails finish the activity
				Log.i("LG", "JSONException:"+e.getMessage()); 
				Intent returnIntent = new Intent();
		    	 setResult(RESULT_CANCELED,returnIntent);     
		    	 finish();
			}
		}

		//Log.i("LG", "Items:"+items.toString());
		BrowseItemAdapter adapter = new BrowseItemAdapter(this, 
		        R.layout.browse_row, browseItems);
		ListView lv_tickers = (ListView) this.findViewById(R.id.browse_listView_tickers);
		lv_tickers.setAdapter(adapter);
		lv_tickers.invalidate();
	}
	
	List<BrowseItem> browseItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.activity_browse);
		super.onCreate(savedInstanceState);
		new DownloadBrowse().execute("");

		
		ListView lv_browse = (ListView) findViewById(R.id.browse_listView_tickers);
		lv_browse.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		    public void onItemClick(AdapterView<?> arg0, View view, int position,long itemID) {
		    	
		    	//return what the user selected to the calling activity
		    	 BrowseItem item = browseItems.get(position);
		    	 
		    	 Intent returnIntent = new Intent();
		    	 returnIntent.putExtra("result",item.getItemTicker());
		    	 setResult(RESULT_OK,returnIntent);     
		    	 finish();

		    }
		});
	}
}
