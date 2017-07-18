package com.orz.testrecord;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by DON on 17/07/14.
 */

public class AudioRecorderUtil {

  private static final String TAG = "AudioRecorderUtil";
  private final String      tempSavePath;
  private final Context     mCtx;
  private       AudioRecord mAudioRecord;
  private       String      mSavePath;
  private       String      mCurrentFilePath;
  private       String      mFileDirectory;
  private       String      mFileName;
  private       RecordTask  mRecordTask;
//  private List<Float> mListVoice = new ArrayList<>();

  private int audioSource    = MediaRecorder.AudioSource.MIC;
  private int sampleRateInHz = 44100; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
  private int channelConfig  = AudioFormat.CHANNEL_IN_STEREO;//CHANNEL_OUT_MONO CHANNEL_IN_STEREO
  private int audioFormat    = AudioFormat.ENCODING_PCM_16BIT;
  private int bufferSizeInBytes;//= AudioTrack.MODE_STREAM;
  private boolean isRecording = false;
  private short[] buffer;
  private boolean isPlaying = false;

  public AudioRecorderUtil(Context ctx, String fileDirectory, String fileName) {
    this.mCtx = ctx;
    this.mFileDirectory = fileDirectory;
    this.mFileName = fileName;

    mSavePath = fileDirectory + "/" + fileName;
    tempSavePath = fileDirectory + "/" + fileName.replace(".pcm", ".mp3");
    Log.i(TAG, TAG + " mSavePath=" + mSavePath);
    Log.i(TAG, TAG + " tempSavePath=" + tempSavePath);
    File file = new File(fileDirectory);
    if (!file.exists()) {
      file.mkdirs();
    }
    File fileVoice = new File(mSavePath);
//    File fileVoiceWav = new File(tempSavePath);
    if (!fileVoice.exists()) {
      try {
        fileVoice.createNewFile();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
//    if (!fileVoiceWav.exists()) {
//      try {
//        fileVoiceWav.createNewFile();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }
  }

  public void prepare() {
    bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    mAudioRecord = new AudioRecord(
        audioSource,// 指定在流的类型
        sampleRateInHz,// 设置音频数据的采样率 32k 32000，如果是44.1k就是44100
        channelConfig,// 设置输出声道为双声道立体声，而CHANNEL_OUT_MONO类型是单声道
        audioFormat,// 设置音频数据块是8位还是16位，这里设置为16位。
        bufferSizeInBytes);// 设置模式类型，在这里设置为流类型
    //根据定义好的几个配置，来获取合适的缓冲大小
//    int bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
    //定义缓冲
    buffer = new short[bufferSizeInBytes];
    //开始录制
//    mAudioRecord.startRecording();
  }

  public void start() {
    mAudioRecord.startRecording();
    mRecordTask = new RecordTask();
    mRecordTask.execute();
  }

  public void pause() {
    isRecording = false;
  }

  public void resume() {
    mAudioRecord.startRecording();
    mRecordTask = new RecordTask();
    mRecordTask.execute();
  }

  public void stop() {
    isRecording = false;

    mAudioRecord.stop();
    mAudioRecord.release();

//    copyWaveFile(mSavePath, tempSavePath);
  }

  public void cancel() {
    isRecording = false;

    mAudioRecord.stop();
    mAudioRecord.release();

  }

  class RecordTask extends AsyncTask<Void, Integer, Void> {

    @Override protected Void doInBackground(Void... params) {

      isRecording = true;

      //开通输出流到指定的文件
      try {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(mSavePath)));
        long time = System.currentTimeMillis();
        while (isRecording) {
          int readSize = mAudioRecord.read(buffer, 0, buffer.length);
          if (AudioRecord.ERROR_INVALID_OPERATION != readSize && dos != null) {

//            calc1(buffer, 0, readSize);
            boolean isShow = false;
            long v = 0;
            for (int i = 0; i < readSize; i++) {
              dos.writeShort(buffer[i]);

              // 将 buffer 内容取出，进行平方和运算
              v += buffer[i] * buffer[i];

              if (System.currentTimeMillis() - time > 500) {
                time = System.currentTimeMillis();
                isShow = true;
              }
            }

            if (isShow) {
              // 平方和除以数据总长度，得到音量大小。
              double mean = v / (double) readSize;
              double volume = 10 * Math.log10(mean);
              Log.d(TAG, "分贝值:" + volume + " # " + mean);
            }

          }
        }

        //-y -ac 1 -ar 16000 -f s16le -i RawAudio.pcm -c:a libmp3lame -q:a 2 yyyy.mp3
//        String strCmd = "-y -i " + mSavePath + " " + tempSavePath;
        String strCmd = "-y -ac 2 -ar 44100 -f s16be -i " + mSavePath + " -acodec libmp3lame -vol 2400 " + tempSavePath;
        Log.i(TAG, TAG + " # cmd " + strCmd);
        final String[] cmd = strCmd.split(" ");
        try {
          FFmpeg.getInstance(mCtx).loadBinary(new FFmpegLoadBinaryResponseHandler() {
            @Override public void onFailure() {

            }

            @Override public void onSuccess() {
              try {
                FFmpeg.getInstance(mCtx).execute(cmd, new ExecuteBinaryResponseHandler() {
                  @Override
                  public void onFailure(String s) {
                    Log.i(TAG, "execFFmpegBinary # onFailure" + s);
                  }

                  @Override
                  public void onSuccess(String s) {
                    Log.i(TAG, "execFFmpegBinary # onSuccess" + s);
                  }

                  @Override
                  public void onProgress(String s) {
                  }

                  @Override
                  public void onStart() {
                  }

                  @Override
                  public void onFinish() {
                    Log.i(TAG, "execFFmpegBinary # onFinish");
                  }
                });
              } catch (FFmpegCommandAlreadyRunningException e) {
                e.printStackTrace();
              }
            }

            @Override public void onStart() {

            }

            @Override public void onFinish() {

            }
          });
        } catch (FFmpegNotSupportedException e) {
          e.printStackTrace();
        }

      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return null;
    }
  }

  void calc1(short[] lin, int off, int len) {
    int i, j;
    for (i = 0; i < len; i++) {
      j = lin[i + off];
      lin[i + off] = (short) (j >> 2);
    }
  }

  public boolean isRecording() {
    return isRecording;
  }

  public void play() {
    new PlayTask().execute();
  }

  class PlayTask extends AsyncTask<Void, Integer, Void> {

    @Override
    protected Void doInBackground(Void... arg0) {
      isPlaying = true;
      int bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioSource);
      short[] buffer = new short[bufferSize / 4];
      try {
        //定义输入流，将音频写入到AudioTrack类中，实现播放
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(mSavePath)));
        //实例AudioTrack
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, AudioFormat.CHANNEL_OUT_STEREO, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        //开始播放
        track.play();
        //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
        while (isPlaying && dis.available() > 0) {
          int i = 0;
          while (dis.available() > 0 && i < buffer.length) {
            buffer[i] = dis.readShort();
            i++;
          }
          //然后将数据写入到AudioTrack中
          track.write(buffer, 0, buffer.length);

        }

        //播放结束
        isPlaying = false;
        track.stop();
        dis.close();
      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }
      return null;
    }
  }

  public void encodeFile() {

//    MediaCodec.createEncoderByType()
  }
}