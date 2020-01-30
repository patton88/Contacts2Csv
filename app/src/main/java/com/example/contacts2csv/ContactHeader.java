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
public class ContactHeader {
    //public Map<String, String> mMapCol;            //联系人信息列表头
    //public Vector<Pair<String, String>> mVecCol;   //联系人信息列表头，Map会自动排序，用Vector也不合理
    public JSONObject m_jsonHeader;     //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject

    //经输出后查看，混乱无用
    //public LinkedHashMap m_lhmapFields; //联系人信息列表头所有原生字段

    public ContactHeader() {
        // ------------------- 联系人信息列表头 - Begin
        try {
            //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject
            m_jsonHeader = new JSONObject(new LinkedHashMap());
            //m_lhmapFields = new LinkedHashMap();

            //命名惯例：变量名首字母一般小写，函数名、类名、常量名首字母一般大写
            //1.00、jsonG00StructName，获得通讯录中联系人的名字。G00 Group00。1个大项，9个小项
            m_jsonHeader.put("jsonG00StructName", new JSONObject(new LinkedHashMap()));

            String arr2JsonG00StructName[][] = {
                    {"__mimetype_0", StructuredName.CONTENT_ITEM_TYPE},             //StructuredName.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/name";
                    {"displayName", StructuredName.DISPLAY_NAME},	                //StructuredName.DISPLAY_NAME = "data1";
                    {"lastName", StructuredName.GIVEN_NAME},			            //StructuredName.GIVEN_NAME = "data2";
                    {"firstName", StructuredName.FAMILY_NAME},                      //StructuredName.FAMILY_NAME = "data3";
                    {"prefix", StructuredName.PREFIX},				                //StructuredName.PREFIX = "data4";
                    {"middleName", StructuredName.MIDDLE_NAME},                     //StructuredName.MIDDLE_NAME = "data5";
                    {"suffix", StructuredName.SUFFIX},		                        //StructuredName.SUFFIX = "data6";
                    {"phoneticLastName", StructuredName.PHONETIC_GIVEN_NAME},		//StructuredName.PHONETIC_GIVEN_NAME = "data7";
                    {"phoneticFirstName", StructuredName.PHONETIC_FAMILY_NAME},	    //StructuredName.PHONETIC_MIDDLE_NAME = "data8";
                    {"phoneticMiddleName", StructuredName.PHONETIC_MIDDLE_NAME},    //StructuredName.PHONETIC_FAMILY_NAME = "data9";
            };
            Arr2Json(arr2JsonG00StructName, m_jsonHeader.getJSONObject("jsonG00StructName"));

            //1.01、jsonG01Phone，获取电话信息。1个大项，20个小项，共20项
            m_jsonHeader.put("jsonG01Phone", new JSONObject(new LinkedHashMap()));
            String arr2JsonG01Phone[][] = {
                    {"__mimetype_0", Phone.CONTENT_ITEM_TYPE},                  //Phone.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone_v2";
                    {"__mimetype_1", Phone.NUMBER},                             //Phone.NUMBER = "data1";
                    {"__mimetype_2", Phone.TYPE},                               //Phone.TYPE = "data2";
                    {"__mimetype_3", Phone.LABEL},                              //Phone.TYPE = "data3";
                    {"homeNum", String.valueOf(Phone.TYPE_HOME)},               //Phone.TYPE_HOME = 1;
                    {"mobile", String.valueOf(Phone.TYPE_MOBILE)},              //Phone.TYPE_MOBILE = 2;
                    {"workNum", String.valueOf(Phone.TYPE_WORK)},               //Phone.TYPE_WORK = 3;
                    {"workFax", String.valueOf(Phone.TYPE_FAX_WORK)},           //Phone.TYPE_FAX_WORK = 4;
                    {"homeFax", String.valueOf(Phone.TYPE_FAX_HOME)},           //Phone.TYPE_FAX_HOME = 5;
                    {"pager", String.valueOf(Phone.TYPE_PAGER)},                //Phone.TYPE_PAGER = 6;
                    {"otherNum", String.valueOf(Phone.TYPE_OTHER)},			    //Phone.TYPE_OTHER = 7;
                    {"callbackNum", String.valueOf(Phone.TYPE_CALLBACK)},	    //Phone.TYPE_CALLBACK = 8;
                    {"carNum", String.valueOf(Phone.TYPE_CAR)},			        //Phone.TYPE_CAR = 9;
                    {"compMainTel", String.valueOf(Phone.TYPE_COMPANY_MAIN)},   //Phone.TYPE_COMPANY_MAIN = 10;
                    {"isdn", String.valueOf(Phone.TYPE_ISDN)},			        //Phone.TYPE_ISDN = 11;
                    {"mainTel", String.valueOf(Phone.TYPE_MAIN)},			    //Phone.TYPE_MAIN = 12;
                    {"otherFax", String.valueOf(Phone.TYPE_OTHER_FAX)},			//Phone.TYPE_OTHER_FAX = 13;
                    {"wirelessDev", String.valueOf(Phone.TYPE_RADIO)},		    //Phone.TYPE_RADIO = 14;
                    {"telegram", String.valueOf(Phone.TYPE_TELEX)},			    //Phone.TYPE_TELEX = 15;
                    {"tty_tdd", String.valueOf(Phone.TYPE_TTY_TDD)},			//Phone.TYPE_TTY_TDD = 16;
                    {"workMobile", String.valueOf(Phone.TYPE_WORK_MOBILE)},		//Phone.TYPE_WORK_MOBILE = 17;
                    {"workPager", String.valueOf(Phone.TYPE_WORK_PAGER)},		//Phone.TYPE_WORK_PAGER = 18;
                    {"assistantNum", String.valueOf(Phone.TYPE_ASSISTANT)},		//Phone.TYPE_ASSISTANT = 19;
                    {"mms", String.valueOf(Phone.TYPE_MMS)},			        //Phone.TYPE_MMS = 20;
            };
            Arr2Json(arr2JsonG01Phone, m_jsonHeader.getJSONObject("jsonG01Phone"));

            //1.02、jsonG02Email，查找Email地址。1个大项，4个小项
            m_jsonHeader.put("jsonG02Email", new JSONObject(new LinkedHashMap()));
            String arr2JsonG02Email[][] = {
                    {"__mimetype_0", Email.CONTENT_ITEM_TYPE},          //Email.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/email_v2";
                    {"__mimetype_1", Email.DATA},                       //Email.DATA = "data1";
                    {"__mimetype_2", Email.TYPE},                       //Email.TYPE = "data2";
                    {"__mimetype_3", Email.LABEL},                      //Email.LABEL = "data3";
                    {"homeEmail"  , String.valueOf(Email.TYPE_HOME)},   //Email.TYPE_HOME = 1;
                    {"workEmail"  , String.valueOf(Email.TYPE_WORK)},   //Email.TYPE_WORK = 2;
                    {"otherEmail" , String.valueOf(Email.TYPE_OTHER)},  //Email.TYPE_OTHER = 3;
                    {"mobileEmail", String.valueOf(Email.TYPE_MOBILE)}, //Email.TYPE_MOBILE = 4;
            };
            Arr2Json(arr2JsonG02Email, m_jsonHeader.getJSONObject("jsonG02Email"));

            //1.03、jsonG03Event，查找Event地址。1个大项，3个小项
            m_jsonHeader.put("jsonG03Event", new JSONObject(new LinkedHashMap()));
            String arr2JsonG03Event[][] = {
                    {"__mimetype_0", Event.CONTENT_ITEM_TYPE},              //Event.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_1", Event.START_DATE},                     //Event.START_DATE = "data1";
                    {"__mimetype_2", Event.TYPE},                           //Event.TYPE = "data2";
                    {"__mimetype_3", Event.LABEL},                          //Event.LABEL = "data3";
                    {"anniversary", String.valueOf(Event.TYPE_ANNIVERSARY)},//Event.TYPE_ANNIVERSARY = 1;
                    {"otherday", String.valueOf(Event.TYPE_OTHER)},         //Event.TYPE_OTHER = 2;
                    {"birthday", String.valueOf(Event.TYPE_BIRTHDAY)},      //Event.TYPE_BIRTHDAY = 3;
            };
            Arr2Json(arr2JsonG03Event, m_jsonHeader.getJSONObject("jsonG03Event"));

            //1.04、jsonG04ImType，即时消息
            JSONObject jsonG04ImType = new JSONObject(new LinkedHashMap());
            m_jsonHeader.put("jsonG04ImType", jsonG04ImType);
            String arr2JsonG04ImType[][] = {
                    {"__mimetype_0", Im.CONTENT_ITEM_TYPE},                         //Im.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_1", Im.DATA},                                      //Im.DATA = "data1";
                    {"__mimetype_2", Im.TYPE},                                      //Im.TYPE = "data2";
                    {"__mimetype_3", Im.LABEL},                                     //Im.LABEL = "data3";
                    {"__mimetype_4", Im.PROTOCOL},                                  //Im.PROTOCOL = "data5";
                    {"__mimetype_5", Im.CUSTOM_PROTOCOL},                           //Im.PROTOCOL = "data6";
            };
            Arr2Json(arr2JsonG04ImType, jsonG04ImType);

            //1.04.00、JsonG04_00HomeImType，家庭即时消息，Im.TYPE_HOME = 1。1个大项，15个小项
            jsonG04ImType.put("JsonG04_00HomeImType", new JSONObject(new LinkedHashMap()));
            String arr2JsonG04_00HomeImType[][] = {
                    {"__mimetype_0", Im.CONTENT_ITEM_TYPE},                         //Im.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_1", Im.DATA},                                      //Im.DATA = "data1";
                    {"__mimetype_2", Im.TYPE},                                      //Im.TYPE = "data2";
                    {"__mimetype_3", Im.LABEL},                                     //Im.LABEL = "data3";
                    {"__mimetype_4", Im.PROTOCOL},                                  //Im.PROTOCOL = "data5";
                    {"__mimetype_5", Im.CUSTOM_PROTOCOL},                           //Im.PROTOCOL = "data6";
                    {"__mimetype_6", String.valueOf(Im.TYPE_HOME)},                 //Im.TYPE_HOME = 1;
                    {"homeCustomIm", String.valueOf(Im.PROTOCOL_CUSTOM)},           //Im.PROTOCOL_CUSTOM = -1;
                    {"homeAimIm", String.valueOf(Im.PROTOCOL_AIM)},                 //Im.PROTOCOL_AIM = 0;
                    {"homeMsnIm", String.valueOf(Im.PROTOCOL_MSN)},                 //Im.PROTOCOL_MSN = 1;
                    {"homeYahooIm", String.valueOf(Im.PROTOCOL_YAHOO)},             //Im.PROTOCOL_YAHOO = 2;
                    {"homeSkypeIm", String.valueOf(Im.PROTOCOL_SKYPE)},             //Im.PROTOCOL_SKYPE = 3;
                    {"homeQqIm", String.valueOf(Im.PROTOCOL_QQ)},                   //Im.PROTOCOL_QQ = 4;
                    {"homeGoogleTalkIm", String.valueOf(Im.PROTOCOL_GOOGLE_TALK)},  //Im.PROTOCOL_GOOGLE_TALK = 5;
                    {"homeIcqIm", String.valueOf(Im.PROTOCOL_ICQ)},                 //Im.PROTOCOL_ICQ = 6;
                    {"homeJabberIm", String.valueOf(Im.PROTOCOL_JABBER)},           //Im.PROTOCOL_JABBER = 7;
                    {"homeNetmeetingIm", String.valueOf(Im.PROTOCOL_NETMEETING)},   //Im.PROTOCOL_NETMEETING = 8;
            };
            Arr2Json(arr2JsonG04_00HomeImType, jsonG04ImType.getJSONObject("JsonG04_00HomeImType"));

            //1.04.01、JsonG04_01WorkImType，家庭即时消息，Im.TYPE_WORK = 2。1个大项，15个小项
            jsonG04ImType.put("JsonG04_00WorkImType", new JSONObject(new LinkedHashMap()));
            String arr2JsonG04_01WorkImType[][] = {
                    {"__mimetype_0", Im.CONTENT_ITEM_TYPE},                         //Im.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_1", Im.DATA},                                      //Im.DATA = "data1";
                    {"__mimetype_2", Im.TYPE},                                      //Im.TYPE = "data2";
                    {"__mimetype_3", Im.LABEL},                                     //Im.LABEL = "data3";
                    {"__mimetype_4", Im.PROTOCOL},                                  //Im.PROTOCOL = "data5";
                    {"__mimetype_5", Im.CUSTOM_PROTOCOL},                           //Im.PROTOCOL = "data6";
                    {"__mimetype_6", String.valueOf(Im.TYPE_WORK)},                 //Im.TYPE_WORK = 2;
                    {"workCustomIm", String.valueOf(Im.PROTOCOL_CUSTOM)},           //Im.PROTOCOL_CUSTOM = -1;
                    {"workAimIm", String.valueOf(Im.PROTOCOL_AIM)},                 //Im.PROTOCOL_AIM = 0;
                    {"workMsnIm", String.valueOf(Im.PROTOCOL_MSN)},                 //Im.PROTOCOL_MSN = 1;
                    {"workYahooIm", String.valueOf(Im.PROTOCOL_YAHOO)},             //Im.PROTOCOL_YAHOO = 2;
                    {"workSkypeIm", String.valueOf(Im.PROTOCOL_SKYPE)},             //Im.PROTOCOL_SKYPE = 3;
                    {"workQqIm", String.valueOf(Im.PROTOCOL_QQ)},                   //Im.PROTOCOL_QQ = 4;
                    {"workGoogleTalkIm", String.valueOf(Im.PROTOCOL_GOOGLE_TALK)},  //Im.PROTOCOL_GOOGLE_TALK = 5;
                    {"workIcqIm", String.valueOf(Im.PROTOCOL_ICQ)},                 //Im.PROTOCOL_ICQ = 6;
                    {"workJabberIm", String.valueOf(Im.PROTOCOL_JABBER)},           //Im.PROTOCOL_JABBER = 7;
                    {"workNetmeetingIm", String.valueOf(Im.PROTOCOL_NETMEETING)},   //Im.PROTOCOL_NETMEETING = 8;
            };
            Arr2Json(arr2JsonG04_01WorkImType, jsonG04ImType.getJSONObject("JsonG04_00WorkImType"));

            //1.04.02、JsonG04_02OtherImType，家庭即时消息，Im.TYPE_OTHER = 3。1个大项，15个小项
            jsonG04ImType.put("JsonG04_02OtherImType", new JSONObject(new LinkedHashMap()));
            String arr2JsonG04_02OtherImType[][] = {
                    {"__mimetype_0", Im.CONTENT_ITEM_TYPE},                         //Im.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_1", Im.DATA},                                      //Im.DATA = "data1";
                    {"__mimetype_2", Im.TYPE},                                      //Im.TYPE = "data2";
                    {"__mimetype_3", Im.LABEL},                                     //Im.LABEL = "data3";
                    {"__mimetype_4", Im.PROTOCOL},                                  //Im.PROTOCOL = "data5";
                    {"__mimetype_5", Im.CUSTOM_PROTOCOL},                           //Im.PROTOCOL = "data6";
                    {"__mimetype_6", String.valueOf(Im.TYPE_OTHER)},                //Im.TYPE_OTHER = 3;
                    {"otherCustomIm", String.valueOf(Im.PROTOCOL_CUSTOM)},           //Im.PROTOCOL_CUSTOM = -1;
                    {"otherAimIm", String.valueOf(Im.PROTOCOL_AIM)},                 //Im.PROTOCOL_AIM = 0;
                    {"otherMsnIm", String.valueOf(Im.PROTOCOL_MSN)},                 //Im.PROTOCOL_MSN = 1;
                    {"otherYahooIm", String.valueOf(Im.PROTOCOL_YAHOO)},             //Im.PROTOCOL_YAHOO = 2;
                    {"otherSkypeIm", String.valueOf(Im.PROTOCOL_SKYPE)},             //Im.PROTOCOL_SKYPE = 3;
                    {"otherQqIm", String.valueOf(Im.PROTOCOL_QQ)},                   //Im.PROTOCOL_QQ = 4;
                    {"otherGoogleTalkIm", String.valueOf(Im.PROTOCOL_GOOGLE_TALK)},  //Im.PROTOCOL_GOOGLE_TALK = 5;
                    {"otherIcqIm", String.valueOf(Im.PROTOCOL_ICQ)},                 //Im.PROTOCOL_ICQ = 6;
                    {"otherJabberIm", String.valueOf(Im.PROTOCOL_JABBER)},           //Im.PROTOCOL_JABBER = 7;
                    {"otherNetmeetingIm", String.valueOf(Im.PROTOCOL_NETMEETING)},   //Im.PROTOCOL_NETMEETING = 8;
            };
            Arr2Json(arr2JsonG04_02OtherImType, jsonG04ImType.getJSONObject("JsonG04_02OtherImType"));

            //1.05、jsonG05Remark，获取备注信息。1个大项，1个小项
            m_jsonHeader.put("jsonG05Remark", new JSONObject(new LinkedHashMap()));
            String arr2JsonG05Remark[][] = {
                    {"__mimetype_0", Note.CONTENT_ITEM_TYPE},   //Note.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";
                    {"__mimetype_1", Note.NOTE},                //Note.NOTE = "data1"
                    {"remark", Note.NOTE}                       //Note.NOTE = "data1";
            };
            Arr2Json(arr2JsonG05Remark, m_jsonHeader.getJSONObject("jsonG05Remark"));

            //1.06、jsonG06NickName，获取昵称信息。1个大项，5个小项
            m_jsonHeader.put("jsonG06NickName", new JSONObject(new LinkedHashMap()));
            String arr2JsonG06NickName[][] = {
                    {"__mimetype_0", Nickname.CONTENT_ITEM_TYPE},                   //Nickname.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/nickname";
                    {"__mimetype_1", Nickname.NAME},                                //Nickname.NAME = "data1";
                    {"__mimetype_2", Nickname.TYPE},                                //Nickname.TYPE = "data2";
                    {"__mimetype_3", Nickname.LABEL},                               //Nickname.LABEL = "data3";
                    {"defaultNickName", String.valueOf(Nickname.TYPE_DEFAULT)},     //Nickname.TYPE_DEFAULT = 1;
                    {"otherNickName", String.valueOf(Nickname.TYPE_OTHER_NAME)},    //Nickname.TYPE_OTHER_NAME = 2;
                    {"maindenNickName", String.valueOf(Nickname.TYPE_MAIDEN_NAME)}, //Nickname.TYPE_MAIDEN_NAME = 3;
                    {"shortNickName", String.valueOf(Nickname.TYPE_SHORT_NAME)},    //Nickname.TYPE_SHORT_NAME = 4;
                    {"initialsNickName", String.valueOf(Nickname.TYPE_INITIALS)},   //Nickname.TYPE_INITIALS = 5;
            };
            // /** @TYPE_MAINDEN_NAME deprecated Use TYPE_MAIDEN_NAME instead. */
            Arr2Json(arr2JsonG06NickName, m_jsonHeader.getJSONObject("jsonG06NickName"));

            //1.07、jsonG07OrgType，获取组织信息
            JSONObject jsonG07OrgType = new JSONObject(new LinkedHashMap());
            m_jsonHeader.put("jsonG07OrgType", jsonG07OrgType);

            //I:\Android\Android-SDK-Windows\sources\android-29\android\provider\ContactsContract.java
            // Columns common across the specific types.
            //protected interface CommonColumns extends BaseTypes {
                // The data for the contact method. Type: TEXT
                //public static final String DATA = DataColumns.DATA1;

                // The type of data, for example Home or Work. Type: INTEGER
                //public static final String TYPE = DataColumns.DATA2;

                // The user defined label for the the contact method. Type: TEXT
                //public static final String LABEL = DataColumns.DATA3;
            //}

            //1.07.00、jsonG07_00WorkOrgType，单位组织信息，TYPE_WORK = 1。1个大项，7个小项;
            jsonG07OrgType.put("jsonG07_00WorkOrgType", new JSONObject(new LinkedHashMap()));
            String arr2JsonG07_00WorkOrgType[][] = {
                    {"__mimetype_0", Organization.CONTENT_ITEM_TYPE},           //Organization.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";
                    {"__mimetype_1", Organization.DATA},                        //Organization.TYPE = "data1";
                    {"__mimetype_2", Organization.TYPE},                        //Organization.TYPE = "data2";
                    {"__mimetype_3", Organization.LABEL},                       //Organization.TYPE = "data3";
                    {"__mimetype_6", String.valueOf(Organization.TYPE_WORK)},   //Organization.TYPE_WORK = 1;
                    {"workCompany", Organization.COMPANY},                      //Organization.COMPANY = "data1";
                    {"workJobTitle", Organization.TITLE},                       //Organization.TITLE = "data4";
                    {"workDepartment", Organization.DEPARTMENT},                //Organization.DEPARTMENT = "data5";
                    {"workJobDescription", Organization.DEPARTMENT},            //Organization.JOB_DESCRIPTION = "data6";
                    {"workSymbol", Organization.DEPARTMENT},                    //Organization.SYMBOL = "data7";
                    {"workPhoneticName", Organization.DEPARTMENT},              //Organization.PHONETIC_NAME = "data8";
                    {"workOfficeLocation", Organization.DEPARTMENT},            //Organization.OFFICE_LOCATION = "data9";
            };
            Arr2Json(arr2JsonG07_00WorkOrgType, jsonG07OrgType.getJSONObject("jsonG07_00WorkOrgType"));

            //1.07.01、jsonG07_01OtherOrgType，其他组织信息，TYPE_OTHER = 2。1个大项，7个小项;
            jsonG07OrgType.put("jsonG07_01OtherOrgType", new JSONObject(new LinkedHashMap()));
            String arr2JsonG07_01OtherOrgType[][] = {
                    {"__mimetype_0", Organization.CONTENT_ITEM_TYPE},           //Organization.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";
                    {"__mimetype_1", Organization.DATA},                        //Organization.TYPE = "data1";
                    {"__mimetype_2", Organization.TYPE},                        //Organization.TYPE = "data2";
                    {"__mimetype_3", Organization.LABEL},                       //Organization.TYPE = "data3";
                    {"__mimetype_6", String.valueOf(Organization.TYPE_OTHER)},  //Organization.TYPE_OTHER = 2;
                    {"otherCompany", Organization.COMPANY},                     //Organization.COMPANY = "data1";
                    {"otherJobTitle", Organization.TITLE},                      //Organization.TITLE = "data4";
                    {"otherDepartment", Organization.DEPARTMENT},               //Organization.DEPARTMENT = "data5";
                    {"otherJobDescription", Organization.DEPARTMENT},           //Organization.JOB_DESCRIPTION = "data6";
                    {"otherSymbol", Organization.DEPARTMENT},                   //Organization.SYMBOL = "data7";
                    {"otherPhoneticName", Organization.DEPARTMENT},             //Organization.PHONETIC_NAME = "data8";
                    {"otherOfficeLocation", Organization.DEPARTMENT},           //Organization.OFFICE_LOCATION = "data9";
            };
            Arr2Json(arr2JsonG07_01OtherOrgType, jsonG07OrgType.getJSONObject("jsonG07_01OtherOrgType"));

            //1.08、jsonG08WebType，获取网站信息
            m_jsonHeader.put("jsonG08WebType", new JSONObject(new LinkedHashMap()));
            String arr2JsonG08WebType[][] = {
                    {"__mimetype_0", Website.CONTENT_ITEM_TYPE},            //Website.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/website";
                    {"__mimetype_1", Website.URL},                          //Website.URL = "data1";
                    {"__mimetype_2", Website.TYPE},                         //Website.URL = "data2";
                    {"__mimetype_3", Website.LABEL},                        //Website.URL = "data3";
                    {"homepage", String.valueOf(Website.TYPE_HOMEPAGE)},    //Website.TYPE_HOMEPAGE = 1;
                    {"blog", String.valueOf(Website.TYPE_BLOG)},            //Website.TYPE_BLOG = 2;
                    {"profile", String.valueOf(Website.TYPE_PROFILE)},      //Website.TYPE_PROFILE = 3;
                    {"home", String.valueOf(Website.TYPE_HOME)},            //Website.TYPE_HOME = 4;
                    {"workPage", String.valueOf(Website.TYPE_WORK)},        //Website.TYPE_WORK = 5;
                    {"ftpPage", String.valueOf(Website.TYPE_FTP)},          //Website.TYPE_FTP = 6;
                    {"otherPage", String.valueOf(Website.TYPE_OTHER)},      //Website.TYPE_OTHER = 7;
            };
            Arr2Json(arr2JsonG08WebType, m_jsonHeader.getJSONObject("jsonG08WebType"));

            //1.09、jsonG09PostalType，查找通讯地址
            JSONObject jsonG09PostalType = new JSONObject(new LinkedHashMap());
            m_jsonHeader.put("jsonG09PostalType", jsonG09PostalType);

            //1.09.00、jsonG09_00HomePostal，住宅通讯地址，TYPE_WORK = 2。1个大项，8个小项;
            jsonG09PostalType.put("jsonG09_00HomePostal", new JSONObject(new LinkedHashMap()));
            String arr2jsonG09_00HomePostal[][] = {
                    {"__mimetype_0", StructuredPostal.CONTENT_ITEM_TYPE},           //StructuredPostal.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";
                    {"__mimetype_1", StructuredPostal.DATA},                        //StructuredPostal.TYPE = "data1";
                    {"__mimetype_2", StructuredPostal.TYPE},                        //StructuredPostal.TYPE = "data2";
                    {"__mimetype_3", StructuredPostal.LABEL},                       //StructuredPostal.TYPE = "data3";
                    {"__mimetype_6", String.valueOf(StructuredPostal.TYPE_HOME)},   //StructuredPostal.TYPE_HOME = 1;
                    {"homeFormattedAddress", StructuredPostal.FORMATTED_ADDRESS},   //StructuredPostal.FORMATTED_ADDRESS = "data1";
                    {"homeStreet", StructuredPostal.STREET},                        //StructuredPostal.STREET = "data4";
                    {"homeBox", StructuredPostal.POBOX},                            //StructuredPostal.POBOX = "data5";
                    {"homeArea", StructuredPostal.NEIGHBORHOOD},                    //StructuredPostal.NEIGHBORHOOD = "data6";
                    {"homeCity", StructuredPostal.CITY},                            //StructuredPostal.CITY = "data7";
                    {"homeState", StructuredPostal.REGION},                         //StructuredPostal.REGION = "data8";
                    {"homeZip", StructuredPostal.POSTCODE},                         //StructuredPostal.POSTCODE = "data9";
                    {"homeCountry", StructuredPostal.COUNTRY},                      //StructuredPostal.COUNTRY = "data10";
            };
            Arr2Json(arr2jsonG09_00HomePostal, jsonG09PostalType.getJSONObject("jsonG09_00HomePostal"));

            //1.09.01、jsonG09_01WorkPostal，单位通讯地址，TYPE_HOME = 1。1个大项，8个小项;
            jsonG09PostalType.put("jsonG09_01WorkPostal", new JSONObject(new LinkedHashMap()));
            String arr2jsonG09_01WorkPostal[][] = {
                    {"__mimetype_0", StructuredPostal.CONTENT_ITEM_TYPE},           //StructuredPostal.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";
                    {"__mimetype_1", StructuredPostal.DATA},                        //StructuredPostal.TYPE = "data1";
                    {"__mimetype_2", StructuredPostal.TYPE},                        //StructuredPostal.TYPE = "data2";
                    {"__mimetype_3", StructuredPostal.LABEL},                       //StructuredPostal.TYPE = "data3";
                    {"__mimetype_6", String.valueOf(StructuredPostal.TYPE_WORK)},   //StructuredPostal.TYPE_WORK = 2;
                    {"workFormattedAddress", StructuredPostal.FORMATTED_ADDRESS},   //StructuredPostal.FORMATTED_ADDRESS = "data1";
                    {"workStreet", StructuredPostal.STREET},                        //StructuredPostal.STREET = "data4";
                    {"workBox", StructuredPostal.POBOX},                            //StructuredPostal.POBOX = "data5";
                    {"workArea", StructuredPostal.NEIGHBORHOOD},                    //StructuredPostal.NEIGHBORHOOD = "data6";
                    {"workCity", StructuredPostal.CITY},                            //StructuredPostal.CITY = "data7";
                    {"workState", StructuredPostal.REGION},                         //StructuredPostal.REGION = "data8";
                    {"workZip", StructuredPostal.POSTCODE},                         //StructuredPostal.POSTCODE = "data9";
                    {"workCountry", StructuredPostal.COUNTRY},                      //StructuredPostal.COUNTRY = "data10";
            };
            Arr2Json(arr2jsonG09_01WorkPostal, jsonG09PostalType.getJSONObject("jsonG09_01WorkPostal"));

            //1.09.02、jsonG09_02OtherPostal，其他通讯地址，TYPE_OTHER = 3。1个大项，8个小项;
            jsonG09PostalType.put("jsonG09_02OtherPostal", new JSONObject(new LinkedHashMap()));
            String arr2JsonG09_02OtherPostal[][] = {
                    {"__mimetype_0", StructuredPostal.CONTENT_ITEM_TYPE},           //StructuredPostal.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";
                    {"__mimetype_1", StructuredPostal.DATA},                        //StructuredPostal.TYPE = "data1";
                    {"__mimetype_2", StructuredPostal.TYPE},                        //StructuredPostal.TYPE = "data2";
                    {"__mimetype_3", StructuredPostal.LABEL},                       //StructuredPostal.TYPE = "data3";
                    {"__mimetype_6", String.valueOf(StructuredPostal.TYPE_OTHER)},  //StructuredPostal.TYPE_OTHER = 3;
                    {"otherFormattedAddress", StructuredPostal.FORMATTED_ADDRESS},  //StructuredPostal.FORMATTED_ADDRESS = "data1";
                    {"otherStreet", StructuredPostal.STREET},                       //StructuredPostal.STREET = "data4";
                    {"otherBox", StructuredPostal.POBOX},                           //StructuredPostal.POBOX = "data5";
                    {"otherArea", StructuredPostal.NEIGHBORHOOD},                   //StructuredPostal.NEIGHBORHOOD = "data6";
                    {"otherCity", StructuredPostal.CITY},                           //StructuredPostal.CITY = "data7";
                    {"otherState", StructuredPostal.REGION},                        //StructuredPostal.REGION = "data8";
                    {"otherZip", StructuredPostal.POSTCODE},                        //StructuredPostal.POSTCODE = "data9";
                    {"otherCountry", StructuredPostal.COUNTRY},                     //StructuredPostal.COUNTRY = "data10";
            };
            Arr2Json(arr2JsonG09_02OtherPostal, jsonG09PostalType.getJSONObject("jsonG09_02OtherPostal"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // ------------------- 联系人信息列表头 - End
    }

    private void Arr2Json(String[][] s2Arr, JSONObject jsonObject) {
        try {
            for(String[] sArr : s2Arr){
                JSONObject json = new JSONObject(new LinkedHashMap());
                //为避免与联系人信息标识重名，前面添加双下划线
                json.put("__first", sArr[1]);        // sArr[1] 用于存放 mimetype 类型编码
                json.put("__second", "0");   // 用于存放用户值
                jsonObject.put(sArr[0], json);             // sArr[0] 用于存放 mimetype 类型可读标题

                //经输出后查看，混乱无用
                //m_lhmapFields.put(sArr[0], sArr[1]);        //保存联系人信息列表头所有原生字段，供插入联系人使用
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
