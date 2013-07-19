package com.raad287.ltcglobal;

import com.raad287.ltcglobal.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.raad287.ltcglobal.LTGComm;

public class PortfolioActivity extends Activity {
	
	JSONObject jPortfolio=null;
	Constants constants = new Constants();
	
	public class PortfolioHandler extends Handler
	{

		@Override
		public void handleMessage(Message msg) {
			if(msg==null) { return; }
			
			TextView tv_status= (TextView) findViewById(R.id.portfolio_tv_status);
			
			if(msg.arg1== constants.MSG_QUERY_FAILURE)
			{
				//tv_status.setText("Error: Unable to download portfolio");
				
				Bundle returnBundle = msg.getData();
				
				if(returnBundle.containsKey("data"))
				{
					if(returnBundle.getString("data").equals("error_ratelimit"))
					{
						tv_status.setText("Error: Rate limit exceeded, please try again in 60 seconds");
					}
					else if (returnBundle.getString("data").equals("error_nullbundle"))
					{
						tv_status.setText("Error: parsing error, null bundle");
					}
					else if (returnBundle.getString("data").equals("error_jsonexception"))
					{
						tv_status.setText("Error: parsing error, jsonexception");
					}
					else if (returnBundle.getString("data").equals("error_badapi"))
					{
						tv_status.setText("Error: invalid API key");
					}
					else if (returnBundle.getString("data").equals("error_downloadexception"))
					{
						tv_status.setText("Error: downloading error");
					}
					else
					{
						tv_status.setText("Error: query failure");
					}
				}
			}
			
			// Query has been completed
			if (msg.arg1 == constants.MSG_QUERY_RETURN )
			{
				Bundle returnBundle = msg.getData();
				
				if(returnBundle.getString("query").equals("portfolio"))
				{
					try{
						jPortfolio = new JSONObject (returnBundle.getString("data"));
						
						// Save portfolio in shared preferences
						SharedPreferences sp = getSharedPreferences(constants.SHARED_PREF_KEY, 0);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString(constants.PREF_PORTFOLIO, jPortfolio.toString() );
						editor.commit();
						
						updateDisplay();
						tv_status.setText("Portfolio Updated");
						
					} catch(JSONException e) { 
						Log.i("LG", e.getMessage());
						tv_status.setText("Error: Bad portfolio data");
						return;
					}
					
					
				}
			}
			super.handleMessage(msg);
			
		}
	}
	
	PortfolioHandler handler= new PortfolioHandler();
	LTGComm comm= new LTGComm(handler);
	
	
	public void updateDisplay()
	{
		TableLayout tl_data = (TableLayout) findViewById(R.id.portfolio_tl_data);
		TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
		Spinner spn = (Spinner) findViewById(R.id.portfolio_spinner_selector);
		
		if(jPortfolio==null)
		{
			// Nothing in portfolio
			return;
		}
		else
		{
			tv_status.setText("Generating display..");
			String selection = (String)spn.getSelectedItem();
			
			TextView tv_updated = (TextView) findViewById(R.id.portfolio_tv_updated);
			try
			{
				//Log.i("LG", "jPortfolioNames="+jPortfolio.names().toString());
				tv_updated.setText(jPortfolio.getString("Updated"));
			} catch (JSONException e) { Log.i("LG", "PortfolioActivity:updateDisplay:Exception:"+e.getMessage()); }
			
			if (selection.equals("Personal"))
			{
				fillPersonal();
			}
			else if (selection.equals("Trades"))
			{
				fillTrades();
			}
			else if (selection.equals("Dividends"))
			{
				fillDividends();
			}
			else if (selection.equals("Deposits"))
			{
				fillDeposits();
			}
			else if (selection.equals("Withdrawals"))
			{
				fillWithdrawls();
			}
			tv_status.setText("Display Updated");
		}
		
	}
	
	public void fillPersonal()
	{
		if(jPortfolio==null) { return; }
	
		TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
		TableLayout tl_data = (TableLayout) findViewById(R.id.portfolio_tl_data);
		tl_data.removeAllViews();
		
		try
		{
			//Log.i("LG", jPortfolio.toString());
			JSONObject jPersonal = jPortfolio.getJSONObject("Portfolio");
			
			//Log.i("LG", jPersonal.toString());
			
			// make sure there's entries
			if(jPersonal.names()==null)
			{
				Log.i("LG", "PortfolioActivity:fillPersonal:No portfolio data");
				tv_status.setText("Error: No portfolio data");
				return;
			}
			
			int num_entries = jPersonal.names().length();
			
			// Lable Row
			TableRow tr_lbl_row = new TableRow(this);
			TextView tv_lbl_ticker = new TextView(this);
			TextView tv_lbl_quantity = new TextView(this);
			TextView tv_lbl_last_change = new TextView(this);
			
			tv_lbl_ticker.setPadding(0, 0, 10, 0);
			tv_lbl_quantity.setPadding(0, 0, 10, 0);
			
			tv_lbl_ticker.setText("Ticker");
			tv_lbl_quantity.setText("Quantity");
			tv_lbl_last_change.setText("Last Change");
			
			tr_lbl_row.addView(tv_lbl_ticker);
			tr_lbl_row.addView(tv_lbl_quantity);
			tr_lbl_row.addView(tv_lbl_last_change);
			tl_data.addView(tr_lbl_row);
			
			for(int i=0; i<num_entries; i++)
			{
				// Entries are sorted by order of download
				JSONObject jEntry = jPersonal.getJSONObject(String.valueOf(i));
				String ticker = jEntry.getString("ticker");
				String quantity = jEntry.getString("quantity");
				String last_change = jEntry.getString("last_change");
			
				// Display Data
				TableRow tr_row = new TableRow(this);
				TextView tv_ticker= new TextView(this);
				TextView tv_quantity= new TextView(this);
				TextView tv_last_change = new TextView(this);
				
				tv_ticker.setText(ticker);
				tv_quantity.setText(quantity);
				tv_last_change.setText(last_change);
				
				tv_ticker.setPadding(0, 0, 10, 0);
				tv_quantity.setPadding(0, 0, 10, 0);
				
				tr_row.addView(tv_ticker);
				tr_row.addView(tv_quantity);
				tr_row.addView(tv_last_change);
				tl_data.addView(tr_row);
				
			}
		} catch (JSONException e)
		{
			Log.i("LG", "PortfolioActivity:fillPersonal:JSONException:"+e.getMessage());
			tv_status.setText("Error: Bad portfolio data");
		}
	
	}
	
	public void fillTrades()
	{
		if(jPortfolio==null) { return; }
	
		TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
		TableLayout tl_data = (TableLayout) findViewById(R.id.portfolio_tl_data);
		tl_data.removeAllViews();
		
		try
		{
			//Log.i("LG", jPortfolio.toString());
			JSONObject jTrades = jPortfolio.getJSONObject("Portfolio Trades");
			
			//Log.i("LG", jTrades.toString());
			
			// make sure there's entries
			if(jTrades.names()==null)
			{
				Log.i("LG", "PortfolioActivity:fillTrades:No portfolio data");
				tv_status.setText("Error: No portfolio data");
				return;
			}
			
			int num_entries = jTrades.names().length();
			
			// Lable Row
			TableRow tr_lbl_row = new TableRow(this);
			TextView tv_lbl_ticker = new TextView(this);
			TextView tv_lbl_operation = new TextView(this);
			TextView tv_lbl_quantity = new TextView(this);
			TextView tv_lbl_amount = new TextView(this);
			TextView tv_lbl_timestamp = new TextView(this);
			
			tv_lbl_ticker.setPadding(0, 0, 10, 0);
			tv_lbl_operation.setPadding(0, 0, 10, 0);
			tv_lbl_quantity.setPadding(0, 0, 10, 0);
			tv_lbl_amount.setPadding(0, 0, 10, 0);
			
			tv_lbl_ticker.setText("Ticker");
			tv_lbl_operation.setText("Operation");
			tv_lbl_quantity.setText("Quantity");
			tv_lbl_amount.setText("Amount");
			tv_lbl_timestamp.setText("Timestamp");
			
			tr_lbl_row.addView(tv_lbl_ticker);
			tr_lbl_row.addView(tv_lbl_operation);
			tr_lbl_row.addView(tv_lbl_quantity);
			tr_lbl_row.addView(tv_lbl_amount);
			tr_lbl_row.addView(tv_lbl_timestamp);
			
			tl_data.addView(tr_lbl_row);
			
			for(int i=0; i<num_entries; i++)
			{
				// Entries are sorted by order of download
				JSONObject jEntry = jTrades.getJSONObject(String.valueOf(i));
				String ticker = jEntry.getString("ticker");
				String operation = jEntry.getString("operation");
				String quantity = jEntry.getString("quantity");
				String amount = jEntry.getString("amount");
				String timestamp = jEntry.getString("timestamp");
			
				// Display Data
				TableRow tr_row = new TableRow(this);
				TextView tv_ticker= new TextView(this);
				TextView tv_operation = new TextView(this);
				TextView tv_quantity= new TextView(this);
				TextView tv_amount = new TextView(this);
				TextView tv_timestamp = new TextView(this);
				
				tv_ticker.setText(ticker);
				tv_operation.setText(operation);
				tv_quantity.setText(quantity);
				tv_amount.setText(amount);
				tv_timestamp.setText(timestamp);
				
				tv_ticker.setPadding(0, 0, 10, 0);
				tv_operation.setPadding(0, 0, 10, 0);
				tv_quantity.setPadding(0, 0, 10, 0);
				tv_amount.setPadding(0, 0, 10, 0);
				
				tr_row.addView(tv_ticker);
				tr_row.addView(tv_operation);
				tr_row.addView(tv_quantity);
				tr_row.addView(tv_amount);
				tr_row.addView(tv_timestamp);
				tl_data.addView(tr_row);
				
			}
		} catch (JSONException e)
		{
			Log.i("LG", "PortfolioActivity:fillPersonal:JSONException:"+e.getMessage());
			tv_status.setText("Error: Bad portfolio data");
		}
	
	}
	
	public void fillDividends()
	{
		if(jPortfolio==null) { return; }
	
		TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
		TableLayout tl_data = (TableLayout) findViewById(R.id.portfolio_tl_data);
		tl_data.removeAllViews();
		
		try
		{
			//Log.i("LG", jPortfolio.toString());
			JSONObject jDividends = jPortfolio.getJSONObject("Portfolio Dividends");
			
			//Log.i("LG", jDividends.toString());
			
			// make sure there's entries
			if(jDividends.names()==null)
			{
				Log.i("LG", "PortfolioActivity:Dividends:No portfolio data");
				tv_status.setText("Error: No portfolio data");
				return;
			}
			
			int num_entries = jDividends.names().length();
			
			// Lable Row
			TableRow tr_lbl_row = new TableRow(this);
			TextView tv_lbl_ticker = new TextView(this);
			TextView tv_lbl_shares_paid = new TextView(this);
			TextView tv_lbl_per_share = new TextView(this);
			TextView tv_lbl_total_paid = new TextView(this);
			TextView tv_lbl_timestamp = new TextView(this);
			
			tv_lbl_ticker.setPadding(0, 0, 10, 0);
			tv_lbl_shares_paid.setPadding(0, 0, 10, 0);
			tv_lbl_per_share.setPadding(0, 0, 10, 0);
			tv_lbl_total_paid.setPadding(0, 0, 10, 0);
			
			tv_lbl_ticker.setText("Ticker");
			tv_lbl_shares_paid.setText("Shares Paid");
			tv_lbl_per_share.setText("Per Share");
			tv_lbl_total_paid.setText("Total Paid");
			tv_lbl_timestamp.setText("Timestamp");
			
			tr_lbl_row.addView(tv_lbl_ticker);
			tr_lbl_row.addView(tv_lbl_shares_paid);
			tr_lbl_row.addView(tv_lbl_per_share);
			tr_lbl_row.addView(tv_lbl_total_paid);
			tr_lbl_row.addView(tv_lbl_timestamp);
			
			tl_data.addView(tr_lbl_row);
			
			for(int i=0; i<num_entries; i++)
			{
				// Entries are sorted by order of download
				JSONObject jEntry = jDividends.getJSONObject(String.valueOf(i));
				String ticker = jEntry.getString("ticker");
				String shares_paid = jEntry.getString("shares_paid");
				String per_share = jEntry.getString("per_share");
				String total_paid = jEntry.getString("total_paid");
				String timestamp = jEntry.getString("timestamp");
			
				// Display Data
				TableRow tr_row = new TableRow(this);
				TextView tv_ticker = new TextView(this);
				TextView tv_shares_paid = new TextView(this);
				TextView tv_per_share = new TextView(this);
				TextView tv_total_paid = new TextView(this);
				TextView tv_timestamp = new TextView(this);
				
				tv_ticker.setText(ticker);
				tv_shares_paid.setText(shares_paid);
				tv_per_share.setText(per_share);
				tv_total_paid.setText(total_paid);
				tv_timestamp.setText(timestamp);
				
				tv_ticker.setPadding(0, 0, 10, 0);
				tv_shares_paid.setPadding(0, 0, 10, 0);
				tv_per_share.setPadding(0, 0, 10, 0);
				tv_total_paid.setPadding(0, 0, 10, 0);
				
				tr_row.addView(tv_ticker);
				tr_row.addView(tv_shares_paid);
				tr_row.addView(tv_per_share);
				tr_row.addView(tv_total_paid);
				tr_row.addView(tv_timestamp);
				tl_data.addView(tr_row);
				
			}
		} catch (JSONException e)
		{
			Log.i("LG", "PortfolioActivity:Dividends:JSONException:"+e.getMessage());
			tv_status.setText("Error: Bad portfolio data");
		}
	}
	
	public void fillDeposits()
	{
		if(jPortfolio==null) { return; }
	
		TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
		TableLayout tl_data = (TableLayout) findViewById(R.id.portfolio_tl_data);
		tl_data.removeAllViews();
		
		try
		{
			//Log.i("LG", jPortfolio.toString());
			JSONObject jDeposits = jPortfolio.getJSONObject("Portfolio Deposits");
			
			//Log.i("LG", jDeposits.toString());
			
			// make sure there's entries
			if(jDeposits.names()==null)
			{
				Log.i("LG", "PortfolioActivity:Deposits:No portfolio data");
				tv_status.setText("Error: No portfolio data");
				return;
			}
			
			int num_entries = jDeposits.names().length();
			
			// Lable Row
			TableRow tr_lbl_row = new TableRow(this);
			TextView tv_lbl_description = new TextView(this);
			TextView tv_lbl_amount = new TextView(this);
			TextView tv_lbl_timestamp = new TextView(this);
			TextView tv_lbl_tid = new TextView(this);
			
			tv_lbl_description.setPadding(0, 0, 10, 0);
			tv_lbl_amount.setPadding(0, 0, 10, 0);
			tv_lbl_timestamp.setPadding(0, 0, 10, 0);
			
			
			tv_lbl_description.setText("Description");
			tv_lbl_amount.setText("Amount");
			tv_lbl_timestamp.setText("Timestamp");
			tv_lbl_tid.setText("Transaction ID");
			
			tr_lbl_row.addView(tv_lbl_description);
			tr_lbl_row.addView(tv_lbl_amount);
			tr_lbl_row.addView(tv_lbl_timestamp);
			tr_lbl_row.addView(tv_lbl_tid);
			
			tl_data.addView(tr_lbl_row);
			
			for(int i=0; i<num_entries; i++)
			{
				// Entries are sorted by order of download
				JSONObject jEntry = jDeposits.getJSONObject(String.valueOf(i));
				String description = jEntry.getString("description");
				String amount = jEntry.getString("amount");
				String timestamp = jEntry.getString("timestamp");
				String tid = jEntry.getString("transaction_id");
			
				// Display Data
				TableRow tr_row = new TableRow(this);
				TextView tv_description = new TextView(this);
				TextView tv_amount = new TextView(this);
				TextView tv_timestamp = new TextView(this);
				TextView tv_tid = new TextView(this);
				
				tv_description.setText(description);
				tv_amount.setText(amount);
				tv_timestamp.setText(timestamp);
				tv_tid.setText(tid);
				
				tv_description.setPadding(0, 0, 10, 0);
				tv_amount.setPadding(0, 0, 10, 0);
				tv_timestamp.setPadding(0, 0, 10, 0);
				
				tr_row.addView(tv_description);
				tr_row.addView(tv_amount);
				tr_row.addView(tv_timestamp);
				tr_row.addView(tv_tid);
		
				tl_data.addView(tr_row);
				
			}
		} catch (JSONException e)
		{
			Log.i("LG", "PortfolioActivity:Deposits:JSONException:"+e.getMessage());
			tv_status.setText("Error: Bad portfolio data");
		}

	}
	
	public void fillWithdrawls()
	{
		if(jPortfolio==null) { return; }
	
		TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
		TableLayout tl_data = (TableLayout) findViewById(R.id.portfolio_tl_data);
		tl_data.removeAllViews();
		
		try
		{
			//Log.i("LG", jPortfolio.toString());
			JSONObject jWithdrawls = jPortfolio.getJSONObject("Portfolio Withdrawls");
			
			//Log.i("LG", jDeposits.toString());
			
			// make sure there's entries
			if(jWithdrawls.names()==null)
			{
				Log.i("LG", "PortfolioActivity:Withdrawls:No portfolio data");
				tv_status.setText("Error: No portfolio data");
				return;
			}
			
			int num_entries = jWithdrawls.names().length();
			
			// Lable Row
			TableRow tr_lbl_row = new TableRow(this);
			TextView tv_lbl_description = new TextView(this);
			TextView tv_lbl_amount = new TextView(this);
			TextView tv_lbl_timestamp = new TextView(this);
			TextView tv_lbl_tid = new TextView(this);
			
			tv_lbl_description.setPadding(0, 0, 10, 0);
			tv_lbl_amount.setPadding(0, 0, 10, 0);
			tv_lbl_timestamp.setPadding(0, 0, 10, 0);
			
			
			tv_lbl_description.setText("Description");
			tv_lbl_amount.setText("Amount");
			tv_lbl_timestamp.setText("Timestamp");
			tv_lbl_tid.setText("Transaction ID");
			
			tr_lbl_row.addView(tv_lbl_description);
			tr_lbl_row.addView(tv_lbl_amount);
			tr_lbl_row.addView(tv_lbl_timestamp);
			tr_lbl_row.addView(tv_lbl_tid);
			
			tl_data.addView(tr_lbl_row);
			
			for(int i=0; i<num_entries; i++)
			{
				// Entries are sorted by order of download
				JSONObject jEntry = jWithdrawls.getJSONObject(String.valueOf(i));
				String description = jEntry.getString("description");
				String amount = jEntry.getString("amount");
				String timestamp = jEntry.getString("timestamp");
				String tid = jEntry.getString("transaction_id");
			
				// Display Data
				TableRow tr_row = new TableRow(this);
				TextView tv_description = new TextView(this);
				TextView tv_amount = new TextView(this);
				TextView tv_timestamp = new TextView(this);
				TextView tv_tid = new TextView(this);
				
				tv_description.setText(description);
				tv_amount.setText(amount);
				tv_timestamp.setText(timestamp);
				tv_tid.setText(tid);
				
				tv_description.setPadding(0, 0, 10, 0);
				tv_amount.setPadding(0, 0, 10, 0);
				tv_timestamp.setPadding(0, 0, 10, 0);
				
				tr_row.addView(tv_description);
				tr_row.addView(tv_amount);
				tr_row.addView(tv_timestamp);
				tr_row.addView(tv_tid);
		
				tl_data.addView(tr_row);
				
			}
		} catch (JSONException e)
		{
			Log.i("LG", "PortfolioActivity:Withdrawls:JSONException:"+e.getMessage());
			tv_status.setText("Error: Bad portfolio data");
		}
		
	}
	
	public class SpinnerListener implements OnItemSelectedListener {
		int curScreen;
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			Spinner spn = (Spinner)findViewById(R.id.portfolio_spinner_selector);
			{
				if(jPortfolio!=null) { updateDisplay(); }
			}
		}
		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.i("LG", "PortfolioActivity:onResume:getting settings");
		// Settings
    	SharedPreferences sp = getSharedPreferences(constants.SHARED_PREF_KEY, 0);
    	String api_key=sp.getString(constants.PREF_PAPI_KEY, constants.DEFAULT_PAPI_KEY);
    	TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
    	
    	// If the saved portfolio isn't empty, attempt to load it
    	if(!sp.getString(constants.PREF_PORTFOLIO, constants.DEFAULT_PORTFOLIO).equals(constants.DEFAULT_PORTFOLIO))
    	{
    		try
    		{
    			//Log.i("LG", "PortfolioActivity:onResume:PREF_PORTFOLIO="+sp.getString(PREF_PORTFOLIO, DEFAULT_PORTFOLIO));
    			jPortfolio = new JSONObject(sp.getString(constants.PREF_PORTFOLIO, constants.DEFAULT_PORTFOLIO));
    			
    			if(jPortfolio.toString().equals("")) { jPortfolio=null; } 
  
    		} catch (JSONException e) { Log.i("LG", "PortfolioActivity:onResume:Exception:"+e.getMessage()); }
    	} 
    	if(jPortfolio!=null) {updateDisplay(); }
    	
    	if(api_key.equals(""))
    	{
    		Log.i("LG", "PortfolioActivity:onResume:no api");
    		tv_status.setText("Error: No API key key is saved");
    	}
    	else {
    		Log.i("LG", "PortfolioActivity:onResume:downloading portfolio");
    		tv_status.setText("Downloading portfolio..");
    		comm.getPortfolio(api_key);
    		
    	}
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_portfolio);
		
		
		 //Selector
    	Spinner spn_nav = (Spinner) findViewById(R.id.portfolio_spinner_selector);
    	ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.selector_portfolio, android.R.layout.simple_spinner_item);
    	adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	spn_nav.setAdapter(adapter);
    	spn_nav.setSelection(0);
    	spn_nav.setOnItemSelectedListener(new SpinnerListener());
    	
    	// Update Button
     	// Launch the browse activity
     	Button btn_update = (Button) findViewById(R.id.portfolio_btn_update);
     	btn_update.setOnClickListener( new Button.OnClickListener() {
				public void onClick(View v) {
					// Settings
			    	SharedPreferences sp = getSharedPreferences(constants.SHARED_PREF_KEY, 0);
			    	String api_key=sp.getString(constants.PREF_PAPI_KEY, constants.DEFAULT_PAPI_KEY);
			    	TextView tv_status = (TextView) findViewById(R.id.portfolio_tv_status);
			    	
			    	if(api_key.equals(""))
			    	{
			    		tv_status.setText("Error: No API key key is saved");
			    	}
			    	else {
			    		Log.i("LG", "PortfolioActivity:UpdateButton:getting portfolio");
			    		tv_status.setText("Downloading portfolio..");
			    		comm.getPortfolio(api_key);
			    	}
					
     					
				}
     	});
	}

}
