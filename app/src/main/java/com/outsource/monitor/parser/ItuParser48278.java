package com.outsource.monitor.parser;

import java.util.ArrayList;

public class ItuParser48278 extends  ParserBase48278
{
    public static class DataHead {
        public static class HeadItem {
            public String name;
            public float minVal;
            public String unit;
        }

        public byte kindId;
        public long frequence;
        public ArrayList<HeadItem> dataHead = new ArrayList<HeadItem>();
    }

    public static class DataValue {
        public long frequence;
        public ArrayList<Float> valueList = new ArrayList<Float>();
    }

    public DataHead m_dataHead;
    public DataValue m_dataValue;

    public void ParseDataHead(ByteArray byteArray) {
       // Log(byteArray.toHexString());
        m_dataHead = new DataHead();

        m_dataHead.kindId = byteArray.getByte(); //kindID
        m_dataHead.frequence = byteArray.getLong();    //Ƶ��
        //Log("fre==="+m_dataHead.frequence);
        short dataCount = byteArray.getShort();

        int i;
        DataHead.HeadItem headItem;
        for (i = 0; i < dataCount; ++i) {
            headItem = new DataHead.HeadItem();
            m_dataHead.dataHead.add(headItem);
            headItem.name = byteArray.getString(16, "gb2312");
            headItem.minVal = byteArray.getFloat();
            headItem.unit = byteArray.getString(10, "gb2312");
        }
        return;
    }

    public void ParseMonitorData(ByteArray byteArray) {
        //Log("ParseMonitorData");
        m_dataValue = new DataValue();
        m_dataValue.frequence = byteArray.getLong();    //Ƶ��
        //Log("fre==="+m_dataValue.frequence);

        float data;
        while (byteArray.getPostion() < m_frameDataLen + 4)    //head "FFFFFFFF"
        {
            data = byteArray.getFloat();
            m_dataValue.valueList.add(data);
        }

        return;
    }

    public static ItuParser48278 TryParse(byte[] bytes, int pos) {

       int dataLen = ParserBase48278.CheckBytes(bytes, pos);
        if(dataLen == -1)
        {
            return  null;
        }

        ItuParser48278 ituParse = new ItuParser48278();
        ituParse.ParserData(ByteUtil.SubBytes(bytes, 0, dataLen + 8));
        ituParse.byteLen = dataLen + 8;
        return ituParse;
    }
}
