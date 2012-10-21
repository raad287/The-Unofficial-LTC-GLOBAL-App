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
import java.util.Iterator;
import java.util.Set;

import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;
import com.androidplot.xy.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
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
import android.os.Build;
import android.os.Bundle;

import android.app.Activity;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;




public class MainActivity extends Activity {
	public static final String URL_API_HISTORY = "http://www.litecoinglobal.com/api/history/";
	public static final String URL_API_TICKER = "http://www.litecoinglobal.com/api/ticker/";
	public static final String URL_API_SECURITY ="http://www.litecoinglobal.com/security/";
	
	final Context context = this;
	
	public class SpinnerListener implements OnItemSelectedListener {

		int curScreen;
		

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Spinner spn = (Spinner)findViewById(R.id.main_spinner_selector);
			EditText et = (EditText)findViewById(R.id.main_editText_ticker);
			if(!et.getText().toString().equals("")) // make sure there's something
				{
					Query((String)spn.getSelectedItem(),et.getText().toString());
					
				}
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
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
				
			} catch (JSONException e) { 	
				Log.i("LG", "JSONException:"+e.getMessage()); 
				return null;}
	    }
		
		return jHistory;
	}
	
	// parses the security page to fill contract & prospectus, notifications and motions
	// returns JSON
	public JSONObject parseSecurityHTML(String sPage)
	{
		// Must do error checking before this point, will crash if bad request
		
		JSONObject jContract=new JSONObject();
		JSONObject jNotifications = new JSONObject();
		JSONObject jMotions = new JSONObject();
		
		
		// Find Ticker
		int id1=0;
		int id2=0;
		int id3=0;
		id1=sPage.indexOf("Ticker</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<td>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</td>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		char[] buffer=new char[id3-(id2+4)];
		
		sPage.getChars(id2+4, id3, buffer, 0);
		String parse = String.copyValueOf(buffer);
		try {	jContract.put("ticker", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Peer Approval
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Approval</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<td>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</td>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+4)];
		
		sPage.getChars(id2+4, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("peer approval", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Shares Issued
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Issued</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<td>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</td>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+4)];
		
		sPage.getChars(id2+4, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("shares issued", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }		
		
		// Find Shares Outstanding
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Outstanding</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<td>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</td>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+4)];
		
		sPage.getChars(id2+4, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("shares outstanding", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }

		// Find Issuer
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Issuer</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<td>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</td>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+4)];
		
		sPage.getChars(id2+4, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("issuer", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Issuer Detail
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Detail</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("issuer detail", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Contract
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Contract</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("contract", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		
		// Find Executive Summary
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Summary</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("executive summary", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Business Description
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Description</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("business description", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Definition of the Market
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Market</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("definition of market", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Products and Services
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Services</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("products and services", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Organization and Management
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("and Management</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("organization and management", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Market Strategy
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Strategy</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("marketing strategy", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		// Find Financial Management
		Log.i("LG","sPage:"+sPage.toString());
		id1=sPage.indexOf("Strategy</th>");
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf("<pre>",id1); //start
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf("</pre>",id2); //end
		Log.i("LG","id3:"+Integer.toString(id3));
	
		buffer=new char[id3-(id2+5)];
		
		sPage.getChars(id2+5, id3, buffer, 0);
		parse = String.copyValueOf(buffer);
		try {	jContract.put("financial management", parse);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		
		
		
		
		return jContract;
		
		
		
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
        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("0.00"));
        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS, 10);
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
        Iterator series_iterator=mySimpleXYPlot.getSeriesSet().iterator();
      
        
   
        // clear out the previous series
        while(series_iterator.hasNext())
        {
        	mySimpleXYPlot.removeSeries((XYSeries)series_iterator.next());
        }
        
     		Log.i("LG", "Get Ids");
     		JSONArray jIds = jHistory.names();
     		if(jIds==null)
     		{
     			return;
     		}
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
					amount[i] = jTrade.getDouble("amount");
					quantity[i] = jTrade.getLong("quantity");
					
				} catch (JSONException e) {
					Log.i("LG", "JSONException:"+e.getMessage());
				}
    		}
     	
        
        // create our series from our array of nums:
        XYSeries series = new SimpleXYSeries(
                Arrays.asList(timestamps),
                Arrays.asList(amount),
                "Price");

        
 
        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter series1Format = new LineAndPointFormatter(
                Color.rgb(0, 200, 0),                   // line color
                Color.rgb(0, 100, 0),                   // point color
                null);                                  // fill color (none)
 
 
        // same as above:
        mySimpleXYPlot.addSeries(series,series1Format);
 
        
     
        mySimpleXYPlot.redraw();
        //mySimpleXYPlot.invalidate();
	}

	
	
	// fillTable: fill trade history table main UI
	public void fillTable(JSONObject jHistory, boolean desc)
	{
		// do nothing if the history is null
		if(jHistory==null)
		{
			return;
		}
		
		// Get ids
		Log.i("LG", "Get Ids");
		JSONArray jIds = new JSONArray();
		jIds = jHistory.names();
		if(jIds==null)
		{
			return;
		}
		int num_trades=jIds.length();
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
		TableLayout tl_trade_history = (TableLayout) findViewById(R.id.main_tableLayout_data);
		tl_trade_history.removeAllViews();
		
		TextView tv_title = new TextView(this);
		tv_title.setText("Trade History");
		tl_trade_history.addView(tv_title);
		
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
	
	public void fillContract(JSONObject jContract)
	{
		TableLayout tl_data = (TableLayout) findViewById(R.id.main_tableLayout_data);
		tl_data.removeAllViews();
		
		try {
		// Name & Ticker row
		TableRow tr_ticker = new TableRow(this);
		tr_ticker.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_ticker = new TextView(this);
		tv_ticker.setText("Contract & Prospectus: "+jContract.getString("ticker")+"\n");
		tr_ticker.addView(tv_ticker);
		tl_data.addView(tr_ticker);
		
		// Contract row
		TableRow tr_contract = new TableRow(this);
		tr_contract.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_contract = new TextView(this);
		TextView tv_contract = new TextView(this);
		tv_lbl_contract.setText("Contract:");
		tv_contract.setText(jContract.getString("contract")+"\n");
		tl_data.addView(tv_lbl_contract);
		tl_data.addView(tv_contract);
		//tl_data.addView(tr_contract);
		
		// peer row
		TableRow tr_peer = new TableRow(this);
		tr_peer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_peer = new TextView(this);
		TextView tv_peer = new TextView(this);
		tv_lbl_peer.setText("Peer Approval:");
		tv_peer.setText(jContract.getString("peer approval")+"\n");
		tl_data.addView(tv_lbl_peer);
		tl_data.addView(tv_peer);
		//tl_data.addView(tr_peer);
		
		// shares issued row
		TableRow tr_sIssued = new TableRow(this);
		tr_sIssued.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_sIssued = new TextView(this);
		TextView tv_sIssued = new TextView(this);
		tv_lbl_sIssued.setText("Shares Issued:");
		tv_sIssued.setText(jContract.getString("shares issued")+"\n");
		tl_data.addView(tv_lbl_sIssued);
		tl_data.addView(tv_sIssued);
		//tl_data.addView(tr_sIssued);
		
		// Shares Outstanding row
		TableRow tr_sOutstanding = new TableRow(this);
		tr_sOutstanding.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_sOutstanding = new TextView(this);
		TextView tv_sOutstanding = new TextView(this);
		tv_lbl_sOutstanding.setText("Shares Outstanding:");
		tv_sOutstanding.setText(jContract.getString("shares outstanding")+"\n");
		tl_data.addView(tv_lbl_sOutstanding);
		tl_data.addView(tv_sOutstanding);
		//tl_data.addView(tr_sOutstanding);
		
		// issuer row
		TableRow tr_issuer = new TableRow(this);
		tr_issuer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_issuer = new TextView(this);
		TextView tv_issuer = new TextView(this);
		tv_lbl_issuer.setText("Issuer:");
		tv_issuer.setText(jContract.getString("issuer")+"\n");
		tl_data.addView(tv_lbl_issuer);
		tl_data.addView(tv_issuer);
		//tl_data.addView(tr_issuer);
		
		// issuerDetail row
		TableRow tr_issuerDetail = new TableRow(this);
		tr_issuerDetail.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_issuerDetail = new TextView(this);
		TextView tv_issuerDetail = new TextView(this);
		tv_lbl_issuerDetail.setText("Issuer Detail:");
		tv_issuerDetail.setText(jContract.getString("issuer detail")+"\n");
		tl_data.addView(tv_lbl_issuerDetail);
		tl_data.addView(tv_issuerDetail);
		//tl_data.addView(tr_issuerDetail);
		
		
		
		// executive summary row
		TableRow tr_executive = new TableRow(this);
		tr_executive.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_executive = new TextView(this);
		TextView tv_executive = new TextView(this);
		tv_lbl_executive.setText("Executive Summary:");
		tv_executive.setText(jContract.getString("executive summary")+"\n");
		tl_data.addView(tv_lbl_executive);
		tl_data.addView(tv_executive);
		//tl_data.addView(tr_executive);
		
		// description row
		TableRow tr_description = new TableRow(this);
		tr_description.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_description = new TextView(this);
		TextView tv_description = new TextView(this);
		tv_lbl_description.setText("Business Description:");
		tv_description.setText(jContract.getString("business description")+"\n");
		tl_data.addView(tv_lbl_description);
		tl_data.addView(tv_description);
		//tl_data.addView(tr_description);
		
		// market row
		TableRow tr_market = new TableRow(this);
		tr_market.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_market = new TextView(this);
		TextView tv_market = new TextView(this);
		tv_lbl_market.setText("Definition of the Market:");
		tv_market.setText(jContract.getString("definition of market")+"\n");
		tl_data.addView(tv_lbl_market);
		tl_data.addView(tv_market);
		//tl_data.addView(tr_market);
		
		// services row
		TableRow tr_services = new TableRow(this);
		tr_services.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_services = new TextView(this);
		TextView tv_services = new TextView(this);
		tv_lbl_services.setText("Products and Services:");
		tv_services.setText(jContract.getString("products and services")+"\n");
		tl_data.addView(tv_lbl_services);
		tl_data.addView(tv_services);
		//tl_data.addView(tr_services);
		
		// management row
		TableRow tr_management = new TableRow(this);
		tr_management.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_management = new TextView(this);
		TextView tv_management = new TextView(this);
		tv_lbl_management.setText("Organization and Management:");
		tv_management.setText(jContract.getString("organization and management")+"\n");
		tl_data.addView(tv_lbl_management);
		tl_data.addView(tv_management);
		//tl_data.addView(tr_management);
		
		// strategy row
		TableRow tr_strategy = new TableRow(this);
		tr_strategy.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_strategy = new TextView(this);
		TextView tv_strategy = new TextView(this);
		tv_lbl_strategy.setText("Marketing Strategy:");
		tv_strategy.setText(jContract.getString("marketing strategy")+"\n");
		tl_data.addView(tv_lbl_strategy);
		tl_data.addView(tv_strategy);
		//tl_data.addView(tr_strategy);
		
		// financial row
		TableRow tr_financial = new TableRow(this);
		tr_financial.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_lbl_financial = new TextView(this);
		TextView tv_financial = new TextView(this);
		tv_lbl_financial.setText("Financial Management:");
		tv_financial.setText(jContract.getString("financial management")+"\n");
		tl_data.addView(tv_lbl_financial);
		tl_data.addView(tv_financial);
		//tl_data.addView(tr_financial);
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		tl_data.invalidate();
		
	}
	
	public void Query(String type, String ticker)
	{
		if (type.equals("Trade History"))
		{
			new DownloadTicker().execute(URL_API_TICKER+ticker);
			new DownloadHistory().execute(URL_API_HISTORY+ticker);
		}
		else if(type.equals("Orders"))
		{
			
		}
		else if(type.equals("Dividends"))
		{
			
		}
		else if(type.equals("Contract & Prospectus"))
		{
			new DownloadTicker().execute(URL_API_TICKER+ticker);
			new DownloadSecurity().execute(ticker);
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
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
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
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
			
		 	return parseTickersJSON(sb.toString());
		}

		@Override
		protected void onPostExecute(JSONObject jTicker) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			
			if(jTicker!=null) { 
				fillTicker(jTicker); 
				tv.setText("Success");
			}
			else { tv.setText("Failed"); }
			tv.invalidate();
			
			super.onPostExecute(jTicker);
			
		}
		
	}
	
	public class DownloadSecurity extends AsyncTask<String, Integer, JSONObject> 
	{
		

		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading Security...");
			tv.invalidate();
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download trades");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_SECURITY+tickers[0]);
			
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

			return parseSecurityHTML(sb.toString());
			
		}

		@Override
		protected void onPostExecute(JSONObject jContract) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			
			if(jContract!=null) { 
				fillContract(jContract);	 
				tv.setText("Success");
				
				
			}
			else { tv.setText("HTMLParse failed"); }
			tv.invalidate();
					
			super.onPostExecute(jContract);
			
		}
		
	}
	


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  
        //Prevent keyboard from popping up automatically
        getWindow().setSoftInputMode(
        	      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initChart();
        
      //Selector
    	Spinner spn_nav = (Spinner) findViewById(R.id.main_spinner_selector);
    	ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.selector, android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spn_nav.setAdapter(adapter);
    	spn_nav.setSelection(0);
    	spn_nav.setOnItemSelectedListener(new SpinnerListener());
        
        // Lookup Button
        Button btn_lookup = (Button) findViewById(R.id.main_btn_lookup);
     	btn_lookup.setOnClickListener( new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText et_ticker = (EditText) findViewById(R.id.main_editText_ticker);
     				Spinner spn = (Spinner) findViewById(R.id.main_spinner_selector);
     				
					if(!et_ticker.getText().toString().equals("")) // make sure there's something
     				{
     					Query((String)spn.getSelectedItem(),et_ticker.getText().toString());
     					
     				}
					
				}
     		});
     	
     	//Browse Button
     	Button btn_browse = (Button) findViewById(R.id.main_btn_browse);
     	btn_browse.setOnClickListener( new Button.OnClickListener() {
				@Override
				public void onClick(View v) {
     					Intent intent = new Intent(getApplicationContext(), BrowseActivity.class);
     					startActivityForResult(intent,1);
     					
				}
     		});
     
		
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	if (requestCode == 1) {

    	     if(resultCode == RESULT_OK){

    	      String result=data.getStringExtra("result");
    	     EditText et = (EditText) findViewById(R.id.main_editText_ticker);
    	     et.setText(result);
    	     
    	     Spinner spn= (Spinner) findViewById(R.id.main_spinner_selector);
    	     Query((String)spn.getSelectedItem(),result);
    	      

    	}

    	if (resultCode == RESULT_CANCELED) {

    	     TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
    	     tv.setText("Unable to download all ticker data");
    	     tv.invalidate();
    	}
    	}//onAcrivityResult

    }
 }



