package com.example;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

/**
 * Created by Administrator on 2016/10/23.
 */
public class MonitorSocketClient {

    private static int BuffLen = 1024*1024;

    public static void main(String[] args) throws Exception {
        singleFrequency();
    }

    protected static String singleFrequency() {

        Socket socket = null;
        byte[] recvBuff = new byte[BuffLen];
        String sr = null ;
        int pos = 0;
        try {
            socket = new Socket("127.0.0.1", 5050);
            socket.setSoTimeout(6 * 1000);
            // ��������
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            //单频测量
            //String cmd = "RMTP:ITU:4403000100113:frequency:97.1MHz\nifbw:15kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
            //中频分析
            //String cmd = "RMTP:IFANALYSIS:4403000100113:frequency:98.1MHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";
            //频段扫描
            //String cmd = "RMTP:FSCAN:4403000100113:frequency:98.1MHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";

            //单频测向
            String cmd = "RMTP:DF:4403000100113:frequency:98.1MHz\nifbw:30kHz\nspan:15kHz\nrecordthreshold:=40\ndemodmode:FM\n#";

            //String cmd = "RMTP:ALTER:frequency:98.1MHz\nifbw:30kHz\n#";
            bos.write(cmd.getBytes());
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(bos.toByteArray(), 0, bos.size());
            outputStream.flush();

            InputStream inputStream = socket.getInputStream();
            int recvLen;
            int readLen;
            ParserBase48278 parser;
            while(true)
            {
                recvLen =inputStream.read(recvBuff, pos, BuffLen-pos);
                pos += recvLen;

                //中频分析
//                IfpanParser48278 ifan =  IfpanParser48278.TryParse(recvBuff, pos);
//                if(ifan != null)
//                {
//                    if(ifan.m_dataHead != null)
//                    {
//                        System.out.println("Get Head data:" + ifan.m_dataHead.frequence);
//                    }
//
//                    if(ifan.m_dataValue != null)
//                    {
//                        System.out.println("Get Value data:" + ifan.m_dataValue.cenerVal+" valuelen:" +ifan.m_dataValue.valueList.size());
//                    }
//
//                    System.arraycopy(recvBuff, pos, recvBuff, 0, ifan.byteLen);
//                    pos -= ifan.byteLen;
//                }
                //中频分析 end

                //频段扫描
//                FscanParser48278 fsan =  FscanParser48278.TryParse(recvBuff, pos);
//                if(fsan != null)
//                {
//                    if(fsan.m_dataHead != null)
//                    {
//                        System.out.println("Get Head data:" + fsan.m_dataHead.nFscanSegment);
//                    }
//
//                    if(fsan.m_dataValue != null)
//                    {
//                        System.out.println("Get Value data:" + fsan.m_dataValue.dataNum+" valuelen:" +fsan.m_dataValue.values.size());
//                    }

                //单频测向
                DFParser48278 dfParser = DFParser48278.TryParse(recvBuff, pos);
                parser = dfParser;
                if(dfParser != null)
                {
                    if(dfParser.m_dataHead != null)
                    {
                        System.out.println("Get Head data:" + dfParser.m_dataHead.freq);
                    }

                    if(dfParser.m_dataValue != null)
                    {
                        System.out.println("Get Value data:" + dfParser.m_dataValue.value+" valuelen:" +dfParser.m_dataValue.dfValue);
                    }
                    //单频测向 end

                    System.arraycopy(recvBuff, pos, recvBuff, 0, parser.byteLen);
                    pos -= parser.byteLen;
               }
                //频段扫描 end
            }

//            inputStream.close();
//            System.out.println("head:==");
//
//            bos.close();
//            outputStream.close();
//            socket.close();
        } catch (UnknownHostException e) {
            sr = "�׽����쳣";
            e.printStackTrace();
        } catch (SocketException e) {
            sr = "�������ݳ�ʱ";
            e.printStackTrace();
        } catch (IOException e) {
            sr = "�׽��ֶ�д�쳣";
            e.printStackTrace();
        }
        return  sr ;
    }
}
