package com.example.OctopusMessage;

/**
 * @author glsite.com
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */

//java自动识别文件编码格式UTF-8,UTF-8无BOM，GBK。原创guying4875 最后发布于2018-07-13 16:53:58 阅读数 4287
//本文链接：https://blog.csdn.net/guying4875/article/details/81034022
//背景：在解读properties配置文件时windows操作系统编辑过的内容上传后总是无法通过键获取文件中内容，
//讲过分析是文件的编码格式为UTF-8带BOM的，因此通过该程序获取文件编码格式

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.BitSet;
import java.util.logging.Logger;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * @author 自动识别文件编码格式
 *
 */
public class FileEncodeUtil {
    //private static Logger logger = LoggerFactory.getLogger(FileEncodeUtil.class);
    private static int BYTE_SIZE = 8;
    private static String m_fullFilePath;

    /**
     * 通过文件全名称获取编码集名称
     *
     * @param fullFilePath  文件绝对路径
     * @param ignoreBom
     * @return
     * @throws Exception
     */
    public static String getFileEncode(String fullFilePath, boolean ignoreBom){
        //logger.debug("fullFilePath ; {}", fullFilePath);
        //System.out.println("fullFilePath : " + fullFilePath);
        BufferedInputStream bis = null;
        String sEncode = "";
        m_fullFilePath = fullFilePath;
        try {
            bis = new BufferedInputStream(new FileInputStream(fullFilePath));
            try {
                sEncode = getFileEncode(bis, ignoreBom);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return sEncode;
    }

    /**
     * 通过文件缓存流获取编码集名称，文件流必须为未曾
     *
     * @param bis
     * @param ignoreBom 是否忽略utf-8 bom
     * @return
     * @throws Exception
     */
    public static String getFileEncode(BufferedInputStream bis, boolean ignoreBom) throws Exception {
        bis.mark(0);

        // -1   0xFF
        // -2   0xFE
        // -17  0xEF
        // -69  0xBB
        // -65  0xBF

        //在不同的Unicode编码中，对应的bom的二进制字节如下：
        //Bytes     Decs            Encoding
        //FF FE     -1 -2           Unicode
        //FE FF     -2 -1           UTF-16BE
        //FF FF     -1 -1           UTF-16LE
        //EF BB BF  -17 -69 -65     UTF-8

        String sFilEncode = "";
        byte[] head = new byte[3];
        bis.read(head);
        if (head[0] == -1 && head[1] == -2) {
            //sFilEncode = "Unicode";
            sFilEncode = "UTF-16";
        } else if (head[0] == -2 && head[1] == -1) {
            sFilEncode = "UTF-16BE";
        } else if (head[0] == -1 && head[1] == -1) {
            sFilEncode = "UTF-16LE";
        } else if (head[0] == -17 && head[1] == -69 && head[2] == -65) { //前面都是带文件头BOM
            if (ignoreBom) {
                sFilEncode = "UTF-8";
            } else {
                //sFilEncode = "UTF-8_BOM";
                //I:\Android\Android-SDK-Windows\sources\android-29\java\nio\charset\Charset.java
                sFilEncode = "UTF-8";   //Android不支持"UTF-8_BOM"
            }
        //} else if (isUTF8(bis)) {     //后面是不带文件头BOM
        } else if (isUTF8_2()) {        //解决在 AS3.5 API 22 x86-x64 AVD 中运行报错的问题
            sFilEncode = "UTF-8";
        } else {
            //sFilEncode = "GBK";     //默认编码格式
            sFilEncode = "GBK";     //默认编码格式
        }
        //logger.info("result encode type : " + sFilEncode);
        //System.out.println("result encode type : " + sFilEncode);
        return sFilEncode;
    }

    /**
     * 是否是无BOM的UTF8格式，不判断常规场景，只区分无BOM UTF8和GBK
     *
     * @param bis
     * @return
     */
    // 解决在 AS3.5 API 22 x86-x64 AVD 中运行报错的问题
    //W/System.err: java.io.IOException: Mark has been invalidated.
    //        at java.io.BufferedInputStream.reset(BufferedInputStream.java:336)
    //        at com.example.OctopusMessage.FileEncodeUtil.isUTF8(FileEncodeUtil.java:125)
    private static boolean isUTF8_2(/*BufferedInputStream bis*/) throws Exception {
        //bis.reset();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(m_fullFilePath));

        //读取第一个字节
        int code = bis.read();
        do {
            BitSet bitSet = convert2BitSet(code);
            //判断是否为单字节
            if (bitSet.get(0)) {//多字节时，再读取N个字节
                if (!checkMultiByte(bis, bitSet)) {//未检测通过,直接返回
                    return false;
                }
            } else {
                //单字节时什么都不用做，再次读取字节
            }
            code = bis.read();
        } while (code != -1);
        return true;
    }

    /**
     * 是否是无BOM的UTF8格式，不判断常规场景，只区分无BOM UTF8和GBK
     *
     * @param bis
     * @return
     */
    private static boolean isUTF8( BufferedInputStream bis) throws Exception {
        bis.reset();

        //读取第一个字节
        int code = bis.read();
        do {
            BitSet bitSet = convert2BitSet(code);
            //判断是否为单字节
            if (bitSet.get(0)) {//多字节时，再读取N个字节
                if (!checkMultiByte(bis, bitSet)) {//未检测通过,直接返回
                    return false;
                }
            } else {
                //单字节时什么都不用做，再次读取字节
            }
            code = bis.read();
        } while (code != -1);
        return true;
    }

    /**
     * 检测多字节，判断是否为utf8，已经读取了一个字节
     *
     * @param bis
     * @param bitSet
     * @return
     */
    private static boolean checkMultiByte(BufferedInputStream bis, BitSet bitSet) throws Exception {
        int count = getCountOfSequential(bitSet);
        byte[] bytes = new byte[count - 1];//已经读取了一个字节，不能再读取
        bis.read(bytes);
        for (byte b : bytes) {
            if (!checkUtf8Byte(b)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检测单字节，判断是否为utf8
     *
     * @param b
     * @return
     */
    private static boolean checkUtf8Byte(byte b) throws Exception {
        BitSet bitSet = convert2BitSet(b);
        return bitSet.get(0) && !bitSet.get(1);
    }

    /**
     * 检测bitSet中从开始有多少个连续的1
     *
     * @param bitSet
     * @return
     */
    private static int getCountOfSequential( BitSet bitSet) {
        int count = 0;
        for (int i = 0; i < BYTE_SIZE; i++) {
            if (bitSet.get(i)) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }


    /**
     * 将整形转为BitSet
     *
     * @param code
     * @return
     */
    private static BitSet convert2BitSet(int code) {
        BitSet bitSet = new BitSet(BYTE_SIZE);

        for (int i = 0; i < BYTE_SIZE; i++) {
            int tmp3 = code >> (BYTE_SIZE - i - 1);
            int tmp2 = 0x1 & tmp3;
            if (tmp2 == 1) {
                bitSet.set(i);
            }
        }
        return bitSet;
    }

    /**
     * 将一指定编码的文件转换为另一编码的文件
     *
     * @param oldFullFileName
     * @param oldCharsetName
     * @param newFullFileName
     * @param newCharsetName
     */
    public static void convert(String oldFullFileName, String oldCharsetName, String newFullFileName, String newCharsetName) throws Exception {
        //logger.info("the old file name is : {}, The oldCharsetName is : {}", oldFullFileName, oldCharsetName);
        //logger.info("the new file name is : {}, The newCharsetName is : {}", newFullFileName, newCharsetName);
        //System.out.println("the old file name is : " + oldFullFileName + ", The oldCharsetName is : " + oldCharsetName);
        //System.out.println("the new file name is : " + newFullFileName + ", The newCharsetName is : " + newCharsetName);

        StringBuffer content = new StringBuffer();

        BufferedReader bin = new BufferedReader(new InputStreamReader(new FileInputStream(oldFullFileName), oldCharsetName));
        String line;
        while ((line = bin.readLine()) != null) {
            content.append(line);
            content.append(System.getProperty("line.separator"));
        }
        newFullFileName = newFullFileName.replace("\\", "/");
        File dir = new File(newFullFileName.substring(0, newFullFileName.lastIndexOf("/")));
        if (!dir.exists()) {
            dir.mkdirs();
        }
        Writer out = new OutputStreamWriter(new FileOutputStream(newFullFileName), newCharsetName);
        out.write(content.toString());
    }
}

//调用方式
//public static void main(String[] args) {
//		try {
//			String filePath ="/home/zhanghy/文档/国寿/实时计算paas/ee/error/分/config/function_list.properties";
//			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath));
//			String code = FileEncodeUtil.getFileEncode(bis, false);
//?????System.out.println("文件编码格式为：" + code);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
