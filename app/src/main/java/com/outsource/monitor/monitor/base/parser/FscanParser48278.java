package com.outsource.monitor.monitor.base.parser;

import java.util.ArrayList;

/**
 * Created by wuly on 2016/11/26.
 * 频段扫描
 */
public class FscanParser48278 extends  ParserBase48278
{

    public static class DataHead
    {
        public byte kindId; //频段扫描数据 0x06
        public short nFscanSegment; //频段扫描段数
        public ArrayList<FcanParam> fscanParamList = new ArrayList<>();

        static public class FcanParam
        {
            public long startFreq;
            public long endFreq;
            public long step;
            public int nPoints; //文档为本段点数 应该是值的个数
        }
    }

    public  static class DataValue
    {
        public int dataNum; //数据个数
        public long startFreq;  //起始电频
        public ArrayList<Float> values = new ArrayList<>(); //实际电频*100
    }

    public DataHead m_dataHead;
    public DataValue m_dataValue;

    @Override
    void ParseDataHead(ByteArray byteArray) {
        m_dataHead = new DataHead();
        m_dataHead.kindId = byteArray.getByte();
        m_dataHead.nFscanSegment = byteArray.getShort();
        int i;
        DataHead.FcanParam paramItem;
        for(i=0; i<m_dataHead.nFscanSegment; ++i)
        {
            paramItem = new DataHead.FcanParam();
            paramItem.startFreq = byteArray.getLong();
            paramItem.endFreq = byteArray.getLong();
            paramItem.step = byteArray.getLong();
            paramItem.nPoints = byteArray.getInt();
            m_dataHead.fscanParamList.add(paramItem);
        }
    }

    @Override
    void ParseMonitorData(ByteArray byteArray)
    {
        m_dataValue = new DataValue();
        m_dataValue.dataNum = byteArray.getInt();
        m_dataValue.startFreq = byteArray.getLong();

        int i;
        for(i=0; i<m_dataValue.dataNum; ++i)
        {
            m_dataValue.values.add(byteArray.getShort() / 100f);
        }
    }

    public static FscanParser48278 TryParse(byte[] bytes, int pos) {

        int dataLen = ParserBase48278.CheckBytes(bytes, pos);
        if(dataLen == -1)
        {
            return  null;
        }

        FscanParser48278 fFscanParser48278 = new FscanParser48278();
        fFscanParser48278.ParserData(ByteUtil.SubBytes(bytes, 0, dataLen + 8));
        fFscanParser48278.byteLen = dataLen + 8;
        return fFscanParser48278;
    }
}
