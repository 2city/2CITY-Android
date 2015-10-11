package com.twocity;

import com.parse.Parse;

import android.app.Application;

public class MyApplication extends Application{
	public void onCreate() {
		  Parse.initialize(this, "TWbCpafyft1l00iKwOwPSoMHamNAsa4AC6qoV34n", "J7PKDrwRLpRbzS7sJQvQA9CsSE5uWG5Xc73PgLm8");
		}
}
