package com.raad287.ltcglobal;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

public class LTGParser {
	
	
	// parseTickersRE: raw string return from ticker, and format into JSON using trade timestamp as id
		public JSONObject parseHistoryRE(String sHistory)
		{ 
			Log.i("LG" , "In parser");
			JSONObject jHistory = new JSONObject();
			String re1=".*?";	// Non-greedy match on filler
			String re2="\\{.*?\\}";	// Uninteresting: cbraces
			String re3=".*?";	// Non-greedy match on filler
			String re4="(\\{.*?\\})";	// Curly Braces 1
		
			Pattern p = Pattern.compile(re1+re2+re3+re4,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
			Matcher m = p.matcher(sHistory);
		    
			Log.i("LG", "Parsing...");
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
					}
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
	
}
