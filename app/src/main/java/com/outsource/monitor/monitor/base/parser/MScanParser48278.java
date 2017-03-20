package com.outsource.monitor.monitor.base.parser;

import java.util.ArrayList;

/**
 * Created by wuly on 2016/11/26.
 *  离散扫描
 */
public class MScanParser48278 extends  ParserBase48278
{
    public static class DataHead
    {
        public byte kindId; //离散扫描数据kindId = 0x05;
        public short nMScanPoint;   //离散扫描点数
        public ArrayList<Long> freqList = new ArrayList<>();
    }

    public static class DataValue
    {
        public static class ValueItem
        {
            public int refFrequence;    //频率索引 long or int?
            public short freValue;      //实际电频*100
        }

        public int dataCount; //离散扫描数据个数
        public ArrayList<ValueItem> valueList = new ArrayList<>();
    }

    public DataHead m_dataHead;
    public DataValue m_dataValue;

    @Override
    void ParseDataHead(ByteArray byteArray)
    {
        m_dataHead = new DataHead();
        m_dataHead.kindId = byteArray.getByte();
        m_dataHead.nMScanPoint = byteArray.getShort();
        int i;
        for(i=0; i<m_dataHead.nMScanPoint; ++i)
        {
            m_dataHead.freqList.add(byteArray.getLong());
        }
    }

    @Override
    void ParseMonitorData(ByteArray byteArray)
    {
        m_dataValue = new DataValue();
        m_dataValue.dataCount = byteArray.getInt();
        int i;
        DataValue.ValueItem valueItem;
        for(i=0; i<m_dataValue.dataCount; ++i)
        {
            valueItem = new DataValue.ValueItem();
            valueItem.refFrequence = byteArray.getInt();
            valueItem.freValue = byteArray.getShort();
        }
    }

    public static MScanParser48278 TryParse(byte[] bytes, int pos) {

        int dataLen = MScanParser48278.CheckBytes(bytes, pos);
        if(dataLen == -1)
        {
            return  null;
        }

        MScanParser48278 mScanParser48278 = new MScanParser48278();
        mScanParser48278.ParserData(ByteUtil.SubBytes(bytes, 0, dataLen + 8));
        mScanParser48278.byteLen = dataLen + 8;
        return mScanParser48278;
    }
}
