package ai.doufu;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

import com.iflytek.cloud.speech.LexiconListener;
import com.iflytek.cloud.speech.RecognizerListener;
import com.iflytek.cloud.speech.RecognizerResult;
import com.iflytek.cloud.speech.Setting;
import com.iflytek.cloud.speech.SpeechConstant;
import com.iflytek.cloud.speech.SpeechError;
import com.iflytek.cloud.speech.SpeechEvent;
import com.iflytek.cloud.speech.SpeechRecognizer;
import com.iflytek.cloud.speech.SpeechSynthesizer;
import com.iflytek.cloud.speech.SpeechUtility;
import com.iflytek.cloud.speech.SynthesizeToUriListener;
import com.iflytek.cloud.speech.UserWords;

public class IFlymsc {

	private static final String APPID = "5a97f195";

	private static final String USER_WORDS = "{\"userword\":[{\"name\":\"计算机词汇\",\"words\":[\"随机存储器\",\"只读存储器\",\"扩充数据输出\",\"局部总线\",\"压缩光盘\",\"十七寸显示器\"]},{\"name\":\"我的词汇\",\"words\":[\"槐花树老街\",\"王小贰\",\"发炎\",\"公事\"]}]}";

	private static IFlymsc mObject;

	private static StringBuffer mResult = new StringBuffer();
	
	private boolean mIsLoop = true;
	
	private long start = 0;

	public static void main(String args[]) {
		if (args == null || args.length < 1) {
			System.out.println("Usage:  java IFlymsc.jar audio_file [true]");
			return;
		}
		
		String filename = args[0];
		Setting.setShowLog( false );
		DebugLog.show_tag = false;
		
		if( args.length==2 && args[1].equals("true") ){
			//在应用发布版本中，请勿显示日志，详情见此函数说明。
			Setting.setShowLog( true );
			DebugLog.show_tag = true;
		}
		
		SpeechUtility.createUtility("appid=" + APPID);	
		//getMscObj().loop();
		getMscObj().start(filename);
	}

	private static IFlymsc getMscObj() {
		if (mObject == null)
			mObject = new IFlymsc();
		return mObject;
	}

	private boolean onLoop() {
		try {
			String filename = "./audio.pcm";
			
			DebugLog.Log( "Begin to Recognize " + filename );
			start = (new Date()).getTime() / 1000;
			Recognize(filename);
			
			mIsLoop = false;
			
		} catch (Exception e) {
		}
		return false;
	}
	
	private boolean start(String filename) {
		try {
			//String filename = "./audio.pcm";
			
			DebugLog.Log( "Begin to Recognize " + filename );
			start = (new Date()).getTime() / 1000;
			Recognize(filename);
			
			mIsLoop = false;
			
		} catch (Exception e) {
		}
		return false;
	}

	// *************************************音频流听写*************************************

	/**
	 * 听写
	 */
	
	private boolean mIsEndOfSpeech = false;
	private void Recognize(String filename) {
		if (SpeechRecognizer.getRecognizer() == null)
			SpeechRecognizer.createRecognizer();
		mIsEndOfSpeech = false;
		RecognizePcmfileByte(filename);
	}

	/**
	 * 自动化测试注意要点 如果直接从音频文件识别，需要模拟真实的音速，防止音频队列的堵塞
	 */
	public void RecognizePcmfileByte(String filename) {
		SpeechRecognizer recognizer = SpeechRecognizer.getRecognizer();
		
		setParam(recognizer);
		
//		recognizer.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
//		//写音频流时，文件是应用层已有的，不必再保存
//		recognizer.setParameter(SpeechConstant.ASR_AUDIO_PATH,
//				"./iat_test.pcm");
//		recognizer.setParameter( SpeechConstant.RESULT_TYPE, "plain" );
		
		recognizer.startListening(recListener);
		
		FileInputStream fis = null;
		final byte[] buffer = new byte[64*1024];
		try {
			//fis = new FileInputStream(new File(System.getProperty("user.dir") + "\\" +  filename));
			fis = new FileInputStream(new File(filename));
			if (0 == fis.available()) {
				mResult.append("no audio avaible!");
				recognizer.cancel();
			} else {
				int lenRead = buffer.length;
				while( buffer.length==lenRead && !mIsEndOfSpeech ){
					lenRead = fis.read( buffer );
					recognizer.writeAudio( buffer, 0, lenRead );
				}//end of while
				
				recognizer.stopListening();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fis) {
					fis.close();
					fis = null;
				}
		} catch (IOException e) {
				e.printStackTrace();
			}
		}//end of try-catch-finally
		
	}
	
	private  void setParam(SpeechRecognizer mIat) {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		mIat.setParameter(SpeechConstant.DOMAIN, "iat");
		
		// 设置听写引擎
//		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		//mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "plain");
		
		// 设置语言
		mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
		mIat.setParameter(SpeechConstant.ACCENT, null);
		
		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS,  "4000");
		
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS,  "3000");
		
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT,  "1");
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		//mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "pcm");
		//mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH,   "audio.pcm");
		
		// 设置音频来源为外部文件
		mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-1");
		// 也可以像以下这样直接设置音频文件路径识别（要求设置文件的全路径）：
		//mIat.setParameter(SpeechConstant.AUDIO_SOURCE, "-2");
		//mIat.setParameter(SpeechConstant.ASR_SOURCE_PATH, "audio.pcm");
		
	}

	/**
	 * 听写监听器
	 */
	private RecognizerListener recListener = new RecognizerListener() {

		public void onBeginOfSpeech() {
//			DebugLog.Log( "onBeginOfSpeech enter" );
//			DebugLog.Log("*************开始录音*************");
		}

		public void onEndOfSpeech() {
//			DebugLog.Log( "onEndOfSpeech enter" );
			mIsEndOfSpeech = true;
		}

		public void onVolumeChanged(int volume) {
//			DebugLog.Log( "onVolumeChanged enter" );
//			if (volume > 0)
//				DebugLog.Log("*************音量值:" + volume + "*************");
		}

		public void onResult(RecognizerResult result, boolean islast) {
			DebugLog.Log( "onResult enter" );
			//long cost = (new Date()).getTime() / 1000 - start;
			//DebugLog.Log( "Cost seconds: " + cost);
			
			mResult.append(result.getResultString());
			
			if( islast ){
				String result_string = mResult.toString();
				System.out.println(result_string);
				
				DebugLog.Log("识别结果为:" + result_string);
				mIsEndOfSpeech = true;
				mResult.delete(0, mResult.length());
				waitupLoop();
			}
		}

		public void onError(SpeechError error) {
			mIsEndOfSpeech = true;
			DebugLog.Log("*************" + error.getErrorCode()
					+ "*************");
			waitupLoop();
		}

		public void onEvent(int eventType, int arg1, int agr2, String msg) {
			DebugLog.Log( "onEvent enter" );
		}

	};

	// *************************************无声合成*************************************

	/**
	 * 合成
	 */
	private void Synthesize() {
		SpeechSynthesizer speechSynthesizer = SpeechSynthesizer
				.createSynthesizer();
		// 设置发音人
		speechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");

		//启用合成音频流事件，不需要时，不用设置此参数
		speechSynthesizer.setParameter( SpeechConstant.TTS_BUFFER_EVENT, "1" );
		// 设置合成音频保存位置（可自定义保存位置），默认不保存
		speechSynthesizer.synthesizeToUri("语音合成测试程序 ", "./tts_test.pcm",
				synthesizeToUriListener);
	}

	/**
	 * 合成监听器
	 */
	SynthesizeToUriListener synthesizeToUriListener = new SynthesizeToUriListener() {

		public void onBufferProgress(int progress) {
			DebugLog.Log("*************合成进度*************" + progress);

		}

		public void onSynthesizeCompleted(String uri, SpeechError error) {
			if (error == null) {
				DebugLog.Log("*************合成成功*************");
				DebugLog.Log("合成音频生成路径：" + uri);
			} else
				DebugLog.Log("*************" + error.getErrorCode()
						+ "*************");
			waitupLoop();

		}


		@Override
		public void onEvent(int eventType, int arg1, int arg2, int arg3, Object obj1, Object obj2) {
			if( SpeechEvent.EVENT_TTS_BUFFER == eventType ){
				DebugLog.Log( "onEvent: type="+eventType
						+", arg1="+arg1
						+", arg2="+arg2
						+", arg3="+arg3
						+", obj2="+(String)obj2 );
				ArrayList<?> bufs = null;
				if( obj1 instanceof ArrayList<?> ){
					bufs = (ArrayList<?>) obj1;
				}else{
					DebugLog.Log( "onEvent error obj1 is not ArrayList !" );
				}//end of if-else instance of ArrayList
				
				if( null != bufs ){
					for( final Object obj : bufs ){
						if( obj instanceof byte[] ){
							final byte[] buf = (byte[]) obj;
							DebugLog.Log( "onEvent buf length: "+buf.length );
						}else{
							DebugLog.Log( "onEvent error element is not byte[] !" );
						}
					}//end of for
				}//end of if bufs not null
			}//end of if tts buffer event
		}

	};

	// *************************************词表上传*************************************

	/**
	 * 词表上传
	 */
	private void uploadUserWords() {
		SpeechRecognizer recognizer = SpeechRecognizer.getRecognizer();
		if ( recognizer == null) {
			recognizer = SpeechRecognizer.createRecognizer();
			
			if( null == recognizer ){
				DebugLog.Log( "获取识别实例实败！" );
				waitupLoop();
				return;
			}
		}

		UserWords userwords = new UserWords(USER_WORDS);
		recognizer.setParameter( SpeechConstant.DATA_TYPE, "userword" );
		recognizer.updateLexicon("userwords",
				userwords.toString(), 
				lexiconListener);
	}

	/**
	 * 词表上传监听器
	 */
	LexiconListener lexiconListener = new LexiconListener() {
		@Override
		public void onLexiconUpdated(String lexiconId, SpeechError error) {
			if (error == null)
				DebugLog.Log("*************上传成功*************");
			else
				DebugLog.Log("*************" + error.getErrorCode()
						+ "*************");
			waitupLoop();
		}

	};

	private void waitupLoop(){
		synchronized(this){
			IFlymsc.this.notify();
		}
	}

	public void loop() {
		while (mIsLoop) {
			try {
				if (onLoop()) {
					synchronized(this){
						this.wait();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
