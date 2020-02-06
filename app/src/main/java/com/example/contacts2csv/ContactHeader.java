//存储通讯录所有联系人字段的数据类ContactsData
//20190年12月09日
package com.example.contacts2csv;

import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.provider.ContactsContract.CommonDataKinds.Relation;
import android.provider.ContactsContract.CommonDataKinds.SipAddress;

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
    //public Map<String, String> mMapCol;           //联系人信息列表头
    //public Vector<Pair<String, String>> mVecCol;  //联系人信息列表头，Map会自动排序，用Vector也不合理
    public JSONObject m_jsonHeader;                 //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject

    //经输出后查看，混乱无用
    //public LinkedHashMap m_lhmapFields;           //联系人信息列表头所有原生字段

    //联系人所有字段的MIMETYPE解释
    //import android.provider.ContactsContract.CommonDataKinds.xxx;
    //来自MIMETYPE字段官方文档：https://developer.android.google.cn/reference/android/provider/ContactsContract.Data.html
    String[] MIMETYPES_temp = new String[]{
            //mimetype数据字段                  //存储json变量            //旧存储json变量        //数据内容
            StructuredName.CONTENT_ITEM_TYPE,   //jsonG00StructName       //jsonG00StructName     //联系人名称 (G：Group)
            Phone.CONTENT_ITEM_TYPE,            //jsonG01Phone            //jsonG01Phone          //联系人电话
            Email.CONTENT_ITEM_TYPE,            //jsonG02Email            //jsonG02Email          //邮箱
            Photo.CONTENT_ITEM_TYPE,            //jsonG03Photo            //                      //头像
            Organization.CONTENT_ITEM_TYPE,     //jsonG04OrgSet           //jsonG07OrgType        //公司
            Im.CONTENT_ITEM_TYPE,               //jsonG05ImSet            //jsonG04Im             //即时通信
            Nickname.CONTENT_ITEM_TYPE,         //jsonG06NickName         //jsonG06NickName       //昵称
            Note.CONTENT_ITEM_TYPE,             //jsonG07Note             //jsonG05Remark         //备注
            StructuredPostal.CONTENT_ITEM_TYPE, //jsonG08PostalSet        //jsonG09PostalType     //地址
            GroupMembership.CONTENT_ITEM_TYPE,  //jsonG09GroupMember      //                      //分组信息
            Website.CONTENT_ITEM_TYPE,          //jsonG10WebSet           //jsonG08WebType        //网站
            Event.CONTENT_ITEM_TYPE,            //jsonG11Event            //jsonG03Event          //重要日期
            Relation.CONTENT_ITEM_TYPE,         //jsonG12Relation         //                      //家庭关系
            SipAddress.CONTENT_ITEM_TYPE,       //jsonG13SipAddress       //                      //网络电话
    };

    public ContactHeader() {
        // ------------------- 联系人信息列表头 - Begin
        try {
            //联系人信息列表头，Map会自动排序，用Vector也不合理，所以用JSONObject
            m_jsonHeader = new JSONObject(new LinkedHashMap());
            //m_lhmapFields = new LinkedHashMap();

            //命名惯例：变量名首字母一般小写，函数名、类名、常量名首字母一般大写
            //00、jsonG00StructName，获得通讯录中联系人的名字。G00 Group00。
            m_jsonHeader.put("jsonG00StructName", new JSONObject(new LinkedHashMap()));
            String arr2JsonG00StructName[][] = {
                    {"__mimetype_item", StructuredName.CONTENT_ITEM_TYPE},          //StructuredName.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/name";
                    {"__mimetype_fun", "fun02"},                                    //无需处理 xxx.TYPE_CUSTOM，用 dumpJson4layAll() 处理
                    {"displayName", StructuredName.DISPLAY_NAME},                   //StructuredName.DISPLAY_NAME = "data1";
                    {"lastName", StructuredName.GIVEN_NAME},                        //StructuredName.GIVEN_NAME = "data2";
                    {"firstName", StructuredName.FAMILY_NAME},                      //StructuredName.FAMILY_NAME = "data3";
                    {"prefix", StructuredName.PREFIX},                              //StructuredName.PREFIX = "data4";
                    {"middleName", StructuredName.MIDDLE_NAME},                     //StructuredName.MIDDLE_NAME = "data5";
                    {"suffix", StructuredName.SUFFIX},                              //StructuredName.SUFFIX = "data6";
                    {"phoneticLastName", StructuredName.PHONETIC_GIVEN_NAME},       //StructuredName.PHONETIC_GIVEN_NAME = "data7";
                    {"phoneticMiddleName", StructuredName.PHONETIC_MIDDLE_NAME},    //StructuredName.PHONETIC_FAMILY_NAME = "data8";
                    {"phoneticFirstName", StructuredName.PHONETIC_FAMILY_NAME},     //StructuredName.PHONETIC_MIDDLE_NAME = "data9";
            };
            Arr2Json(arr2JsonG00StructName, m_jsonHeader.getJSONObject("jsonG00StructName"));
            //以下测试表明，初始化数组时，最后多一个英文逗号，不会增加一个空元素
            //System.out.println("jsonG00StructName.length() = " + m_jsonHeader.getJSONObject("jsonG00StructName").length());
            //I/System.out: jsonG00StructName.length() = 11
            //还有 AS3.5 AVD 未启动时，点击 AS3.5 的 Run'app' 按钮启动 app 时，AVD 明明已经正常启动，而 app 往往始一直卡死等待，app 界面迟迟不出现
            // 这可能是因为 AVD 启动时，后台自动加载上次关机时正在运行的旧版 app (不显示界面)，导致 AVD 重启时新版 app 无法正常启动。
            // 解决办法是点击 AVD 中的任务键，关闭已经在后台运行的旧版 app (不显示界面)，新版 app 便会正常运行

            //Android 获取联系人的号码的Phone.TYPE_CUSTOM类型处理
            //......
            //
            //int index = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            //int typeindex = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
            //int labelindex = phonesCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
            //
            //String phoneNumber = phonesCursor.getString(index);
            //int phone_type = phonesCursor.getInt(typeindex);
            //
            //String phoneLabel = "";
            //if (phone_type == ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM)
            //　　phoneLabel = phones.getString(labelindex);
            //else
            //　　phoneLabel = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(mContext.getResources(), phone_type, "");
            //......

            // Phone 数据有三个字段存储：NUMBER 为 Phone 值；TYPE 为类型，当为自定义（TYPE_CUSTOM）时，LABEL 字段要写入用户自定义的类型
            //01、jsonG01Phone，获取电话信息。
            m_jsonHeader.put("jsonG01Phone", new JSONObject(new LinkedHashMap()));
            String arr2JsonG01Phone[][] = {
                    {"__mimetype_item", Phone.CONTENT_ITEM_TYPE},               //Phone.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/phone_v2";
                    {"__mimetype_data", Phone.NUMBER},                          //Phone.NUMBER = "data1";
                    {"__mimetype_type", Phone.TYPE},                            //Phone.TYPE = "data2";
                    {"__mimetype_label", Phone.LABEL},                          //Phone.TYPE = "data3";
                    {"__mimetype_fun", "fun01"},                                // "jsonG01Phone"，需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customNum", String.valueOf(Phone.TYPE_CUSTOM)},           //Phone.TYPE_CUSTOM = 0;    //Put the actual type in LABEL
                    {"homeNum", String.valueOf(Phone.TYPE_HOME)},               //Phone.TYPE_HOME = 1;
                    {"mobile", String.valueOf(Phone.TYPE_MOBILE)},              //Phone.TYPE_MOBILE = 2;
                    {"workNum", String.valueOf(Phone.TYPE_WORK)},               //Phone.TYPE_WORK = 3;
                    {"workFax", String.valueOf(Phone.TYPE_FAX_WORK)},           //Phone.TYPE_FAX_WORK = 4;
                    {"homeFax", String.valueOf(Phone.TYPE_FAX_HOME)},           //Phone.TYPE_FAX_HOME = 5;
                    {"pager", String.valueOf(Phone.TYPE_PAGER)},                //Phone.TYPE_PAGER = 6;
                    {"otherNum", String.valueOf(Phone.TYPE_OTHER)},             //Phone.TYPE_OTHER = 7;
                    {"callbackNum", String.valueOf(Phone.TYPE_CALLBACK)},       //Phone.TYPE_CALLBACK = 8;
                    {"carNum", String.valueOf(Phone.TYPE_CAR)},                 //Phone.TYPE_CAR = 9;
                    {"compMainTel", String.valueOf(Phone.TYPE_COMPANY_MAIN)},   //Phone.TYPE_COMPANY_MAIN = 10;
                    {"isdn", String.valueOf(Phone.TYPE_ISDN)},                  //Phone.TYPE_ISDN = 11;
                    {"mainTel", String.valueOf(Phone.TYPE_MAIN)},               //Phone.TYPE_MAIN = 12;
                    {"otherFax", String.valueOf(Phone.TYPE_OTHER_FAX)},         //Phone.TYPE_OTHER_FAX = 13;
                    {"wirelessDev", String.valueOf(Phone.TYPE_RADIO)},          //Phone.TYPE_RADIO = 14;
                    {"telegram", String.valueOf(Phone.TYPE_TELEX)},             //Phone.TYPE_TELEX = 15;
                    {"tty_tdd", String.valueOf(Phone.TYPE_TTY_TDD)},            //Phone.TYPE_TTY_TDD = 16;
                    {"workMobile", String.valueOf(Phone.TYPE_WORK_MOBILE)},     //Phone.TYPE_WORK_MOBILE = 17;
                    {"workPager", String.valueOf(Phone.TYPE_WORK_PAGER)},       //Phone.TYPE_WORK_PAGER = 18;
                    {"assistantNum", String.valueOf(Phone.TYPE_ASSISTANT)},     //Phone.TYPE_ASSISTANT = 19;
                    {"mms", String.valueOf(Phone.TYPE_MMS)},                    //Phone.TYPE_MMS = 20;
            };
            Arr2Json(arr2JsonG01Phone, m_jsonHeader.getJSONObject("jsonG01Phone"));

            //02、jsonG02Email，查找Email地址。
            m_jsonHeader.put("jsonG02Email", new JSONObject(new LinkedHashMap()));
            String arr2JsonG02Email[][] = {
                    {"__mimetype_item", Email.CONTENT_ITEM_TYPE},       //Email.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/email_v2";
                    {"__mimetype_data", Email.ADDRESS},                 //Email.DATA = "data1";
                    {"__mimetype_type", Email.TYPE},                    //Email.TYPE = "data2";
                    {"__mimetype_label", Email.LABEL},                  //Email.LABEL = "data3";
                    {"__mimetype_fun", "fun00"},                        // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customEmail", String.valueOf(Email.TYPE_CUSTOM)}, //Email.TYPE_CUSTOM = 0;    //Put the actual type in LABEL
                    {"homeEmail", String.valueOf(Email.TYPE_HOME)},     //Email.TYPE_HOME = 1;
                    {"workEmail", String.valueOf(Email.TYPE_WORK)},     //Email.TYPE_WORK = 2;
                    {"otherEmail", String.valueOf(Email.TYPE_OTHER)},   //Email.TYPE_OTHER = 3;
                    {"mobileEmail", String.valueOf(Email.TYPE_MOBILE)}, //Email.TYPE_MOBILE = 4;
            };
            Arr2Json(arr2JsonG02Email, m_jsonHeader.getJSONObject("jsonG02Email"));

            //03、jsonG03Photo，头像。
            m_jsonHeader.put("jsonG03Photo", new JSONObject(new LinkedHashMap()));
            String arr2jsonG03Photo[][] = {
                    {"__mimetype_item", Photo.CONTENT_ITEM_TYPE},           //Photo.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/photo";
                    {"__mimetype_data", Photo.PHOTO_FILE_ID},               //Photo.PHOTO_FILE_ID = "data14";   //用于显示较大的头像
                    {"__mimetype_fun", "fun03"},                            // "jsonG03Photo"，单独用 dumpPhoto() 处理
                    {"photo", Photo.PHOTO},                                 //Photo.PHOTO = "data15";
            };
            Arr2Json(arr2jsonG03Photo, m_jsonHeader.getJSONObject("jsonG03Photo"));

            //04、jsonG04OrgSet，获取组织信息。Set 数据需要分类处理，包括 jsonG04OrgSet、jsonG05ImSet、jsonG08PostalSet
            JSONObject jsonG04OrgSet = new JSONObject(new LinkedHashMap());
            m_jsonHeader.put("jsonG04OrgSet", jsonG04OrgSet);
            String arr2jsonG04OrgSet[][] = {
                    {"__mimetype_item", Organization.CONTENT_ITEM_TYPE},        //Organization.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/organization";
                    {"__mimetype_data", Organization.COMPANY},                  //Organization.COMPANY = "data1";
                    {"__mimetype_type", Organization.TYPE},                     //Organization.TYPE = "data2";
                    {"__mimetype_label", Organization.LABEL},                   //Organization.LABEL = "data3";
                    {"__mimetype_subtype_custom", String.valueOf(Organization.TYPE_CUSTOM)},  //Organization.TYPE_CUSTOM = 0;    //Put the actual type in LABEL
                    {"__mimetype_subtype_work", String.valueOf(Organization.TYPE_WORK)},      //Organization.TYPE_WORK = 2;
                    {"__mimetype_subtype_other", String.valueOf(Organization.TYPE_OTHER)},    //Organization.TYPE_WORK = 3;
                    {"__mimetype_fun", "fun04"},                                // Set 数据需要分类处理，用 fun04_dumpJson4laySet() 处理
                    {"JobTitle", Organization.TITLE},                           //Organization.TITLE = "data4";
                    {"Department", Organization.DEPARTMENT},                    //Organization.DEPARTMENT = "data5";
                    {"JobDescription", Organization.JOB_DESCRIPTION},           //Organization.JOB_DESCRIPTION = "data6";
                    {"Symbol", Organization.SYMBOL},                            //Organization.SYMBOL = "data7";
                    {"PhoneticName", Organization.PHONETIC_NAME},               //Organization.PHONETIC_NAME = "data8";
                    {"OfficeLocation", Organization.OFFICE_LOCATION},           //Organization.OFFICE_LOCATION = "data9";
                    {"PhoneticNameStyle", Organization.PHONETIC_NAME_STYLE},    //Organization.PHONETIC_NAME_STYLE = "data10";
            };
            Arr2Json(arr2jsonG04OrgSet, m_jsonHeader.getJSONObject("jsonG04OrgSet"));

            // 对im操作
            // A、首先判断 Im.TYPE(data2) 的类型；
            // B、若 data2 中取出的是自定义类型 Im.TYPE_CUSTOM(0)，就要取 Im.LABEL(data3) 中的值；此时在 CUSTOM_PROTOCOL(data6) 中有一份和data3一样的数据
            // C、接着取 Im.PROTOCOL(data5) 的值，判断是哪种Im。
            //05、jsonG05ImSet，即时消息。Set 数据需要分类处理，包括 jsonG04OrgSet、jsonG05ImSet、jsonG08PostalSet
            m_jsonHeader.put("jsonG05ImSet", new JSONObject(new LinkedHashMap()));
            String arr2jsonG05ImSet[][] = {
                    {"__mimetype_item", Im.CONTENT_ITEM_TYPE},                      //Im.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_data", Im.DATA},                                   //Im.DATA = "data1";
                    {"__mimetype_type", Im.TYPE},                                   //Im.TYPE = "data2";
                    {"__mimetype_label", Im.LABEL},                                 //Im.LABEL = "data3";
                    {"__mimetype_protocal", Im.PROTOCOL},                           //Im.PROTOCOL = "data5";
                    {"__mimetype_custom_protocal", Im.CUSTOM_PROTOCOL},             //Im.PROTOCOL = "data6";
                    {"__mimetype_subtype_custom", String.valueOf(Im.TYPE_CUSTOM)},  //Im.TYPE_CUSTOM = 0;
                    {"__mimetype_subtype_home", String.valueOf(Im.TYPE_HOME)},      //Im.TYPE_HOME = 1;
                    {"__mimetype_subtype_work", String.valueOf(Im.TYPE_WORK)},      //Im.TYPE_WORK = 2;
                    {"__mimetype_subtype_other", String.valueOf(Im.TYPE_OTHER)},    //Im.TYPE_WORK = 3;
                    {"__mimetype_fun", "fun00"},                                    // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"CustomIm", String.valueOf(Im.PROTOCOL_CUSTOM)},               //Im.PROTOCOL_CUSTOM = -1;
                    {"AimIm(CustomTypeIm)", String.valueOf(Im.PROTOCOL_AIM)},       //Im.PROTOCOL_AIM = 0;
                    {"MsnIm", String.valueOf(Im.PROTOCOL_MSN)},                     //Im.PROTOCOL_MSN = 1;
                    {"YahooIm", String.valueOf(Im.PROTOCOL_YAHOO)},                 //Im.PROTOCOL_YAHOO = 2;
                    {"SkypeIm", String.valueOf(Im.PROTOCOL_SKYPE)},                 //Im.PROTOCOL_SKYPE = 3;
                    {"QqIm", String.valueOf(Im.PROTOCOL_QQ)},                       //Im.PROTOCOL_QQ = 4;
                    {"GoogleTalkIm", String.valueOf(Im.PROTOCOL_GOOGLE_TALK)},      //Im.PROTOCOL_GOOGLE_TALK = 5;
                    {"IcqIm", String.valueOf(Im.PROTOCOL_ICQ)},                     //Im.PROTOCOL_ICQ = 6;
                    {"JabberIm", String.valueOf(Im.PROTOCOL_JABBER)},               //Im.PROTOCOL_JABBER = 7;
                    {"NetmeetingIm", String.valueOf(Im.PROTOCOL_NETMEETING)},       //Im.PROTOCOL_NETMEETING = 8;
            };
            Arr2Json(arr2jsonG05ImSet, m_jsonHeader.getJSONObject("jsonG05ImSet"));

            //06、jsonG06NickName，获取昵称信息。
            m_jsonHeader.put("jsonG06NickName", new JSONObject(new LinkedHashMap()));
            String arr2JsonG06NickName[][] = {
                    {"__mimetype_item", Nickname.CONTENT_ITEM_TYPE},                //Nickname.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/nickname";
                    {"__mimetype_data", Nickname.NAME},                             //Nickname.NAME = "data1";
                    {"__mimetype_type", Nickname.TYPE},                             //Nickname.TYPE = "data2";
                    {"__mimetype_label", Nickname.LABEL},                           //Nickname.LABEL = "data3";
                    {"__mimetype_fun", "fun00"},                                    // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customNickName", String.valueOf(Nickname.TYPE_CUSTOM)},       //Nickname.TYPE_CUSTOM = 0;
                    {"defaultNickName", String.valueOf(Nickname.TYPE_DEFAULT)},     //Nickname.TYPE_DEFAULT = 1;
                    {"otherNickName", String.valueOf(Nickname.TYPE_OTHER_NAME)},    //Nickname.TYPE_OTHER_NAME = 2;
                    {"maindenNickName", String.valueOf(Nickname.TYPE_MAIDEN_NAME)}, //Nickname.TYPE_MAIDEN_NAME = 3;
                    {"shortNickName", String.valueOf(Nickname.TYPE_SHORT_NAME)},    //Nickname.TYPE_SHORT_NAME = 4;
                    {"initialsNickName", String.valueOf(Nickname.TYPE_INITIALS)},   //Nickname.TYPE_INITIALS = 5;
            };      //@TYPE_MAINDEN_NAME deprecated Use TYPE_MAIDEN_NAME instead.
            Arr2Json(arr2JsonG06NickName, m_jsonHeader.getJSONObject("jsonG06NickName"));

            //07、jsonG07Note，获取备注信息。
            m_jsonHeader.put("jsonG07Note", new JSONObject(new LinkedHashMap()));
            String arr2jsonG07Note[][] = {                          //无 xxx.TYPE_CUSTOM(实际类型在 LABEL 中)，用 dumpJson4layAll() 处理
                    {"__mimetype_item", Note.CONTENT_ITEM_TYPE},    //Note.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";
                    {"__mimetype_fun", "fun02"},                    //无需处理 xxx.TYPE_CUSTOM，用 dumpJson4layAll() 处理
                    {"note", Note.NOTE}                             //Note.NOTE = "data1";
            };
            Arr2Json(arr2jsonG07Note, m_jsonHeader.getJSONObject("jsonG07Note"));

            //08、jsonG08PostalSet，查找通讯地址。Set 数据需要分类处理，包括 jsonG04OrgSet、jsonG05ImSet、jsonG08PostalSet
            JSONObject jsonG08PostalSet = new JSONObject(new LinkedHashMap());
            m_jsonHeader.put("jsonG08PostalSet", jsonG08PostalSet);
            String arr2jsonG08PostalSet[][] = {                                     //有 xxx.TYPE_CUSTOM(实际类型在 LABEL 中)，用 fun00_dumpJson4lay() 处理
                    {"__mimetype_item", StructuredPostal.CONTENT_ITEM_TYPE},        //StructuredPostal.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/postal-address_v2";
                    {"__mimetype_data", StructuredPostal.FORMATTED_ADDRESS},        //StructuredPostal.FORMATTED_ADDRESS = "data1";
                    {"__mimetype_type", StructuredPostal.TYPE},                     //StructuredPostal.TYPE = "data2";
                    {"__mimetype_label", StructuredPostal.LABEL},                   //StructuredPostal.TYPE = "data3";
                    {"__mimetype_subtype_custom", String.valueOf(StructuredPostal.TYPE_CUSTOM)},  //StructuredPostal.TYPE_CUSTOM = 0;
                    {"__mimetype_subtype_home", String.valueOf(StructuredPostal.TYPE_HOME)},      //StructuredPostal.TYPE_HOME = 1;
                    {"__mimetype_subtype_work", String.valueOf(StructuredPostal.TYPE_WORK)},      //StructuredPostal.TYPE_WORK = 2;
                    {"__mimetype_subtype_other", String.valueOf(StructuredPostal.TYPE_OTHER)},    //StructuredPostal.TYPE_WORK = 3;
                    {"__mimetype_fun", "fun04"},                                    // Set 数据需要分类处理，用 fun04_dumpJson4laySet() 处理
                    {"Street", StructuredPostal.STREET},                            //StructuredPostal.STREET = "data4";
                    {"Box", StructuredPostal.POBOX},                                //StructuredPostal.POBOX = "data5";
                    {"Area", StructuredPostal.NEIGHBORHOOD},                        //StructuredPostal.NEIGHBORHOOD = "data6";
                    {"City", StructuredPostal.CITY},                                //StructuredPostal.CITY = "data7";
                    {"State", StructuredPostal.REGION},                             //StructuredPostal.REGION = "data8";
                    {"Zip", StructuredPostal.POSTCODE},                             //StructuredPostal.POSTCODE = "data9";
                    {"Country", StructuredPostal.COUNTRY},                          //StructuredPostal.COUNTRY = "data10";
            };
            Arr2Json(arr2jsonG08PostalSet, m_jsonHeader.getJSONObject("jsonG08PostalSet"));

            //09、jsonG09GroupMember，分组信息。
            m_jsonHeader.put("jsonG09GroupMember", new JSONObject(new LinkedHashMap()));
            String arr2jsonG09GroupMember[][] = {
                    {"__mimetype_item", GroupMembership.CONTENT_ITEM_TYPE}, //GroupMembership.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/group_membership";
                    {"__mimetype_fun", "fun02"},                            //无需处理 xxx.TYPE_CUSTOM，用 dumpJson4layAll() 处理
                    {"groupId", GroupMembership.GROUP_ROW_ID},              //GroupMembership.GROUP_ROW_ID = "data1";
                    {"groupSourceId", GroupMembership.GROUP_SOURCE_ID},     //GroupMembership.GROUP_SOURCE_ID = "group_sourceid";
                    //{"groupsTitle", Groups.TITLE},                          //Group.TITLE = "title";

            };
            Arr2Json(arr2jsonG09GroupMember, m_jsonHeader.getJSONObject("jsonG09GroupMember"));

            //10、jsonG10Website，获取网站信息。
            m_jsonHeader.put("jsonG10Website", new JSONObject(new LinkedHashMap()));
            String arr2jsonG10Website[][] = {
                    {"__mimetype_item", Website.CONTENT_ITEM_TYPE},         //Website.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/website";
                    {"__mimetype_data", Website.URL},                       //Website.URL = "data1";
                    {"__mimetype_type", Website.TYPE},                      //Website.URL = "data2";
                    {"__mimetype_label", Website.LABEL},                    //Website.URL = "data3";
                    {"__mimetype_4", Website.DATA},                         //Website.DATA = "data1";
                    {"__mimetype_fun", "fun00"},                            // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customWebsite", String.valueOf(Website.TYPE_CUSTOM)}, //Website.TYPE_CUSTOM = 0;    //Put the actual type in LABEL
                    {"homepage", String.valueOf(Website.TYPE_HOMEPAGE)},    //Website.TYPE_HOMEPAGE = 1;
                    {"blog", String.valueOf(Website.TYPE_BLOG)},            //Website.TYPE_BLOG = 2;
                    {"profile", String.valueOf(Website.TYPE_PROFILE)},      //Website.TYPE_PROFILE = 3;
                    {"home", String.valueOf(Website.TYPE_HOME)},            //Website.TYPE_HOME = 4;
                    {"workPage", String.valueOf(Website.TYPE_WORK)},        //Website.TYPE_WORK = 5;
                    {"ftpPage", String.valueOf(Website.TYPE_FTP)},          //Website.TYPE_FTP = 6;
                    {"otherPage", String.valueOf(Website.TYPE_OTHER)},      //Website.TYPE_OTHER = 7;
            };
            Arr2Json(arr2jsonG10Website, m_jsonHeader.getJSONObject("jsonG10Website"));

            //11、jsonG11Event，查找Event重要事件、纪念日。
            m_jsonHeader.put("jsonG11Event", new JSONObject(new LinkedHashMap()));
            String arr2jsonG11Event[][] = {
                    {"__mimetype_item", Event.CONTENT_ITEM_TYPE},           //Event.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/contact_event";
                    {"__mimetype_data", Event.START_DATE},                  //Event.START_DATE = "data1";
                    {"__mimetype_type", Event.TYPE},                        //Event.TYPE = "data2";
                    {"__mimetype_label", Event.LABEL},                      //Event.LABEL = "data3";
                    {"__mimetype_fun", "fun00"},                            // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customEvent", String.valueOf(Event.TYPE_CUSTOM)},     //Event.TYPE_CUSTOM = 0;    //Put the actual type in LABEL
                    {"anniversary", String.valueOf(Event.TYPE_ANNIVERSARY)},//Event.TYPE_ANNIVERSARY = 1;
                    {"otherday", String.valueOf(Event.TYPE_OTHER)},         //Event.TYPE_OTHER = 2;
                    {"birthday", String.valueOf(Event.TYPE_BIRTHDAY)},      //Event.TYPE_BIRTHDAY = 3;
            };
            Arr2Json(arr2jsonG11Event, m_jsonHeader.getJSONObject("jsonG11Event"));

            //12、jsonG12Relation，家庭关系。
            m_jsonHeader.put("jsonG12Relation", new JSONObject(new LinkedHashMap()));
            String arr2jsonG12Relation[][] = {
                    {"__mimetype_item", Relation.CONTENT_ITEM_TYPE},                        //Relation.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/relation";
                    {"__mimetype_data", Relation.NAME},                                     //Relation.NAME = "data1";
                    {"__mimetype_type", Relation.TYPE},                                     //Relation.TYPE = "data2";
                    {"__mimetype_label", Relation.LABEL},                                   //Relation.LABEL = "data3";
                    {"__mimetype_fun", "fun00"},                                            // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customRelation", String.valueOf(Relation.TYPE_CUSTOM)},               //Relation.TYPE_CUSTOM = 0;    //Put the actual type in LABEL
                    {"assistant", String.valueOf(Relation.TYPE_ASSISTANT)},                 //Relation.TYPE_ASSISTANT = 1;
                    {"brother", String.valueOf(Relation.TYPE_BROTHER)},                     //Relation.TYPE_BROTHER = 2;
                    {"child", String.valueOf(Relation.TYPE_CHILD)},                         //Relation.TYPE_CHILD = 3;
                    {"domestic_partner", String.valueOf(Relation.TYPE_DOMESTIC_PARTNER)},   //Relation.TYPE_DOMESTIC_PARTNER = 4;
                    {"father", String.valueOf(Relation.TYPE_FATHER)},                       //Relation.TYPE_FATHER = 5;
                    {"friend", String.valueOf(Relation.TYPE_FRIEND)},                       //Relation.TYPE_FRIEND = 6;
                    {"manager", String.valueOf(Relation.TYPE_MANAGER)},                     //Relation.TYPE_MANAGER = 7;
                    {"mother", String.valueOf(Relation.TYPE_MOTHER)},                       //Relation.TYPE_MOTHER = 8;
                    {"parent", String.valueOf(Relation.TYPE_PARENT)},                       //Relation.TYPE_PARENT = 9;
                    {"partner", String.valueOf(Relation.TYPE_PARTNER)},                     //Relation.TYPE_PARTNER = 10;
                    {"referred_by", String.valueOf(Relation.TYPE_REFERRED_BY)},             //Relation.TYPE_REFERRED_BY = 11;
                    {"relative", String.valueOf(Relation.TYPE_RELATIVE)},                   //Relation.TYPE_RELATIVE = 12;
                    {"sister", String.valueOf(Relation.TYPE_SISTER)},                       //Relation.TYPE_SISTER = 13;
                    {"spouse", String.valueOf(Relation.TYPE_SPOUSE)},                       //Relation.TYPE_SPOUSE = 14;
            };
            Arr2Json(arr2jsonG12Relation, m_jsonHeader.getJSONObject("jsonG12Relation"));

            //13、jsonG13SipAddress，网络电话。
            m_jsonHeader.put("jsonG13SipAddress", new JSONObject(new LinkedHashMap()));
            String arr2jsonG13SipAddress[][] = {
                    {"__mimetype_item", SipAddress.CONTENT_ITEM_TYPE},      //SipAddress.CONTENT_ITEM_TYPE = "vnd.android.cursor.item/sip_address";
                    {"__mimetype_data", SipAddress.SIP_ADDRESS},            //SipAddress.DATA = "data1";
                    {"__mimetype_type", SipAddress.TYPE},                   //SipAddress.TYPE = "data2";
                    {"__mimetype_label", SipAddress.LABEL},                 //SipAddress.LABEL = "data3";
                    {"__mimetype_fun", "fun00"},                            // 默认需要处理 xxx.TYPE_CUSTOM，用 fun00_dumpJson4lay() 处理
                    {"customSip", String.valueOf(SipAddress.TYPE_CUSTOM)},  //SipAddress.TYPE_CUSTOM = 0
                    {"homeSip", String.valueOf(SipAddress.TYPE_HOME)},      //SipAddress.TYPE_HOME = 1;
                    {"workSip", String.valueOf(SipAddress.TYPE_WORK)},      //SipAddress.TYPE_WORK = 2;
                    {"otherSip", String.valueOf(SipAddress.TYPE_OTHER)},    //SipAddress.TYPE_WORK = 3;
            };
            Arr2Json(arr2jsonG13SipAddress, m_jsonHeader.getJSONObject("jsonG13SipAddress"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // ------------------- 联系人信息列表头 - End
    }

    private void Arr2Json(String[][] s2Arr, JSONObject jsonObject) {
        try {
            for (String[] sArr : s2Arr) {
                JSONObject json = new JSONObject(new LinkedHashMap());
                //为避免与联系人信息标识重名，前面添加双下划线
                json.put("__first", sArr[1]);       // sArr[1] 用于存放 mimetype 类型编码
                json.put("__second", "0");  // 用于存放用户值
                //json.put("__third", "");                // 用于存放字段名前缀
                jsonObject.put(sArr[0], json);            // sArr[0] 用于存放 mimetype 类型可读标题

                //经输出后查看，混乱无用
                //m_lhmapFields.put(sArr[0], sArr[1]);    //保存联系人信息列表头所有原生字段，供插入联系人使用
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
