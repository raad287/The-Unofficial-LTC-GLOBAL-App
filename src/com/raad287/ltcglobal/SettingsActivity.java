package com.raad287.ltcglobal;

import com.raad287.ltcglobal.Constants;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import android.os.Build;

public class SettingsActivity extends Activity {
	
	Constants constants = new Constants();
	
	
	// saveSettings: save settings into shared preferences
	public void saveSettings()
	{
		SharedPreferences sp = getSharedPreferences(constants.SHARED_PREF_KEY, 0);
		SharedPreferences.Editor editor = sp.edit();
		EditText et_minDomain = (EditText) findViewById(R.id.settings_et_minDomain);
		EditText et_maxDomain = (EditText) findViewById(R.id.settings_et_maxDomain);
		EditText et_minRange = (EditText) findViewById(R.id.settings_et_minRange);
		EditText et_maxRange = (EditText) findViewById(R.id.settings_et_maxRange);
		EditText et_papikey = (EditText) findViewById(R.id.settings_et_apikey);
		
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		float min_domain = Float.valueOf(et_minDomain.getText().toString());
		float max_domain = Float.valueOf(et_maxDomain.getText().toString());
		float min_range = Float.valueOf(et_minRange.getText().toString());
		float max_range = Float.valueOf(et_maxRange.getText().toString());
		String papi_key = et_papikey.getText().toString();
		
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
		
		
		editor.putFloat(constants.PREF_MIN_DOMAIN, min_domain);
		editor.putFloat(constants.PREF_MAX_DOMAIN, max_domain);
		editor.putFloat(constants.PREF_MIN_RANGE, min_range);
		editor.putFloat(constants.PREF_MAX_RANGE, max_range);
		editor.putString(constants.PREF_PAPI_KEY, papi_key);
		
		editor.putBoolean(constants.PREF_MIN_DOMAIN_AUTO, cb_auto_minDomain.isChecked());
		editor.putBoolean(constants.PREF_MAX_DOMAIN_AUTO, cb_auto_maxDomain.isChecked());
		editor.putBoolean(constants.PREF_MIN_RANGE_AUTO, cb_auto_minRange.isChecked());
		editor.putBoolean(constants.PREF_MAX_RANGE_AUTO, cb_auto_maxRange.isChecked());
		
		if (editor.commit()) { Toast.makeText(getBaseContext(), "Settings Saved", Toast.LENGTH_SHORT).show(); }
		else { Toast.makeText(getBaseContext(), "Error: Unable to save", Toast.LENGTH_SHORT).show(); } 
	}
	

	public void loadDefaults()
	{
		EditText et_minDomain = (EditText) findViewById(R.id.settings_et_minDomain);
		EditText et_maxDomain = (EditText) findViewById(R.id.settings_et_maxDomain);
		EditText et_minRange = (EditText) findViewById(R.id.settings_et_minRange);
		EditText et_maxRange = (EditText) findViewById(R.id.settings_et_maxRange);
		EditText et_papi_key = (EditText) findViewById(R.id.settings_et_apikey);
		
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		et_minDomain.setText( String.valueOf( constants.DEFAULT_MIN_DOMAIN));
		et_maxDomain.setText( String.valueOf(constants.DEFAULT_MAX_DOMAIN));
		et_minRange.setText( String.valueOf( constants.DEFAULT_MIN_RANGE));
		et_maxRange.setText( String.valueOf(constants.DEFAULT_MAX_RANGE));
		et_papi_key.setText(constants.DEFAULT_PAPI_KEY);
		
		cb_auto_minDomain.setChecked( constants.DEFAULT_MIN_DOMAIN_AUTO);
		cb_auto_maxDomain.setChecked(constants.DEFAULT_MAX_DOMAIN_AUTO);
		cb_auto_minRange.setChecked(constants.DEFAULT_MIN_RANGE_AUTO);
		cb_auto_maxRange.setChecked( constants.DEFAULT_MAX_RANGE_AUTO);
		
		
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
		SharedPreferences sp = getSharedPreferences(constants.SHARED_PREF_KEY, 0);
		EditText et_minDomain = (EditText) findViewById(R.id.settings_et_minDomain);
		EditText et_maxDomain = (EditText) findViewById(R.id.settings_et_maxDomain);
		EditText et_minRange = (EditText) findViewById(R.id.settings_et_minRange);
		EditText et_maxRange = (EditText) findViewById(R.id.settings_et_maxRange);
		EditText et_papi_key = (EditText) findViewById(R.id.settings_et_apikey);
		
		CheckBox cb_auto_minDomain = (CheckBox) findViewById(R.id.settings_cb_auto_minDomain);
		CheckBox cb_auto_maxDomain = (CheckBox) findViewById(R.id.settings_cb_auto_maxDomain);
		CheckBox cb_auto_minRange = (CheckBox) findViewById(R.id.settings_cb_auto_minRange);
		CheckBox cb_auto_maxRange = (CheckBox) findViewById(R.id.settings_cb_auto_maxRange);
		
		et_minDomain.setText( String.valueOf( sp.getFloat(constants.PREF_MIN_DOMAIN, constants.DEFAULT_MIN_DOMAIN)));
		et_maxDomain.setText( String.valueOf( sp.getFloat(constants.PREF_MAX_DOMAIN, constants.DEFAULT_MAX_DOMAIN)));
		et_minRange.setText( String.valueOf( sp.getFloat(constants.PREF_MIN_RANGE, constants.DEFAULT_MIN_RANGE)));
		et_maxRange.setText( String.valueOf( sp.getFloat(constants.PREF_MAX_RANGE, constants.DEFAULT_MAX_RANGE)));
		et_papi_key.setText( String.valueOf(sp.getString(constants.PREF_PAPI_KEY, constants.DEFAULT_PAPI_KEY)));
		
		cb_auto_minDomain.setChecked(sp.getBoolean(constants.PREF_MIN_DOMAIN_AUTO, constants.DEFAULT_MIN_DOMAIN_AUTO));
		cb_auto_maxDomain.setChecked(sp.getBoolean(constants.PREF_MAX_DOMAIN_AUTO, constants.DEFAULT_MAX_DOMAIN_AUTO));
		cb_auto_minRange.setChecked(sp.getBoolean(constants.PREF_MIN_RANGE_AUTO, constants.DEFAULT_MIN_RANGE_AUTO));
		cb_auto_maxRange.setChecked(sp.getBoolean(constants.PREF_MAX_RANGE_AUTO, constants.DEFAULT_MAX_RANGE_AUTO));
		
		
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
		Button btn_external = (Button) findViewById(R.id.settings_btn_external);
		Button btn_paste = (Button) findViewById(R.id.settings_btn_paste);
		
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
		
		// External Button
		btn_external.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				Uri uriUrl = Uri.parse(constants.URL_EXTERNAL);
			    Intent iBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
			    startActivity(iBrowser);
			} 
		});
		
		// Paste Button
		// Paste requires API Level 11 or higher disable the paste button if the API level is to low
		if (Build.VERSION.SDK_INT<11)
		{
			btn_paste.setEnabled(false); // Disabled
			btn_paste.setVisibility(8); // Gone
		}
		else {
			btn_paste.setOnClickListener(new Button.OnClickListener() {
				@TargetApi(Build.VERSION_CODES.HONEYCOMB)
				public void onClick(View arg0) {
					ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
					String pasteData = "";
					EditText et_papi_key = (EditText) findViewById(R.id.settings_et_apikey);
					
					pasteData = clipboard.getPrimaryClip().getItemAt(0).getText().toString();
					et_papi_key.setText(pasteData);
				} 
			});
		}
		
		
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
