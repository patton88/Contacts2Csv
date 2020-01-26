package com.example.contacts2csv;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;

import com.example.contacts2csv.ContactContant;
import com.example.contacts2csv.ContactInfo;
import com.example.contacts2csv.ContactToolUtils;

public class ContactToolInsertUtils extends ContactToolUtils {
	private static final String TAG = "ContactToolInsertUtils";
	
	private static int successCount = 0;
	private static int failCount = 0;
	private static boolean isGbk = false;
	
	public static boolean insertIntoContact(Context context,String path,String charset){
		init(charset);
		try{
			ArrayList<ContactInfo> arrayList = readFromFile(path);
			if(arrayList == null){
				Log.e(TAG, "Error in insertIntoContact arrayList == null");
				return false;
			}
			Iterator<ContactInfo> iterator = arrayList.iterator();
			while(iterator.hasNext()){
				ContactInfo contactInfo = iterator.next();
				if(doInsertIntoContact(context,contactInfo)){
					successCount++;
				}
			}
		}catch (Exception e) {
			Log.e(TAG, "Error in insertIntoContact result : " + e.getMessage());
		}
		return true;
	}

	private static void init(String charset){
		successCount = 0;
		failCount =0;
		isGbk = charset.equals(ContactContant.CHARSET_GBK);
	}
	
	/**read txt file*/
	private static ArrayList<ContactInfo> readFromFile(String path){
		//read from file
		ArrayList<String> stringsArrayList = doReadFile(path);
		if(stringsArrayList == null){
			Log.e(TAG, "Error in readFromFile stringsArrayList == null");
			return null;
		}
		ArrayList<ContactInfo> contactInfoArrayList = handleReadStrings(stringsArrayList);
		return contactInfoArrayList;
	}
	
	private static ArrayList<String> doReadFile(String path){
		FileInputStream in = null;	
		ArrayList<String> arrayList = new ArrayList<String>();
	    try {
            byte[] tempbytes = new byte[ContactContant.BUFFER_SIZE];
            in = new FileInputStream(path);
            while (in.read(tempbytes) != -1) {
            	int length = 0;
            	int first = length;
            	for(int i = 0;i < tempbytes.length;i++){           		
            		if(tempbytes[i] == ContactContant.ENTER_CHAR_LINUX){
            			length = i;
            			byte[] nowBytes = new byte[length - first];
                    	System.arraycopy(tempbytes, first, nowBytes, 0, length - first);
                    	if(isGbk){
                    		arrayList.add(new String(nowBytes,ContactContant.CHARSET_GBK).trim());
                    	}
                    	else {
                    		arrayList.add(new String(nowBytes,ContactContant.CHARSET_UTF8).trim());
						}
                    	first = i + 1;
            		}
            	}
            	
            }
        } catch (Exception e1) {
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                	return null;
                }
            }
        }
        return arrayList;
	}

	private static ArrayList<ContactInfo> handleReadStrings(ArrayList<String> arrayList){
		ArrayList<ContactInfo> contactInfoArrayList = new ArrayList<ContactInfo>();
		Iterator<String> infos = arrayList.iterator();
		while(infos.hasNext()){
			String info = infos.next();
			String[] infoStrings = info.split(ContactContant.SPACE_REGULAR);
			String displayName = null;
			String mobileNum = null;
			String homeNum = null;
			switch (infoStrings.length) {
			case 0:
				//do nothing
				continue;
			case 1:
				displayName = infoStrings[0];
				break;
			case 2:
				displayName = infoStrings[0];
				if(infoStrings[1].length() == ContactContant.MOBILE_NUM_LENGTH){
					mobileNum = infoStrings[1];
				}
				else{
					homeNum = infoStrings[1];
				}
				break;
			default:
				//length >= 3
				displayName = infoStrings[0];
				mobileNum = infoStrings[1];
				homeNum = infoStrings[2];
			}
			//check displayName mobileNum and homeNum
			if(displayName == null || displayName.matches(ContactContant.NUM_REGULAR)){
				Log.e(TAG, "Error in handleReadStrings displayName == null");
				failCount++;
				continue;
			}
			if(mobileNum != null && (mobileNum.length() != ContactContant.MOBILE_NUM_LENGTH 
					|| !mobileNum.matches(ContactContant.NUM_REGULAR))){
				Log.e(TAG, "Error in handleReadStrings mobileNum is not all num or mobileNum == null");
				failCount++;
				continue;
			}
			if(homeNum != null && !(homeNum.matches(ContactContant.HOME_REGULAR_01) || 
					homeNum.matches(ContactContant.HOME_REGULAR_02))){
				Log.e(TAG, "Error in handleReadStrings homeNum is not all num");
				failCount++;
				continue;
			}
			contactInfoArrayList.add(new ContactInfo(displayName, mobileNum, homeNum));
		}
		return contactInfoArrayList;
	}
	
	/**insert into database*/
	private static boolean doInsertIntoContact(Context context,ContactInfo contactInfo){
		Log.d(TAG, "in doInsertIntoContact contactInfo = null? " + (contactInfo == null));
		try {
			ContentValues contentValues = new ContentValues();
	    	Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, contentValues);
	    	long rowId = ContentUris.parseId(uri);
	    	
	    	String name = contactInfo.getDisplayName();
	    	String mobileNum = contactInfo.getMobileNum();
	    	String homeNum = contactInfo.getHomeNum();
	    	
	    	//insert name
	    	if(name != null){
	        	contentValues.clear();
	        	contentValues.put(Data.RAW_CONTACT_ID, rowId);
	        	contentValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
	    		int index = name.length() / 2;
	    		String displayName = name;
	    		//check name if english name
	    		String givenName = null;
	    		String familyName = null;
	    		if(checkEnglishName(displayName) == false){
	    			givenName = name.substring(index);
	    		 	familyName = name.substring(0, index);
	    		}
	    		else {
					givenName = familyName = displayName;
				}
	    		contentValues.put(StructuredName.DISPLAY_NAME, displayName);
	        	contentValues.put(StructuredName.GIVEN_NAME, givenName);
	        	contentValues.put(StructuredName.FAMILY_NAME, familyName);
	        	context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
	    	}
	    	
	    	if(mobileNum != null){
	    		//insert phone
		    	contentValues.clear();
		    	contentValues.put(Data.RAW_CONTACT_ID, rowId);
		    	contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		    	contentValues.put(Phone.NUMBER, mobileNum);
		    	contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
		    	context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
	    	}
	    	
	    	if(homeNum != null){
		    	//insert houseNum
		    	contentValues.clear();
		    	contentValues.put(Data.RAW_CONTACT_ID, rowId);
		    	contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
		    	contentValues.put(Phone.NUMBER, homeNum);
		    	contentValues.put(Phone.TYPE, Phone.TYPE_HOME);
		    	context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
	    	}
		} catch (Exception e) {
			return false;
		}
		return true;   	
	}
	
	private static boolean checkEnglishName(String name){
		char[] nameChars = name.toCharArray();
		for(int i = 0;i < nameChars.length;i++){
			if((nameChars[i] >= 'a' && nameChars[i] <= 'z') ||
					(nameChars[i] >= 'A' && nameChars[i] <= 'Z')){
				continue;
			}
			return false;
		}
		return true;
	}
		
	public static int getSuccessCount() {
		return successCount;
	}

	public static int getFailCount() {
		return failCount;
	}
}
