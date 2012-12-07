package com.raad287.ltcglobal;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.*;
import android.net.Uri;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
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
import android.util.Log;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
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
	public static final String URL_API_ORDERS = "https://www.litecoinglobal.com/api/orders/";
	
	final Context context = this;
	
	
	public class SpinnerListener implements OnItemSelectedListener {

		int curScreen;
		

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Spinner spn = (Spinner)findViewById(R.id.main_spinner_selector);
			EditText et = (EditText)findViewById(R.id.main_editText_ticker);
			if(!et.getText().toString().equals("")) // make sure there's something
				{
					Query((String)spn.getSelectedItem(),et.getText().toString());
					
				}
			
		}

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
	
	public String stripHTML(String s)
	{
		Log.i("LG", "stripHTML");
		StringBuilder sb = new StringBuilder();
		String sStart = "<";
		String sEnd =">";
		sb.append(s);
		while(sb.indexOf(sStart)!=-1 && sb.indexOf(sEnd, sb.indexOf(sStart))!=-1)
		{
			sb.delete(sb.indexOf(sStart),sb.indexOf(sEnd, sb.indexOf(sStart))+1);
		}
		return sb.toString();
	}
	
	public String parseSecurityLookup(String sPage, String key, String start, String end)
	{
		// Example key = "Ticker</th>" tag = "<td>"
		// Find Ticker
		int id1=0;
		int id2=0;
		int id3=0;
		id1=sPage.indexOf(key);
		Log.i("LG","id1:"+Integer.toString(id1));
		id2=sPage.indexOf(start,id1); //start
		
		Log.i("LG","id2:"+Integer.toString(id2));
		id3=sPage.indexOf(end,id2); //end
		
		Log.i("LG","id3:"+Integer.toString(id3));
			
		char[] buffer=new char[id3-(id2+start.length())];
				
		sPage.getChars(id2+start.length(), id3, buffer, 0);
		String parse = String.copyValueOf(buffer);
		return parse;
	}
	
	public JSONObject parseNotificationsHTML(String sPage)
	{
		Log.i("LG", "parseNotificationHTML");
		JSONObject jNotifications = new JSONObject();
		int id_notification_start = sPage.indexOf("<div id=\"tab4\" class=\"tab_content\">");
		int id_notification_end = sPage.indexOf("<div id=\"tab5\" class=\"tab_content\">");
		String sNotifications= sPage.substring(id_notification_start, id_notification_end);
		sNotifications = stripHTML(sNotifications);
		
		StringBuffer sb = new StringBuffer();

		//Filter out \r and \t from 
		for (int j = 0; j < sNotifications.length(); j++) {
			if ((sNotifications.charAt(j) != '\r') && (sNotifications.charAt(j) != '\t')) {
			   
			  // Filter out &xx;	
				if(j<sNotifications.length()-3) {
				   if( (sNotifications.charAt(j)!='&') && (sNotifications.charAt(j+3)!=';') )
				   {
					   sb.append(sNotifications.charAt(j));
				   }
				   else {
					   // skip over
					   j=j+3;
				   } 
				}else 
				{ sb.append(sNotifications.charAt(j)); }
	        }}
		sNotifications = sb.toString();
		
		// Filter out leading newline's and whitespace
		int marker=0;
		for (int i=0; i<sNotifications.length(); i++)
		{
			if(sNotifications.charAt(i)!=(' ') && sNotifications.charAt(i)!=('\r') 
					&& sNotifications.charAt(i)!=('\r'))
			{
				marker=i;
				break;
			}
		}
		if(marker>0)   // delete the whitespace if found
		{
			StringBuilder sb2 = new StringBuilder(sNotifications);
			sb2.delete(0, marker);
			sNotifications=sb2.toString();
		}
		
		
		//Log.i("LG", "Parsed Notifications: "+sNotifications);
		try {
			jNotifications.put("string", sNotifications);
		}catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		return jNotifications;
	}
	
	public JSONObject parseDividendsHTML(String sPage)
	{
		Log.i("LG", "parseDividendsHTML");
		JSONObject jDividends = new JSONObject();
		
		int id_notification_start = sPage.indexOf("<th colspan=\"5\" scope=\"col\">Dividends</th>");
		int id_notification_end = sPage.indexOf("<th colspan=\"3\" scope=\"col\">Trade History (last 20)</th>");
		
		if (id_notification_start==-1 || id_notification_end==-1) { return null; }
		
		String sDividends= sPage.substring(id_notification_start, id_notification_end);
		sDividends = stripHTML(sDividends);
		
		StringBuffer sb = new StringBuffer();

		//Filter out \r and \t from 
		for (int j = 0; j < sDividends.length(); j++) {
			if ((sDividends.charAt(j) != '\r') && (sDividends.charAt(j) != '\t')) {
			   
			  // Filter out &xx;	
				if(j<sDividends.length()-2) {
				   if( (sDividends.charAt(j)!='&') && (sDividends.charAt(j+2)!=';') )
				   {
					   sb.append(sDividends.charAt(j));
				   }
				   else {
					   // skip over
					   j=j+3;
				   } 
				}else 
				{ sb.append(sDividends.charAt(j)); }
				
	        }}
		sDividends = sb.toString();
		
		// Filter out leading newline's and whitespace
		int marker=0;
		for (int i=0; i<sDividends.length(); i++)
		{
			if(sDividends.charAt(i)!=(' ') && sDividends.charAt(i)!=('\r') 
					&& sDividends.charAt(i)!=('\r'))
			{
				marker=i;
				break;
			}
		}

		StringBuilder sb2 = new StringBuilder(sDividends);
		if(sDividends.length()>11)
		{
			sb2.delete(0, 10);
			sDividends = sb2.toString();
		}

		//Log.i("LG", "Parsed Dividends: "+sDividends);
		try {
			jDividends.put("string", sDividends);
		}catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		return jDividends;
	}
	
	
	public JSONObject parseMotionsHTML(String sPage)
	{
		Log.i("LG", "parseMotionsHTML");
		JSONObject jMotions = new JSONObject();
		int id_notification_start = sPage.indexOf("<div id=\"tab5\" class=\"tab_content\">");
		int id_notification_end = sPage.indexOf("<!-- GeoTrust QuickSSL [tm] Smart  Icon tag. Do not edit. -->");
		String sMotions= sPage.substring(id_notification_start, id_notification_end);
		sMotions = stripHTML(sMotions);
		
		StringBuffer sb = new StringBuffer();

		//Filter out \r and \t from 
		for (int j = 0; j < sMotions.length(); j++) {
			if ((sMotions.charAt(j) != '\r') && (sMotions.charAt(j) != '\t')) {
			   
			  // Filter out &xx;	
				if(j<sMotions.length()-3) {
				   if( (sMotions.charAt(j)!='&') && (sMotions.charAt(j+3)!=';') )
				   {
					   sb.append(sMotions.charAt(j));
				   }
				   else {
					   // skip over
					   j=j+3;
				   } 
				}else 
				{ sb.append(sMotions.charAt(j)); }
	        }}
		sMotions = sb.toString();
		
		// Filter out leading newline's and whitespace
		int marker=0;
		for (int i=0; i<sMotions.length(); i++)
		{
			if(sMotions.charAt(i)!=(' ') && sMotions.charAt(i)!=('\r') 
					&& sMotions.charAt(i)!=('\r'))
			{
				marker=i;
				break;
			}
		}
		if(marker>0)   // delete the whitespace if found
		{
			StringBuilder sb2 = new StringBuilder(sMotions);
			sb2.delete(0, marker);
			sMotions=sb2.toString();
		}
		
		
		//Log.i("LG", "Parsed Notifications: "+sMotions);
		try {
			jMotions.put("string", sMotions);
		}catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		return jMotions;
	}
	
	public JSONObject parseSecurityHTML(String sPage)
	{
		// Must do error checking before this point, will crash if bad request
		
		JSONObject jContract=new JSONObject();
		
		// Parse Contract
		try{
			jContract.put("ticker", parseSecurityLookup(sPage, "Ticker</th>", "<td>", "</td>"));
			jContract.put("peer approval", parseSecurityLookup(sPage, "Approval</th>", "<td>", "</td>"));
			jContract.put("shares issued", parseSecurityLookup(sPage, "Issued</th>", "<td>", "</td>"));
			jContract.put("shares outstanding", parseSecurityLookup(sPage, "Outstanding</th>", "<td>", "</td>"));
			jContract.put("issuer", parseSecurityLookup(sPage, "Issuer</th>", "<td>", "</td>"));
			jContract.put("issuer detail", parseSecurityLookup(sPage, "Detail</th>", "<pre>", "</pre>"));
			jContract.put("contract", parseSecurityLookup(sPage, "Contract</th>", "<pre>", "</pre>"));
			jContract.put("executive summary", parseSecurityLookup(sPage, "Summary</th>", "<pre>", "</pre>"));
			jContract.put("business description", parseSecurityLookup(sPage, "Description</th>", "<pre>", "</pre>"));
			jContract.put("definition of market", parseSecurityLookup(sPage, "Market</th>", "<pre>", "</pre>"));
			jContract.put("products and services", parseSecurityLookup(sPage, "Services</th>", "<pre>", "</pre>"));
			jContract.put("organization and management", parseSecurityLookup(sPage, "and Management</th>", "<pre>", "</pre>"));
			jContract.put("marketing strategy", parseSecurityLookup(sPage, "Strategy</th>", "<pre>", "</pre>"));
			jContract.put("executive summary", parseSecurityLookup(sPage, "Summary</th>", "<pre>", "</pre>"));
			jContract.put("financial management", parseSecurityLookup(sPage, "Financial Management</th>", "<pre>", "</pre>"));
			
		}catch (JSONException e) { Log.i("LG", e.getMessage()); }

		//Filter
		for(int i=0; i<jContract.names().length(); i++)
		{
			
			String line="";
			try {
				line = jContract.getString(jContract.names().getString(i));
			} catch (JSONException e) { Log.i("LG", "JSONException:"+e.getMessage()); }
			
			StringBuffer sb = new StringBuffer();
			//Filter out \r and \t from  strings in jContract
			for (int j = 0; j < line.length(); j++) {
				if ((line.charAt(j) != '\r') && (line.charAt(j) != '\t')) {
				   
				  // Filter out &xx;	
					if(j<line.length()-3) {
					   if( (line.charAt(j)!='&') && (line.charAt(j+3)!=';') )
					   {
						   sb.append(line.charAt(j));
					   }
					   else {
						   // skip over
						   j=j+3;
					   } }
					else { sb.append(line.charAt(j)); }
		        }}
			line = sb.toString();

			try {
				jContract.put(jContract.names().getString(i), line);
			} catch (JSONException e) {
				Log.i("LG", "JSONException:"+e.getMessage()); }	 }
		
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
	
	// take unsorted json string, return json object
	public JSONObject parseOrdersJSON(String sOrders)
	{
		// parse orders from string
		JSONObject jOrders;
		try
		{
			jOrders = new JSONObject(sOrders);
			Log.i("LG", "Orders Parsed!");
			Log.i("LG", jOrders.toString());

		} catch (JSONException e) { 
			Log.i("LG", "JSONException:" + e.getMessage());
			return null;
		}
		
		
		Log.i("LG", "Get Ids");
     	JSONArray jIds = jOrders.names();
     	if(jIds==null)
     	{
     		return null;
     	}
     	
     	JSONObject jBids = new JSONObject();
     	JSONObject jAsks = new JSONObject();
     	
 		long[] sorted_ids= new long[jIds.length()];
 		for (int i=0; i<jIds.length(); i++)
 		{
 			try {
 				sorted_ids[i]=jIds.getLong(i);
 			} catch (JSONException e) { Log.i("LG", "JSONException:"+e.getMessage()); }
 		}
 		
 		// Sort
 		Log.i("LG", "Sort");
 		Arrays.sort(sorted_ids); 	// Ascending
 		
     	// Fill jBids and jAsks
     	Log.i("LG", "Filling jBids and jAsks");
     	try 
     	{
	     	for (int i=0; i<jIds.length(); i++)
	     	{
	     		// pull the orders by the sorted ids
	     		JSONObject jOrder = jOrders.getJSONObject(String.valueOf(sorted_ids[i]));
	     		
	     		if(jOrder.getString("buy_sell").equals("bid"))
	     		{
	     			if(jBids.names()!=null) { jBids.put(String.valueOf(jBids.names().length()), jOrder); }
	     			else { jBids.put("0", jOrder); }
	     		}
	     		else if(jOrder.getString("buy_sell").equals("ask"))
	     		{
	     			if(jAsks.names()!=null) { jAsks.put(String.valueOf(jAsks.names().length()), jOrder); }
	     			else { jAsks.put("0", jOrder); }
	     		}
	     		
	     	}
     	} catch (JSONException e) { Log.i("LG", e.getMessage()); }
     	
     	jOrders=null;
     	try {
     		jOrders=new JSONObject();
	     	jOrders.put("bids", jBids);
	     	jOrders.put("asks", jAsks);
     	} catch (JSONException e) { Log.i("LG", e.getMessage()); return null; }
     	
     	return jOrders;
		
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
        Iterator<XYSeries> series_iterator=mySimpleXYPlot.getSeriesSet().iterator();
      
        
   
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

        Paint p = series1Format.getLinePaint();
        p.setStrokeWidth(3);
        series1Format.setLinePaint(p);
        
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
        
        mySimpleXYPlot.addSeries(series,series1Format);
        try {
			mySimpleXYPlot.setTitle(jHistory.getString("ticker_name"));
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
        
     
        mySimpleXYPlot.redraw();
        //mySimpleXYPlot.invalidate();
	}
	
	public void fillOrdersChart(JSONObject jOrders, String ticker)
	{
		
		JSONObject jBids,jAsks;
		try 
		{
	     	jBids = jOrders.getJSONObject("bids");
	     	jAsks = jOrders.getJSONObject("asks"); 
		} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
		
		Number[] bid_amount,bid_quantity=null;
		Number[] ask_amount,ask_quantity=null;
		XYSeries bid_series=null;
		XYSeries ask_series=null;
     	
     	// Fill Bid Arrays
		if(jBids.names()!=null)
		{
			bid_amount = new Number[jBids.names().length()]; 
			bid_quantity = new Number[jBids.names().length()];
		
	     	try 
	     	{
		     	for(int i=0; i<jBids.names().length()-1; i++)
		     	{
		     		JSONObject jBid=jBids.getJSONObject(String.valueOf(i));
		     		bid_amount[i]=jBid.getDouble("amount");
		     		bid_quantity[i]=jBid.getDouble("quantity");
		     	}
	     	} catch (JSONException e) { Log.i("LG", e.getMessage()); }
	     	
	     	// Create Bid Series
	     	bid_series = new SimpleXYSeries(
	     			Arrays.asList(bid_amount),
	                Arrays.asList(bid_quantity),
	                "Bids");
		}
		
     	
     	
     	// Fill Ask Arrays
		if(jAsks.names()!=null)
		{
			ask_amount = new Number[jAsks.names().length()]; 
			ask_quantity = new Number[jAsks.names().length()];
	     	
	     	try
	     	{
		     	for(int i=0; i<jAsks.names().length()-1; i++)
		     	{
		     		JSONObject jAsk=jAsks.getJSONObject(String.valueOf(i));
		     		ask_amount[i]=jAsk.getDouble("amount");
		     		ask_quantity[i]=jAsk.getDouble("quantity");
		     	}
	     	} catch (JSONException e) { Log.i("LG", e.getMessage()); }
	     	
	     	ask_series = new SimpleXYSeries(
	     			Arrays.asList(ask_amount),
	                Arrays.asList(ask_quantity),
	                "Bids");
		}
		
		// initialize our XYPlot reference:
     	XYPlot mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
     	Iterator<XYSeries> series_iterator=mySimpleXYPlot.getSeriesSet().iterator();
     	
     	// clear out the previous series
     	while(series_iterator.hasNext())
     	{
     		mySimpleXYPlot.removeSeries((XYSeries)series_iterator.next());
     	}
     	
     	//customize domain and range labels
        mySimpleXYPlot.setDomainLabel("Amount");
        mySimpleXYPlot.setRangeLabel("Quantity");
        
  
        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("0"));
        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS, 10);
        
        // set the top boundary to the average of the bids and asks quantities
        double average=0;
        
       
        
        mySimpleXYPlot.setDomainValueFormat(new DecimalFormat("0.00"));
        mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, 2);	
        mySimpleXYPlot.setTicksPerRangeLabel(3); 	// reduce the number of range labels
        mySimpleXYPlot.disableAllMarkup();			// remove placement aids
         

        
        // 
        // ask Formatter
        LineAndPointFormatter askFormat = new LineAndPointFormatter(
                Color.rgb(200, 0, 0),                   // line color
                Color.rgb(100, 0, 0),                   // point color
                Color.rgb(100, 0, 0));                  // fill color); 
        Paint p = askFormat.getLinePaint();
        p.setStrokeWidth(3);
        askFormat.setLinePaint(p);
        
        // bid Formatter
        LineAndPointFormatter bidFormat = new LineAndPointFormatter(
                Color.rgb(0, 0, 200),                   // line color
                Color.rgb(0, 0, 100),                   // point color
                Color.rgb(0, 0, 100));                  // fill color);      
 
        p = bidFormat.getLinePaint();
        p.setStrokeWidth(3);
        bidFormat.setLinePaint(p);
        
        if(bid_series!=null) { mySimpleXYPlot.addSeries(bid_series,bidFormat); }		// add bid series
        if(ask_series!=null) { mySimpleXYPlot.addSeries(ask_series,askFormat); }
        
		mySimpleXYPlot.setTitle(ticker );

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
		//Log.i("LG", "Get Ids");
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
		//Log.i("LG", "Fill Layout");

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
			//Log.i("LG", "Fill Table");
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
	
			//Log.i("LG", "Add TextViews");
			tr_trade.addView(tv_timestamp);
			tr_trade.addView(tv_amount);
			tr_trade.addView(tv_quantity);
			tl_trade_history.addView(tr_trade);
		}
	}
	
	// fillTable: fill trade history table main UI
	public void fillOrdersTable(JSONObject jOrders)
	{
		JSONObject jBids,jAsks;
		try { 
			jBids = jOrders.getJSONObject("bids");
			jAsks = jOrders.getJSONObject("asks");
		} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }

			
		TableLayout tl_trade_history = (TableLayout) findViewById(R.id.main_tableLayout_data);
		tl_trade_history.removeAllViews();
			
		TextView tv_title = new TextView(this);
		tv_title.setText("Orderbook");
		tl_trade_history.addView(tv_title);
		
			
		TableRow tr_main_row=new TableRow(this);
		
		// Generate Bid Table Layout
		TableLayout tl_bids = new TableLayout(this);
		if (jBids.names()!=null)
		{
			// Add bid description row
			TableRow tr_desc = new TableRow(this);
			TextView tv_desc_amount=new TextView(this);
			tv_desc_amount.setText("Bid Amt");
			tr_desc.addView(tv_desc_amount);
			TextView tv_desc_quantity=new TextView(this);
			tv_desc_quantity.setText("Bid Qty");
			tr_desc.addView(tv_desc_quantity);
			tl_bids.addView(tr_desc);
			
			//reverse the order of the bids row
			long sorted_ids[] = new long[jBids.names().length()];
			for (int i=0; i<jBids.names().length(); i++)
			{
				try
				{
					sorted_ids[i]=jBids.names().getLong(i);
				} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
			}
			Arrays.sort(sorted_ids); 	// Ascending
			long[] asc_ids = new long [sorted_ids.length];
			for (int i=0; i<sorted_ids.length; i++) { asc_ids[i]=sorted_ids[i]; }
			for (int i=0; i<sorted_ids.length; i++) { sorted_ids[i]=asc_ids[(sorted_ids.length-1)-i]; }
				

			// iterate through bids
			for (int i=0; i<jBids.names().length(); i++)
			{
				//Log.i("LG", "Fill Table");
				// Trade Row
				TableRow tr_bid = new TableRow(this);
				tr_bid.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					
				TextView tv_amount = new TextView(this);
				TextView tv_quantity = new TextView(this);
				tv_amount.setPadding(0, 0, 10, 0);
				tv_quantity.setPadding(0, 0, 20, 0);
					
				// Pull order data from JSON
				
				try {
					JSONObject jBid = jBids.getJSONObject(String.valueOf(sorted_ids[i]));
					tv_amount.setText(jBid.getString("amount"));
					tv_quantity.setText(jBid.getString("quantity"));
				} catch (JSONException e) {
					Log.i("LG", "JSONException:"+e.getMessage());
				}
			
				tr_bid.addView(tv_amount);
				tr_bid.addView(tv_quantity);
				tl_bids.addView(tr_bid);
			}
		}
		// Add TableLayout tl_bids to main row
		tr_main_row.addView(tl_bids);
			
		// Generate Ask Table Layout
		TableLayout tl_asks = new TableLayout(this);
		if (jAsks.names()!=null)
		{
			// Add ask description row
			TableRow tr_desc = new TableRow(this);
			TextView tv_desc_amount=new TextView(this);
			tv_desc_amount.setText("Ask Amt");
			tr_desc.addView(tv_desc_amount);
			TextView tv_desc_quantity=new TextView(this);
			tv_desc_quantity.setText("Ask Qty");
			tr_desc.addView(tv_desc_quantity);
			tl_asks.addView(tr_desc);
			
			for (int i=0; i<jBids.names().length(); i++)
			{
				//Log.i("LG", "Fill Table");
				// Trade Row
				TableRow tr_ask = new TableRow(this);
				tr_ask.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
								
				TextView tv_amount = new TextView(this);
				TextView tv_quantity = new TextView(this);
				tv_amount.setPadding(0, 0, 10, 0);
				tv_quantity.setPadding(0, 0, 20, 0);
	
				// Pull order data from JSON
				try {
					JSONObject jAsk = jAsks.getJSONObject(String.valueOf(i));
					tv_amount.setText(jAsk.getString("amount"));
					tv_quantity.setText(jAsk.getString("quantity"));
				} catch (JSONException e) {
					Log.i("LG", "JSONException:"+e.getMessage());
				}
						
				tr_ask.addView(tv_amount);
				tr_ask.addView(tv_quantity);
				tl_asks.addView(tr_ask);
			}
		}

		tr_main_row.addView(tl_asks);				// Add TableLayout tl_bids to main row
		tl_trade_history.addView(tr_main_row);		// add main row to main layout
		
			
	}
	
	// Fill tl_data with notification query
	public void fillNotifications(JSONObject jNotifications, String sTitle)
	{
		//Log.i("LG", "JNotifications:"+jNotifications.toString());
		TableLayout tl_data = (TableLayout) findViewById(R.id.main_tableLayout_data);
		tl_data.removeAllViews();
		TextView tv_title = new TextView(this);
		tv_title.setText(sTitle);
		tl_data.addView(tv_title);
		TextView tv_notifications = new TextView(this);
		try {
			tv_notifications.setText(jNotifications.getString("string"));
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		tl_data.addView(tv_notifications);
		tl_data.invalidate();
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
		Log.i("LG", "Query: Type:"+type+" for "+ticker);
		if (type.equals("Trade History"))
		{
			new DownloadTicker().execute(ticker);
			new DownloadHistory().execute(ticker);
		}
		else if(type.equals("Motions"))
		{
			new DownloadTicker().execute(ticker);
			new DownloadMotions().execute(ticker);
		}
		else if(type.equals("Dividends"))
		{
			new DownloadTicker().execute(ticker);
			new DownloadDividends().execute(ticker);
		}
		else if(type.equals("Contract & Prospectus"))
		{
			new DownloadTicker().execute(ticker);
			new DownloadSecurity().execute(ticker);
		}
		else if(type.equals("Notifications"))
		{
			new DownloadTicker().execute(ticker);
			new DownloadNotifications().execute(ticker);
		}
		else if(type.equals("Orderbook"))
		{
			new DownloadTicker().execute(ticker);
			new DownloadOrders().execute(ticker);
		}
	}

	public class DownloadDividends extends AsyncTask<String, Integer, JSONObject> 
	{
		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading Dividends...");
			tv.invalidate();
			super.onPreExecute();
		}
	
		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadMotions:doInBackground: Executing download dividends");
			
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
	
			//return sb.toString();
			return parseDividendsHTML(sb.toString());
		}
	
		@Override
		protected void onPostExecute(JSONObject jPage) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			Log.i("LG", "Notification download postExecute");
			
			//JSONObject jMotions = parseMotionsHTML(sPage);
			if(jPage!=null) { 
				fillNotifications(jPage, "Dividends");
				tv.setText("Success");
			}
			else { 
				tv.setText("No Dividends, or failed to parse"); 
				TableLayout tl_data = (TableLayout)findViewById(R.id.main_tableLayout_data);
				tl_data.removeAllViews();
				tl_data.invalidate();
			
			}
			tv.invalidate();
					
			super.onPostExecute(jPage);
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
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download history");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_HISTORY+tickers[0]);
			
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
			
			// parse history into json object
			JSONObject jHistory = parseHistoryRE(sb.toString());
			try {
				jHistory.put("ticker_name", tickers[0]);
			} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		 	return jHistory;
		}

		@Override
		protected void onPostExecute(JSONObject jHistory) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			if(jHistory !=null)
			{
				fillChart(jHistory,true);
				jHistory.remove("ticker_name");
				fillTable(jHistory,true);
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
	
	public class DownloadOrders extends AsyncTask<String, Integer, JSONObject> 
	{
		

		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading Orders...");
			tv.invalidate();
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download orders");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_ORDERS+tickers[0]);
			
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
					
			//return sb.toString();
			// parse orders into json object
			JSONObject jOrders = parseOrdersJSON(sb.toString());
			try {
				jOrders.put("ticker_name", tickers[0]);
			} catch (JSONException e) { Log.i("LG", e.getMessage()); }
			
			return jOrders;
		}

		@Override
		protected void onPostExecute(JSONObject jOrders) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			//JSONObject jOrders = parseOrdersJSON(sOrders);
			
			if(jOrders !=null)
			{
				String ticker="";
				try {
					ticker = jOrders.getString("ticker_name");
				} catch (JSONException e) { Log.i("LG", e.getMessage()); }
				
				jOrders.remove("ticker_name");
				fillOrdersChart(jOrders, ticker);
				//jOrders.remove("ticker_name");
				fillOrdersTable(jOrders);
				//tv.setText("Success");
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
			
			super.onPostExecute(jOrders);
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
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download ticker");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_TICKER+tickers[0]);
			
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
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download security");
			
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
	
	public class DownloadNotifications extends AsyncTask<String, Integer, JSONObject> 
	{
		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading Notifications...");
			tv.invalidate();
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download notifications");
			
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

			//return sb.toString();
			return parseNotificationsHTML(sb.toString());
		}

		@Override
		protected void onPostExecute(JSONObject jPage) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			Log.i("LG", "Notification download postExecute");
			
			//JSONObject jNotifications = parseNotificationsHTML(sPage);
			if(jPage!=null) { 
				fillNotifications(jPage, "Notifications");	 
				tv.setText("Success");
			}
			else { tv.setText("HTMLParse failed"); }
			tv.invalidate();
					
			super.onPostExecute(jPage);
		}
	}
	
	public class DownloadMotions extends AsyncTask<String, Integer, JSONObject> 
	{
		@Override
		protected void onPreExecute() {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			tv.setText("Downloading Motions...");
			tv.invalidate();
			super.onPreExecute();
		}

		@Override
		// doInBackground: take string array containing ticker names and download
		protected JSONObject doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadMotions:doInBackground: Executing download motions");
			
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

			//return sb.toString();
			return parseMotionsHTML(sb.toString());
		}

		@Override
		protected void onPostExecute(JSONObject jPage) {
			TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
			Log.i("LG", "Notification download postExecute");
			
			//JSONObject jMotions = parseMotionsHTML(sPage);
			if(jPage!=null) { 
				fillNotifications(jPage, "Motions");	 
				tv.setText("Success");
			}
			else { tv.setText("HTMLParse failed"); }
			tv.invalidate();
					
			super.onPostExecute(jPage);
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
				public void onClick(View v) {
					EditText et_ticker = (EditText) findViewById(R.id.main_editText_ticker);
     				Spinner spn = (Spinner) findViewById(R.id.main_spinner_selector);
     				
					if(!et_ticker.getText().toString().equals("")) // make sure there's something
     				{
     					Query((String)spn.getSelectedItem(),et_ticker.getText().toString());
     					
     				}
					
				}
     		});
     	
     	// Browse Button
     	// Launch the browse activity
     	Button btn_browse = (Button) findViewById(R.id.main_btn_browse);
     	btn_browse.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
     					Intent intent = new Intent(getApplicationContext(), BrowseActivity.class);
     					startActivityForResult(intent,1);
     					
				}
     	});
     	
     	// Settings Button
     	// Launch the settings activity
     	Button btn_settings = (Button) findViewById(R.id.main_btn_settings);
     	btn_settings.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
     					//Intent intent = new Intent(getApplicationContext(), BrowseActivity.class);
     					//startActivityForResult(intent,1);
     					
				}
     	});
     	
     	// External Button
     	// Launch the browser toward the securities page
     	Button btn_external = (Button) findViewById(R.id.main_btn_external);
     	btn_external.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					EditText et_ticker = (EditText) findViewById(R.id.main_editText_ticker);
     				
					if(!et_ticker.getText().toString().equals("")) // make sure there's something
     				{
						Uri uriUrl = Uri.parse(URL_API_SECURITY+et_ticker.getText().toString());
					    Intent iBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
					    startActivity(iBrowser);
     				}
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



