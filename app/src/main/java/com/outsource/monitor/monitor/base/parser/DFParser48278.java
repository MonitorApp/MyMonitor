package com.outsource.monitor.monitor.base.parser;

/**
 * Created by wuly on 2016/11/26.
 *  单频测向
 */
public class DFParser48278 extends  ParserBase48278
{
    public static class DataHead
    {
        public byte kindId; //时域数据类型 0x03
        public long freq;
    }

    public  static class DataValue
    {
        public float value; //浮点数电平值数据
        public float dfValue; //测向电平，浮点数
        public float quality; //测向质量
        public float dir; //方位角
        public float pitchAngle; //俯仰角
        public float luopanzhi; //罗盘值
    }

    public DataHead m_dataHead;
    public DataValue m_dataValue;
    @Override
    void ParseDataHead(ByteArray byteArray) {
        m_dataHead = new DataHead();
        m_dataHead.kindId = byteArray.getByte();
        m_dataHead.freq = byteArray.getLong();
    }

    @Override
    void ParseMonitorData(ByteArray byteArray) {
        m_dataValue = new DataValue();
        m_dataValue.value = byteArray.getFloat();
        m_dataValue.dfValue = byteArray.getFloat();
        m_dataValue.quality = byteArray.getFloat();
        m_dataValue.dir = byteArray.getFloat();
        m_dataValue.pitchAngle = byteArray.getFloat();
        m_dataValue.luopanzhi = byteArray.getFloat();
    }

    public static DFParser48278 TryParse(byte[] bytes, int pos) {

        int dataLen = ParserBase48278.CheckBytes(bytes, pos);
        if(dataLen == -1)
        {
            return  null;
        }

        DFParser48278 dfParser48278 = new DFParser48278();
        dfParser48278.ParserData(ByteUtil.SubBytes(bytes, 0, dataLen + 8));
        dfParser48278.byteLen = dataLen + 8;
        return dfParser48278;
    }
}
