package com.example.contacts2csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import android.net.Uri;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Contacts.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

public class ContactInsert {
    private JSONObject m_jsonContactData;            //用于存放获取的所有记录数据
    private final String m_sTAG = getClass().getSimpleName();
    private int m_iSuccessCount = 0;
    private int m_iFailCount = 0;
    private String m_sHead[];
    private ArrayList<ContactInfo> m_contactArrayList;

    private void init() {
        m_jsonContactData = new JSONObject(new LinkedHashMap());
        m_iSuccessCount = 0;
        m_iFailCount = 0;
    }

    //E/ContactInsert: Error in insertContacts result : length=13; index=30
    public boolean insertContacts(Context context, String sPath) {
        init();
        //int n = 1;
        //System.out.println("insertContacts" + n++);
        try {
            ArrayList<String> arrList = readFile(sPath);        //从文件读取联系人信息存入arrList
            //System.out.println("insertContacts" + n++);

            m_contactArrayList = handleReadStrings(arrList);    //获得联系人信息存入m_contactArrayList

            //System.out.println("insertContacts" + n++);

            for (ContactInfo contact : m_contactArrayList) {
                if (doInsertContact(context, contact)) {
                    m_iSuccessCount++;
                }
            }

        } catch (Exception e) {
            Log.e(m_sTAG, "Error in insertContacts result : " + e.getMessage());
        }
        return true;
    }

    // insert into database
    private boolean doInsertContact(Context context, ContactInfo contactInfo) {
        //Log.d(m_sTAG, "in doInsertIntoContact contactInfo = null? " + (contactInfo == null));
        //int n = 1;
        try {
            //System.out.println("doInsertContact_" + n++);
            ContentValues contentValues = new ContentValues();
            Uri uri = context.getContentResolver().insert(RawContacts.CONTENT_URI, contentValues);
            long rowId = ContentUris.parseId(uri);

            //insert name
            if (contactInfo.displayName != null) {
                contentValues.clear();
                contentValues.put(Data.RAW_CONTACT_ID, rowId);
                contentValues.put(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
                contentValues.put(StructuredName.DISPLAY_NAME, contactInfo.displayName);
                contentValues.put(StructuredName.GIVEN_NAME, contactInfo.lastName);
                contentValues.put(StructuredName.FAMILY_NAME, contactInfo.firstName);
                context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
            }

            if (contactInfo.mobileNum.size() > 0) {
                //insert phone
                for (String s : contactInfo.mobileNum) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    contentValues.put(Phone.NUMBER, s);
                    contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

            if (contactInfo.telNum.size() > 0) {
                //insert phone
                for (String data : contactInfo.telNum) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    contentValues.put(Phone.NUMBER, data);
                    contentValues.put(Phone.TYPE, Phone.TYPE_MOBILE);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

            if (contactInfo.Email.size() > 0) {
                //insert phone
                for (String data : contactInfo.Email) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                    contentValues.put(Email.DATA, data);
                    contentValues.put(Email.TYPE, Email.TYPE_HOME);
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }

            //原文链接：https://blog.csdn.net/hudan2714/article/details/8241334
            //如果是查询IM数据，则需要关心，以下几个字段：
            //Data.RAW_CONTACT_ID表示联系人的ID
            //Data.MIMETYPE：表示mime类型，查im则类型为：Im.CONTENT_ITEM_TYPE
            //Data.DATA1：表示用户填写的数据，如：是QQ类型，则此为QQ号：123456789
            //Data.DATA2：表示数据类型，个人感觉是源码中对应的此类型：（目前手机中读取出来的值都为3）
            //      public static final int TYPE_HOME = 1;
            //      public static final int TYPE_WORK = 2;
            //      public static final int TYPE_OTHER = 3;
            //      data2就是type，是不是都是3啊（用手机测试，也是这个值）。
            //
            //Data.DATA5：在源码中是Im.PROTOCOL：它才表示的是真正的类型，如源码中对应的类型：
            //      public static final int PROTOCOL_CUSTOM = -1;
            //      public static final int PROTOCOL_AIM = 0;
            //      public static final int PROTOCOL_MSN = 1;
            //      public static final int PROTOCOL_YAHOO = 2;
            //      public static final int PROTOCOL_SKYPE = 3;
            //      public static final int PROTOCOL_QQ = 4;
            //      public static final int PROTOCOL_GOOGLE_TALK = 5;
            //      public static final int PROTOCOL_ICQ = 6;
            //      public static final int PROTOCOL_JABBER = 7;
            //      public static final int PROTOCOL_NETMEETING = 8;
            //
            //而当PROTOCOL的取值为-1时，则要取出Data.DATA6的值。
            //Data.DATA6 ：在源码中是Im.CUSTOM_PROTOCOL字段，它表示是用户自定义的值，
            //也就是只有data5为-1时，这个取取出来才不是null（上图能很好的证明了）
            //基本上只要了解这几个字段，就能正确的完成Im数据的操作。

            //所以，对im操作，首先要注意：data2的数据类型。若为3、Im.TYPE_OTHER、自定义类型，就要取label值。
            //                              自定义的名称为Label，存在Data.data3字段中。
            //                            接着要注意：data5的值，它能判断出来是哪种Im.

            if (contactInfo.Im.size() > 0) {
                //insert phone
                for (String data : contactInfo.Im) {
                    contentValues.clear();
                    contentValues.put(Data.RAW_CONTACT_ID, rowId);
                    contentValues.put(Data.MIMETYPE, Im.CONTENT_ITEM_TYPE);
                    contentValues.put(Im.DATA1, data);          //用户填写的Im数据
                    contentValues.put(Im.DATA2, Im.TYPE_OTHER); //Im数据类型（目前手机中读取出来的值都为3，Im.TYPE_OTHER，自定义类型）
                    //contentValues.put(Im.DATA3, Im.PROTOCOL_QQ);//Im真正的类型，对应在源码中的Im.PROTOCOL
                    contentValues.put(Im.DATA5, Im.PROTOCOL_QQ);//Im真正的类型，对应在源码中的Im.PROTOCOL
                    context.getContentResolver().insert(ContactsContract.Data.CONTENT_URI, contentValues);
                }
            }
            //System.out.println("doInsertContact_" + n++);

        } catch (Exception e) {
            return false;
        }
        //System.out.println("doInsertContact_" + n++);
        return true;
    }

    private void aryList2json(ArrayList<String> arrayList) {
        for (int i = 0; i < arrayList.size(); i++) {
            String str = arrayList.get(i);
            String[] sArr = str.split(",", -1); //添加后面的参数，保证结尾空字符串不会被丢弃。

            if (0 == i) {
                m_sHead = sArr;    //得到表头存入 m_sHead
                continue;
            }




            ContactInfo contactInfo = new ContactInfo();
            for (int j = 0; j < m_sHead.length; j++) {
                if (m_sHead[j].equals("displayName")) {
                    contactInfo.displayName = sArr[j];
                } else if (m_sHead[j].equals("lastName")) {
                    contactInfo.lastName = sArr[j];
                } else if (m_sHead[j].equals("firstName")) {
                    contactInfo.firstName = sArr[j];
                } else if ((m_sHead[j].indexOf("mobileEmail") == -1) && (m_sHead[j].indexOf("mobile") != -1)) {
                    if (!TextUtils.isEmpty(sArr[j])) {
                        contactInfo.mobileNum.add(sArr[j]);
                    }
                } else if (m_sHead[j].indexOf("Email") != -1) {
                    if (!TextUtils.isEmpty(sArr[j])) {
                        contactInfo.Email.add(sArr[j]);
                    }
                } else if (m_sHead[j].indexOf("Im") != -1) {
                    if (!TextUtils.isEmpty(sArr[j])) {
                        contactInfo.Im.add(sArr[j]);
                    }
                }
            }

            //contactArrayList.add(contactInfo);
            //System.out.println("handleReadStrings_" + n++);
        }
        //System.out.println("handleReadStrings_" + n++);
    }

    //java String split 使用注意点和问题
    //当字符串只包含分隔符时，返回数组没有元素；
    //当字符串不包含分隔符时，返回数组只包含一个元素（该字符串本身）；
    //字符串最尾部出现的分隔符可以看成不存在，不影响字符串的分隔；
    //字符串最前端出现的分隔符将分隔出一个空字符串以及剩下的部分的正常分隔；
    //不知道这么做的原因是什么，所以在使用split()中需要注意这些问题，解决方法其实也挺简单的，变通下即可。
    //————————————————
    //版权声明：本文为CSDN博主「好奇怪的花」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
    //原文链接：https://blog.csdn.net/qq_43680117/article/details/86703123

    //String的split()的两种方法
    //public String[] split(String regex)
    //该方法就是给定的表达式和限制参数0来调用两参数split方法。
    //
    //public String[] split(String regex,int limit)
    //规定：使用regex进行字符串切割称为模式匹配
    //
    //参数regex：
    //1.如果表达式不匹配输入的任何内容，返回的数组只具有一个元素，即此字符串。（尤其注意空字符串这种情况，他也是一个字符串）
    //2.可以匹配的情况下，每一个字符串都由另一个匹配给定表达式的子字符串终止，或者由此字符串末尾终止（数组中的字符串按照他们在此字符串出现的顺序排列）
    //
    //参数：limit：
    //该参数用于控制模式匹配使用的次数，可以影响到数组的长度
    //1.limit>0:
    //模式匹配将被最多应用n-1次，数组的长度将不会大于n，数组的最后一项将包含所有超出最后匹配的定界符的输入。
    //2.limit<0:
    //模式匹配将应用尽可能多的次数，而且数组的长度是任何长度。
    //3.lilmit=0:
    //模式匹配将被应用尽可能多的次数，数组可以是任何长度，并且结尾空字符串将被丢弃。
    //————————————————
    //版权声明：本文为CSDN博主「billwatson」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
    //原文链接：https://blog.csdn.net/u013006139/article/details/51747148


    private ArrayList<ContactInfo> handleReadStrings(ArrayList<String> arrayList) {
        ArrayList<ContactInfo> contactArrayList = new ArrayList<ContactInfo>();
//        int n = 1;
//        System.out.println("handleReadStrings_" + n++);
//        System.out.println("arrayList.size() = " + arrayList.size());

        for (int i = 0; i < arrayList.size(); i++) {
            String str = arrayList.get(i);
            String[] sArr = str.split(",", -1); //添加后面的参数，保证结尾空字符串不会被丢弃。

//            System.out.println("str_" + i + " = " + str);
//            System.out.println("sArr.size() = " + sArr.length);
            if (0 == i) {
                m_sHead = sArr;    //得到表头存入 m_sHead
                continue;
            }

            ContactInfo contactInfo = new ContactInfo();
            for (int j = 0; j < m_sHead.length; j++) {
                if (m_sHead[j].equals("displayName")) {
                    contactInfo.displayName = sArr[j];
                } else if (m_sHead[j].equals("lastName")) {
                    contactInfo.lastName = sArr[j];
                } else if (m_sHead[j].equals("firstName")) {
                    contactInfo.firstName = sArr[j];
                } else if ((m_sHead[j].indexOf("mobileEmail") == -1) && (m_sHead[j].indexOf("mobile") != -1)) {
                    if (!TextUtils.isEmpty(sArr[j])) {
                        contactInfo.mobileNum.add(sArr[j]);
                    }
                } else if (m_sHead[j].indexOf("Email") != -1) {
                    if (!TextUtils.isEmpty(sArr[j])) {
                        contactInfo.Email.add(sArr[j]);
                    }
                } else if (m_sHead[j].indexOf("Im") != -1) {
                    if (!TextUtils.isEmpty(sArr[j])) {
                        contactInfo.Im.add(sArr[j]);
                    }
                }
            }

            contactArrayList.add(contactInfo);
            //System.out.println("handleReadStrings_" + n++);
        }
        //System.out.println("handleReadStrings_" + n++);
        return contactArrayList;
    }

    //sPath 是文件的绝对路径
    public ArrayList<String> readFile(String sPath) {
        ArrayList<String> arrList = new ArrayList<String>();
        File file = new File(sPath); //注意：必须提供文件绝对路径
        String sCodeFormat = com.example.OctopusMessage.FileEncodeUtil.getFileEncode(file.getAbsolutePath(), false);

        BufferedReader reader = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, sCodeFormat));

            String strTemp = "";
            //一次读入一行，直到读入null为文件结束
            while ((strTemp = reader.readLine()) != null) {
                strTemp = strTemp.replace("，", ",");    //将所有中文逗号全部替换为英文逗号
                arrList.add(strTemp);
                //System.out.println(strTemp);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        return arrList;
    }

    public int getSuccessCount() {
        return m_iSuccessCount;
    }

    public int getFailCount() {
        return m_iFailCount;
    }
}
