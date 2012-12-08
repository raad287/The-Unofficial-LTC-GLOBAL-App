package com.raad287.ltcglobal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private static String SHARED_PREF_KEY="ULTCG";
	private static String PREF_MIN_DOMAIN = "MIN_DOMAIN";
	private static String PREF_MIN_DOMAIN_AUTO = "MIN_DOMAIN_AUTO";
	private static String PREF_MAX_DOMAIN = "MAX_DOMAIN";
	private static String PREF_MAX_DOMAIN_AUTO = "MAX_DOMAIN_AUTO";
	private static String PREF_MIN_RANGE = "MIN_RANGE";
	private static String PREF_MIN_RANGE_AUTO = "MIN_RANGE_AUTO";
	private static String PREF_MAX_RANGE = "MAX_RANGE";
	private static String PREF_MAX_RANGE_AUTO = "MAX_RANGE_AUTO";
	
	
	private static float DEFAULT_MIN_DOMAIN = 0;
	private static float DEFAULT_MAX_DOMAIN = 500;
	private static float DEFAULT_MIN_RANGE = 0;
	private static float DEFAULT_MAX_RANGE =500;
	private static boolean DEFAULT_MIN_DOMAIN_AUTO = true;
	private static boolean DEFAULT_MAX_DOMAIN_AUTO = true;
	private static boolean DEFAULT_MIN_RANGE_AUTO = true;
	private static boolean DEFAULT_MAX_RANGE_AUTO = true;
	
	// saveSettings: save settings into shared preferences
	public void saveSettings()
	{
		SharedPreferences sp = getSharedPreferences(SHARED_PREF_KEY, 0);
		SharedPreferences.Editor editor = sp.edit();
		EditText et_minDomain = (EditText) findViewById(R.id.settings_et_minDomain);
		EditText et_maxDomain = (EditText) findViewById(R.id.settings_et_maxDomain);
		EditText et_minRange = (EditText) findViewById(R.id.settings_et_minRange);
		EditText et_maxRange = (EditText) findViewById(R.id.settings_et_maxRange);
		
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		float min_domain = Float.valueOf(et_minDomain.getText().toString());
		float max_domain = Float.valueOf(et_maxDomain.getText().toString());
		float min_range = Float.valueOf(et_minRange.getText().toString());
		float max_range = Float.valueOf(et_maxRange.getText().toString());
		
		// basic error checking
		if(min_domain<0) 
		{ 
			min_domain=0; 
			et_minDomain.setText(String.valueOf(min_domain));
		}
		
		if(max_domain<=min_domain) 
		{ 
			max_domain=min_domain+1;
			et_maxDomain.setText(String.valueOf(max_domain));
		}
		
		if(min_range<0) 
		{ 
			min_range=0; 
			et_minRange.setText(String.valueOf(min_range));
		}
		if(max_range<=min_range) 
		{ 
			max_range=min_range+1; 
			et_maxRange.setText(String.valueOf(max_range));
		}
		
		
		editor.putFloat(PREF_MIN_DOMAIN, min_domain);
		editor.putFloat(PREF_MAX_DOMAIN, max_domain);
		editor.putFloat(PREF_MIN_RANGE, min_range);
		editor.putFloat(PREF_MAX_RANGE, max_range);
		
		editor.putBoolean(PREF_MIN_DOMAIN_AUTO, cb_auto_minDomain.isChecked());
		editor.putBoolean(PREF_MAX_DOMAIN_AUTO, cb_auto_maxDomain.isChecked());
		editor.putBoolean(PREF_MIN_RANGE_AUTO, cb_auto_minRange.isChecked());
		editor.putBoolean(PREF_MAX_RANGE_AUTO, cb_auto_maxRange.isChecked());
		
		if (editor.commit()) { Toast.makeText(getBaseContext(), "Settings Saved", Toast.LENGTH_SHORT).show(); }
		else { Toast.makeText(getBaseContext(), "Error: Unable to save", Toast.LENGTH_SHORT).show(); } 
	}
	

	public void loadDefaults()
	{
		EditText et_minDomain = (EditText) findViewById(R.id.settings_et_minDomain);
		EditText et_maxDomain = (EditText) findViewById(R.id.settings_et_maxDomain);
		EditText et_minRange = (EditText) findViewById(R.id.settings_et_minRange);
		EditText et_maxRange = (EditText) findViewById(R.id.settings_et_maxRange);
		
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		et_minDomain.setText( String.valueOf( DEFAULT_MIN_DOMAIN));
		et_maxDomain.setText( String.valueOf(DEFAULT_MAX_DOMAIN));
		et_minRange.setText( String.valueOf( DEFAULT_MIN_RANGE));
		et_maxRange.setText( String.valueOf(DEFAULT_MAX_RANGE));
		
		cb_auto_minDomain.setChecked( DEFAULT_MIN_DOMAIN_AUTO);
		cb_auto_maxDomain.setChecked(DEFAULT_MAX_DOMAIN_AUTO);
		cb_auto_minRange.setChecked(DEFAULT_MIN_RANGE_AUTO);
		cb_auto_maxRange.setChecked( DEFAULT_MAX_RANGE_AUTO);
		
		
		// Disable editTexts if Auto is enabled
		if(cb_auto_minDomain.isChecked()) { et_minDomain.setEnabled(false); }
		else { et_minDomain.setEnabled(true); }
		
		if(cb_auto_maxDomain.isChecked()) { et_maxDomain.setEnabled(false); }
		else { et_maxDomain.setEnabled(true); }
		
		if(cb_auto_minRange.isChecked()) { et_minRange.setEnabled(false); }
		else { et_minRange.setEnabled(true); }
		
		if(cb_auto_maxRange.isChecked()) { et_maxRange.setEnabled(false); }
		else { et_minDomain.setEnabled(true); }
	}
	// loadSettings: load settings from shared preferences file, update UI
	public void loadSettings()
	{
		SharedPreferences sp = getSharedPreferences(SHARED_PREF_KEY, 0);
		EditText et_minDomain = (EditText) findViewById(R.id.settings_et_minDomain);
		EditText et_maxDomain = (EditText) findViewById(R.id.settings_et_maxDomain);
		EditText et_minRange = (EditText) findViewById(R.id.settings_et_minRange);
		EditText et_maxRange = (EditText) findViewById(R.id.settings_et_maxRange);
		
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		et_minDomain.setText( String.valueOf( sp.getFloat(PREF_MIN_DOMAIN, DEFAULT_MIN_DOMAIN)));
		et_maxDomain.setText( String.valueOf( sp.getFloat(PREF_MAX_DOMAIN, DEFAULT_MAX_DOMAIN)));
		et_minRange.setText( String.valueOf( sp.getFloat(PREF_MIN_RANGE, DEFAULT_MIN_RANGE)));
		et_maxRange.setText( String.valueOf( sp.getFloat(PREF_MAX_RANGE, DEFAULT_MAX_RANGE)));
		
		cb_auto_minDomain.setChecked(sp.getBoolean(PREF_MIN_DOMAIN_AUTO, DEFAULT_MIN_DOMAIN_AUTO));
		cb_auto_maxDomain.setChecked(sp.getBoolean(PREF_MAX_DOMAIN_AUTO, DEFAULT_MAX_DOMAIN_AUTO));
		cb_auto_minRange.setChecked(sp.getBoolean(PREF_MIN_RANGE_AUTO, DEFAULT_MIN_RANGE_AUTO));
		cb_auto_maxRange.setChecked(sp.getBoolean(PREF_MAX_RANGE_AUTO, DEFAULT_MAX_RANGE_AUTO));
		
		
		// Disable editTexts if Auto is enabled
		if(cb_auto_minDomain.isChecked()) { et_minDomain.setEnabled(false); }
		else { et_minDomain.setEnabled(true); }
		
		if(cb_auto_maxDomain.isChecked()) { et_maxDomain.setEnabled(false); }
		else { et_maxDomain.setEnabled(true); }
		
		if(cb_auto_minRange.isChecked()) { et_minRange.setEnabled(false); }
		else { et_minRange.setEnabled(true); }
		
		if(cb_auto_maxRange.isChecked()) { et_maxRange.setEnabled(false); }
		else { et_maxRange.setEnabled(true); }
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		 
		//Prevent keyboard from popping up automatically
        getWindow().setSoftInputMode(
        	      WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		setContentView(R.layout.activity_settings);
		
		// load settings from shared preferences
		loadSettings();
		
		Button btn_save = (Button) findViewById(R.id.settings_btn_save);
		Button btn_loadDefaults = (Button) findViewById(R.id.settings_btn_loadDefault);
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		// Save Button
		btn_save.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				saveSettings();
			} 
		});
		
		// Load Defaults
		btn_loadDefaults.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				loadDefaults();
			} 
		});
		
		// Enable / Disable Associated EditTexts when the Auto Checkbox is Checked/Unchecked
		// minDomain Checkbox
		cb_auto_minDomain.setOnClickListener(new CheckBox.OnClickListener() {
			public void onClick(View arg0) {
				CheckBox cb = (CheckBox) arg0;
				EditText et = (EditText) findViewById (R.id.settings_et_minDomain);
				if(cb.isChecked()) { et.setEnabled(false); }
				else { et.setEnabled(true); }
			} 
		});
		
		// maxDomain Checkbox
		cb_auto_maxDomain.setOnClickListener(new CheckBox.OnClickListener() {
			public void onClick(View arg0) {
				CheckBox cb = (CheckBox) arg0;
				EditText et = (EditText) findViewById (R.id.settings_et_maxDomain);
				if(cb.isChecked()) { et.setEnabled(false); }
				else { et.setEnabled(true); }
			} 
		});
		
		// minRange Checkbox
		cb_auto_minRange.setOnClickListener(new CheckBox.OnClickListener() {
			public void onClick(View arg0) {
				CheckBox cb = (CheckBox) arg0;
				EditText et = (EditText) findViewById (R.id.settings_et_minRange);
				if(cb.isChecked()) { et.setEnabled(false); }
				else { et.setEnabled(true); }
			} 
		});
		
		// maxRange Checkbox
		cb_auto_maxRange.setOnClickListener(new CheckBox.OnClickListener() {
			public void onClick(View arg0) {
				CheckBox cb = (CheckBox) arg0;
				EditText et = (EditText) findViewById (R.id.settings_et_maxRange);
				if(cb.isChecked()) { et.setEnabled(false); }
				else { et.setEnabled(true); }
			} 
		});
		
		
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		saveSettings();
		
		Intent returnIntent = new Intent();
		setResult(RESULT_OK,returnIntent);     
   	 	finish();
		
		super.onPause();
	}

}
