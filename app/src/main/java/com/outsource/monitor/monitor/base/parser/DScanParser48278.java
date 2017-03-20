package com.outsource.monitor.monitor.base.parser;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/3/20.
 * 数字扫描
 */
public class DScanParser48278 extends  ParserBase48278
{
    public static class DataHead
    {
        public byte kindId; //离散扫描数据kindId = 0x08;
        public long nStartFreq;   //起始频段
        public long endFreq;        //结束频段
        public long ifbw;       //整数Hz
        public int pointCount;
    }

    public static class DataValue
    {
        public int pointCount;  //数据个数
        public long freIndex;   //本包起始频率索引
        public ArrayList<Short> valueList = new ArrayList<>();  //电平 (实际电频*100）)
    }

    public DataHead m_dataHead;
    public DataValue m_dataValue;

    @Override
    void ParseDataHead(ByteArray byteArray)
    {
        m_dataHead = new DataHead();
        m_dataHead.kindId = byteArray.getByte();
        m_dataHead.nStartFreq = byteArray.getLong();
        m_dataHead.endFreq = byteArray.getLong();
        m_dataHead.ifbw = byteArray.getLong();
        m_dataHead.pointCount = byteArray.getInt();
    }

    @Override
    void ParseMonitorData(ByteArray byteArray)
    {
        m_dataValue = new DataValue();
        m_dataValue.pointCount = byteArray.getInt();
        m_dataValue.freIndex = byteArray.getLong();
        int i;
        for(i=0; i<m_dataValue.pointCount; ++i)
        {
            m_dataValue.valueList.add(byteArray.getShort());
        }
    }

    public static DScanParser48278 TryParse(byte[] bytes, int pos) {

        int dataLen = DScanParser48278.CheckBytes(bytes, pos);
        if(dataLen == -1)
        {
            return  null;
        }

        DScanParser48278 dScanParser48278 = new DScanParser48278();
        dScanParser48278.ParserData(ByteUtil.SubBytes(bytes, 0, dataLen + 8));
        dScanParser48278.byteLen = dataLen + 8;
        return dScanParser48278;
    }
}
