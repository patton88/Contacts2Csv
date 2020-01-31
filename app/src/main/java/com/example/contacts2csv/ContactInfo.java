package com.example.contacts2csv;

import java.util.ArrayList;

public class ContactInfo {
    public String lastName;                 //   后缀名
    public String firstName;                //   家族名
    public String displayName;              //0、姓名
    public ArrayList<String> mobileNum;     //1、移动电话
    public ArrayList<String> telNum;        //   有线电话
    public ArrayList<String> Event;         //2、Event重要事件、纪念日
    public ArrayList<String> Email;         //3、Email
    public ArrayList<String> Im;            //4、即时通讯QQ、MSN等
    public ArrayList<String> note;          //5、备注信息
    public ArrayList<String> nickName;      //6、昵称信息
    public ArrayList<String> Organ;         //7、机构信息
    public ArrayList<String> Website;       //8、Website，获取网站信息
    public ArrayList<String> postalAddress; //9、通讯地址

    public ContactInfo(
            String displayName,
            String lastName,
            String firstName,
            ArrayList<String> mobileNum,
            ArrayList<String> telNum,
            ArrayList<String> Event,
            ArrayList<String> Email,
            ArrayList<String> Im,
            ArrayList<String> note,
            ArrayList<String> nickName,
            ArrayList<String> Organ,
            ArrayList<String> Website,
            ArrayList<String> postalAddress
    ) {
        this.displayName = displayName;     //   后缀名
        this.lastName = lastName;           //   家族名
        this.firstName = firstName;         //0、姓名
        this.mobileNum = mobileNum;         //1、移动电话
        this.telNum = telNum;               //   有线电话
        this.Event = Event;                 //2、Event重要事件、纪念日
        this.Email = Email;                 //3、Email
        this.Im = Im;                       //4、即时通讯QQ、MSN等
        this.note = note;                   //5、备注信息
        this.nickName = nickName;           //6、昵称信息
        this.Organ = Organ;                 //7、机构信息
        this.Website = Website;             //8、Website，获取网站信息
        this.postalAddress = postalAddress; //9、通讯地址
    }

    public ContactInfo() {
        this.displayName = "";                          //   后缀名
        this.lastName = "";                             //   家族名
        this.firstName = "";                            //0、姓名
        this.mobileNum = new ArrayList<String>();       //1、移动电话
        this.telNum = new ArrayList<String>();          //   有线电话
        this.Event = new ArrayList<String>();           //2、Event重要事件、纪念日
        this.Email = new ArrayList<String>();           //3、Email
        this.Im = new ArrayList<String>();              //4、即时通讯QQ、MSN等
        this.note = new ArrayList<String>();            //5、备注信息
        this.nickName = new ArrayList<String>();        //6、昵称信息
        this.Organ = new ArrayList<String>();           //7、机构信息
        this.Website = new ArrayList<String>();         //8、Website，获取网站信息
        this.postalAddress = new ArrayList<String>();   //9、通讯地址
    }
}
