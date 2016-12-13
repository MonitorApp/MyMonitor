package com.outsource.monitor.parser;

/**
 * Created by Administrator on 2016/11/17.
 */
public abstract class ParserBase48278
{
    public static final int FRAME_TYPE_DATA = 0;
    public static final int FRAME_TYPE_INFO = 4;
    public static final int FRAME_TYPE_HEAD = 6;

    protected static void Log(Object obj) {
        System.out.println(obj);
    }

    public  static int CheckBytes(byte[] bytes, int pos)
    {
        if (pos <= 8) {
            Log("data less 8 byte!");
            return -1;
        }

        if (bytes[0] != (byte) 0xFF || bytes[1] != (byte) 0xFF
                || bytes[2] != (byte) 0xFF || bytes[3] != (byte) 0xFF) {
            Log("frame head error!====" + ByteUtil.byte2HexString(bytes, 0, 4));
            return -1;
        }

        int dataLen = ByteUtil.getInt(bytes, 4);
        if (pos < dataLen + 8) {
            Log("bytes data not enough! required====" + dataLen + 8);
            return -1;
        }

        if (bytes[dataLen + 4] != (byte) 0xEE || bytes[dataLen + 5] != (byte) 0xEE
                || bytes[dataLen + 6] != (byte) 0xEE || bytes[dataLen + 7] != (byte) 0xEE) {
            Log("frame end error!====" + ByteUtil.byte2HexString(bytes, dataLen + 4, 4));
            return -1;
        }

        return dataLen;
    }

    public int m_frameDataLen;
    public byte m_frameType;
    public int byteLen;

    public int ParserData(byte[] bytes) {
        ByteArray byteArray = new ByteArray(bytes);
        //Log(byteArray.toHexString());
        //Log("Head==="+ByteUtil.byte2HexStr(byteArray.getSubBytes(4)));

        byteArray.movePostion(4);
        m_frameDataLen = byteArray.getInt();
        // System.out.println( "frameDataLen==="+m_frameDataLen);

        byteArray.movePostion(4); //crcУ����
        byteArray.movePostion(16); //ʱ���

        //System.out.println("get type:"+byteArray.getPostion());
        m_frameType = byteArray.getByte();

        //System.out.println("get type:"+m_frameType);

        switch (m_frameType) {
            case FRAME_TYPE_DATA:
                ParseMonitorData(byteArray);
                break;
            case FRAME_TYPE_INFO:
                Log("Type FRAME_TYPE_INFO not support");
                break;
            case FRAME_TYPE_HEAD:
                ParseDataHead(byteArray);
                break;
            default:
                Log("Unknw Type=="+m_frameType);
                break;
        }

        //Log("End==="+ByteUtil.byte2HexStr(byteArray.getSubBytes(4)));
        return byteArray.getPostion();
    }

    public void Release() {
        m_frameDataLen = 0;
        m_frameType = 0;
        byteLen = 0;
    }

    abstract void ParseDataHead(ByteArray byteArray);
    abstract void ParseMonitorData(ByteArray byteArray);
}
