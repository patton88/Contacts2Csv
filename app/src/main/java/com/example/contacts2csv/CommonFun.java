package com.example.contacts2csv;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.contacts2csv.MainActivity.m_Fun;
import static com.example.contacts2csv.MainActivity.m_MA;

/**
 * @author glsite.com
 * @version 1.0$
 * @des CommonFun
 * @updateAuthor MinJun$
 * @updateDes CommonFun
 */
public class CommonFun {
    public String funRemove(String str) {
        str = str.replace(" ", "");
        str = str.replace("-", "");
        str = str.replace("+86", "");
        str = str.replace("+", "");
        str = str.replace("\\", "");
        str = str.replace("(", "");
        str = str.replace(")", "");
        str = str.replace("，", ",");
        return str;
    }

    public void showToast(String str, int toastId) {
        Toast.makeText(m_MA, str, toastId).show();
    }

    public void sysOut(String strName, String str) {
        System.out.println(strName + str);
    }

    //获取含后缀的文件名
    public String getFileName(String PathAndName) {
        int start = PathAndName.lastIndexOf("/");
        if (start != -1) {
            return PathAndName.substring(start + 1);
        } else {
            return null;
        }
    }

    //延时 iSeconds 秒数
    public void delayX(int iSeconds) {
        try {
            Thread.currentThread().sleep(iSeconds * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Java lastIndexOf() 方法
    //Java String类Java String类
    //
    //lastIndexOf() 方法有以下四种形式：
    //public int lastIndexOf(int ch): 返回指定字符在此字符串中最后一次出现处的索引，如果此字符串中没有这样的字符，则返回 -1。
    //public int lastIndexOf(int ch, int fromIndex): 返回指定字符在此字符串中最后一次出现处的索引，从指定的索引处开始进行反向搜索，如果此字符串中没有这样的字符，则返回 -1。
    //public int lastIndexOf(String str): 返回指定子字符串在此字符串中最右边出现处的索引，如果此字符串中没有这样的字符，则返回 -1。
    //public int lastIndexOf(String str, int fromIndex): 返回指定子字符串在此字符串中最后一次出现处的索引，从指定的索引开始反向搜索，如果此字符串中没有这样的字符，则返回 -1。
    //
    //语法
    //public int lastIndexOf(int ch)
    //或
    //public int lastIndexOf(int ch, int fromIndex)
    //或
    //public int lastIndexOf(String str)
    //或
    //public int lastIndexOf(String str, int fromIndex)
    //
    //参数
    //ch -- 字符。
    //fromIndex -- 开始搜索的索引位置。
    //str -- 要搜索的子字符串。
    //
    //返回值
    //指定子字符串在字符串中第一次出现处的索引值。

    //获取文件后缀(不含点号)
    public String getFileSuffix(String PathAndName) {
        int start = PathAndName.lastIndexOf(".");
        if (start != -1) {
            return PathAndName.substring(start + 1);
        } else {
            return "";
        }
    }

    //获取不含后缀的文件名
    public String getFileNamePure(String PathAndName) {
        int start = PathAndName.lastIndexOf("/");   //没有找到返回 -1
        int end = PathAndName.lastIndexOf(".");
        if(-1 == start){
            start = -1;
        }
        if(-1 == end){
            end = PathAndName.length() - 1;
        }
        return PathAndName.substring(start + 1, end);
    }

    //获取文件所在目录
    public String getFilePath(String PathAndName) {
        int end = PathAndName.lastIndexOf("/");
        if (end != -1) {
            return PathAndName.substring(0, end);
        } else {
            return PathAndName;
        }
    }

    // 获得绝对路径：mInFilePath + "/" + m_sInFileName下面的新文件对象。
    public File GetNewFile(String filePath, String fileName, int iFlag) {
        File file = new File(filePath, GetNewFileName(filePath, fileName, iFlag));
        return file;
    }

    // 获得绝对路径PathAndName下面的新文件文件名称。
    // iFlag：0，不重名的新文件名称；iFlag：1，最新回执信息文件 Receipt_x.txt 名称
    public String GetNewFileName(String PathAndName, int iFlag) {
        return GetNewFileName(getFilePath(PathAndName), getFileName(PathAndName), iFlag);
    }

    // 获得绝对路径：mInFilePath + "/" + m_sInFileName下面的新文件文件名称。
    // iFlag：0，不重名的新文件名称；iFlag：1，最新回执信息文件 Receipt_x.txt 名称
    public String GetNewFileName(String filePath, String fileName, int iFlag) {
        String namePure = getFileNamePure(filePath + "/" + fileName); //不含后缀的文件名
        namePure = namePure.substring(0, namePure.length() - 2);      //去除文件名后面的2个字符：_x

        File fileDir = new File(filePath);
        String[] filesNames = fileDir.list();   //获取 filePath 目录下的所有文件名称
        //printArr(filesNames);
        //I/System.out: str1 : OctopusMessage_Config.xml
        //I/System.out: str2 : NamePhoneNicks_1.txt
        //I/System.out: str3 : Phones_1.txt
        //I/System.out: str4 : NamePhoneNicks_2.txt
        //I/System.out: str5 : Receipt_1.txt
        //I/System.out: str6 : Phones_2.txt
        //I/System.out: str7 : Help_2.txt
        //I/System.out: str8 : NamePhoneNicks_3.txt
        //I/System.out: str9 : Phones_3.txt
        //I/System.out: str10 : NamePhoneNicks_4.txt
        //I/System.out: str11 : Names_1.txt
        //I/System.out: str12 : UserPath_3.txt
        //I/System.out: str13 : Phones_7.txt
        //I/System.out: str14 : Receipt_6.txt
        int n = 0;
        for (String name : filesNames){ //NamePhoneNicks_x.txt
            String sp = getFileNamePure(name);
            //System.out.println("(sp.substring(0, namePure.length() - 1)).equals(namePure) = " + (sp.substring(0, namePure.length() - 1)).equals(namePure));
            //System.out.println("namePure = " + namePure);
            //System.out.println("sp.substring(0, namePure.length()) = " + sp.substring(0, namePure.length()));
            if (sp.length() >= (namePure.length() + 2) && (sp.substring(0, namePure.length())).equals(namePure)){
                String s = sp.substring(namePure.length() + 1, sp.length());
                if (isNum(s)) {
                    n = Math.max(n, Integer.valueOf(s));
                }
            }
        }

        // iFlag：0，不重名的新文件名称；iFlag：1，最新回执信息文件 Receipt_x.txt 名称
        if (iFlag < 0 || iFlag > 1 || (1 == iFlag && 0 == n)) {
            return  "";  //iFlag非法，或者没有找到最新回执信息文件 Receipt_x.txt 名称
        } else if (0 == iFlag) {
            n = (0 == n) ? 1 : (n + 1);
        }

        String nameNew = namePure + "_" + String.valueOf(n) + "." + getFileSuffix(filePath + "/" + fileName);
        //System.out.println("nameNew = " + nameNew);
        return nameNew;
    }

    // 查找最新图片文件名。filePath 为目录绝对路径，filenamePure 不含后缀的文件名称
    // iFlag：0，不重名的新文件名称；iFlag：1，最新文件 Wang_Wu_19 名称
    public String GetNewPhotoFileName(String filePath, String filenamePure, int iFlag) {
        //filenamePure = filenamePure.substring(0, filenamePure.length() - 2);      //去除文件名后面的2个字符：_x
        if (filenamePure.indexOf("_") > 0) {      // 若 filenamePure 末尾包含 "_ddd" 数字子串，便去除
            if (m_Fun.isNum(filenamePure.substring(filenamePure.lastIndexOf("_") + 1))) {
                filenamePure = filenamePure.substring(0, filenamePure.lastIndexOf("_"));
            }
        }

        File fileDir = new File(filePath);
        String[] filesNames = fileDir.list();   //获取 filePath 目录下的所有文件名称
        if (null == filesNames) {   //若 null == filesNames ，App会崩溃
            return "";
        }
        int n = 0;
        String suffix = "";
        for (String name : filesNames){         //NamePhoneNicks_x.png
            suffix = getFileSuffix(name);
            //System.out.println("suffix = " + suffix);

            if (!(suffix.equals("bmp") || suffix.equals("png") || suffix.equals("jpg"))) {
                continue;   // 若文件后缀非 "bmp"、"png"、"jpg"，则跳过后续处理，继续循环
            }

            String pure = getFileNamePure(name);
            if (pure.equals(filenamePure)) {
                n = -1;
                break;
            } else if (pure.length() >= (filenamePure.length() + 2) && (pure.substring(0, filenamePure.length())).equals(filenamePure)){
                String s = pure.substring(filenamePure.length() + 1, pure.length());
                if (isNum(s)) {
                    n = Math.max(n, Integer.valueOf(s));
                }
            }
        }

        String filenameNew = "";
        // iFlag：0，不重名的新文件名称；iFlag：1，最新文件 Wang_Wu_19 名称
        if (iFlag < 0 || iFlag > 1 || (1 == iFlag && 0 == n)) {
            return  "";    //iFlag非法，或者没有找到最新回执信息文件 Receipt(_x).txt 名称
        } else if (0 == iFlag) {
            n = (0 == n) ? 1 : (n + 1);
        } else if (1 == iFlag && -1 == n) {
            filenameNew = filenamePure + suffix;
        } else {
            filenameNew = filenamePure + "_" + String.valueOf(n) + "." + suffix;
        }
        //System.out.println("filenameNew = " + filenameNew);
        return filenameNew;
    }

    //android 重命名文件 原创cw2004100021124 发布于2017-10-17 16:38:46 阅读数 6526  收藏
    //https://blog.csdn.net/cw2004100021124/article/details/78262138
    private void createFile() {
        File sdCard = Environment.getExternalStorageDirectory();
        String fileName = "data.txt";
        File file = new File(sdCard, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
                String oldPath = file.getAbsolutePath();
                String newPath = "";
                newPath = oldPath.replace(fileName, "other.txt");
                renameFile(oldPath, newPath);
                //file is create
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            String oldPath = file.getAbsolutePath();
            if (!TextUtils.isEmpty(oldPath)) {
                String newPath = file.getAbsolutePath() + "other.txt";
                newPath = oldPath.replace(fileName, "other.txt");
                renameFile(oldPath, newPath);
            }

        }
    }

    /**
     * oldPath 和 newPath必须是新旧文件的绝对路径
     */
    public void renameFile(String oldPath, String newPath) {
        if (TextUtils.isEmpty(oldPath)) {
            return;
        }

        if (TextUtils.isEmpty(newPath)) {
            return;
        }

        File file = new File(oldPath);
        file.renameTo(new File(newPath));
    }

    //判断PhoneNum是否是13、15、18打头的11位手机号
    public boolean isMobileNum(String PhoneNum) {
        boolean bMobileNum = false;
        if (2 > PhoneNum.length()) {
            return bMobileNum;
        }
        //int arrHead[] = {13, 15, 18};
        int arrHead[] = {13, 15, 18, 55};   // Android Studio AVD 的号码为 55 打头的4位号码
        for (int head : arrHead) {
            //if (PhoneNum.indexOf(String.valueOf(head)) != -1 && 11 == PhoneNum.length()) {

            //            System.out.println("PhoneNum.substring(0, 2) = " + PhoneNum.substring(0, 2));
            //            System.out.println("String.valueOf(head) = " + String.valueOf(head));
            //            System.out.println("PhoneNum.length() = " + PhoneNum.length());

            //Android判断字符串相等，   必须用：PhoneNum.substring(0, 2).equals(String.valueOf(head))
            //                          不能用：PhoneNum.substring(0, 2) == String.valueOf(head)

            //public String substring（int beginIndex，int endIndex），返回一个新字符串，它是原字符串的一个子串。
            //注意：该子串从 beginIndex 开始，到 endIndex - 1 处结束。因此，该子串长度为 endIndex - beginIndex。
            //if (PhoneNum.substring(0, 2).equals(String.valueOf(head)) && 11 == PhoneNum.length()) {
            // Android Studio AVD 的号码为 55 打头的4位号码
            if (PhoneNum.substring(0, 2).equals(String.valueOf(head)) && (11 == PhoneNum.length() || 4 == PhoneNum.length())) {
                bMobileNum = true;
                break;  //跳出最里层循环
            }
        }

        return bMobileNum;
    }

    //Java使用正则表达式判断字符串是数字
    public boolean isNum(String str) {
        if (str.equals("")) {
            return false;
        }
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public void printArr(String [] arr){
        int n = 1;
        for(String s : arr) {
            System.out.println("str" + n++ +" : " + s);
        }
    }

    /**
     * 字符串转16进制字符串
     *
     * @param strPart
     * @return
     */
    public String string2HexString(String strPart) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < strPart.length(); i++) {
            int ch = (int) strPart.charAt(i);
            String strHex = Integer.toHexString(ch);
            hexString.append(strHex);
        }
        return hexString.toString();
    }

    public void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // Logcat 输出 JSONObject 完整结构
    public void logJson(JSONObject json) {
        try {
            System.out.println("JSONObject : \n" + json.toString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 输出 JSONObject 完整结构到文件，filePath 为目录路径，fileName为文件名
    public void Json2File(JSONObject json, String filePath, String fileName) {
        String newFileName = GetNewFileName(filePath, fileName, 0);

        try {
            //System.out.println("JSONObject : \n" + json.toString(4));
            String str = "JSONObject : \n" + json.toString(4);
            writeFile(filePath + "/" + newFileName, str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 写文本文件。sPath 是文件的绝对路径，str 是需要写入的字符串
    public void writeFile(String sPath, String str) {
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
