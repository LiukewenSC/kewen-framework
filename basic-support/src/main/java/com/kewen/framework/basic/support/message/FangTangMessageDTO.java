package com.kewen.framework.basic.support.message;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FangTangMessageDTO {
    /**
     * title: 消息标题，必填。最大长度为 32 。
     */
    private String title;
    /**
     * desp: 消息内容，选填。支持 Markdown语法 ，最大长度为 32KB ,消息卡片截取前 30 显示。
     */
    private String desp;
    /**
     * short: 消息卡片内容，选填。最大长度64。如果不指定，将自动从desp中截取生成。
     * 这里 Java是关键字，只能换一个
     */
    private String shortDesp;
    /**
     * noip: 是否隐藏调用IP，选填。如果不指定，则显示；为1则隐藏。
     */
    private String noip;
    /**
     * channel: 动态指定本次推送使用的消息通道，选填。如不指定，则使用网站上的消息通道页面设置的通道。支持最多两个通道，多个通道值用竖线|隔开。
     *  比如，同时发送服务号和企业微信应用消息通道，则使用 9|66 。通道对应的值如下：
     *  官方Android版·β=98 企业微信应用消息=66 企业微信群机器人=1 钉钉群机器人=2 飞书群机器人=3   Bark_iOS=8  测试号=0 自定义=88  PushDeer=18 方糖服务号=9
     */
    private String channel;
    /**
     * openid: 消息抄送的openid，选填。只支持测试号和企业微信应用消息通道。测试号的 openid 从测试号页面获得 ，多个 openid 用 , 隔开。
     *  企业微信应用消息通道的 openid 参数，内容为接收人在企业微信中的 UID（可在消息通道页面配置好该通道后通过链接查看） ,
     *  多个人请 | 隔开，即可发给特定人/多个人。不填则发送给通道配置页面的接收人。
     */
    private String openid;
}
