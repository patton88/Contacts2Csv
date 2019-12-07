//获取通讯录所有字段
//2012年01月02日 14:48:01 yemh111 阅读数：1368
package com.example.contacts2csv;
// 编译error: unmappable character for encoding UTF-8。
// 解决办法-OK，用文本编辑器打开文件，将文件另存为UTF-8格式
// 有警告，没关系：uses unchecked or unsafe operations. Recompile with -Xlint:unchecked for details.

import java.util.Iterator;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.Data;
import android.util.Pair;

//import cn.ihope.mozyhome.domain.Contacts;

public class GetContactsInfo {
    public int mIntSum;
    private Context mContext;
    private JSONObject mContactData;
    private JSONObject mJSONObject;
    private JSONObject mRelustJSON;

//    private Map<String, String> mMapCol;            //联系人信息列表头
//    private Vector<Pair<String, String>> mVecCol;   //联系人信息列表头，Map会自动排序，用Vector也不合理
    private JSONObject mJsonCol;                //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject

    private JSONObject mJsonG00StructName;		//mJsonG00StructName，获得通讯录中联系人的名字
    private JSONObject mJsonG01Phone;			//mJsonG01Phone，获取电话信息
    private JSONObject mJsonG02Email;			//mJsonG02Email，查找Email地址
    private JSONObject mJsonG03Event;			//mJsonG03Event，查找Event地址
    private JSONObject mJsonG04Im;				//mJsonG04Im，即时消息
    private JSONObject mJsonG05Remark;			//mJsonG05Remark，获取备注信息
    private JSONObject mJsonG06NickName;		//mJsonG06NickName，获取昵称信息
    private JSONObject mJsonG07OrgType;			//mJsonG07OrgType，获取组织信息
    private JSONObject mJsonG07_00WorkOrgType;  //mJsonG07_00WorkOrgType，单位组织信息
    private JSONObject mJsonG07_01OtherOrgType;	//mJsonG07_01OtherOrgType，其他组织信息
    private JSONObject mJsonG08WebType;			//mJsonG08WebType，获取网站信息
    private JSONObject mJsonG09PostalType;		//mJsonG09PostalType，查找通讯地址
    private JSONObject mJsonG09_00WorkPostal;	//mJsonG09_00WorkPostal，单位通讯地址
    private JSONObject mJsonG09_01HomePostal;	//mJsonG09_01HomePostal，住宅通讯地址
    private JSONObject mJsonG09_02OtherPostal;	//mJsonG09_02OtherPostal，其他通讯地址

    public GetContactsInfo(Context context) {
        this.mContext = context;
    }

    /*
    JsonObject数据排序（顺序）问题    2018年01月22日 10:18:21 中二涛 阅读数：14043
    JsonObject内部是用Hashmap来存储的，所以输出是按key的排序来的，如果要让JsonObject按固定顺序（put的顺序）排列，
    可以修改JsonObject的定义HashMap改为LinkedHashMap。
    public JSONObject() {
            this.map = new LinkedHashMap();  //new HashMap();
    }
    即定义JsonObject可以这样：JSONObject jsonObj = new JSONObject(new LinkedHashMap());
    或者 JSONObject jsonObj = new JSONObject(true);
    */

    public String getContactInfo() throws JSONException {

        // ------------------- 联系人信息列表头 - Begin
        mJsonCol = new JSONObject(new LinkedHashMap());         //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject

        //mJsonG00StructName，获得通讯录中联系人的名字
        mJsonG00StructName = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG00StructName", mJsonG00StructName);
        mJsonG00StructName.put("displayName", StructuredName.DISPLAY_NAME);		            //DISPLAY_NAME = "data1";
        mJsonG00StructName.put("lastName", StructuredName.GIVEN_NAME);			            //GIVEN_NAME = "data2";
        mJsonG00StructName.put("firstName", StructuredName.FAMILY_NAME);                    //FAMILY_NAME = "data3";
        mJsonG00StructName.put("prefix", StructuredName.PREFIX);				            //PREFIX = "data4";
        mJsonG00StructName.put("middleName", StructuredName.MIDDLE_NAME);                   //MIDDLE_NAME = "data5";
        mJsonG00StructName.put("suffix", StructuredName.SUFFIX);		                    //SUFFIX = "data6";
        mJsonG00StructName.put("phoneticLastName", StructuredName.PHONETIC_GIVEN_NAME);		//PHONETIC_GIVEN_NAME = "data7";
        mJsonG00StructName.put("phoneticFirstName", StructuredName.PHONETIC_FAMILY_NAME);	//PHONETIC_MIDDLE_NAME = "data8";
        mJsonG00StructName.put("phoneticMiddleName", StructuredName.PHONETIC_MIDDLE_NAME);	//PHONETIC_FAMILY_NAME = "data9";

        //mJsonG01Phone，获取电话信息，共20项
        mJsonG01Phone = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG01Phone", mJsonG01Phone);
        mJsonG01Phone.put("homeNum", Phone.NUMBER);			//TYPE_HOME = 1;
        mJsonG01Phone.put("mobile", Phone.NUMBER);          //TYPE_MOBILE = 2;
        mJsonG01Phone.put("workNum", Phone.NUMBER);         //TYPE_WORK = 3;
        mJsonG01Phone.put("workFax", Phone.NUMBER);         //TYPE_FAX_WORK = 4;
        mJsonG01Phone.put("homeFax", Phone.NUMBER);         //TYPE_FAX_HOME = 5;
        mJsonG01Phone.put("pager", Phone.NUMBER);           //TYPE_PAGER = 6;
        mJsonG01Phone.put("otherNum", Phone.NUMBER);        //TYPE_OTHER = 7;
        mJsonG01Phone.put("callbackNum", Phone.NUMBER);     //TYPE_CALLBACK = 8;
        mJsonG01Phone.put("carNum", Phone.NUMBER);          //TYPE_CAR = 9;
        mJsonG01Phone.put("compMainTel", Phone.NUMBER);     //TYPE_COMPANY_MAIN = 10;
        mJsonG01Phone.put("isdn", Phone.NUMBER);            //TYPE_ISDN = 11;
        mJsonG01Phone.put("mainTel", Phone.NUMBER);         //TYPE_MAIN = 12;
        mJsonG01Phone.put("otherFax", Phone.NUMBER);        //TYPE_OTHER_FAX = 13;
        mJsonG01Phone.put("wirelessDev", Phone.NUMBER);     //TYPE_RADIO = 14;
        mJsonG01Phone.put("telegram", Phone.NUMBER);        //TYPE_TELEX = 15;
        mJsonG01Phone.put("tty_tdd", Phone.NUMBER);         //TYPE_TTY_TDD = 16;
        mJsonG01Phone.put("workMobile", Phone.NUMBER);      //TYPE_WORK_MOBILE = 17;
        mJsonG01Phone.put("workPager", Phone.NUMBER);       //TYPE_WORK_PAGER = 18;
        mJsonG01Phone.put("assistantNum", Phone.NUMBER);    //TYPE_ASSISTANT = 19;
        mJsonG01Phone.put("mms", Phone.NUMBER);             //TYPE_MMS = 20;


        //mJsonG02Email，查找Email地址
        mJsonG02Email = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG02Email", mJsonG02Email);
        mJsonG02Email.put("homeEmail", Email.DATA);         //TYPE_HOME = 1;
        mJsonG02Email.put("workEmail", Email.DATA);         //TYPE_WORK = 2;
        mJsonG02Email.put("otherEmail", Email.DATA);        //TYPE_OTHER = 3;
        mJsonG02Email.put("mobileEmail", Email.DATA);       //TYPE_MOBILE = 4;

        //mJsonG03Event，查找Event地址
        mJsonG03Event = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG03Event", mJsonG03Event);
        mJsonG03Event.put("anniversary", Event.START_DATE); //TYPE_ANNIVERSARY = 1;
        mJsonG03Event.put("otherday", Event.START_DATE);    //TYPE_OTHER = 2;
        mJsonG03Event.put("birthday", Event.START_DATE);    //TYPE_BIRTHDAY = 3;

        //mJsonG04Im，即时消息
        mJsonG04Im = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG04Im", mJsonG04Im);
        mJsonG04Im.put("homeMsg", Im.DATA);         //TYPE_HOME = 1;
        mJsonG04Im.put("workMsg", Im.DATA);         //TYPE_WORK = 2;
        mJsonG04Im.put("otherMsg", Im.DATA);        //TYPE_OTHER = 3;
        mJsonG04Im.put("customIm", Im.DATA);        //PROTOCOL_CUSTOM = -1;
        mJsonG04Im.put("aimIm", Im.DATA);           //PROTOCOL_AIM = 0;
        mJsonG04Im.put("msnIm", Im.DATA);           //PROTOCOL_MSN = 1;
        mJsonG04Im.put("yahooIm", Im.DATA);         //PROTOCOL_YAHOO = 2;
        mJsonG04Im.put("skypeIm", Im.DATA);         //PROTOCOL_SKYPE = 3;
        mJsonG04Im.put("qqIm", Im.DATA);            //PROTOCOL_QQ = 4;
        mJsonG04Im.put("googleTalkIm", Im.DATA);    //PROTOCOL_GOOGLE_TALK = 5;
        mJsonG04Im.put("icqIm", Im.DATA);           //PROTOCOL_ICQ = 6;
        mJsonG04Im.put("jabberIm", Im.DATA);        //PROTOCOL_JABBER = 7;
        mJsonG04Im.put("netmeetingIm", Im.DATA);    //PROTOCOL_NETMEETING = 8;

        //mJsonG05Remark，获取备注信息
        mJsonG05Remark = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG05Remark", mJsonG05Remark);
        mJsonG05Remark.put("remark", Note.NOTE);

        //mJsonG06NickName，获取昵称信息
        mJsonG06NickName = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG06NickName", mJsonG06NickName);
        mJsonG06NickName.put("defaultNickName", Nickname.NAME);     //TYPE_DEFAULT = 1;
        mJsonG06NickName.put("otherNickName", Nickname.NAME);       //TYPE_OTHER_NAME = 2;
        mJsonG06NickName.put("maindenNickName", Nickname.NAME);     //TYPE_MAINDEN_NAME = 3;
        mJsonG06NickName.put("shortNickName", Nickname.NAME);       //TYPE_SHORT_NAME = 4;
        mJsonG06NickName.put("initialsNickName", Nickname.NAME);    //TYPE_INITIALS = 5;

        //mJsonG07OrgType，获取组织信息
        mJsonG07OrgType = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG07OrgType", mJsonG07OrgType);

        //mJsonG07_00WorkOrgType，单位组织信息，TYPE_WORK = 1;
        mJsonG07_00WorkOrgType = new JSONObject(new LinkedHashMap());
        mJsonG07OrgType.put("mJsonG07_00WorkOrgType", mJsonG07_00WorkOrgType);
        mJsonG07_00WorkOrgType.put("workCompany", Organization.COMPANY);               //COMPANY = "data1";
        mJsonG07_00WorkOrgType.put("workJobTitle", Organization.TITLE);                //TITLE = "data4";
        mJsonG07_00WorkOrgType.put("workDepartment", Organization.DEPARTMENT);         //DEPARTMENT = "data5";
        mJsonG07_00WorkOrgType.put("workJobDescription", Organization.DEPARTMENT);     //JOB_DESCRIPTION = "data6";
        mJsonG07_00WorkOrgType.put("workSymbol", Organization.DEPARTMENT);             //SYMBOL = "data7";
        mJsonG07_00WorkOrgType.put("workPhoneticName", Organization.DEPARTMENT);       //PHONETIC_NAME = "data8";
        mJsonG07_00WorkOrgType.put("workOfficeLocation", Organization.DEPARTMENT);     //OFFICE_LOCATION = "data9";

        //mJsonG07_01OtherOrgType，其他组织信息，TYPE_OTHER = 2;
        mJsonG07_01OtherOrgType = new JSONObject(new LinkedHashMap());
        mJsonG07OrgType.put("mJsonG07_01OtherOrgType", mJsonG07_01OtherOrgType);
        mJsonG07_01OtherOrgType.put("otherCompany", Organization.COMPANY);              //COMPANY = "data1";
        mJsonG07_01OtherOrgType.put("otherJobTitle", Organization.TITLE);               //TITLE = "data4";
        mJsonG07_01OtherOrgType.put("otherDepartment", Organization.DEPARTMENT);        //DEPARTMENT = "data5";
        mJsonG07_01OtherOrgType.put("otherJobDescription", Organization.DEPARTMENT);    //JOB_DESCRIPTION = "data6";
        mJsonG07_01OtherOrgType.put("otherSymbol", Organization.DEPARTMENT);            //SYMBOL = "data7";
        mJsonG07_01OtherOrgType.put("otherPhoneticName", Organization.DEPARTMENT);      //PHONETIC_NAME = "data8";
        mJsonG07_01OtherOrgType.put("otherOfficeLocation", Organization.DEPARTMENT);    //OFFICE_LOCATION = "data9";

        //mJsonG08WebType，获取网站信息
        mJsonG08WebType = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG08WebType", mJsonG08WebType);
        mJsonG08WebType.put("homepage", Website.URL);           //TYPE_HOMEPAGE = 1;
        mJsonG08WebType.put("blog", Website.URL);               //TYPE_BLOG = 2;
        mJsonG08WebType.put("profile", Website.URL);            //TYPE_PROFILE = 3;
        mJsonG08WebType.put("home", Website.URL);               //TYPE_HOME = 4;
        mJsonG08WebType.put("workPage", Website.URL);           //TYPE_WORK = 5;
        mJsonG08WebType.put("ftpPage", Website.URL);            //TYPE_FTP = 6;
        mJsonG08WebType.put("otherPage", Website.URL);          //TYPE_OTHER = 7;

        //mJsonG09PostalType，查找通讯地址
        mJsonG09PostalType = new JSONObject(new LinkedHashMap());
        mJsonCol.put("mJsonG09PostalType", mJsonG09PostalType);

        //mJsonG09_00WorkPostal，单位通讯地址，TYPE_HOME = 1;
        mJsonG09_00WorkPostal = new JSONObject(new LinkedHashMap());
        mJsonG09PostalType.put("mJsonG09_00WorkPostal", mJsonG09_00WorkPostal);
        mJsonG09_00WorkPostal.put("workFormattedAddress", StructuredPostal.FORMATTED_ADDRESS);  //FORMATTED_ADDRESS = "data1";
        mJsonG09_00WorkPostal.put("workStreet", StructuredPostal.STREET);                       //STREET = "data4";
        mJsonG09_00WorkPostal.put("workBox", StructuredPostal.POBOX);                           //POBOX = "data5";
        mJsonG09_00WorkPostal.put("workArea", StructuredPostal.NEIGHBORHOOD);                   //NEIGHBORHOOD = "data6";
        mJsonG09_00WorkPostal.put("workCiry", StructuredPostal.CITY);                           //CITY = "data7";
        mJsonG09_00WorkPostal.put("workState", StructuredPostal.REGION);                        //REGION = "data8";
        mJsonG09_00WorkPostal.put("workZip", StructuredPostal.POSTCODE);                        //POSTCODE = "data9";
        mJsonG09_00WorkPostal.put("workCountry", StructuredPostal.COUNTRY);                     //COUNTRY = "data10";

        //mJsonG09_01HomePostal，住宅通讯地址，TYPE_WORK = 2;
        mJsonG09_01HomePostal = new JSONObject(new LinkedHashMap());
        mJsonG09PostalType.put("mJsonG09_01HomePostal", mJsonG09_01HomePostal);
        mJsonG09_00WorkPostal.put("homeFormattedAddress", StructuredPostal.FORMATTED_ADDRESS);  //FORMATTED_ADDRESS = "data1";
        mJsonG09_00WorkPostal.put("homeStreet", StructuredPostal.STREET);                       //STREET = "data4";
        mJsonG09_00WorkPostal.put("homeBox", StructuredPostal.POBOX);                           //POBOX = "data5";
        mJsonG09_00WorkPostal.put("homeArea", StructuredPostal.NEIGHBORHOOD);                   //NEIGHBORHOOD = "data6";
        mJsonG09_00WorkPostal.put("homeCiry", StructuredPostal.CITY);                           //CITY = "data7";
        mJsonG09_00WorkPostal.put("homeState", StructuredPostal.REGION);                        //REGION = "data8";
        mJsonG09_00WorkPostal.put("homeZip", StructuredPostal.POSTCODE);                        //POSTCODE = "data9";
        mJsonG09_00WorkPostal.put("homeCountry", StructuredPostal.COUNTRY);                     //COUNTRY = "data10";

        //mJsonG09_02OtherPostal，其他通讯地址，TYPE_OTHER = 3;
        mJsonG09_02OtherPostal = new JSONObject(new LinkedHashMap());
        mJsonG09PostalType.put("mJsonG09_02OtherPostal", mJsonG09_02OtherPostal);
        mJsonG09_00WorkPostal.put("otherFormattedAddress", StructuredPostal.FORMATTED_ADDRESS);  //FORMATTED_ADDRESS = "data1";
        mJsonG09_00WorkPostal.put("otherStreet", StructuredPostal.STREET);                       //STREET = "data4";
        mJsonG09_00WorkPostal.put("otherBox", StructuredPostal.POBOX);                           //POBOX = "data5";
        mJsonG09_00WorkPostal.put("otherArea", StructuredPostal.NEIGHBORHOOD);                   //NEIGHBORHOOD = "data6";
        mJsonG09_00WorkPostal.put("otherCiry", StructuredPostal.CITY);                           //CITY = "data7";
        mJsonG09_00WorkPostal.put("otherState", StructuredPostal.REGION);                        //REGION = "data8";
        mJsonG09_00WorkPostal.put("otherZip", StructuredPostal.POSTCODE);                        //POSTCODE = "data9";
        mJsonG09_00WorkPostal.put("otherCountry", StructuredPostal.COUNTRY);                     //COUNTRY = "data10";
        // ------------------- 联系人信息列表头 - End

        // 获得通讯录信息 ，URI是ContactsContract.Contacts.CONTENT_URI
        mContactData = new JSONObject(new LinkedHashMap());  //解决JsonObject数据固定顺序
        String mimetype = "";
        int oldrid = -1;
        int contactId = -1;
        Cursor cursor = mContext.getContentResolver().query(Data.CONTENT_URI, null, null, null, Data.RAW_CONTACT_ID);
        mIntSum = 0;
        while (cursor.moveToNext()) {
            contactId = cursor.getInt(cursor.getColumnIndex(Data.RAW_CONTACT_ID));
            if (oldrid != contactId) {
                mJSONObject = new JSONObject(new LinkedHashMap());
                mRelustJSON = new JSONObject(new LinkedHashMap());

                //这里放入的jsonObject是一个对象(引用或指针)，放了之后还可以进行操作
                mContactData.put("contact" + mIntSum, mRelustJSON);
                mIntSum++;
                oldrid = contactId;
            }

            // 取得mimetype类型
            mimetype = cursor.getString(cursor.getColumnIndex(Data.MIMETYPE));
            // 获得通讯录中每个联系人的ID

            //mJsonG00StructName，获得通讯录中联系人的名字
            if (StructuredName.CONTENT_ITEM_TYPE.equals(mimetype)) {
                Iterator<String> it = mJsonCol.getJSONObject("mJsonG00StructName").keys();
                while (it.hasNext()) {
                    String key = it.next();
                    try {
                        String str = mJsonCol.getJSONObject("mJsonG00StructName").getString(key);
                        mJSONObject.put(key, cursor.getString(cursor.getColumnIndex(str)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            //mJsonG01Phone，获取电话信息，共20项
            if (Phone.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出电话类型
                int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));

                // 1、住宅电话
                if (phoneType == Phone.TYPE_HOME) {
                    String homeNum = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("homeNum")));
                    homeNum = funRemove(homeNum);
                    //System.out.println(homeNum);
                    mJSONObject.put("homeNum", homeNum);
                    //System.out.println("homeNum" + mJSONObject.getString("homeNum"));
                }

                // 2、手机
                if (phoneType == Phone.TYPE_MOBILE) {
                    String mobile = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("mobile")));
                    mobile = funRemove(mobile);
                    mJSONObject.put("mobile", mobile);
                    //System.out.println("mobile" + mJSONObject.getString("mobile"));
                }

                // 3、单位电话
                if (phoneType == Phone.TYPE_WORK) {
                    String jobNum = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("workNum")));
                    jobNum = funRemove(jobNum);
                    mJSONObject.put("workNum", jobNum);
                }

                // 4、单位传真
                if (phoneType == Phone.TYPE_FAX_WORK) {
                    String workFax = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("workFax")));
                    mJSONObject.put("workFax", workFax);
                }

                // 5、住宅传真
                if (phoneType == Phone.TYPE_FAX_HOME) {
                    String homeFax = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("homeFax")));
                    mJSONObject.put("homeFax", homeFax);
                }

                // 6、寻呼机
                if (phoneType == Phone.TYPE_PAGER) {
                    String pager = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("pager")));
                    mJSONObject.put("pager", pager);
                }

                // 7、Other电话
                if (phoneType == Phone.TYPE_OTHER) {
                    String otherNum = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("otherNum")));
                    otherNum = funRemove(otherNum);
                    //System.out.println(otherNum);
                    mJSONObject.put("otherNum", otherNum);
                    //System.out.println("otherNum" + mJSONObject.getString("otherNum"));
                }

                // 8、回拨号码
                if (phoneType == Phone.TYPE_CALLBACK) {
                    String quickNum = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("callbackNum")));
                    mJSONObject.put("callbackNum", quickNum);
                }

                // 9、车载电话
                if (phoneType == Phone.TYPE_CAR) {
                    String carNum = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("carNum")));
                    mJSONObject.put("carNum", carNum);
                }

                // 10、公司总机
                if (phoneType == Phone.TYPE_COMPANY_MAIN) {
                    String jobTel = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("compMainTel")));
                    mJSONObject.put("compMainTel", jobTel);
                }

                // 11、ISDN
                if (phoneType == Phone.TYPE_ISDN) {
                    String isdn = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("isdn")));
                    mJSONObject.put("isdn", isdn);
                }

                // 12、总机
                if (phoneType == Phone.TYPE_MAIN) {
                    String tel = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("mainTel")));
                    mJSONObject.put("mainTel", tel);
                }

                // 13、Other传真
                if (phoneType == Phone.TYPE_OTHER_FAX ) {
                    String homeFax = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("otherFax")));
                    mJSONObject.put("otherFax", homeFax);
                }

                // 14、无线装置
                if (phoneType == Phone.TYPE_RADIO) {
                    String wirelessDev = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("wirelessDev")));
                    mJSONObject.put("wirelessDev", wirelessDev);
                }

                // 15、电报
                if (phoneType == Phone.TYPE_TELEX) {
                    String telegram = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("telegram")));
                    mJSONObject.put("telegram", telegram);
                }

                // 16、TTY_TDD
                if (phoneType == Phone.TYPE_TTY_TDD) {
                    String tty_tdd = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("tty_tdd")));
                    mJSONObject.put("tty_tdd", tty_tdd);
                }

                // 17、单位手机
                if (phoneType == Phone.TYPE_WORK_MOBILE) {
                    String jobMobile = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("workMobile")));
                    jobMobile = funRemove(jobMobile);
                    mJSONObject.put("workMobile", jobMobile);
                }

                // 18、单位寻呼机
                if (phoneType == Phone.TYPE_WORK_PAGER) {
                    String jobPager = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("workPager")));
                    mJSONObject.put("workPager", jobPager);
                }

                // 19、助理
                if (phoneType == Phone.TYPE_ASSISTANT) {
                    String assistantNum = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("assistantNum")));
                    mJSONObject.put("assistantNum", assistantNum);
                }

                // 20、彩信
                if (phoneType == Phone.TYPE_MMS) {
                    String mms = cursor.getString(cursor.getColumnIndex(mJsonG01Phone.getString("mms")));
                    mJSONObject.put("mms", mms);
                }
            }

            //mJsonG02Email，查找Email地址
            if (Email.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出邮件类型
                int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));

                if (emailType == Email.TYPE_HOME) {         // 住宅邮件地址，TYPE_HOME = 1;
                    String homeEmail = cursor.getString(cursor.getColumnIndex(mJsonG02Email.getString("homeEmail")));
                    mJSONObject.put("homeEmail", homeEmail);
                }
                else if (emailType == Email.TYPE_WORK) {    // 单位邮件地址，TYPE_WORK = 2;
                    String jobEmail = cursor.getString(cursor.getColumnIndex(mJsonG02Email.getString("workEmail")));
                    mJSONObject.put("workEmail", jobEmail);
                }
                else if (emailType == Email.TYPE_OTHER) {    // 单位邮件地址，TYPE_OTHER = 3;
                    String jobEmail = cursor.getString(cursor.getColumnIndex(mJsonG02Email.getString("otherEmail")));
                    mJSONObject.put("otherEmail", jobEmail);
                }
                else if (emailType == Email.TYPE_MOBILE) {  // 手机邮件地址，TYPE_MOBILE = 4;
                    String mobileEmail = cursor.getString(cursor.getColumnIndex(mJsonG02Email.getString("mobileEmail")));
                    mJSONObject.put("mobileEmail", mobileEmail);
                }
            }

            //mJsonG03Event，查找Event地址
            if (Event.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出时间类型
                int eventType = cursor.getInt(cursor.getColumnIndex(Event.TYPE));

                // 周年纪念日，TYPE_ANNIVERSARY = 1;
                if (eventType == Event.TYPE_ANNIVERSARY) {
                    String anniversary = cursor.getString(cursor.getColumnIndex(mJsonG03Event.getString("anniversary")));
                    mJSONObject.put("anniversary", anniversary);
                }
                // 其他日子，TYPE_OTHER = 2;
                else if (eventType == Event.TYPE_OTHER) {
                    String otherday = cursor.getString(cursor.getColumnIndex(mJsonG03Event.getString("otherday")));
                    mJSONObject.put("otherday", otherday);
                }
                // 生日，TYPE_BIRTHDAY = 3;
                else if (eventType == Event.TYPE_BIRTHDAY) {
                    String birthday = cursor.getString(cursor.getColumnIndex(mJsonG03Event.getString("birthday")));
                    mJSONObject.put("birthday", birthday);
                }
            }

            //mJsonG04Im，即时消息
            if (Im.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出即时消息类型
                int protocal = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));

                if (Im.TYPE_HOME == protocal) {
                    String homeMsg = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("homeMsg")));
                    mJSONObject.put("homeMsg", homeMsg);
                }
                else if (Im.TYPE_WORK == protocal) {
                    String workMsg = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("workMsg")));
                    mJSONObject.put("workMsg", workMsg);
                }
                else if (Im.TYPE_OTHER == protocal) {
                    String otherMsg = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("otherMsg")));
                    mJSONObject.put("otherMsg", otherMsg);
                }
                else if (Im.PROTOCOL_CUSTOM == protocal) {
                    String customIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("customIm")));
                    mJSONObject.put("customIm", customIm);
                }
                else if (Im.PROTOCOL_AIM == protocal) {
                    String aimIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("aimIm")));
                    mJSONObject.put("aimIm", aimIm);
                }
                else if (Im.PROTOCOL_MSN == protocal) {
                    String msnIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("msnIm")));
                    mJSONObject.put("msnIm", msnIm);
                }
                else if (Im.PROTOCOL_YAHOO == protocal) {
                    String yahooIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("yahooIm")));
                    mJSONObject.put("yahooIm", yahooIm);
                }
                else if (Im.PROTOCOL_SKYPE == protocal) {
                    String skypeIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("skypeIm")));
                    mJSONObject.put("skypeIm", skypeIm);
                }
                else if (Im.PROTOCOL_QQ == protocal) {
                    String qqIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("qqIm")));
                    mJSONObject.put("qqIm", qqIm);
                }
                else if (Im.PROTOCOL_GOOGLE_TALK == protocal) {
                    String googleTalkIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("googleTalkIm")));
                    mJSONObject.put("googleTalkIm", googleTalkIm);
                }
                else if (Im.PROTOCOL_ICQ == protocal) {
                    String icqIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("icqIm")));
                    mJSONObject.put("icqIm", icqIm);
                }
                else if (Im.PROTOCOL_JABBER == protocal) {
                    String jabberIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("jabberIm")));
                    mJSONObject.put("jabberIm", jabberIm);
                }
                else if (Im.PROTOCOL_NETMEETING == protocal) {
                    String netmeetingIm = cursor.getString(cursor.getColumnIndex(mJsonG04Im.getString("netmeetingIm")));
                    mJSONObject.put("netmeetingIm", netmeetingIm);
                }
            }

            //mJsonG05Remark，获取备注信息
            if (Note.CONTENT_ITEM_TYPE.equals(mimetype)) {
                String remark = cursor.getString(cursor.getColumnIndex(mJsonG05Remark.getString("remark")));
                mJSONObject.put("remark", remark);
            }

            //mJsonG06NickName，获取昵称信息
            if (Nickname.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出昵称类型
                int nickType = cursor.getInt(cursor.getColumnIndex(Nickname.NAME));

                if (Nickname.TYPE_DEFAULT == nickType) {
                    String defaultNickName = cursor.getString(cursor.getColumnIndex(mJsonG06NickName.getString("defaultNickName")));
                    mJSONObject.put("defaultNickName", defaultNickName);
                }
                else if (Nickname.TYPE_OTHER_NAME == nickType) {
                    String otherNickName = cursor.getString(cursor.getColumnIndex(mJsonG06NickName.getString("otherNickName")));
                    mJSONObject.put("otherNickName", otherNickName);
                }
                else if (Nickname.TYPE_MAINDEN_NAME == nickType) {
                    String maindenNickName = cursor.getString(cursor.getColumnIndex(mJsonG06NickName.getString("maindenNickName")));
                    mJSONObject.put("maindenNickName", maindenNickName);
                }
                else if (Nickname.TYPE_SHORT_NAME == nickType) {
                    String shortNickName = cursor.getString(cursor.getColumnIndex(mJsonG06NickName.getString("shortNickName")));
                    mJSONObject.put("shortNickName", shortNickName);
                }
                else if (Nickname.TYPE_INITIALS == nickType) {
                    String initialsNickName = cursor.getString(cursor.getColumnIndex(mJsonG06NickName.getString("initialsNickName")));
                    mJSONObject.put("initialsNickName", initialsNickName);
                }
            }

            //mJsonG07OrgType，获取组织信息
            if (Organization.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出组织类型
                int orgType = cursor.getInt(cursor.getColumnIndex(Organization.TYPE));

                //mJsonG07_00WorkOrgType，单位组织信息，TYPE_WORK = 1;
                if (orgType == Organization.TYPE_WORK) {
//                    Iterator<String> it = mJsonCol.getJSONObject("mJsonG07_00WorkOrgType").keys();
//                    while (it.hasNext()) {
//                        String key = it.next();
//                        try {
//                            String str = mJsonCol.getJSONObject("mJsonG07_00WorkOrgType").getString(key);
//                            mJSONObject.put(key, cursor.getString(cursor.getColumnIndex(str)));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }
                //mJsonG07_01OtherOrgType，其他组织信息，TYPE_OTHER = 2;
                else if (orgType == Organization.TYPE_WORK) {
                    Iterator<String> it = mJsonCol.getJSONObject("mJsonG07_01OtherOrgType").keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        try {
                            String str = mJsonCol.getJSONObject("mJsonG07_01OtherOrgType").getString(key);
                            mJSONObject.put(key, cursor.getString(cursor.getColumnIndex(str)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            //mJsonG08WebType，获取网站信息
            if (Website.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出组织类型
                int webType = cursor.getInt(cursor.getColumnIndex(Website.TYPE));

                // 主页，TYPE_HOMEPAGE = 1;
                if (webType == Website.TYPE_CUSTOM) {
                    String homepage = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("homepage")));
                    mJSONObject.put("homepage", homepage);
                }
                // 博客，TYPE_BLOG = 2;
                else if (webType == Website.TYPE_BLOG) {
                    String blog = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("blog")));
                    mJSONObject.put("blog", blog);
                }
                // 个人主页，TYPE_PROFILE = 3;
                else if (webType == Website.TYPE_HOMEPAGE) {
                    String profile = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("profile")));
                    mJSONObject.put("profile", profile);
                }
                // 家庭主页，TYPE_HOME = 4;
                else if (webType == Website.TYPE_HOMEPAGE) {
                    String home = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("home")));
                    mJSONObject.put("home", home);
                }
                // 工作主页，TYPE_WORK = 5;
                else if (webType == Website.TYPE_WORK) {
                    String workPage = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("workPage")));
                    mJSONObject.put("workPage", workPage);
                }
                // ftp主页，TYPE_FTP = 6;
                else if (webType == Website.TYPE_WORK) {
                    String ftpPage = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("ftpPage")));
                    mJSONObject.put("ftpPage", ftpPage);
                }
                // 其他主页，TYPE_OTHER = 7;
                else if (webType == Website.TYPE_WORK) {
                    String otherPage = cursor.getString(cursor.getColumnIndex(mJsonG08WebType.getString("otherPage")));
                    mJSONObject.put("otherPage", otherPage);
                }
            }

            //mJsonG09PostalType，查找通讯地址
            if (StructuredPostal.CONTENT_ITEM_TYPE.equals(mimetype)) {
                // 取出通讯地址类型
                int postalType = cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE));

                //mJsonG09_00WorkPostal，单位通讯地址，TYPE_HOME = 1;
                if (postalType == StructuredPostal.TYPE_WORK) {
                    Iterator<String> it = mJsonCol.getJSONObject("mJsonG09_00WorkPostal").keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        try {
                            String str = mJsonCol.getJSONObject("mJsonG09_00WorkPostal").getString(key);
                            mJSONObject.put(key, cursor.getString(cursor.getColumnIndex(str)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //mJsonG09_01HomePostal，住宅通讯地址，TYPE_WORK = 2;
                if (postalType == StructuredPostal.TYPE_HOME) {
                    //E/ContactOutputTool: Error in outputContacts No value for mJsonG09_01HomePostal
                    //JSONObject遍历
//                    Iterator<String> it = mJsonCol.getJSONObject("mJsonG09_01HomePostal").keys();
//                    while (it.hasNext()) {
//                        String key = it.next();
//                        try {
//                            String str = mJsonCol.getJSONObject("mJsonG09_01HomePostal").getString(key);
//                            mJSONObject.put(key, cursor.getString(cursor.getColumnIndex(str)));
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
                }

                //mJsonG09_02OtherPostal，其他通讯地址，TYPE_OTHER = 3;
                if (postalType == StructuredPostal.TYPE_OTHER) {
                    Iterator<String> it = mJsonCol.getJSONObject("mJsonG09_02OtherPostal").keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        try {
                            String str = mJsonCol.getJSONObject("mJsonG09_02OtherPostal").getString(key);
                            mJSONObject.put(key, cursor.getString(cursor.getColumnIndex(str)));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            // 填充所有字段到mJSONObject
            fillAllFields();
        }
        cursor.close();
        //Log.i("mContactData", mContactData.toString());
        //System.out.println(mContactData.toString());

        //traverseJSON5(mContactData);
        //System.out.println(traverseJSON5(mContactData));

        //return mContactData.toString();
        return traverseJSON5(mContactData);
    }

    // 填充所有字段到mJSONObject
    private void fillAllFields() throws JSONException {
        //JSONObject属性遍历
        Iterator<String> it1 = mJsonCol.keys();
        while (it1.hasNext()) {
            String key1 = it1.next();
            Iterator<String> it2 = mJsonCol.getJSONObject(key1).keys();
            while (it2.hasNext()) {
                String key2 = it2.next();
                try {
                    try {
                        JSONObject json = mJsonCol.getJSONObject(key1).getJSONObject(key2);// 抛错，说明对应value不是JSONObject
                        Iterator<String> it3 = mJsonCol.getJSONObject(key1).getJSONObject(key2).keys();
                        while (it3.hasNext()) {
                            String key3 = it3.next();
                            try {
                                if (mJSONObject.has(key3)) {
                                    mRelustJSON.put(key3, mJSONObject.getString(key3));
                                }else {
                                    mRelustJSON.put(key3, "");
                                }
                            } catch (JSONException e3) {
                                e3.printStackTrace();
                            }
                        }
                    } catch (Exception e2) {
                        if (mJSONObject.has(key2)) {
                            mRelustJSON.put(key2, mJSONObject.getString(key2));
                        }else{
                            mRelustJSON.put(key2, "");
                        }
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    //I/System.out:
    //prefix,firstName,middleName,lastname,suffix,phoneticFirstName,phoneticMiddleName,phoneticLastName,mobile,homeNum,jobNum,workFax,homeFax,pager,quickNum,jobTel,carNum,isdn,tel,wirelessDev,telegram,tty_tdd,jobMobile,jobPager,assistantNum,mms,homeEmail,jobEmail,mobileEmail,birthday,anniversary,workMsg,instantsMsg,remark,nickName,company,jobTitle,department,home,homePage,workPage,street,ciry,box,area,state,zip,country,homeStreet,homeCity,homeBox,homeArea,homeState,homeZip,homeCountry,otherStreet,otherCity,otherBox,otherArea,otherState,otherZip,otherCountry
    //Zhangsan,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
    //1 598-647-2331,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

    //mobile,lastname,homeNum
    //1 332-348-9735,Zhangsan
    //1 358-647-3317,Lisi,1 598-647-2331

    //1 332-348-9735,Zhangsan
    //1 358-647-3317,Lisi,1 598-647-2331

    //I/System.out: 1 332-348-9735,Zhangsan,1 358-647-3317,Lisi,1 598-647-2331

    /*
    {"mobile":"1 332-348-9735","lastName":"Zhangsan"},
    {"mobile":"1 358-647-3317","lastName":"Lisi","homeNum":"1 598-647-2331"}
    */

    public static String traverseJSON5(JSONObject jsonObject) throws JSONException {
        Pair<String, String> pair = new Pair<String, String>("", "");
        String value = "";
        //JSONObject属性遍历
        Iterator<String> it = jsonObject.keys();
        while (it.hasNext()) {
            String key = it.next();
            //System.out.println(key);    //I/System.out: contact0            contact1
            try {
                if ("" != value) {
                    value += "\n";
                }
                //System.out.println(mJSONObject.getJSONObject(key).toString());
                pair = traverseJSON3(jsonObject.getJSONObject(key), pair.first);
                //System.out.println(pair.second);
                value += pair.second;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return pair.first + "\n" + value;
    }

    public static Pair<String, String> traverseJSON3(JSONObject jsonObject, String head) throws JSONException {
        String headNew = "";
        String value = "";
        //JSONObject属性遍历
        Iterator<String> it = jsonObject.keys();
        int i = -1;
        while (it.hasNext()) {
            i++;
            String key = it.next();
            if ("" != headNew) {
                headNew += ",";
            }
            headNew += key;

            try {
                if (i > 0) {
                    value += ",";
                }
                value += jsonObject.getString(key);
                //System.out.println(mJSONObject.getString(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (head.length() > headNew.length()) {
            headNew = head;
        }

        return new Pair<String, String>(headNew, value);
    }

    private static String funRemove(String str) {
        str = str.replace(" ", "");
        str = str.replace("-", "");
        str = str.replace("+86", "");
        str = str.replace("+", "");
        return str;
    }

}

/*
mobile,prefix,firstName,middleName,lastname,suffix,phoneticFirstName,phoneticMiddleName,phoneticLastName,homeNum,jobNum,workFax,homeFax,pager,quickNum,jobTel,carNum,isdn,tel,wirelessDev,telegram,tty_tdd,jobMobile,jobPager,assistantNum,mms,homeEmail,jobEmail,mobileEmail,birthday,anniversary,workMsg,instantsMsg,remark,nickName,company,jobTitle,department,home,homePage,workPage,street,ciry,box,area,state,zip,country,homeStreet,homeCity,homeBox,homeArea,homeState,homeZip,homeCountry,otherStreet,otherCity,otherBox,otherArea,otherState,otherZip,otherCountry
13323489735,,,,Zhangsan,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
13586473317,,,,Lisi,,,,,15986472331,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
*/