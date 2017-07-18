package com.orz.testrecord;

import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import java.io.File;
import java.util.UUID;

/**
 * Created by DON on 17/07/14.
 */

public class MediaRecorderUtil {
  private MediaRecorder mMediaRecorder;
  private String        mSavePath;
  private String        mCurrentFilePath;
  public MediaRecorderUtil(String savePath, String fileName) {
    mSavePath = savePath + "/" + fileName;
    File file = new File(mSavePath);
    if (!file.exists()) file.mkdirs();
  }
//  开始录音
  public void startRecord() {
    try {
      mMediaRecorder = new MediaRecorder();
      File file = new File(mSavePath, generateFileName());
      mCurrentFilePath = file.getAbsolutePath();
      // 设置录音文件的保存位置
      mMediaRecorder.setOutputFile(mCurrentFilePath);
      mMediaRecorder.setAudioChannels(2);
      //44100是目前的标准，但是某些设备仍然支持22050，16000，11025
      mMediaRecorder.setAudioSamplingRate(11025);
//    采用频率（the sampling rate）：模拟信息转成数字信号的采样率。
//    采样位数：8位 或者 16位 去存储每一次的采样结果。
//    声道数：单声道，立体声道。
//    比特率（Bit rate ）/位率：声音中的比特率是指将模拟声音信号转换成数字声音信号后，单位时间内的二进制数据量，是间接衡量音频质量的一个指标。
//    比特率(bps) = 采样频率（HZ） 采样位数（Bit） 声道数
      mMediaRecorder.setAudioEncodingBitRate(128000);//1411200 96000
//    mRecorder.setAudioEncodingBitRate(11025 * 16 * 2);//1411200 96000
//    mRecorder.setAudioEncodingBitRate(44100 * 16 * 2);//1411200 96000
      // 设置录音的来源（从哪里录音）
      mMediaRecorder.setAudioSource(AudioSource.MIC);
      // 设置录音的保存格式
      mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      // 设置录音的编码
      mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

      mMediaRecorder.prepare();
      mMediaRecorder.start();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
//  停止录音
  public void stopAndRelease() {
    if (mMediaRecorder == null) return;
    mMediaRecorder.stop();
    mMediaRecorder.release();
    mMediaRecorder = null;
  }
//  取消本次录音操作
  public void cancel() {
    this.stopAndRelease();
    if (mCurrentFilePath != null) {
      File file = new File(mCurrentFilePath);
      file.delete();
      mCurrentFilePath = null;
    }
  }
  private String generateFileName() {
    return UUID.randomUUID().toString() + ".amr";
  }
//  得到录音文件的路径
  public String getCurrentFilePath() {
    return mCurrentFilePath;
  }
}
