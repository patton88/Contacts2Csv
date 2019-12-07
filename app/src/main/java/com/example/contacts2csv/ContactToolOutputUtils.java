package com.example.contacts2csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import javax.crypto.NullCipher;

public class ContactToolOutputUtils extends ContactToolUtils {
	private static final String TAG = "ContactOutputTool";
	private static int mCount = 0;

	private static List<Person> personList;

	public static boolean outputContacts(Context context) {
		init();
		try {
			//String result = getFromContactDatabase(context);
			GetContactsInfo getContactsInfo = new GetContactsInfo(context);
			String result = getContactsInfo.getContactInfo();
			writeFile(ContactContant.OUTPUT_PATH, result);
			//System.out.println(getContactsInfo.mIntSum);
			mCount = getContactsInfo.mIntSum;
		} catch (Exception e) {
			Log.e(TAG, "Error in outputContacts " + e.getMessage());
			return false;
		}
		return true;
	}

	private static String getFromContactDatabase(Context context) {
		queryContacts(context);

		String strResult = "";
		for(Person p : personList){
			//System.out.println(p.toString());
			strResult += p.getName() + ',' + p.getPhone() + '\n';
		}

		return strResult;
	}

		private static void init() {
		mCount = 0;
		personList = new ArrayList<Person>();
	}

	private static void writeFile(String path, String buffer) {
		try {
			File file = new File(path);
			FileWriter writer = new FileWriter(file, false);
			writer.write(buffer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int getCount(){
		//return personList.size();
		return mCount;
	}

	/**
	 * 查询联系人
	 * @Context context
	 */
	public static String queryContacts(Context context) {
		// 1. 去raw_contacts表中取所有联系人的_id
		Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");
		Uri dataUri = Uri.parse("content://com.android.contacts/data");

		Cursor cursor = context.getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
		if(cursor != null && cursor.getCount() > 0) {
			while(cursor.moveToNext()) {
				int id = cursor.getInt(0);

				// 2. 去data表中根据上面取到的_id查询对应id的数据.
				String selection = "raw_contact_id = ?";
				String[] selectionArgs = {String.valueOf(id)};
				Cursor c = context.getContentResolver().query(dataUri, new String[]{"data1", "mimetype"}, selection, selectionArgs, null);
				Person p = new Person();
				if(c != null && c.getCount() > 0) {
					while(c.moveToNext()) {
						String mimetype = c.getString(1);		// 当前取的是mimetype的值
						String data1 = c.getString(0);			// 当前取的是data1数据
						data1 = funRemove(data1);

						if("vnd.android.cursor.item/phone_v2".equals(mimetype)) {
							//Log.i(TAG, "号码: " + data1);
							//p.setPhone(p.getPhone() + "," + data1);
                            if(null == p.getPhone()){
                                p.setPhone(data1);
                            }else {
                                p.setPhone(p.getPhone() + "," + data1);
                            }
						} else if("vnd.android.cursor.item/name".equals(mimetype)) {
							//Log.i(TAG, "姓名: " + data1);
							p.setName(data1);
						} else if("vnd.android.cursor.item/email_v2".equals(mimetype)) {
							//Log.i(TAG, "邮箱: " + data1);
							p.setEmail(data1);
						}
					}
					c.close();
					if(null != p.getName()){
						personList.add(p);
					}
				}
			}
			cursor.close();
		}

		for(Person p : personList){
			System.out.println(p.toString());
            //Log.i(TAG, p.toString());
		}

		return "";
	}

	private static String funRemove(String str)
	{
		str = str.replace(" ", "");
		str = str.replace("-", "");
		str = str.replace("+86", "");
		str = str.replace("+", "");
		return str;
	}

	/*
	Person{name='Zhangsan', email='null', phone='13323489735,13443222323,13778786565,13243454545,12234453454'}
    Person{name='WangWwu', email='null', phone='1332489799'}
    */

	/*
	Person{name='Zhangsan', email='null', phone='1 332-348-9735,1 344-322-2323,1 377-878-6565,1 324-345-4545,1 223-445-3454'}
    Person{name='WangWwu', email='null', phone='1 332-489-799'}
    */

	/*
	Person{name='Zhangsan', email='null', phone='null,1 332-348-9735,1 344-322-2323,1 377-878-6565,1 324-345-4545,1 223-445-3454'}
    Person{name='WangWwu', email='null', phone='null,1 332-489-799'}
    */

	/*
	03-10 20:02:45.805 5245-5245/? E/libprocessgroup: failed to make and chown /acct/uid_10062: Read-only file system
	03-10 20:02:45.805 5245-5245/? W/Zygote: createProcessGroup failed, kernel missing CONFIG_CGROUP_CPUACCT?
	03-10 20:02:45.806 5245-5245/? I/art: Not late-enabling -Xcheck:jni (already on)
	03-10 20:02:51.190 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 号码: 1 332-348-9735
	03-10 20:02:51.190 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 姓名: Zhangsan
	03-10 20:02:51.190 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 号码: 1 344-322-2323
	03-10 20:02:51.190 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 号码: 1 377-878-6565
	03-10 20:02:51.190 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 号码: 1 324-345-4545
	03-10 20:02:51.190 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 号码: 1 223-445-3454
	03-10 20:02:51.196 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 号码: 1 332-489-799
	03-10 20:02:51.196 5245-5266/com.zyc.contact.tool I/ContactOutputTool: 姓名: WangWwu
	*/

}
