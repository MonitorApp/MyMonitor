package com.outsource.monitor.parser;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/11/17.
 * 中频分析
 */
public class IfpanParser48278 extends  ParserBase48278
{
    public static class DataHead {
        public byte nDataType; //0x02 代表中频数据
        public long frequence;
        public long span;   //测量跨距
        public long ifbw;  //中频带宽
    }

    public  static class DataValue
    {
        public short cenerVal; //中心点电平（实际电平*100）
        public int dotCount;    //频谱曲线点数
        public long centerFre;  //中心频率
//        public ArrayList<Short> valueList = new ArrayList<Short>(); //电频数值区（实际电频*100）
        public ArrayList<Float> levelList = new ArrayList<>();
    }

    public DataHead m_dataHead;
    public DataValue m_dataValue;

    @Override
    void ParseDataHead(ByteArray byteArray) {
        m_dataHead = new DataHead();
        m_dataHead.nDataType = byteArray.getByte();
        m_dataHead.frequence = byteArray.getLong();
        m_dataHead.span = byteArray.getLong();
        m_dataHead.ifbw = byteArray.getLong();
    }

    @Override
    void ParseMonitorData(ByteArray byteArray) {
        m_dataValue = new DataValue();
        m_dataValue.cenerVal = byteArray.getShort();
        m_dataValue.dotCount = byteArray.getInt();
        m_dataValue.centerFre = byteArray.getLong();

       // m_dataValue.levelValue = byteArray.getShort();
        while (byteArray.getPostion() < m_frameDataLen + 4)    //head "FFFFFFFF"
        {
            short data = byteArray.getShort();
            m_dataValue.levelList.add((float) data / 100);
        }
    }

    public static IfpanParser48278 TryParse(byte[] bytes, int pos) {

        int dataLen = ParserBase48278.CheckBytes(bytes, pos);
        if(dataLen == -1)
        {
            return  null;
        }

        IfpanParser48278 ifpanParse = new IfpanParser48278();
        ifpanParse.ParserData(ByteUtil.SubBytes(bytes, 0, dataLen + 8));
        ifpanParse.byteLen = dataLen + 8;
        return ifpanParse;
    }
}
