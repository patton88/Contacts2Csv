//存储通讯录所有联系人字段的数据类ContactsData
//20190年12月09日
package com.example.contacts2csv;


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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

/**
 * @author glsite.com
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class ContactsHeader {
    //public Map<String, String> mMapCol;            //联系人信息列表头
    //public Vector<Pair<String, String>> mVecCol;   //联系人信息列表头，Map会自动排序，用Vector也不合理
    public JSONObject mJsonHeader;     //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject

    public void init() throws JSONException {
        // ------------------- 联系人信息列表头 - Begin
        mJsonHeader = new JSONObject(new LinkedHashMap());         //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject

        //1.00、mJsonG00StructName，获得通讯录中联系人的名字。1个大项，9个小项
        mJsonHeader.put("mJsonG00StructName", new JSONObject(new LinkedHashMap()));
        String strJsonG00StructName[][] = {
                {"displayName", StructuredName.DISPLAY_NAME},	                //DISPLAY_NAME = "data1";
                {"lastName", StructuredName.GIVEN_NAME},			            //GIVEN_NAME = "data2";
                {"firstName", StructuredName.FAMILY_NAME},                      //FAMILY_NAME = "data3";
                {"prefix", StructuredName.PREFIX},				                //PREFIX = "data4";
                {"middleName", StructuredName.MIDDLE_NAME},                     //MIDDLE_NAME = "data5";
                {"suffix", StructuredName.SUFFIX},		                        //SUFFIX = "data6";
                {"phoneticLastName", StructuredName.PHONETIC_GIVEN_NAME},		//PHONETIC_GIVEN_NAME = "data7";
                {"phoneticFirstName", StructuredName.PHONETIC_FAMILY_NAME},	    //PHONETIC_MIDDLE_NAME = "data8";
                {"phoneticMiddleName", StructuredName.PHONETIC_MIDDLE_NAME},    //PHONETIC_FAMILY_NAME = "data9";
        };
        add2Json(strJsonG00StructName, mJsonHeader.getJSONObject("mJsonG00StructName"));
        //        for(String[] strArr : strJsonG00StructName){
        //            JSONObject json = new JSONObject(new LinkedHashMap());
        //            json.put(strArr[0], strArr[1]);
        //            mJsonG01Phone.put(strArr[0], json);
        //        }

        //1.01、mJsonG01Phone，获取电话信息。1个大项，20个小项，共20项
        mJsonHeader.put("mJsonG01Phone", new JSONObject(new LinkedHashMap()));
        String strJsonG01Phone[][] = {
                {"homeNum", Phone.NUMBER},         //TYPE_HOME = 1;
                {"mobile", Phone.NUMBER},          //TYPE_MOBILE = 2;
                {"workNum", Phone.NUMBER},         //TYPE_WORK = 3;
                {"workFax", Phone.NUMBER},         //TYPE_FAX_WORK = 4;
                {"homeFax", Phone.NUMBER},         //TYPE_FAX_HOME = 5;
                {"pager", Phone.NUMBER},			//TYPE_PAGER = 6;
                {"otherNum", Phone.NUMBER},			//TYPE_OTHER = 7;
                {"callbackNum", Phone.NUMBER},		//TYPE_CALLBACK = 8;
                {"carNum", Phone.NUMBER},			//TYPE_CAR = 9;
                {"compMainTel", Phone.NUMBER},		//TYPE_COMPANY_MAIN = 10;
                {"isdn", Phone.NUMBER},			    //TYPE_ISDN = 11;
                {"mainTel", Phone.NUMBER},			//TYPE_MAIN = 12;
                {"otherFax", Phone.NUMBER},			//TYPE_OTHER_FAX = 13;
                {"wirelessDev", Phone.NUMBER},		//TYPE_RADIO = 14;
                {"telegram", Phone.NUMBER},			//TYPE_TELEX = 15;
                {"tty_tdd", Phone.NUMBER},			//TYPE_TTY_TDD = 16;
                {"workMobile", Phone.NUMBER},		//TYPE_WORK_MOBILE = 17;
                {"workPager", Phone.NUMBER},		//TYPE_WORK_PAGER = 18;
                {"assistantNum", Phone.NUMBER},		//TYPE_ASSISTANT = 19;
                {"mms", Phone.NUMBER},			    //TYPE_MMS = 20;
        };
        add2Json(strJsonG01Phone, mJsonHeader.getJSONObject("mJsonG01Phone"));

        //1.02、mJsonG02Email，查找Email地址。1个大项，4个小项
        mJsonHeader.put("mJsonG02Email", new JSONObject(new LinkedHashMap()));
        String strJsonG02Email[][] = {
                {"homeEmail", Email.DATA},         //TYPE_HOME = 1;
                {"workEmail", Email.DATA},         //TYPE_WORK = 2;
                {"otherEmail", Email.DATA},        //TYPE_OTHER = 3;
                {"mobileEmail", Email.DATA},       //TYPE_MOBILE = 4;
        };
        add2Json(strJsonG02Email, mJsonHeader.getJSONObject("mJsonG02Email"));

        //1.03、mJsonG03Event，查找Event地址。1个大项，3个小项
        mJsonHeader.put("mJsonG03Event", new JSONObject(new LinkedHashMap()));
        String strJsonG03Event[][] = {
                {"anniversary", Event.START_DATE}, //TYPE_ANNIVERSARY = 1;
                {"otherday", Event.START_DATE},    //TYPE_OTHER = 2;
                {"birthday", Event.START_DATE},    //TYPE_BIRTHDAY = 3;
        };
        add2Json(strJsonG03Event, mJsonHeader.getJSONObject("mJsonG03Event"));


        //1.04、mJsonG04Im，即时消息。1个大项，13个小项
        mJsonHeader.put("mJsonG04Im", new JSONObject(new LinkedHashMap()));
        String strJsonG04Im[][] = {
                {"homeMsg", Im.DATA},         //TYPE_HOME = 1;
                {"workMsg", Im.DATA},         //TYPE_WORK = 2;
                {"otherMsg", Im.DATA},        //TYPE_OTHER = 3;
                {"customIm", Im.DATA},        //PROTOCOL_CUSTOM = -1;
                {"aimIm", Im.DATA},           //PROTOCOL_AIM = 0;
                {"msnIm", Im.DATA},           //PROTOCOL_MSN = 1;
                {"yahooIm", Im.DATA},         //PROTOCOL_YAHOO = 2;
                {"skypeIm", Im.DATA},         //PROTOCOL_SKYPE = 3;
                {"qqIm", Im.DATA},            //PROTOCOL_QQ = 4;
                {"googleTalkIm", Im.DATA},    //PROTOCOL_GOOGLE_TALK = 5;
                {"icqIm", Im.DATA},           //PROTOCOL_ICQ = 6;
                {"jabberIm", Im.DATA},        //PROTOCOL_JABBER = 7;
                {"netmeetingIm", Im.DATA},    //PROTOCOL_NETMEETING = 8;
        };
        add2Json(strJsonG04Im, mJsonHeader.getJSONObject("mJsonG04Im"));

        //1.05、mJsonG05Remark，获取备注信息。1个大项，1个小项
        mJsonHeader.put("mJsonG05Remark", new JSONObject(new LinkedHashMap()));
        String strJsonG05Remark[][] = {
                {"remark", Note.NOTE}
        };
        add2Json(strJsonG05Remark, mJsonHeader.getJSONObject("mJsonG05Remark"));

        //1.06、mJsonG06NickName，获取昵称信息。1个大项，5个小项
        mJsonHeader.put("mJsonG06NickName", new JSONObject(new LinkedHashMap()));
        String strJsonG06NickName[][] = {
                {"defaultNickName", Nickname.NAME},     //TYPE_DEFAULT = 1;
                {"otherNickName", Nickname.NAME},       //TYPE_OTHER_NAME = 2;
                {"maindenNickName", Nickname.NAME},     //TYPE_MAINDEN_NAME = 3;
                {"shortNickName", Nickname.NAME},       //TYPE_SHORT_NAME = 4;
                {"initialsNickName", Nickname.NAME},    //TYPE_INITIALS = 5;
        };
        add2Json(strJsonG06NickName, mJsonHeader.getJSONObject("mJsonG06NickName"));

        //1.07、mJsonG07OrgType，获取组织信息
        JSONObject mJsonG07OrgType = new JSONObject(new LinkedHashMap());
        mJsonHeader.put("mJsonG07OrgType", mJsonG07OrgType);

        //1.07.00、mJsonG07_00WorkOrgType，单位组织信息，TYPE_WORK = 1。1个大项，7个小项;
        mJsonG07OrgType.put("mJsonG07_00WorkOrgType", new JSONObject(new LinkedHashMap()));
        String strJsonG07_00WorkOrgType[][] = {
                {"workCompany", Organization.COMPANY},               //COMPANY = "data1";
                {"workJobTitle", Organization.TITLE},                //TITLE = "data4";
                {"workDepartment", Organization.DEPARTMENT},         //DEPARTMENT = "data5";
                {"workJobDescription", Organization.DEPARTMENT},     //JOB_DESCRIPTION = "data6";
                {"workSymbol", Organization.DEPARTMENT},             //SYMBOL = "data7";
                {"workPhoneticName", Organization.DEPARTMENT},       //PHONETIC_NAME = "data8";
                {"workOfficeLocation", Organization.DEPARTMENT},     //OFFICE_LOCATION = "data9";
        };
        add2Json(strJsonG07_00WorkOrgType, mJsonG07OrgType.getJSONObject("mJsonG07_00WorkOrgType"));

        //1.07.01、mJsonG07_01OtherOrgType，其他组织信息，TYPE_OTHER = 2。1个大项，7个小项;
        mJsonG07OrgType.put("mJsonG07_01OtherOrgType", new JSONObject(new LinkedHashMap()));
        String strJsonG07_01OtherOrgType[][] = {
                {"otherCompany", Organization.COMPANY},              //COMPANY = "data1";
                {"otherJobTitle", Organization.TITLE},               //TITLE = "data4";
                {"otherDepartment", Organization.DEPARTMENT},        //DEPARTMENT = "data5";
                {"otherJobDescription", Organization.DEPARTMENT},    //JOB_DESCRIPTION = "data6";
                {"otherSymbol", Organization.DEPARTMENT},            //SYMBOL = "data7";
                {"otherPhoneticName", Organization.DEPARTMENT},      //PHONETIC_NAME = "data8";
                {"otherOfficeLocation", Organization.DEPARTMENT},    //OFFICE_LOCATION = "data9";
        };
        add2Json(strJsonG07_01OtherOrgType, mJsonG07OrgType.getJSONObject("mJsonG07_01OtherOrgType"));

        //1.08、mJsonG08WebType，获取网站信息
        mJsonHeader.put("mJsonG08WebType", new JSONObject(new LinkedHashMap()));
        String strJsonG08WebType[][] = {
                {"homepage", Website.URL},           //TYPE_HOMEPAGE = 1;
                {"blog", Website.URL},               //TYPE_BLOG = 2;
                {"profile", Website.URL},            //TYPE_PROFILE = 3;
                {"home", Website.URL},               //TYPE_HOME = 4;
                {"workPage", Website.URL},           //TYPE_WORK = 5;
                {"ftpPage", Website.URL},            //TYPE_FTP = 6;
                {"otherPage", Website.URL},          //TYPE_OTHER = 7;
        };
        add2Json(strJsonG08WebType, mJsonHeader.getJSONObject("mJsonG08WebType"));

        //1.09、mJsonG09PostalType，查找通讯地址
        JSONObject mJsonG09PostalType = new JSONObject(new LinkedHashMap());
        mJsonHeader.put("mJsonG09PostalType", mJsonG09PostalType);

        //1.09.00、mJsonG09_00WorkPostal，单位通讯地址，TYPE_HOME = 1。1个大项，8个小项;
        mJsonG09PostalType.put("mJsonG09_00WorkPostal", new JSONObject(new LinkedHashMap()));
        String strJsonG09_00WorkPostal[][] = {
                {"workFormattedAddress", StructuredPostal.FORMATTED_ADDRESS},  //FORMATTED_ADDRESS = "data1";
                {"workStreet", StructuredPostal.STREET},                       //STREET = "data4";
                {"workBox", StructuredPostal.POBOX},                           //POBOX = "data5";
                {"workArea", StructuredPostal.NEIGHBORHOOD},                   //NEIGHBORHOOD = "data6";
                {"workCity", StructuredPostal.CITY},                           //CITY = "data7";
                {"workState", StructuredPostal.REGION},                        //REGION = "data8";
                {"workZip", StructuredPostal.POSTCODE},                        //POSTCODE = "data9";
                {"workCountry", StructuredPostal.COUNTRY},                     //COUNTRY = "data10";
        };
        add2Json(strJsonG09_00WorkPostal, mJsonG09PostalType.getJSONObject("mJsonG09_00WorkPostal"));

        //1.09.01、mJsonG09_01HomePostal，住宅通讯地址，TYPE_WORK = 2。1个大项，8个小项;
        mJsonG09PostalType.put("mJsonG09_01HomePostal", new JSONObject(new LinkedHashMap()));
        String strJsonG09_01HomePostal[][] = {
                {"homeFormattedAddress", StructuredPostal.FORMATTED_ADDRESS},  //FORMATTED_ADDRESS = "data1";
                {"homeStreet", StructuredPostal.STREET},                       //STREET = "data4";
                {"homeBox", StructuredPostal.POBOX},                           //POBOX = "data5";
                {"homeArea", StructuredPostal.NEIGHBORHOOD},                   //NEIGHBORHOOD = "data6";
                {"homeCity", StructuredPostal.CITY},                           //CITY = "data7";
                {"homeState", StructuredPostal.REGION},                        //REGION = "data8";
                {"homeZip", StructuredPostal.POSTCODE},                        //POSTCODE = "data9";
                {"homeCountry", StructuredPostal.COUNTRY},                     //COUNTRY = "data10";
        };
        add2Json(strJsonG09_01HomePostal, mJsonG09PostalType.getJSONObject("mJsonG09_01HomePostal"));

        //1.09.02、mJsonG09_02OtherPostal，其他通讯地址，TYPE_OTHER = 3。1个大项，8个小项;
        mJsonG09PostalType.put("mJsonG09_02OtherPostal", new JSONObject(new LinkedHashMap()));
        String strJsonG09_02OtherPostal[][] = {
                {"otherFormattedAddress", StructuredPostal.FORMATTED_ADDRESS},  //FORMATTED_ADDRESS = "data1";
                {"otherStreet", StructuredPostal.STREET},                       //STREET = "data4";
                {"otherBox", StructuredPostal.POBOX},                           //POBOX = "data5";
                {"otherArea", StructuredPostal.NEIGHBORHOOD},                   //NEIGHBORHOOD = "data6";
                {"otherCity", StructuredPostal.CITY},                           //CITY = "data7";
                {"otherState", StructuredPostal.REGION},                        //REGION = "data8";
                {"otherZip", StructuredPostal.POSTCODE},                        //POSTCODE = "data9";
                {"otherCountry", StructuredPostal.COUNTRY},                     //COUNTRY = "data10";
        };
        add2Json(strJsonG09_02OtherPostal, mJsonG09PostalType.getJSONObject("mJsonG09_02OtherPostal"));

        // ------------------- 联系人信息列表头 - End
    }

    private void add2Json(String[][] str2Arr, JSONObject jsonObject) throws JSONException {
        for(String[] strArr : str2Arr){
            JSONObject json = new JSONObject(new LinkedHashMap());
            json.put("__first", strArr[1]);     //为避免与联系人信息标识重名，前面添加双斜杠
            json.put("__second", "0");          //为避免与联系人信息标识重名，前面添加双斜杠
            jsonObject.put(strArr[0], json);
            //System.out.println("strArr_my : " + strArr[0] + ", " + strArr[1]);
            //            JSONObject json = new JSONObject(new LinkedHashMap());
            //            json.put(strArr[0], strArr[1]);
            //            jsonObject.put(strArr[0], json);
        }
    }
}
