package com.example.contacts2csv;

import android.content.Context;
import android.widget.Toast;

public class ContactToolUtils {
	public static void showToast(Context context,String message){
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}
}
