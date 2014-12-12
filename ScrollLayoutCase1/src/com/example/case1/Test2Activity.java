package com.example.case1;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Test2Activity extends Activity {
	private EditText mEditText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test2);
		mEditText=(EditText) findViewById(R.id.et_msg);
	}
	
	public void showMsgClick(View view){
		switch (view.getId()) {
		case R.id.btn_one:
			Toast.makeText(Test2Activity.this, mEditText.getText().toString(),0).show();
			break;
		}
	}
}
