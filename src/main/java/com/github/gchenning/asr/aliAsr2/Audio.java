package com.github.gchenning.asr.aliAsr2;

/*
 * 实现录音机的功能
 * 1.在原来的基础上界面更加精简，字体变大使用户更加方便
 * 2.在停止录音后便直接保存为.mp3文件
 * 3.对停止录音进行改进，将录音按钮设计成类似微信的形式，按下时进行录音，松开即停止录音并保存即可
 * 4.在录音的时候在面板上显示音频波，表示正在录音
 */

import com.alibaba.nls.client.protocol.SpeechReqProtocol;
import com.github.gchenning.asr.system.CustomThreadFactory;
import com.github.gchenning.asr.system.SpeechRecognizerDemo;

import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

import javax.sound.sampled.*;

import java.lang.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Audio{

    //定义录音格式
    AudioFormat af = null;
    //定义目标数据行,可以从中读取音频数据,该 TargetDataLine 接口提供从目标数据行的缓冲区读取所捕获数据的方法。
    TargetDataLine td = null;
    //定义源数据行,源数据行是可以写入数据的数据行。它充当其混频器的源。应用程序将音频字节写入源数据行，这样可处理字节缓冲并将它们传递给混频器。
    SourceDataLine sd = null;
    //定义字节数组输入输出流
    ByteArrayInputStream bais = null;
    ByteArrayOutputStream baos = null;
    //定义音频输入流
    AudioInputStream ais = null;
    //定义停止录音的标志，来控制录音线程的运行
    Boolean stopflag = false;
    //记录开始录音的时间
    long startPlay;
    //设置一个播放的标志
    Boolean playflag;
    //每次保存的最后的文件名
    File tarFile = null;
    //定义音频波形每次显示的字节数
    int intBytes = 0;
    //定义每次录音的时候每次提取字节来画音频波
    byte audioDataBuffer[] = null;
    //定义所需要的组件
    JPanel jp1, jp2, jp3;
    JLabel jl1 = null;
    JButton captureBtn;
    //设置画波形线程的终止的标志
    boolean flag = true;
    //定义播放录音时的一个计数值
    int cnt;
    //定义播放录音时一个缓冲数组
    byte btsPlay[] = null;

    int gridx, gridy, gridwidth, gridheight, anchor, fill, ipadx, ipady;
    double weightx, weighty;
    Insets inset;
    GridBagConstraints c;
    public static volatile int a = 1;


    public static void main(String[] args) throws IOException {
        ScheduledExecutorService timer = new ScheduledThreadPoolExecutor(1, new CustomThreadFactory(), new ThreadPoolExecutor.AbortPolicy());

        //        //创造一个实例
        Audio mr = new Audio();

        timer.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4050);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                a = 2;
            }
        }, 1, 5, TimeUnit.SECONDS);


        //调用录音的方法
        mr.capture();

        //记录开始录音的时间
        mr.startPlay = System.currentTimeMillis();


        while (true) {
            if (a == 2) {
                //调用停止录音的方法
                mr.stop();

                //调用保存录音的方法
                String save = mr.save();
                SpeechRecognizerDemo.sendFile(save);

//                将其放到指定的路径下
//                //定义最终要存放的文件路径
//                String destPath = "audio/";
//                mr.copyFile("audio2/", destPath);
                break;
            }

        }
        System.out.println("结束");
        timer.shutdown();


    }

    //构造函数
    public Audio() {

    }


    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        //当开始录音按钮被按下时就开始录音
        if (e.getSource().equals(captureBtn)) {
            //改变按钮上面的字的内容
            captureBtn.setText("松开 结束");

            //调用录音的方法
            capture();

            //记录开始录音的时间
            startPlay = System.currentTimeMillis();
        }

    }


    public void mouseReleased(MouseEvent e) {
        //当松开录音按钮时停止录音并保存录音的文件
        if (e.getSource().equals(captureBtn)) {
            //调用停止录音的方法
            stop();
            //当松开按钮后对显示波形的面板进行清空
            jp2.repaint();
            //改变按钮上面的字的内容
            captureBtn.setText("按住 说话");
            //调用保存录音的方法
            save();
            //将其放到指定的路径下
            //定义最终要存放的文件路径
            String destPath = "audio/";
            copyFile("audio2/", destPath);

//            System.exit(0);
        }
    }

    public void mouseEntered(MouseEvent e) {


    }


    public void mouseExited(MouseEvent e) {


    }

    //开始录音
    public void capture() {
        try {
            //af为AudioFormat也就是音频格式
            af = getAudioFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, af);
            td = (TargetDataLine) (AudioSystem.getLine(info));

            //打开具有指定格式的行，这样可使行获得所有所需的系统资源并变得可操作。
            td.open(af);
            //允许某一数据行执行数据 I/O
            td.start();

            //启动显示波形的进程
            RecordWave aw = new RecordWave();
            Thread t2 = new Thread(aw);
            t2.start();
            //把显示波形的进程标志设为true
            flag = true;

            Record record = new Record();
            Thread t1 = new Thread(record);
            t1.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
    }

    //停止录音
    public void stop() {
        stopflag = true;
        //将画波形的进程终止
        flag = false;
    }

    //保存录音
    public String save() {
        af = getAudioFormat();
        byte audioData[] = baos.toByteArray();
        bais = new ByteArrayInputStream(audioData);
        ais = new AudioInputStream(bais, af, audioData.length / af.getFrameSize());
        //定义最终保存的文件名
        File file = null;
        //写入文件
        try {
            //以当前的时间命名录音的名字
            //将录音的文件存放到F盘下语音文件夹下
            File filePath = new File("audio/");
            String tarPath = "audio/";
            if (!filePath.exists()) {//如果文件不存在，则创建该目录
                filePath.mkdirs();
            }
            long time = System.currentTimeMillis();
            String fileName = filePath + "/" + time + ".wav";
            file = new File(fileName);
            AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
            return fileName;
//            //将录音产生的wav文件转换为容量较小的mp3格式
//            //定义产生后文件名
//            tarFile = new File(tarPath+time+".mp3");
//            Runtime run = null;
//            //测试当前的路径
//
//            try {
//                run = Runtime.getRuntime();
//                //调用编码器来将wav文件转换为mp3文件
//                //把编码得到的mp3文件先存放到D盘下，然后利用文件拷贝函数将它放到指定的文件夹下同时将D盘下的文件删除
//                Process p=run.exec(filePath+"/"+"lame -b 16 "+filePath+"/"+file.getName()+" "+tarPath+tarFile.getName()); //16为码率，可自行修改
//                //释放进程
//                p.getOutputStream().close();
//                p.getInputStream().close();
//                p.getErrorStream().close();
//                //等待
//                p.waitFor();
//
////				//删除之前保存的的wav文件
////				if(file.exists())
////				{
////					file.delete();
////				}
//
////				//定义最终要存放的文件路径
////				String destPath = "D:/Program Files/apache-tomcat-6.0.35/webapps/XWZ/tempFile/";
////				copyFile(tarPath+tarFile.getName(), destPath);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }finally{
//                //最后都要执行的语句
//                //run调用lame解码器最后释放内存
//                run.freeMemory();
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            //关闭流
            try {

                if (bais != null) {
                    bais.close();
                }
                if (ais != null) {
                    ais.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //文件拷贝方法
    public void copyFile(String srcPath, String destPath) {
        File srcFile = new File(srcPath);
        //如果目的文件夹没有则创建目的文件夹
        (new File(destPath)).mkdirs();
        //在目的文件夹下创建要复制的文件
        File destFile = new File(destPath + "/" + srcFile.getName());
        if (srcFile.isFile() && srcFile.exists()) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = new FileInputStream(srcFile);
                out = new FileOutputStream(destFile);
                //设置缓冲数组
                byte[] buff = new byte[1024 * 5];
                int len = 0;
                while ((len = in.read(buff)) != -1) {
                    out.write(buff, 0, len);
                }
//		        //测试该函数是否执行
//		        System.out.println("ok1");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //关闭流，先开的后关闭
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //复制过后删除源文件夹中的的文件
        if (srcFile.exists()) {
            srcFile.delete();
        }
    }

    //设置AudioFormat的参数
    public AudioFormat getAudioFormat() {
        //下面注释部分是另外一种音频格式，两者都可以
        AudioFormat.Encoding encoding = AudioFormat.Encoding.
                PCM_SIGNED;
        float rate = 8000f;
        int sampleSize = 16;
        String signedString = "signed";
        boolean bigEndian = true;
        int channels = 1;
        return new AudioFormat(encoding, rate, sampleSize, channels,
                (sampleSize / 8) * channels, rate, bigEndian);
//		//采样率是每秒播放和录制的样本数
//		float sampleRate = 16000.0F;
//		// 采样率8000,11025,16000,22050,44100
//		//sampleSizeInBits表示每个具有此格式的声音样本中的位数
//		int sampleSizeInBits = 16;
//		// 8,16
//		int channels = 1;
//		// 单声道为1，立体声为2
//		boolean signed = true;
//		// true,false
//		boolean bigEndian = true;
//		// true,false
//		return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed,bigEndian);
    }

    //录音类，因为要用到MyRecord类中的变量，所以将其做成内部类
    class Record implements Runnable {
        //定义存放录音的字节数组,作为缓冲区
        byte bts[] = new byte[10000];

        //将字节数组包装到流里，最终存入到baos中
        //重写run函数
        @Override
        public void run() {
            baos = new ByteArrayOutputStream();
            try {
                stopflag = false;
                while (stopflag != true) {
                    //当停止录音没按下时，该线程一直执行
                    //从数据行的输入缓冲区读取音频数据。
                    //要读取bts.length长度的字节,cnt 是实际读取的字节数
                    int cnt = td.read(bts, 0, bts.length);
                    if (cnt > 0) {
                        baos.write(bts, 0, cnt);
                    }

                    //开始从音频流中读取字节数
                    byte copyBts[] = bts;
                    bais = new ByteArrayInputStream(copyBts);
                    ais = new AudioInputStream(bais, af, copyBts.length / af.getFrameSize());
                    try {
                        DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, af);
                        sd = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
                        sd.open(af);
                        sd.start();

                        //从音频流中读取
                        int Buffer_Size = 10000;
                        audioDataBuffer = new byte[Buffer_Size];
                        int outBytes;

                        intBytes = ais.read(audioDataBuffer, 0, audioDataBuffer.length);

//						不写到混频器中这样就不会播放
//						if (intBytes >= 0) {
//							outBytes = sd.write(audioDataBuffer, 0,audioDataBuffer.length);
//						}
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    //intBytes = -1;
                    //关闭打开的字节数组流
                    if (baos != null) {
                        baos.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //下面这句td.drain()不能要，这样如果不播放数据就阻塞再次录音会出现其他程序访问错误
                    //td.drain();
                    td.close();

                }
            }
        }

    }

    //画波形的类
    //因为要使用一些主函数中的数据，所以做成内部类
    class RecordWave extends JPanel implements Runnable {
        //用画笔画出波形
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.fillRect(jp2.getX(), jp2.getY(), 800, 380);
            if (audioDataBuffer != null) {
                g.drawLine(jp2.getWidth() / 256, 700, jp2.getWidth() / 256, 700);

                for (int i = 0; i < audioDataBuffer.length - 1; ++i) {
                    g.setColor(Color.RED);
                    g.drawLine(i * jp2.getWidth() / 256, (int) audioDataBuffer[i] + 200, (i + 1)

                            * jp2.getWidth() / 256, (int) audioDataBuffer[i + 1] + 200);
                }
            }
        }

        @Override
        public void run() {
            //刷新波形
            while (true) {
                //System.out.println("ok");
                try {
                    synchronized (this) {
                        //隔多长时间获取
                        Thread.sleep(300);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
//                this.paint(jp2.getGraphics());
                //终止线程

                break;

            }
        }
    }

}
