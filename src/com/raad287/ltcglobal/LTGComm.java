package com.raad287.ltcglobal;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class LTGComm {
	public static final String URL_API_HISTORY = "http://www.litecoinglobal.com/api/history/";
	public static final String URL_API_TICKER = "http://www.litecoinglobal.com/api/ticker/";
	public static final String URL_API_SECURITY ="http://www.litecoinglobal.com/security/";
	public static final String URL_API_ORDERS = "https://www.litecoinglobal.com/api/orders/";
	
	public static int MSG_QUERY_RETURN=2;
	public static int MSG_QUERY_FAILURE=3;

	Handler parentHandler;
	LTGParser parser = new LTGParser();
	
	// Init with context
	public LTGComm(Handler p)
	{
		parentHandler = p;
	}
	
	public void getTicker(String ticker)
	{
		String[] ticker_a = new String[1];
		ticker_a[0]=ticker;
		new DownloadTicker().execute(ticker_a);
	}
	
	public void getDividends(String ticker)
	{
		new DownloadDividends().execute(ticker);
	}
	
	public void getOrders(String ticker)
	{
		new DownloadOrders().execute(ticker);
	}
	
	public void getContract(String ticker)
	{
		new DownloadContract().execute(ticker);
	}
	
	public void getNotifications(String ticker)
	{
		new DownloadNotifications().execute(ticker);
	}
	
	public void getMotions(String ticker)
	{
		new DownloadMotions().execute(ticker);
	}
	
	public void getHistory(String ticker)
	{
		String[] ticker_a = new String[1];
		ticker_a[0]=ticker;
		new DownloadHistory().execute(ticker_a);
	}
	
	// Download Dividends in the background, post to parentHandler
	private class DownloadDividends extends AsyncTask<String, Integer, Bundle> 
	{

		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadMotions:doInBackground: Executing download dividends");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_SECURITY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); return null; }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
	
			// parse dividends
			JSONObject jDividends = parser.parseDividendsHTML(sb.toString());
			if(jDividends==null) { return null; }
			
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jDividends.toString());
			returnBundle.putString("query", "dividends");
			returnBundle.putString("ticker", tickers[0]);
			return returnBundle;
		}
	
		@Override
		protected void onPostExecute(Bundle returnBundle) {
			
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
			
			super.onPostExecute(returnBundle);
		}
	}

	// Download Trade History in the background, post to parentHandler
	private class DownloadHistory extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
			//Log.i("LG", "Downloading History for "+tickers[0]);
			
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
			
			
			JSONObject jHistory=null;
			try
			{
				// Minor string formatting 
				sb.deleteCharAt(0);		// delete leading '['
				sb.deleteCharAt(sb.length()-1); // delete trailing ']'
				
				// parse history into json object
				jHistory = parser.parseHistoryRE(sb.toString());
			} catch (Exception e) { Log.i("LG", e.getMessage()); return null; }
			
			if(jHistory!=null)
			{
				try {
					jHistory.put("ticker_name", tickers[0]);
				} catch (JSONException e) { Log.i("LG", e.getMessage()); }
			}
			else //jHistory==null
			{ 
				return null;
			}
		 	
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jHistory.toString());
			returnBundle.putString("query", "history");
			returnBundle.putString("ticker", tickers[0]);
			
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
			
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
	}
	
	// Download Orderbook in the background, post to parentHandler
	private class DownloadOrders extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
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
			JSONObject jOrders = parser.parseOrdersJSON(sb.toString());
			try {
				jOrders.put("ticker_name", tickers[0]);
			} catch (JSONException e) { Log.i("LG", e.getMessage()); return null; }
			
			if(jOrders==null)
			{
				return null;
			}
			
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jOrders.toString());
			returnBundle.putString("query", "orders");
			returnBundle.putString("ticker", tickers[0]);
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
			
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
		
	}
	

	// Download Ticker in the background, post to parentHandler
	private class DownloadTicker extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_TICKER+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); return null;}
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
			
			JSONObject jTicker=new JSONObject();
			try {
				jTicker = parser.parseTickersJSON(sb.toString());
			} catch (Exception e) { Log.i("LG", e.getMessage()); return null; }
			
			if(jTicker==null)
			{
				return null;
			}
			
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jTicker.toString());
			returnBundle.putString("query", "ticker");
			returnBundle.putString("ticker", tickers[0]);
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
			
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
		
	}
	
	// Download Contract in the background, post to parentHandler
	private class DownloadContract extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_SECURITY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); return null;}
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}

			JSONObject jContract = new JSONObject();
			try {
				jContract = parser.parseSecurityHTML(sb.toString());
			} catch (Exception e) { Log.i("LG", e.getMessage()); return null; }
			
			if(jContract==null)
			{
				return null;
			}
			
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jContract.toString());
			returnBundle.putString("query", "contract");
			returnBundle.putString("ticker", tickers[0]);
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
	}
	
	// Download Notifications in the background, post to parentHandler
	private class DownloadNotifications extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadURL:doInBackground: Executing download notifications");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_SECURITY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); return null; }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}

			JSONObject jNotifications = new JSONObject();
			try 
			{
				jNotifications = parser.parseNotificationsHTML(sb.toString());
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); return null; }
			
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jNotifications.toString());
			returnBundle.putString("query", "notifications");
			returnBundle.putString("ticker", tickers[0]);
			
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
	}
	
	// Download Motions in the background, post to parentHandler
	private class DownloadMotions extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... tickers) {
			Log.i("LG", "MainActivity:DownloadMotions:doInBackground: Executing download motions");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(URL_API_SECURITY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "Exception:"+e.getMessage()); return null; }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
			
			JSONObject jMotions = new JSONObject();

			try {
				jMotions = parser.parseMotionsHTML(sb.toString());
			} catch (Exception e) { Log.i("LG", e.getMessage()); return null; }
			
			// prep return bundle
			Bundle returnBundle = new Bundle();
			returnBundle.putString("data", jMotions.toString());
			returnBundle.putString("query", "motions");
			returnBundle.putString("ticker", tickers[0]);
			
			return returnBundle;
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				msg.arg1=MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
	}

}
