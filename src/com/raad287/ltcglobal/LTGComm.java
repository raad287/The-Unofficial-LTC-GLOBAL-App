package com.raad287.ltcglobal;

import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.raad287.ltcglobal.LTGParser;

import com.raad287.ltcglobal.Constants;

public class LTGComm {
	/*
	public static final String URL_API_HISTORY = "http://www.litecoinglobal.com/api/history/";
	public static final String URL_API_TICKER = "http://www.litecoinglobal.com/api/ticker/";
	public static final String URL_API_SECURITY ="http://www.litecoinglobal.com/security/";
	public static final String URL_API_ORDERS = "https://www.litecoinglobal.com/api/orders/";
	public static final String URL_API_DIVIDENDS = "https://www.litecoinglobal.com/api/dividendHistory/";
	public static final String URL_API_CONTRACT = "https://www.litecoinglobal.com/api/assetContract/";
	public static final String URL_API_PORTFOLIO ="https://www.litecoinglobal.com/api/act?key=";
	public static final String URL_CSV_PORTFOLIO ="https://www.litecoinglobal.com/csv/portfolio?key=";
	public static final String URL_CSV_PORTFOLIO_TRADES="https://www.litecoinglobal.com/csv/trades?key=";
	public static final String URL_CSV_PORTFOLIO_DIVIDENDS="https://www.litecoinglobal.com/csv/dividends?key=";
	public static final String URL_CSV_PORTFOLIO_DEPOSITS="https://www.litecoinglobal.com/csv/deposits?key=";
	public static final String URL_CSV_PORTFOLIO_WITHDRAWLS="https://www.litecoinglobal.com/csv/withdrawals?key=";
	
	public static int MSG_QUERY_RETURN=2;
	public static int MSG_QUERY_FAILURE=3;
	*/
	Handler parentHandler;
	LTGParser parser = new LTGParser();
	Constants constants = new Constants();
	
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
	
	public void getPortfolio(String api_key)
	{
		new DownloadPortfolio().execute(api_key);
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
			Log.i("LG", "LTGComm:DownloadDividends:doInBackground: Executing download dividends");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(constants.URL_API_DIVIDENDS+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadDividends:Exception:"+e.getMessage()); return null; }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
	
			// parse dividends
			//JSONObject jDividends = parser.parseDividendsHTML(sb.toString());
			JSONObject jDividends = parser.parseDividendsJSON(sb.toString());
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
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
			HttpGet http_get = new HttpGet(constants.URL_API_HISTORY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
				
			} catch (Exception e) { 
				Log.i("LG", "LTGComm:DownloadHistory:Exception1:"+e.getMessage());
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
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadHistory:Exception2:"+e.getMessage()); return null; }
			
			if(jHistory!=null)
			{
				try {
					jHistory.put("ticker_name", tickers[0]);
				} catch (JSONException e) { Log.i("LG","LTGComm:DownloadHistory:Exception3:"+ e.getMessage()); }
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
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
			Log.i("LG", "LTGComm:DownloadOrders:doInBackground: Executing download orders");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(constants.URL_API_ORDERS+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
				
			} catch (Exception e) { 
				Log.i("LG", "LTGComm:DownloadOrders:Exception1:"+e.getMessage());
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
			} catch (JSONException e) { Log.i("LG","LTGComm:DownloadOrders:Exception2"+ e.getMessage()); return null; }
			
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
		
	}
	
	// Download the User's portfolio in the background, post to parentHandler
	private class DownloadPortfolio extends AsyncTask<String, Integer, Bundle> 
	{
		@Override
		// doInBackground: take string array containing ticker names and download
		protected Bundle doInBackground(String... apikey) {
			Log.i("LG", "LTGComm:DownloadPortfolio:doInBackground: Executing Download Portfolio");
			
			Bundle returnBundle = new Bundle();
			returnBundle.putString("query", "portfolio");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb_portfolio = new StringBuilder();
			StringBuilder sb_portfolio_trades = new StringBuilder();
			StringBuilder sb_portfolio_dividends = new StringBuilder();
			StringBuilder sb_portfolio_deposits = new StringBuilder();
			StringBuilder sb_portfolio_withdrawls = new StringBuilder();
			
			
			HttpGet http_get_portfolio = new HttpGet(constants.URL_CSV_PORTFOLIO+apikey[0]);
			HttpGet http_get_portfolio_trades = new HttpGet(constants.URL_CSV_PORTFOLIO_TRADES+apikey[0]);
			HttpGet http_get_portfolio_dividends = new HttpGet(constants.URL_CSV_PORTFOLIO_DIVIDENDS+apikey[0]);
			HttpGet http_get_portfolio_deposits = new HttpGet(constants.URL_CSV_PORTFOLIO_DEPOSITS+apikey[0]);
			HttpGet http_get_portfolio_withdrawls = new HttpGet(constants.URL_CSV_PORTFOLIO_WITHDRAWLS+apikey[0]);
				
			try {
				HttpResponse http_response_portfolio=http_client.execute(http_get_portfolio);
				http_client = new DefaultHttpClient();   
				HttpResponse http_response_portfolio_trades=http_client.execute(http_get_portfolio_trades);
				http_client = new DefaultHttpClient();   
				HttpResponse http_response_portfolio_dividends=http_client.execute(http_get_portfolio_dividends);
				http_client = new DefaultHttpClient();   
				HttpResponse http_response_portfolio_deposits=http_client.execute(http_get_portfolio_deposits);
				http_client = new DefaultHttpClient();   
				HttpResponse http_response_portfolio_withdrawls=http_client.execute(http_get_portfolio_withdrawls);
				
				sb_portfolio.append(EntityUtils.toString(http_response_portfolio.getEntity()));
				sb_portfolio_trades.append(EntityUtils.toString(http_response_portfolio_trades.getEntity()));
				sb_portfolio_dividends.append(EntityUtils.toString(http_response_portfolio_dividends.getEntity()));
				sb_portfolio_deposits.append(EntityUtils.toString(http_response_portfolio_deposits.getEntity()));
				sb_portfolio_withdrawls.append(EntityUtils.toString(http_response_portfolio_withdrawls.getEntity()));
				
					
			} catch (Exception e) { 
				Log.i("LG", "LTGComm:DownloadPortfolio:Exception1:"+e.getMessage());
				returnBundle.putString("data", "error_downloadexception");
				return returnBundle;
				
			}
			
			JSONObject jReturn= new JSONObject();
	
			
			// Check for bad server responses
			if(sb_portfolio.toString().contains("Request rate limit exceeded") || 
				sb_portfolio_trades.toString().contains("Request rate limit exceeded") ||
				sb_portfolio_deposits.toString().contains("Request rate limit exceeded") ||
				sb_portfolio_withdrawls.toString().contains("Request rate limit exceeded") ||
				sb_portfolio_dividends.toString().contains("Request rate limit exceeded") )
			{
				// send failure bundle
				returnBundle.putString("data", "error_ratelimit");
				return returnBundle;
			}
			// Check for bad server responses
			else if(sb_portfolio.toString().contains("Invalid api key.") ||
				sb_portfolio_trades.toString().contains("Invalid api key.") ||
				sb_portfolio_deposits.toString().contains("Invalid api key.") ||
				sb_portfolio_withdrawls.toString().contains("Invalid api key.") ||
				sb_portfolio_dividends.toString().contains("Invalid api key.") )
			{
				// send failure bundle
				returnBundle.putString("data", "error_badapi");
				return returnBundle;
			}
			
		
			else
			{
				//parse into json object
				JSONObject jPortfolio = parser.parsePortfolioCSV(sb_portfolio.toString());
				JSONObject jPortfolioTrades = parser.parsePortfolioTradesCSV(sb_portfolio_trades.toString());
				JSONObject jPortfolioDividends = parser.parsePortfolioDividendsCSV(sb_portfolio_dividends.toString());
				JSONObject jPortfolioDeposits = parser.parsePortfolioDepositsCSV(sb_portfolio_deposits.toString());
				JSONObject jPortfolioWithdrawls = parser.parsePortfolioWithdrawlsCSV(sb_portfolio_withdrawls.toString());
				
				// generate updated date
				Calendar c = Calendar.getInstance(); 
				StringBuilder sb_date = new StringBuilder();
				sb_date.append(String.valueOf(c.get(Calendar.MONTH)) + "/");
				sb_date.append(String.valueOf(c.get(Calendar.DAY_OF_MONTH)) + "/");
				sb_date.append(String.valueOf(c.get(Calendar.YEAR)) + " ");
				sb_date.append(String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":");
				sb_date.append(String.valueOf(c.get(Calendar.MINUTE)));
				
				try {
					jReturn.put("Portfolio", jPortfolio);
					jReturn.put("Portfolio Trades", jPortfolioTrades);
					jReturn.put("Portfolio Dividends", jPortfolioDividends);
					jReturn.put("Portfolio Deposits", jPortfolioDeposits);
					jReturn.put("Portfolio Withdrawls", jPortfolioWithdrawls);
					jReturn.put("Updated", sb_date.toString());
				} catch (JSONException e) {
					Log.i("LG", "LTGComm:DownloadPortfolio:JSONException"+e.getMessage());
					returnBundle.putString("data", "error_jsonexception");
					return returnBundle;
				}
			
			}	
			
			// no problems, send bundle
			returnBundle.putString("data", jReturn.toString());
			return returnBundle;
			
		}

		@Override
		protected void onPostExecute(Bundle returnBundle) {
				
			// Return data to parent message handler
			Message msg = new Message();
			if(returnBundle!=null)
			{
				// Bad response
				if(returnBundle.getString("data").contains("error"))
				{
					msg.arg1=constants.MSG_QUERY_FAILURE;
					msg.setData(returnBundle);
					parentHandler.sendMessage(msg);
				}
				// Successfull response
				else
				{
					msg.arg1=constants.MSG_QUERY_RETURN;
					msg.setData(returnBundle);
					parentHandler.sendMessage(msg);
				}
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
				returnBundle = new Bundle();
				returnBundle.putString("data", "error_nullbundle");
				msg.setData(returnBundle);
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
			HttpGet http_get = new HttpGet(constants.URL_API_TICKER+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadTicker:Exception1:"+e.getMessage()); return null;}
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
			
			JSONObject jTicker=new JSONObject();
			try {
				jTicker = parser.parseTickersJSON(sb.toString());
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadTicker:Exception2:"+e.getMessage()); return null; }
			
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
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
			HttpGet http_get = new HttpGet(constants.URL_API_CONTRACT+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadContract:Exception1:"+e.getMessage()); return null;}
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}

			JSONObject jContract = new JSONObject();
			try {
				jContract = parser.parseContractJSON(sb.toString());
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadContract:Exception2:"+e.getMessage()); return null; }
			
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
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
			Log.i("LG", "LTGComm:DownloadNotifications:doInBackground: Executing download notifications");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(constants.URL_API_SECURITY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadNotifications:Exception1:"+e.getMessage()); return null; }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}

			JSONObject jNotifications = new JSONObject();
			try 
			{
				//Log.i("LG", "LTGComm:DownloadNotifications:return from parseNotificationsHTML:"+parser.parseNotificationsHTML(sb.toString()));
				jNotifications = parser.parseNotificationsHTML(sb.toString());
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadNotifications:Exception2:"+e.getMessage()); return null; }
			
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
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
			Log.i("LG", "LTGComm:DownloadMotions:doInBackground: Executing download motions");
			
			HttpClient http_client = new DefaultHttpClient();   
			StringBuilder sb = new StringBuilder();
			HttpGet http_get = new HttpGet(constants.URL_API_SECURITY+tickers[0]);
			
			try {
				HttpResponse http_response=http_client.execute(http_get);
				sb.append(EntityUtils.toString(http_response.getEntity()));
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadMotions:Exception1:"+e.getMessage()); return null; }
			
			// Check for errors
			if(sb.toString().contains("only please") || sb.toString().startsWith("0")
				|| sb.toString().contains("Error")) // error
			{
					return null;
			}
			
			JSONObject jMotions = new JSONObject();

			try {
				jMotions = parser.parseMotionsHTML(sb.toString());
			} catch (Exception e) { Log.i("LG", "LTGComm:DownloadMotions:Exception2:"+e.getMessage()); return null; }
			
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
				msg.arg1=constants.MSG_QUERY_RETURN;
				msg.setData(returnBundle);
				parentHandler.sendMessage(msg);
			}
			else // returnBundle == null
			{
				msg.arg1=constants.MSG_QUERY_FAILURE;
				parentHandler.sendMessage(msg);
			}
						
			super.onPostExecute(returnBundle);
		}
	}

}
