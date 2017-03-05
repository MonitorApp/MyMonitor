package com.outsource.monitor.monitor.base.parser;

import java.nio.charset.Charset;

public class ByteArray {
	private int m_position;
	private byte[] m_bytes;

	public ByteArray(byte[] bytes) {
		m_bytes = bytes;
		m_position = 0;
	}

	public void setPosition(int pos) {
		m_position = pos;
	}

	public void movePostion(int offset)
	{
		m_position += offset;
		m_position = m_position<0?0:m_position;
		m_position = m_position> m_bytes.length?m_bytes.length:m_position;
	}
	
	public int getPostion() {
		return m_position;
	}

	public int getLength() {
		return m_bytes.length;
	}

	public byte[] getSubBytes(int len) {
		byte[] subBytes = ByteUtil.SubBytes(m_bytes, m_position, len);
		m_position += len;
		return subBytes;
	}

	public ByteArray getSubByteArray(int len) {
		return new ByteArray(getSubBytes(len));
	}
	
	public String toHexString() {
		return ByteUtil.byte2HexStr(m_bytes);
	}

	public byte getByte() {
		return m_bytes[m_position++];
	}

	public short getShort() {
		short val = (short) ((0xff & m_bytes[m_position]) | (0xff00 & (m_bytes[m_position + 1] << 8)));
		m_position += 2;
		return val;
	}

	public int getInt() {
		int val = (0xff & m_bytes[m_position])
				| (0xff00 & (m_bytes[m_position + 1] << 8))
				| (0xff0000 & (m_bytes[m_position + 2] << 16))
				| (0xff000000 & (m_bytes[m_position + 3] << 24));
		m_position += 4;
		return val;
	}

	public long getLong() {
		long val = (0xffL & (long) m_bytes[m_position])
				| (0xff00L & ((long) m_bytes[m_position + 1] << 8))
				| (0xff0000L & ((long) m_bytes[m_position + 2] << 16))
				| (0xff000000L & ((long) m_bytes[m_position + 3] << 24))
				| (0xff00000000L & ((long) m_bytes[m_position + 4] << 32))
				| (0xff0000000000L & ((long) m_bytes[m_position + 5] << 40))
				| (0xff000000000000L & ((long) m_bytes[m_position + 6] << 48))
				| (0xff00000000000000L & ((long) m_bytes[m_position + 7] << 56));
		m_position += 8;
		return val;
	}

	public float getFloat() {
		return Float.intBitsToFloat(getInt());
	}
	
	public double getDouble()
	{
        return Double.longBitsToDouble(getLong());
	}
	
    public String getString(int len, String charsetName)
    {
        String str = new String(m_bytes, m_position, len ,Charset.forName(charsetName));
        m_position += len;
        return str;
    }

    public String getString(int len)
    {
        return getString(len, "GBK");
    }
}
