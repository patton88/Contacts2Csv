package com.example.contacts2csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

public class ContactOutput extends ContactUtils {
	private final String m_sTAG = "ContactOutput";
	public int m_iSum = 0;

	public boolean outputContacts(Context context, String sPath) {
		try {
			GetContactInfo getContactInfo = new GetContactInfo(context);
			String sContacts = getContactInfo.getAllContact();
			writeFile(sPath, sContacts);
			m_iSum = getContactInfo.GetContactsSum();
		} catch (Exception e) {
			Log.e(m_sTAG, "Error in outputContacts " + e.getMessage());
			return false;
		}
		return true;
	}

	private static void writeFile(String sPath, String str) {
		try {
			File file = new File(sPath);
			FileWriter writer = new FileWriter(file, false);
			writer.write(str);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
