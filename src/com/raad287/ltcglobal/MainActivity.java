package com.raad287.ltcglobal;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.*;
import android.os.Bundle;
import com.androidplot.Plot;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.*;
 
import java.text.*;
import java.util.Arrays;
import java.util.Date;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.os.AsyncTask;
import android.os.Bundle;

import android.app.Activity;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;




public class MainActivity extends Activity {
	public static final String URL_API_HISTORY = "http://www.litecoinglobal.com/api/history/";
	public static final String URL_API_TICKER = "http://www.litecoinglobal.com/api/ticker/";
	
	final Context context = this;
	
	// parseTickersRE: raw string return from ticker, and format into JSON using trade timestamp as id
	public JSONObject parseHistoryRE(String sHistory)
	{ 
		JSONObject jHistory = new JSONObject();
		String re1=".*?";	// Non-greedy match on filler
		String re2="\\{.*?\\}";	// Uninteresting: cbraces
		String re3=".*?";	// Non-greedy match on filler
		String re4="(\\{.*?\\})";	// Curly Braces 1
	
		Pattern p = Pattern.compile(re1+re2+re3+re4,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(sHistory);
	    
		while (m.find())
	    {
	        String cbraces1=m.group(1);
	        try {
	        	JSONObject jTrade = new JSONObject(cbraces1);
	        	
	        	String timestamp = jTrade.getString("timestamp"); 	// extract timestamp from trade
	        	jTrade.remove("timestamp");		// remove
				jHistory.put(timestamp, jTrade);	// use timestamp as trade id in jHistory
				
			} catch (JSONException e) { 	Log.i("LG", "JSONException:"+e.getMessage()); 	}
	    }
		
		return jHistory;
	}
	
	// take unsorted ticker string, return JSONObject sorted using ticker value as ID
	public JSONObject parseTickersJSON(String sTickers)
	{

		JSONObject jTicker = new JSONObject();
		try {
			jTicker = new JSONObject(sTickers);
			
		} catch (JSONException e) {
			Log.i("LG", "JSONException:"+e.getMessage());
			jTicker.toString();
		}
		
		return jTicker;
	}
	
	public void fillTicker(JSONObject jTicker)
	{
		TextView tv_latest = (TextView) findViewById(R.id.main_textView_latest);
		TextView tv_bid = (TextView) findViewById(R.id.main_textView_bid);
		TextView tv_ask = (TextView) findViewById(R.id.main_textView_ask);
		TextView tv_24h_low = (TextView) findViewById(R.id.main_textView_24h_low);
		TextView tv_24h_high = (TextView) findViewById(R.id.main_textView_24h_high);
		TextView tv_24h_avg = (TextView) findViewById(R.id.main_textView_24h_avg);
		TextView tv_24h_vol = (TextView) findViewById(R.id.main_textView_24h_vol);
		TextView tv_7d_avg = (TextView) findViewById(R.id.main_textView_7d_avg);
		TextView tv_7d_vol = (TextView) findViewById(R.id.main_textView_7d_vol);
		TextView tv_total_vol = (TextView) findViewById(R.id.main_textView_total_vol);
		TextView tv_type = (TextView) findViewById(R.id.main_textView_type);
		TextView tv_ticker = (TextView) findViewById(R.id.main_textView_ticker);
		
		try
		{
			tv_latest.setText(jTicker.getString("latest"));
			tv_bid.setText(jTicker.getString("bid"));
			tv_ask.setText(jTicker.getString("ask"));
			tv_24h_high.setText(jTicker.getString("24h_high"));
			tv_24h_low.setText(jTicker.getString("24h_low"));
			tv_24h_avg.setText(jTicker.getString("24h_avg"));
			tv_24h_vol.setText(jTicker.getString("24h_vol"));
			tv_7d_avg.setText(jTicker.getString("7d_avg"));
			tv_7d_vol.setText(jTicker.getString("7d_vol"));
			tv_total_vol.setText(jTicker.getString("total_vol"));
			tv_type.setText(jTicker.getString("type"));
			tv_ticker.setText(jTicker.getString("ticker"));
			
		}catch (JSONException e) { Log.i("LG", "JSONException:"+e.getMessage()); }
		
	}
	
	public void initChart()
	{
		XYPlot mySimpleXYPlot= (XYPlot) findViewById(R.id.mySimpleXYPlot);
		
		//customize domain and range labels
        mySimpleXYPlot.setDomainLabel("Day of Month");
        mySimpleXYPlot.setRangeLabel("Price/LTC");
        
        // get rid of decimal points
        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("0"));
        mySimpleXYPlot.setRangeStep(XYStepMode.SUBDIVIDE, 10);
        mySimpleXYPlot.setDomainValueFormat(new Format() {

        	// create a simple date format that draws on the year portion of our timestamp.
        	// see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
        	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
 
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
 
                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue() * 1000;
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }
 
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
 
            }});
        
        // reduce the number of range labels
        mySimpleXYPlot.setTicksPerRangeLabel(3);
        
     // by default, AndroidPlot displays developer guides to aid in laying out your plot.
        // To get rid of them call disableAllMarkup():
        mySimpleXYPlot.disableAllMarkup();
	}
		
	public void fillChart(JSONObject jHistory, boolean desc)
	{
		XYPlot mySimpleXYPlot;
		 // initialize our XYPlot reference:
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
 
        
     		Log.i("LG", "Get Ids");
     		JSONArray jIds = jHistory.names();
     		int num_trades=jHistory.names().length();
     		long[] sorted_ids= new long[num_trades];
     		for (int i=0; i<num_trades; i++)
     		{
     			try {
     				sorted_ids[i]=jIds.getLong(i);
     			} catch (JSONException e) { Log.i("LG", "JSONException:"+e.getMessage()); }
     		}
     		
     		// Sort
     		Log.i("LG", "Sort");
     		Arrays.sort(sorted_ids); 	// Ascending
     		if (desc==true) { 
     			long[] asc_ids = new long [num_trades];
     			for (int i=0; i<num_trades; i++) { asc_ids[i]=sorted_ids[i]; }
     			for (int i=0; i<num_trades; i++) { sorted_ids[i]=asc_ids[(num_trades-1)-i]; }
     		}
     		
     		Number[] timestamps = new Number[num_trades];
     		Number[] amount = new Number[num_trades];
     		Number[] quantity = new Number[num_trades];
     		
     		for (int i=0; i<num_trades; i++)
    		{
     			try {
					JSONObject jTrade = jHistory.getJSONObject(Long.toString(sorted_ids[i]));
					timestamps[i] = sorted_ids[i];
					amount[i] = jTrade.getLong("amount");
					quantity[i] = jTrade.getLong("quantity");
					
				} catch (JSONException e) {
					Log.i("LG", "JSONException:"+e.getMessage());
				}
    		}
     	
        
        // create our series from our array of nums:
        XYSeries series2 = new SimpleXYSeries(
                Arrays.asList(timestamps),
                Arrays.asList(amount),
                "Price");
 
        /*
        // Turn the above arrays into XYSeries':
        XYSeries series1 = new SimpleXYSeries(
                Arrays.asList(series1Numbers),          // SimpleXYSeries takes a List so turn our array into a List
                SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, // Y_VALS_ONLY means use the element index as the x value
                "Series1");                             // Set the display title of the series
 		*/
        
 
        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 200, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                null);                                  // fill color (none)
 
 
        // same as above:
        mySimpleXYPlot.addSeries(series2,
                new LineAndPointFormatter(Color.rgb(0, 0, 200), Color.rgb(0, 0, 100), null));
 
        mySimpleXYPlot.invalidate();
	}

	
	
	// fillTable: fill trade history table main UI
	public void fillTable(JSONObject jHistory, boolean desc)
	{
		// Get ids
		Log.i("LG", "Get Ids");
		JSONArray jIds = new JSONArray();
		jIds = jHistory.names();
		int num_trades=jHistory.names().length();
		long[] sorted_ids= new long[num_trades];
		for (int i=0; i<num_trades; i++)
		{
			try {
				sorted_ids[i]=jIds.getLong(i);
			} catch (JSONException e) { Log.i("LG", "JSONException:"+e.getMessage()); }
		}
		
		// Sort
		Log.i("LG", "Sort");
		Arrays.sort(sorted_ids); 	// Ascending
		if (desc==true) { 
			long[] asc_ids = new long [num_trades];
			for (int i=0; i<num_trades; i++) { asc_ids[i]=sorted_ids[i]; }
			for (int i=0; i<num_trades; i++) { sorted_ids[i]=asc_ids[(num_trades-1)-i]; }
			
		}
		
		// Fill Layout
		Log.i("LG", "Fill Layout");

		// Description Row
		TableRow tr_desc = new TableRow(this);
		tr_desc.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_desc_timestamp = new TextView(this);
		TextView tv_desc_amount = new TextView(this);
		TextView tv_desc_quantity = new TextView(this);
		tv_desc_timestamp.setText("Timestamp");
		tv_desc_amount.setText("Amount (LTC)");
		tv_desc_quantity.setText("Quantity");
		tv_desc_timestamp.setPadding(0, 0, 10, 0);
		tv_desc_amount.setPadding(0, 0, 10, 0);
		
		tr_desc.addView(tv_desc_timestamp);
		tr_desc.addView(tv_desc_amount);
		tr_desc.addView(tv_desc_quantity);
		TableLayout tl_trade_history = (TableLayout) findViewById(R.id.main_tableLayout_history);
		tl_trade_history.removeAllViews();
		tl_trade_history.addView(tr_desc);
		for (int i=0; i<num_trades; i++)
		{
			Log.i("LG", "Fill Table");
			// Trade Row
			TableRow tr_trade = new TableRow(this);
			tr_trade.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			TextView tv_timestamp = new TextView(this);
			TextView tv_amount = new TextView(this);
			TextView tv_quantity = new TextView(this);
			tv_timestamp.setPadding(0, 0, 10, 0);
			tv_amount.setPadding(0, 0, 10, 0);
			
			// Pull trade data from JSON
			try {
				JSONObject jTrade = jHistory.getJSONObject(Long.toString(sorted_ids[i]));
				tv_timestamp.setText(Long.toString(sorted_ids[i]));
				tv_amount.setText(jTrade.getString("amount"));
				tv_quantity.setText(jTrade.getString("quantity"));
			} catch (JSONException e) {
				Log.i("LG", "JSONException:"+e.getMessage());
			}
	
			Log.i("LG", "Add TextViews");
			tr_trade.addView(tv_timestamp);
			tr_trade.addView(tv_amount);
			tr_trade.addView(tv_quantity);
			tl_trade_history.addView(tr_trade);
		}
	}

	public class DownloadHistory extends AsyncTask<String, Integer, JSONObject> 
	{
		

		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading History...");
			tv.invalidate();
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download trades");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
				
			} catch (Exception e) { 
				Log.i("LG", "Exception:"+e.getMessage());
				return null;
			}
			
			// Check for errors
			if(sb.toString().contains("only please")) // error
			{
				return null;
			}
					
			// Minor string formatting 
			sb.deleteCharAt(0);		// delete leading '['
			sb.deleteCharAt(sb.length()-1); // delete trailing ']'
			
		 	return parseHistoryRE(sb.toString());
		}

		@Override
		protected void onPostExecute(JSONObject jHistory) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			if(jHistory !=null)
			{
			fillTable(jHistory,true);
			fillChart(jHistory,true);
			tv.setText("Success");
			}
			else
			{
				AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
				alertDialogBuilder.setMessage("Unable to download ticker");
				alertDialogBuilder.setTitle("Error");
				alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               // User clicked OK button
			           }
			       });
				alertDialogBuilder.show();
				
				tv.setText("Failed");
			}
			tv.invalidate();
			super.onPostExecute(jHistory);
		}
		
	}
	
	public class DownloadTicker extends AsyncTask<String, Integer, JSONObject> 
	{
		

		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading Ticker...");
			tv.invalidate();
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download trades");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); }
			
			// Check for errors
			if(sb.toString().contains("only please")) // error
			{
				return null;
			}
			
		 	return parseTickersJSON(sb.toString());
		}

		@Override
		protected void onPostExecute(JSONObject jTicker) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading...");
			if(jTicker!=null) { 
				fillTicker(jTicker); 
				tv.setText("Success");
			}
			else { tv.setText("Failed"); }
			tv.invalidate();
			
			super.onPostExecute(jTicker);
			
		}
		
	}
	


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  
        initChart();
        
        // Setup buttons
        Button btn_lookup = (Button) findViewById(R.id.main_btn_lookup);
     	btn_lookup.setOnClickListener( new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText et_ticker = (EditText) findViewById(R.id.main_editText_ticker);
     				if(!et_ticker.getText().toString().equals("")) // make sure there's something
     				{
     					new DownloadHistory().execute(URL_API_HISTORY+et_ticker.getText().toString());
     					new DownloadTicker().execute(URL_API_TICKER+et_ticker.getText().toString());
     				}
					
				}
     		});
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}



