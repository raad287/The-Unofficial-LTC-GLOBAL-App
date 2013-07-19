package com.raad287.ltcglobal;
import android.app.Activity;

import com.raad287.ltcglobal.Constants;


import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;


import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.series.XYSeries;
import com.androidplot.ui.widget.TitleWidget;
import com.androidplot.xy.*;

import java.text.*;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;

import com.raad287.ltcglobal.LTGComm;
import com.google.ads.*;



public class MainActivity extends Activity {
	
	private AdView adView;
	Constants constants = new Constants();
	
	final Context context = this;
	
	public class LTGHandler extends Handler
	{

		@Override
		public void handleMessage(Message msg) {
			if(msg==null) { return; }
			
			if(msg.arg1== constants.MSG_QUERY_FAILURE)
			{
				TextView tv= (TextView) findViewById(R.id.main_tv_downloading);
				tv.setText("Query Failed.");
			}
			
			// Query has been completed
			if (msg.arg1 == constants.MSG_QUERY_RETURN )
			{
				TextView tv = (TextView) findViewById(R.id.main_tv_downloading);
				
				Bundle returnBundle = msg.getData();
				
				if(returnBundle.getString("query").equals("dividends"))
				{
					JSONObject jDividends = new JSONObject();
					
					try{
						jDividends = new JSONObject (returnBundle.getString("data"));
					} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
					
					fillDividends(jDividends);
						
				}
				
				else if(returnBundle.getString("query").equals("history"))
				{
					JSONObject jHistory = new JSONObject();
					try{
						jHistory = new JSONObject (returnBundle.getString("data"));
					} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
					
					fillHistoryChart(jHistory,true);
					//jHistory.remove("ticker_name");
					fillTable(jHistory,true);
					tv.setText("Success");
				}
				
				else if(returnBundle.getString("query").equals("orders"))
				{
					JSONObject jOrders = new JSONObject();
					try{
						jOrders = new JSONObject (returnBundle.getString("data"));
					} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
					
					fillOrdersChart(jOrders, returnBundle.getString("ticker"));
					fillOrdersTable(jOrders);
					tv.setText("Success");
				}
				
				else if(returnBundle.getString("query").equals("ticker"))
				{
					JSONObject jTicker = new JSONObject();
					try{
						jTicker = new JSONObject (returnBundle.getString("data"));
					} catch(JSONException e) { Log.i("LG", e.getMessage()); return; }
					
					fillTicker(jTicker); 
					tv.setText("Success");
				}
				
				else if(returnBundle.getString("query").equals("contract"))
				{
					JSONObject jContract = new JSONObject();
					try{
						jContract = new JSONObject (returnBundle.getString("data"));
					} catch(JSONException e) { Log.i("LG", e.getMessage()); return; }
					
					fillContract(jContract);	 
					tv.setText("Success");
				}
				
				else if(returnBundle.getString("query").equals("notifications"))
				{
					JSONObject jNotifications = new JSONObject();
					try{
						jNotifications = new JSONObject (returnBundle.getString("data"));
					} catch(JSONException e) { Log.i("LG", e.getMessage()); return; }
					
					fillNotifications(jNotifications, "Notifications");	 
					tv.setText("Success");
					
					
					tv.invalidate();
				}
				
				else if(returnBundle.getString("query").equals("motions"))
				{
					JSONObject jMotions = new JSONObject();
					
					try{
						jMotions = new JSONObject (returnBundle.getString("data"));
					} catch(JSONException e) { Log.i("LG", e.getMessage()); return; }

					fillNotifications(jMotions, "Motions");	 
					tv.setText("Success");
				}
			}
			super.handleMessage(msg);
		}
	}
	
	LTGHandler handler = new LTGHandler();
	
	public class SpinnerListener implements OnItemSelectedListener {
		int curScreen;
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Spinner spn = (Spinner)findViewById(R.id.main_spinner_selector);
			EditText et = (EditText)findViewById(R.id.main_editText_ticker);
			
			Log.i("LG", "MainActivity:SpinnerListener:spn="+(String)spn.getSelectedItem());
			
			if(!et.getText().toString().equals("")) // make sure there's something
			{
				Query((String)spn.getSelectedItem(),et.getText().toString());
			}
		}
		public void onNothingSelected(AdapterView<?> arg0) {
			//
		}
	}
	
	
	
	// parses the security page to fill contract & prospectus, notifications and motions
	// returns JSON
	
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
		
	public void fillHistoryChart(JSONObject jHistory, boolean desc)
	{
		// extract ticker name
		String ticker_name = "Market History";
		try
		{
			ticker_name=jHistory.getString("ticker_name");
			jHistory.remove("ticker_name");
		} catch (JSONException e) { Log.i("LG", "MainActivity:fillHistoryChart:JSONException5:"+e.getMessage()); }
		
		 // initialize our XYPlot reference:
		
		XYPlot mySimpleXYPlot= (XYPlot) findViewById(R.id.mySimpleXYPlot);
		mySimpleXYPlot.clear();
		
		//customize domain and range labels
        mySimpleXYPlot.setDomainLabel("Day of Month");
        if(Constants.SITE.equals("LTC"))
        {
        	mySimpleXYPlot.setRangeLabel("Price/LTC");
        }
        else if (Constants.SITE.equals("BTC"))
        {
        	mySimpleXYPlot.setRangeLabel("Price/BTC");
        }
        
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
        
        // Widen the title widget to avoid clipping
        TitleWidget tw_title = mySimpleXYPlot.getTitleWidget();
        tw_title.setWidth(200);
		
        mySimpleXYPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        Iterator<XYSeries> series_iterator=mySimpleXYPlot.getSeriesSet().iterator();
        mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, 5);	
        
        
        // clear out the previous series
        while(series_iterator.hasNext())
        {
        	mySimpleXYPlot.removeSeries((XYSeries)series_iterator.next());
        }
        
     	
     		JSONArray jIds = jHistory.names();
     		if(jIds==null)
     		{
     			return;
     		}
     		int num_trades=jIds.length();
     
     		long[] sorted_ids= new long[num_trades];
     		
     		
     		for (int i=0; i<num_trades-1; i++)
     		{
     			try {
     				sorted_ids[i]=jIds.getLong(i);
     				//Log.i("LG", "sorted_ids[i]="+String.valueOf(sorted_ids[i])+" jIds.getLong(i)="+String.valueOf(jIds.getString(i)));
     			} catch (JSONException e) { Log.i("LG", "MainActivity:fillHistoryChart:JSONException1 at "+String.valueOf(i)+":"+e.getMessage()); }
     		}
     		
     		// Sort
     		Log.i("LG", "MainActivity:fillHistoryChart:Sort");
     		Arrays.sort(sorted_ids); 	// Ascending
     		if (desc==true) { 
     			Collections.reverse(Arrays.asList(sorted_ids)); // reverse order of array
     		}
     		
     		Number[] timestamps = new Number[num_trades];
     		Number[] amount = new Number[num_trades];
     		Number[] quantity = new Number[num_trades];
     		
     		
     		for (int i=0; i<sorted_ids.length-1; i++)
    		{
     			try {
     				//Log.i("LG", "sorted_id="+sorted_ids[i]);
					JSONObject jTrade = jHistory.getJSONObject(String.valueOf(sorted_ids[i]));
					timestamps[i] = sorted_ids[i];
					amount[i] = jTrade.getDouble("amount");
					quantity[i] = jTrade.getLong("quantity");
					
				} catch (JSONException e) {
					Log.i("LG", "MainActivity:fillHistoryChart:JSONException2 "+String.valueOf(i)+":"+e.getMessage());
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
        
        mySimpleXYPlot.setDomainBoundaries(mySimpleXYPlot.getCalculatedMinX(), mySimpleXYPlot.getCalculatedMaxX(), BoundaryMode.AUTO);
        mySimpleXYPlot.setRangeBoundaries(mySimpleXYPlot.getCalculatedMinY(), mySimpleXYPlot.getCalculatedMaxY(), BoundaryMode.AUTO);
        

		mySimpleXYPlot.setTitle(ticker_name);
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
		
		Number[] bid_amount = null,bid_quantity=null;
		Number[] ask_amount = null,ask_quantity=null;
		XYSeries bid_series=null;
		XYSeries ask_series=null;
     	
     	// Fill Bid Arrays
		if(jBids.names()!=null)
		{
			bid_amount = new Number[jBids.names().length()]; 
			bid_quantity = new Number[jBids.names().length()];
		
	     	try 
	     	{
		     	for(int i=0; i<jBids.names().length(); i++)
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
		     	for(int i=0; i<jAsks.names().length(); i++)
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
     	mySimpleXYPlot.clear();
     	Iterator<XYSeries> series_iterator=mySimpleXYPlot.getSeriesSet().iterator();
     	
     	// clear out the previous series
     	while(series_iterator.hasNext())
     	{
     		mySimpleXYPlot.removeSeries((XYSeries)series_iterator.next());
     	}
     	
     	//customize domain and range labels
        mySimpleXYPlot.setDomainLabel("Amount");
        mySimpleXYPlot.setRangeLabel("Quantity");
        
     // Widen the title widget to avoid clipping
        TitleWidget tw_title = mySimpleXYPlot.getTitleWidget();
        tw_title.setWidth(200);
        
  
        
        mySimpleXYPlot.setRangeValueFormat(new DecimalFormat("0"));
        mySimpleXYPlot.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS, 10);
        
      
        mySimpleXYPlot.setDomainValueFormat(new DecimalFormat("0.00"));
        mySimpleXYPlot.setDomainStep(XYStepMode.SUBDIVIDE, 5);	
        
        mySimpleXYPlot.setTicksPerRangeLabel(3); 	// reduce the number of range labels
        mySimpleXYPlot.disableAllMarkup();			// remove placement aids
         
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
		mySimpleXYPlot.setTitle(ticker );
		// Add the new Series
     	if(bid_series!=null) { mySimpleXYPlot.addSeries(bid_series,bidFormat); }		// add bid series
        if(ask_series!=null) { mySimpleXYPlot.addSeries(ask_series,askFormat); }

        SharedPreferences sp = getSharedPreferences(constants.SHARED_PREF_KEY, 0);

        double Dmin, Dmax, Rmin, Rmax;
        //Calc Domain Minimum
        double low_x=0;
        if (bid_amount!=null)
        {
	        for (int i=0; i<bid_amount.length; i++)
	        {
	        	if (bid_amount[i].doubleValue() < low_x) { low_x=bid_amount[i].doubleValue(); }
	        }
        }
        if (ask_amount!=null)
        {
	        for (int i=0; i<ask_amount.length; i++)
	        {
	        	if (ask_amount[i].doubleValue() < low_x) { low_x=ask_amount[i].doubleValue(); }
	        }
        }
        Dmin=low_x;
        
        //Calc Domain Maximum
        double high_x=1;
        if (bid_amount!=null)
        {
	        for (int i=0; i<bid_amount.length; i++)
	        {
	        	if (bid_amount[i].doubleValue() > high_x) { high_x=bid_amount[i].doubleValue(); }
	        }
        }
        if (ask_amount!=null)
        {
	        for (int i=0; i<ask_amount.length; i++)
	        {
	        	if (ask_amount[i].doubleValue() > high_x) { high_x=ask_amount[i].doubleValue(); }
	        }
        }
        Dmax=high_x;
        
        //Calc Range Minimum
        double low_y=0;
        if (bid_quantity!=null)
        {
	        for (int i=0; i<bid_quantity.length; i++)
	        {
	        	if (bid_quantity[i].doubleValue() < low_y) { low_y=bid_quantity[i].doubleValue(); }
	        }
        }
        if (ask_quantity!=null)
        {
	        for (int i=0; i<ask_quantity.length; i++)
	        {
	        	if (ask_quantity[i].doubleValue() < low_y) { low_y=ask_quantity[i].doubleValue(); }
	        }
        }
        Rmin=low_y;
        
        //Calc Range Maximum
       
        double high_y=1;
        if (bid_quantity!=null)
        {
	        for (int i=0; i<bid_quantity.length; i++)
	        {
	        	if (bid_quantity[i].doubleValue() > high_y) { high_y=bid_quantity[i].doubleValue(); }
	        }
        }
        if (ask_quantity!=null)
        {
	        for (int i=0; i<ask_quantity.length; i++)
	        {
	        	if (ask_quantity[i].doubleValue() > high_y) { high_y=ask_quantity[i].doubleValue(); }
	        }
        }
        Rmax=high_y;
      
        
        // set the domain and range
        if(!sp.getBoolean(constants.PREF_MIN_DOMAIN_AUTO, constants.DEFAULT_MIN_DOMAIN_AUTO))	// Auto Off
        {
        	Dmin= sp.getFloat(constants.PREF_MIN_DOMAIN, constants.DEFAULT_MIN_DOMAIN);
        }
        
        if(!sp.getBoolean(constants.PREF_MAX_DOMAIN_AUTO, constants.DEFAULT_MAX_DOMAIN_AUTO)) // Auto Off
        {
        	Dmax=sp.getFloat(constants.PREF_MAX_DOMAIN, constants.DEFAULT_MAX_DOMAIN);
        }
        
        if(!sp.getBoolean(constants.PREF_MIN_RANGE_AUTO, constants.DEFAULT_MIN_RANGE_AUTO))	// Auto Off
        {
        	Rmin = sp.getFloat(constants.PREF_MIN_RANGE, constants.DEFAULT_MIN_RANGE);
        }
        
        if(!sp.getBoolean(constants.PREF_MAX_RANGE_AUTO, constants.DEFAULT_MAX_RANGE_AUTO))	// Auto Off
        {
        	Rmax = sp.getFloat(constants.PREF_MAX_RANGE, constants.DEFAULT_MAX_RANGE);
        }
    
        if(Dmin < 0 ) { Dmin = 0; }
        if(Rmin < 0 ) { Rmin = 0; }
        if (Dmax < Dmin ) { Dmax = 1 + Dmin + (Dmin/2); }
        if (Rmax < Rmin ) { Rmax = 1 + Rmin + (Rmin/2); }
        
       /* 
        mySimpleXYPlot.setDomainLeftMin(Dmin);
        mySimpleXYPlot.setDomainRightMax(Dmax);
        mySimpleXYPlot.setRangeBottomMin(Rmin);
        mySimpleXYPlot.setRangeTopMax(Rmax);
		*/
        mySimpleXYPlot.setDomainBoundaries(Dmin, Dmax, BoundaryMode.FIXED);
        mySimpleXYPlot.setRangeBoundaries(Rmin, Rmax, BoundaryMode.FIXED);
		
        mySimpleXYPlot.redraw();
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

			
		// erase perious table
		TableLayout tl_main = (TableLayout) findViewById(R.id.main_tableLayout_data);
		tl_main.removeAllViews();
			
		TextView tv_title = new TextView(this);
		tv_title.setText("Orderbook");
		tl_main.addView(tv_title);
		
			
		TableRow tr_main_row=new TableRow(this);
		
		// Generate Bid Table Layout
		TableLayout tl_bids = new TableLayout(this);
		if (jBids.names()!=null)
		{
			// Add bid description row
			TableRow tr_desc = new TableRow(this);
			
			TextView tv_desc_amount=new TextView(this);
			tv_desc_amount.setText("Bid Amt");
			tv_desc_amount.setPadding(0, 0, 10, 0);
			tr_desc.addView(tv_desc_amount);
			
			TextView tv_desc_quantity=new TextView(this);
			tv_desc_quantity.setText("Bid Qty");
			tv_desc_quantity.setPadding(0, 0, 20, 0);
			tr_desc.addView(tv_desc_quantity);
			
			tl_bids.addView(tr_desc);
			/*
			//reverse the order of the bids row
			int sorted_ids[] = new int[jBids.names().length()];
			for (int i=0; i<jBids.names().length(); i++)
			{
				try
				{
					sorted_ids[i]=jBids.names().getInt(i);
				} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
			}
			Arrays.sort(sorted_ids); 	// Ascending
			int[] asc_ids = new int [sorted_ids.length];
			for (int i=0; i<sorted_ids.length; i++) { asc_ids[i]=sorted_ids[i]; }
			for (int i=0; i<sorted_ids.length; i++) { sorted_ids[i]=asc_ids[(sorted_ids.length-1)-i]; }
				
			*/
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
					JSONObject jBid = jBids.getJSONObject(String.valueOf(jBids.names().length()-1-i));
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
			/*
			//reverse the order of the bids row
			int sorted_ids[] = new int[jAsks.names().length()];
			for (int i=0; i<jAsks.names().length(); i++)
			{
				try
				{
					sorted_ids[i]=jAsks.names().getInt(i);
				} catch (JSONException e) { Log.i("LG", e.getMessage()); return; }
			}
			Arrays.sort(sorted_ids); 	// Ascending
			int[] asc_ids = new int [sorted_ids.length];
			for (int i=0; i<sorted_ids.length; i++) { asc_ids[i]=sorted_ids[i]; }
			for (int i=0; i<sorted_ids.length; i++) { sorted_ids[i]=asc_ids[(sorted_ids.length-1)-i]; }
			*/
			for (int i=0; i<jAsks.names().length(); i++)
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
		tl_main.addView(tr_main_row);		// add main row to main layout
		
			
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
		tv_ticker.setText("Contract & Prospectus: "+jContract.getString("Ticker")+"\n");
		tr_ticker.addView(tv_ticker);
		tl_data.addView(tr_ticker);
		
		// Type row
		TableRow tr_type = new TableRow(this);
		tr_type.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_type = new TextView(this);
		tv_ticker.setText("Type: "+jContract.getString("Type")+"\n");
		tr_ticker.addView(tv_type);
		tl_data.addView(tr_type);
		
		// Approved Row
		TableRow tr_approved = new TableRow(this);
		tr_approved.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_approved_lbl = new TextView(this);
		TextView tv_approved = new TextView(this);
		
		tv_approved_lbl.setText("Approved:");
		Log.i("LG", jContract.getString("Approved"));
		if (jContract.getInt("Approved")==1) {
			tv_approved.setText("YES");
			tv_approved.setTextColor(Color.GREEN);
		}
		else {
			tv_approved.setText("NO");
			tv_approved.setTextColor(Color.RED);
		}
		
		tr_approved.addView(tv_approved_lbl);
		tr_approved.addView(tv_approved);
		tl_data.addView(tr_approved);
		
		// Admin Lock Row
		TableRow tr_adminlock = new TableRow(this);
		tr_adminlock.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_adminlock_lbl = new TextView(this);
		TextView tv_adminlock=new TextView(this);
		
		tv_adminlock_lbl.setText("Admin Lock:");
		if (jContract.getInt("Admin Lock")==1) {
			tv_adminlock.setText("YES");
			tv_adminlock.setTextColor(Color.RED);
		}
		else {
			tv_adminlock.setText("NO");
			tv_adminlock.setTextColor(Color.GREEN);
		}
			
		tr_adminlock.addView(tv_adminlock_lbl);
		tr_adminlock.addView(tv_adminlock);
		tl_data.addView(tr_adminlock);
		
		// publictradelock Row
		TableRow tr_publictradelock = new TableRow(this);
		tr_publictradelock.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_publictradelock_lbl = new TextView(this);
		TextView tv_publictradelock = new TextView(this);
		
		tv_publictradelock_lbl.setText("Public Trade Lock:");
		if (jContract.getInt("Public Trade Lock")==1) {
			tv_publictradelock.setText("YES");
			tv_publictradelock.setTextColor(Color.RED);
		}
		else {
			tv_publictradelock.setText("NO");
			tv_publictradelock.setTextColor(Color.GREEN);
		}
		
		// Issuer Lock Row
		TableRow tr_issuerlock = new TableRow(this);
		tr_issuerlock.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_issuerlock_lbl = new TextView(this);
		TextView tv_issuerlock = new TextView(this);
				
		tv_issuerlock_lbl.setText("Issuer Lock:");
		if (jContract.getInt("Issuer Lock")==1) {
			tv_issuerlock.setText("YES");
			tv_issuerlock.setTextColor(Color.RED);
		}
		else {
			tv_issuerlock.setText("NO");
			tv_issuerlock.setTextColor(Color.GREEN);
		}
				
		tr_issuerlock.addView(tv_issuerlock_lbl);
		tr_issuerlock.addView(tv_issuerlock);
		tl_data.addView(tr_issuerlock);
	
		// peer row
		TableRow tr_peer = new TableRow(this);
		tr_peer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_peer_lbl = new TextView(this);
		TextView tv_peer = new TextView(this);
		tv_peer_lbl.setText("Peer Approval:");
		tv_peer.setText(jContract.getString("Peer Approval"));	
		tr_peer.addView(tv_peer_lbl);
		tr_peer.addView(tv_peer);
		tl_data.addView(tr_peer);
		
		// shares issued row
		TableRow tr_sIssued = new TableRow(this);
		tr_sIssued.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_sIssued_lbl = new TextView(this);
		TextView tv_sIssued = new TextView(this);
		tv_sIssued_lbl.setText("Shares Issued:");
		tv_sIssued.setText(jContract.getString("Shares Issued"));
		tr_sIssued.addView(tv_sIssued_lbl);
		tr_sIssued.addView(tv_sIssued);
		tl_data.addView(tr_sIssued);
		
		// Shares Outstanding row
		TableRow tr_sOutstanding = new TableRow(this);
		tr_sOutstanding.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_sOutstanding_lbl = new TextView(this);
		TextView tv_sOutstanding = new TextView(this);
		tv_sOutstanding_lbl.setText("Shares Outstanding:");
		tv_sOutstanding.setText(jContract.getString("Shares Outstanding"));
		tr_sOutstanding.addView(tv_sOutstanding_lbl);
		tr_sOutstanding.addView(tv_sOutstanding);
		tl_data.addView(tr_sOutstanding);
		
		// Issuer Detail
		TextView tv_issuerdetail_lbl = new TextView(this);
		tv_issuerdetail_lbl.setText('\n' + "Issuer Detail:");
		tl_data.addView(tv_issuerdetail_lbl);
		
		TextView tv_issuerdetail = new TextView(this);
		tv_issuerdetail.setText(Html.fromHtml(jContract.getString("Issuer Detail")));
		tv_issuerdetail.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_issuerdetail);
		
		// website Detail
		TextView tv_website_lbl = new TextView(this);
		tv_website_lbl.setText('\n' + "Website:");
		tl_data.addView(tv_website_lbl);
				
		TextView tv_website = new TextView(this);
		tv_website.setText(Html.fromHtml(jContract.getString("Website")));
		tv_website.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_website);
		
		// Forum Detail
		TextView tv_forum_lbl = new TextView(this);
		tv_forum_lbl.setText('\n' + "Forum:");
		tl_data.addView(tv_forum_lbl);
						
		TextView tv_forum = new TextView(this);
		tv_forum.setText(Html.fromHtml(jContract.getString("Forum")));
		tv_forum.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_forum);
		
		// Contract Detail
		TextView tv_contract_lbl = new TextView(this);
		tv_contract_lbl.setText('\n' + "Contract:");
		tl_data.addView(tv_contract_lbl);
						
		TextView tv_contract = new TextView(this);
		tv_contract.setText(Html.fromHtml(jContract.getString("Contract")));
		tv_contract.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_contract);
		
		// Executive Summary
		TextView tv_execsum_lbl = new TextView(this);
		tv_execsum_lbl.setText('\n' + "Executive Summary:");
		tl_data.addView(tv_execsum_lbl);
						
		TextView tv_execsum = new TextView(this);
		tv_execsum.setText(Html.fromHtml(jContract.getString("Executive Summary")));
		tv_execsum.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_execsum);
		
		// Business Description
		TextView tv_bizdesc_lbl = new TextView(this);
		tv_bizdesc_lbl.setText('\n' + "Business Description:");
		tl_data.addView(tv_bizdesc_lbl);
								
		TextView tv_bizdesc = new TextView(this);
		tv_bizdesc.setText(Html.fromHtml(jContract.getString("Business Description")));
		tv_bizdesc.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_bizdesc);
		
		// Market Definition
		TextView tv_marketdesc_lbl = new TextView(this);
		tv_marketdesc_lbl.setText('\n' + "Market Definition:");
		tl_data.addView(tv_marketdesc_lbl);
								
		TextView tv_marketdesc = new TextView(this);
		tv_marketdesc.setText(Html.fromHtml(jContract.getString("Market Definition")));
		tv_marketdesc.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_marketdesc);
		
		// Products and Services
		TextView tv_prodserv_lbl = new TextView(this);
		tv_prodserv_lbl.setText('\n' + "Products and Services:");
		tl_data.addView(tv_prodserv_lbl);
							
		TextView tv_prodserv = new TextView(this);
		tv_prodserv.setText(Html.fromHtml(jContract.getString("Products and Services")));
		tv_prodserv.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_prodserv);
		
		// Organization and Management
		TextView tv_orgman_lbl = new TextView(this);
		tv_orgman_lbl.setText('\n' + "Organization and Management:");
		tl_data.addView(tv_orgman_lbl);
								
		TextView tv_orgman = new TextView(this);
		tv_orgman.setText(Html.fromHtml(jContract.getString("Organization and Management")));
		tv_orgman.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_orgman);
		
		// Marketing Strategy
		TextView tv_marketstrat_lbl = new TextView(this);
		tv_marketstrat_lbl.setText('\n' + "Marketing Strategy:");
		tl_data.addView(tv_marketstrat_lbl);
								
		TextView tv_marketstrat = new TextView(this);
		tv_marketstrat.setText(Html.fromHtml(jContract.getString("Marketing Strategy")));
		tv_marketstrat.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_marketstrat);
		
		// Financial Management
		TextView tv_finman_lbl = new TextView(this);
		tv_finman_lbl.setText('\n' + "Financial Management:");
		tl_data.addView(tv_finman_lbl);
								
		TextView tv_finman = new TextView(this);
		tv_finman.setText(Html.fromHtml(jContract.getString("Financial Management")));
		tv_finman.setMovementMethod(LinkMovementMethod.getInstance());
		tl_data.addView(tv_finman);
		
		
		} catch (JSONException e) { Log.i("LG", e.getMessage()); }
		
		tl_data.invalidate();
		
	}
	
	public void fillDividends(JSONObject jDividends)
	{
		// do nothing if the history is null
		if(jDividends==null)
		{
			return;
		}
		
		// Get ids
		//Log.i("LG", "Get Ids");
		JSONArray jIds = new JSONArray();
		jIds = jDividends.names();
		if(jIds==null) { return; }
		
		int num_dividends=jIds.length()-1;
	
		
		// Fill Layout
		//Log.i("LG", "Fill Layout");

		// Description Row
		TableRow tr_desc = new TableRow(this);
		tr_desc.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		TextView tv_desc_timestamp = new TextView(this);
		TextView tv_desc_amount = new TextView(this);
		TextView tv_desc_sharespaid = new TextView(this);
		TextView tv_desc_status = new TextView(this);
		
		tv_desc_timestamp.setText("Timestamp");
		tv_desc_amount.setText("Amount (LTC)");
		tv_desc_sharespaid.setText("Shares Paid");
		tv_desc_status.setText("Status");
		
		tv_desc_timestamp.setPadding(0, 0, 10, 0);
		tv_desc_amount.setPadding(0, 0, 10, 0);
		tv_desc_sharespaid.setPadding(0, 0, 10, 0);
		
		tr_desc.addView(tv_desc_timestamp);
		tr_desc.addView(tv_desc_amount);
		tr_desc.addView(tv_desc_sharespaid);
		tr_desc.addView(tv_desc_status);
		
		TableLayout tl_dividend_history = (TableLayout) findViewById(R.id.main_tableLayout_data);
		tl_dividend_history.removeAllViews();
		
		TextView tv_title = new TextView(this);
		tv_title.setText("Dividends");
		tl_dividend_history.addView(tv_title);
		
		tl_dividend_history.addView(tr_desc);
		for (int i=0; i<num_dividends; i++)
		{
			//Log.i("LG", "Fill Table");
			// Dividend
			TableRow tr_dividend = new TableRow(this);
			tr_dividend.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			
			TextView tv_timestamp = new TextView(this);
			TextView tv_amount = new TextView(this);
			TextView tv_sharespaid = new TextView(this);
			TextView tv_status= new TextView(this);
			tv_timestamp.setPadding(0, 0, 10, 0);
			tv_amount.setPadding(0, 0, 10, 0);
			tv_sharespaid.setPadding(0, 0, 10, 0);
			
			// Pull dividend data from JSON
			try {
				JSONObject jDividend = jDividends.getJSONObject(Integer.toString(i));
				tv_timestamp.setText(jDividend.getString("process_time"));
				tv_amount.setText(jDividend.getString("amount"));
				tv_sharespaid.setText(jDividend.getString("shares_paid"));
				tv_status.setText(jDividend.getString("status"));
			} catch (JSONException e) {
				Log.i("LG", "JSONException:"+e.getMessage());
			}
	
			//Log.i("LG", "Add TextViews");
			tr_dividend.addView(tv_timestamp);
			tr_dividend.addView(tv_amount);
			tr_dividend.addView(tv_sharespaid);
			tr_dividend.addView(tv_status);
			tl_dividend_history.addView(tr_dividend);
		}
	}
	
	public void Query(String type, String ticker)
	{	
		LTGComm comm = new LTGComm(handler);
		TextView tv_status = (TextView) findViewById(R.id.main_tv_downloading);
		
		
		Log.i("LG", "Query: Type:"+type+" for "+ticker);
		if (type.equals("Trade History"))
		{
			tv_status.setText("Downloading Ticker...");
			comm.getTicker(ticker);
			
			tv_status.setText("Downloading History...");
			comm.getHistory(ticker);
			
		}
		
		else if(type.equals("Motions"))
		{
			//new DownloadTicker().execute(ticker);
			//new DownloadMotions().execute(ticker);
			tv_status.setText("Downloading Ticker...");
			comm.getTicker(ticker);
			tv_status.setText("Downloading Motions...");
			comm.getMotions(ticker);
		}
		else if(type.equals("Dividends"))
		{
			//new DownloadTicker().execute(ticker);
			//new DownloadDividends().execute(ticker);
			tv_status.setText("Downloading Ticker...");
			comm.getTicker(ticker);
			tv_status.setText("Downloading Dividends...");
			comm.getDividends(ticker);
		}
		else if(type.equals("Contract & Prospectus"))
		{
			//new DownloadTicker().execute(ticker);
			//new DownloadSecurity().execute(ticker);
			tv_status.setText("Downloading Ticker...");
			comm.getTicker(ticker);
			tv_status.setText("Downloading Contract...");
			comm.getContract(ticker);
		}
		else if(type.equals("Notifications"))
		{
			//new DownloadTicker().execute(ticker);
			//new DownloadNotifications().execute(ticker);
			tv_status.setText("Downloading Ticker...");
			comm.getTicker(ticker);
			tv_status.setText("Downloading Notifications...");
			comm.getNotifications(ticker);
		}
	
		else if(type.equals("Orderbook"))
		{
			//new DownloadTicker().execute(ticker);
			//new DownloadOrders().execute(ticker);
			tv_status.setText("Downloading Ticker...");
			comm.getTicker(ticker);
			tv_status.setText("Downloading Orders...");
			comm.getOrders(ticker);
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
  
        //Prevent keyboard from popping up automatically
        getWindow().setSoftInputMode(
        	      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        // Disable Chart Markup
        XYPlot mySimpleXYPlot= (XYPlot) findViewById(R.id.mySimpleXYPlot);
        mySimpleXYPlot.disableAllMarkup();
        
        //Selector
    	Spinner spn_nav = (Spinner) findViewById(R.id.main_spinner_selector);
    	ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.selector_main, android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spn_nav.setAdapter(adapter);
    	spn_nav.setSelection(0);
    	spn_nav.setOnItemSelectedListener(new SpinnerListener());
    	
    	// Initialize starting security
    	EditText et_ticker = (EditText) findViewById(R.id.main_editText_ticker);
    	if(constants.SITE.equals("LTC")) { et_ticker.setText("LTC-GLOBAL"); }
    	if(constants.SITE.equals("BTC")) { et_ticker.setText("BTC-TRADING-PT"); }
        
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
     					Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
     					startActivityForResult(intent, 2);
     					
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
						Uri uriUrl = Uri.parse(constants.URL_API_SECURITY+et_ticker.getText().toString());
					    Intent iBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
					    startActivity(iBrowser);
     				}
				}
     	});
     	
     	// Portfolio Button
     	// Launch the portfolio activity
     	Button btn_portfolio = (Button) findViewById(R.id.main_btn_portfolio);
     	btn_portfolio.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getApplicationContext(), PortfolioActivity.class);
					startActivity(intent);
				}
     	});
     	
     	
     	// Insert admob ads if not the paid version
     	if (constants.PAID==false)
     	{
     		
     		LinearLayout ll_admob = (LinearLayout) findViewById(R.id.main_ll_admob);
     		
     		adView = new AdView(this, AdSize.BANNER, constants.API_ADMOB);
     		ll_admob.addView(adView);
     		
     		AdRequest adRequest = new AdRequest();
     		
     		if (constants.DEBUG_MODE)
     		{
     			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);               // Emulator
     			adRequest.addTestDevice("htc-evo-HT265S405632");                // Test Android Device
     		}
     		adView.loadAd(adRequest);
     	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	// Browse Activity
    	if (requestCode == 1) {
	    	if(resultCode == RESULT_OK) {
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
	    }
    	
    	// Settings Activity
    	if ( requestCode==2)
    	{
    		// Refresh the last query
    		EditText et = (EditText) findViewById(R.id.main_editText_ticker);
    		Spinner spn= (Spinner) findViewById(R.id.main_spinner_selector);
    		Query((String)spn.getSelectedItem(), et.getText().toString());
    	}
    		
    	

    }
 }



