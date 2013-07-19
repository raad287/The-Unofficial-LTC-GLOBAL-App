package com.raad287.ltcglobal;

public class Constants {
	// GENERAL
	public static boolean DEBUG_MODE = false;
	public static boolean PAID = true;
	public static String SITE = "LTC"; // "LTC"=LTC-GLOBAL / "BTC"=BTC-TC
	
	// DYNAMIC CONSTANTS
	public String URL_API_HISTORY;
	public String URL_API_TICKER;
	public String URL_API_SECURITY;
	public String URL_API_ORDERS;
	public String URL_API_DIVIDENDS;
	public String URL_API_CONTRACT;
	public String URL_API_PORTFOLIO;
	public String URL_CSV_PORTFOLIO;
	public String URL_CSV_PORTFOLIO_TRADES;
	public String URL_CSV_PORTFOLIO_DIVIDENDS;
	public String URL_CSV_PORTFOLIO_DEPOSITS;
	public String URL_CSV_PORTFOLIO_WITHDRAWLS;
	public String SHARED_PREF_KEY;
	public String URL_EXTERNAL;
	public String API_ADMOB;
	
	// SHARED PREFERENCES
	public static String PREF_MIN_DOMAIN = "MIN_DOMAIN";
	public static String PREF_MIN_DOMAIN_AUTO = "MIN_DOMAIN_AUTO";
	public static String PREF_MAX_DOMAIN = "MAX_DOMAIN";
	public static String PREF_MAX_DOMAIN_AUTO = "MAX_DOMAIN_AUTO";
	public static String PREF_MIN_RANGE = "MIN_RANGE";
	public static String PREF_MIN_RANGE_AUTO = "MIN_RANGE_AUTO";
	public static String PREF_MAX_RANGE = "MAX_RANGE";
	public static String PREF_MAX_RANGE_AUTO = "MAX_RANGE_AUTO";
	public static String PREF_PAPI_KEY = "PAPI_KEY";
	public static String PREF_PORTFOLIO = "PORTFOLIO";
	
	public static float DEFAULT_MIN_DOMAIN = 0;
	public static float DEFAULT_MAX_DOMAIN = 500;
	public static float DEFAULT_MIN_RANGE = 0;
	public static float DEFAULT_MAX_RANGE =500;
	public static boolean DEFAULT_MIN_DOMAIN_AUTO = true;
	public static boolean DEFAULT_MAX_DOMAIN_AUTO = true;
	public static boolean DEFAULT_MIN_RANGE_AUTO = true;
	public static boolean DEFAULT_MAX_RANGE_AUTO = true;
	public static String DEFAULT_PORTFOLIO = "";
	public static String DEFAULT_PAPI_KEY="";
	
	
	// HANDLER CONSTANTS
	public static int MSG_QUERY_RETURN=2;
	public static int MSG_QUERY_FAILURE=3;

	public Constants()
	{
		// LTC-GLOBAL
		if(SITE.equals("LTC"))
		{
			URL_API_HISTORY = "http://www.litecoinglobal.com/api/history/";
			URL_API_TICKER = "http://www.litecoinglobal.com/api/ticker/";
			URL_API_SECURITY ="http://www.litecoinglobal.com/security/";
			URL_API_ORDERS = "https://www.litecoinglobal.com/api/orders/";
			URL_EXTERNAL = "https://www.litecoinglobal.com/";
			URL_API_DIVIDENDS = "https://www.litecoinglobal.com/api/dividendHistory/";
			URL_API_CONTRACT = "https://www.litecoinglobal.com/api/assetContract/";
			URL_API_PORTFOLIO ="https://www.litecoinglobal.com/api/act?key=";
			URL_CSV_PORTFOLIO ="https://www.litecoinglobal.com/csv/portfolio?key=";
			URL_CSV_PORTFOLIO_TRADES="https://www.litecoinglobal.com/csv/trades?key=";
			URL_CSV_PORTFOLIO_DIVIDENDS="https://www.litecoinglobal.com/csv/dividends?key=";
			URL_CSV_PORTFOLIO_DEPOSITS="https://www.litecoinglobal.com/csv/deposits?key=";
			URL_CSV_PORTFOLIO_WITHDRAWLS="https://www.litecoinglobal.com/csv/withdrawals?key=";
			SHARED_PREF_KEY="ULTCG";
			API_ADMOB="a151af2cb1c365c";
		}
		
		// BTC-TC
		if(SITE.equals("BTC"))
		{
			URL_API_HISTORY = "http://www.btct.co/api/history/";
			URL_API_TICKER = "http://www.btct.co/api/ticker/";
			URL_API_SECURITY ="http://www.btct.co/security/";
			URL_API_ORDERS = "https://www.btct.co/api/orders/";
			URL_EXTERNAL = "https://www.btct.co/";
			URL_API_DIVIDENDS = "https://www.btct.co/api/dividendHistory/";
			URL_API_CONTRACT = "https://www.btct.co/api/assetContract/";
			URL_API_PORTFOLIO ="https://www.btct.co/api/act?key=";
			URL_CSV_PORTFOLIO ="https://www.btct.co/csv/portfolio?key=";
			URL_CSV_PORTFOLIO_TRADES="https://www.btct.co/csv/trades?key=";
			URL_CSV_PORTFOLIO_DIVIDENDS="https://www.btct.co/csv/dividends?key=";
			URL_CSV_PORTFOLIO_DEPOSITS="https://www.btct.co/csv/deposits?key=";
			URL_CSV_PORTFOLIO_WITHDRAWLS="https://www.btct.co/csv/withdrawals?key=";
			SHARED_PREF_KEY="BTCTC";
			API_ADMOB="a151af77876dabc";
		}
	}
}
