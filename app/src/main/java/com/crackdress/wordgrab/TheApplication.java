package com.crackdress.wordgrab;

import android.app.Application;
import android.content.Context;


public class TheApplication extends Application {

  public static Context context;

  public void onCreate() {
    super.onCreate();
    TheApplication.context = getApplicationContext();
  }

  public static Context getAppContext() {
    return TheApplication.context;
  }
}
