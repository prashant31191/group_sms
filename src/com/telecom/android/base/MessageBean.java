package com.telecom.android.base;

import android.graphics.Bitmap;

/**
 * 短信实体类
 * @author lsq
 *
 */
public class MessageBean {
	/**
	 * 短信会话
	 */
	public String smsThreadId;
	public String smsAddress;
	public String smsContactDisplayName;
	public String smsAllCount;
	public String smsBody;
	public String smsStrDate;
	public String smsId;
	public boolean isCaogao;
	public int smsNoReadCount;
	public int smsType;
	public long smsDate;
	public String smsContactPhotoId;
	public String smsContactId;
	public Bitmap smsContactPhoto;
	public String toString(){
		return "smsThreadId:"+smsThreadId+"/smsAddress:"+smsAddress+"/smsContactDisplayName:"+smsContactDisplayName
				+"/smsAllCount="+smsAllCount+"/smsBody="+smsBody+"/smsStrDate="+smsStrDate
				+"/smsId="+smsId+"/smsNoReadCount="+smsNoReadCount+"/smsNoReadCount="+smsNoReadCount
				+"/smsType="+smsType+"/smsDate="+smsDate+"/smsContactPhotoId="+smsContactPhotoId;
	}
}
