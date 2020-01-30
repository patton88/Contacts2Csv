package com.example.contacts2csv;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;

import static com.example.contacts2csv.MainActivity.m_MA;

/**
 * @author glsite.com
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class ContactInsertDo {
    private final String m_sTAG = getClass().getSimpleName();

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

    public String doInsertContact(Context context, String sContact) {
        String sArr[] = sContact.split(",", -1); //添加后面的参数，保证结尾空字符串不会被丢弃。
        // 1.判断是否为空
        Log.d(m_sTAG, "in doInsertToContact contactInfo = null? " + (sArr == null));


        for (int i = 0; i < sArr.length; i ++) {

        }

        return "";
    }

}
