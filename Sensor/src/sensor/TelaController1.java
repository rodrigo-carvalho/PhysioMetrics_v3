/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensor;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

//import org.opencv.core.Core;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfByte;
////import org.opencv.highgui.Highgui;
////import org.opencv.highgui.VideoCapture;
//import org.opencv.videoio.VideoCapture;
//import org.opencv.imgcodecs.Imgcodecs;
//
//
//import org.bytedeco.javacv.FFmpegFrameRecorder;
//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.FrameGrabber;
//import org.bytedeco.javacv.FrameRecorder.Exception;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameGrabber;

import jssc.SerialPort;
import static jssc.SerialPort.MASK_RXCHAR;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;
import org.bytedeco.javacpp.avcodec;
import org.opencv.imgproc.Imgproc;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import utils.Utils;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * FXML Controller class
 *
 * @author rodri
 */
public class TelaController1 implements Initializable {

    /**
     * Initializes the controller class.
     */
    @FXML
    private ComboBox<String> comboboxPorta;
    @FXML 
    private TextField nomeprojeto;
    @FXML 
    private TextField nomecoleta;
    @FXML 
    private TextField numerocoleta;
    @FXML 
    private TextField nomepesquisador;
    @FXML
    private Canvas tela;
    @FXML
    private Button buttongravar;
    @FXML
    private Button buttonconectar;
    @FXML
    private Button buttonparar;
    @FXML
    private ImageView currentFrame ;
    private static FFmpegFrameRecorder recorder = null;
    
    private static OpenCVFrameGrabber grabber = null;
    private static final int WEBCAM_DEVICE_INDEX = 0;
    private static final int CAPTUREWIDTH = 600;
    private static final int CAPTUREHRIGHT = 600;
    private static final int FRAME_RATE = 30;
    private static final int GOP_LENGTH_IN_FRAMES = 60;
    private volatile boolean runnable = true;
    private static final long serialVersionUID = 1L;
    
    Mat frame = new Mat();
    
    private Catcher cat;
    private Thread catcher;
    
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    //private VideoCapture capture = new VideoCapture();
     private VideoCapture capture = new VideoCapture(); 
      
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;
    

    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        grabber = new OpenCVFrameGrabber(WEBCAM_DEVICE_INDEX);
        cat = new Catcher();
        runnable = true;

    }

    @FXML
    public void actionConectar(){
        System.out.println("passou aki1");    
        catcher = new Thread(cat);
            catcher.start();
            runnable = true;
    }
    
    
    @FXML
    public void actionGravar(){
        
        if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber;
                            frameGrabber = new Runnable() {
                                
                                @Override
                                public void run()
                                {
                                    // effectively grab and process a single frame
                                    
                                    OpenCVFrameConverter.ToMat converterToMat = new OpenCVFrameConverter.ToMat();
                                    
                                    try {
                                        Frame video = grabber.grab();
                                        
                                        Mat frame = grabFrame();
                                        Frame capturedFrame = null;
                                        BufferedImage image;
                                        Java2DFrameConverter paintConverter = new Java2DFrameConverter();
                                        // convert and show the frame
                                        Image imageToShow = Utils.mat2Image(frame);
                                        updateImageView(currentFrame, imageToShow);

                                        image = this.matToBufferedImage(frame);
                                                                              
                                        recorder = new FFmpegFrameRecorder("outputok.mp4",2);
                                        recorder.setInterleaved(true);
                                        // video options //
                                        recorder.setVideoOption("tune", "zerolatency");
                                        recorder.setVideoOption("preset", "ultrafast");
                                        recorder.setVideoOption("crf", "28");
                                        recorder.setVideoBitrate(2000000);
                                        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);

                                        recorder.setVideoCodec(HIDE_ON_CLOSE);

                                        recorder.setFormat("mp4");
                                        recorder.setFrameRate(FRAME_RATE);
                                        recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
                                        // audio options //
                                        recorder.setAudioOption("crf", "0");
                                        recorder.setAudioQuality(0);
                                        recorder.setAudioBitrate(192000);
                                        recorder.setSampleRate(44100);
                                        recorder.setAudioChannels(2);
                                        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);

                                        try {
                                            recorder.start();
                                        } catch (FrameRecorder.Exception ex) {
                                            Logger.getLogger(TelaController1.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                        try {
                                            recorder.record(video);
                                        } catch (FrameRecorder.Exception ex) {
                                            Logger.getLogger(TelaController1.class.getName()).log(Level.SEVERE, null, ex);
                                        }


                                        } catch (FrameGrabber.Exception ex) {
                                            Logger.getLogger(TelaController1.class.getName()).log(Level.SEVERE, null, ex);
                                        }

                                    

                                }

                                    private BufferedImage matToBufferedImage(Mat frame) {
                                        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                                    }
                            };
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				//this.button.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			//this.button.setText("Start Camera");
			
			// stop the timer
			//this.stopAcquisition();
                }
    }
    
    private Mat grabFrame()
	{
		// init everything
//		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
                    // read the current frame
                    this.capture.read(frame);
                    // if the frame is not empty, process it
                    if (!frame.empty())
                    {
                        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    }
		}
		
		return frame;
}
    
    
    class Catcher implements Runnable {

        @Override
        public void run() {
            synchronized (this) {
               // while (runnable) {
//                    try {
//                        System.out.println("passou aki2");
////                        grabber.setImageWidth(CAPTUREWIDTH);
////                        grabber.setImageHeight(CAPTUREHRIGHT);
////                        grabber.start();
////                        recorder = new FFmpegFrameRecorder(
////                                "output.mp4",
////                                CAPTUREWIDTH, CAPTUREHRIGHT, 2);
//                        recorder = new FFmpegFrameRecorder("output.mp4",2);
//                        recorder.setInterleaved(true);
//                        // video options //
//                        recorder.setVideoOption("tune", "zerolatency");
//                        recorder.setVideoOption("preset", "ultrafast");
//                        recorder.setVideoOption("crf", "28");
//                        recorder.setVideoBitrate(2000000);
//                        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
//                        
//                        recorder.setVideoCodec(HIDE_ON_CLOSE);
//                        
//                        recorder.setFormat("mp4");
//                        recorder.setFrameRate(FRAME_RATE);
//                        recorder.setGopSize(GOP_LENGTH_IN_FRAMES);
//                        // audio options //
//                        recorder.setAudioOption("crf", "0");
//                        recorder.setAudioQuality(0);
//                        recorder.setAudioBitrate(192000);
//                        recorder.setSampleRate(44100);
//                        recorder.setAudioChannels(2);
//                        recorder.setAudioCodec(avcodec.AV_CODEC_ID_AAC);
//
//                        recorder.start();
//
//                        
////                        Java2DFrameConverter paintConverter = new Java2DFrameConverter();
//                        long startTime = System.currentTimeMillis();
//                        long frame = 0;
//                        while ((capture.grab() == true)) {
//                            System.out.println("passou aki3");
//                            recorder.record(frame);
//                            frame++;
//                            long waitMillis = 1000 * frame / FRAME_RATE - (System.currentTimeMillis() - startTime);
//                            while (waitMillis <= 0) {
//                                // If this error appeared, better to consider lower FRAME_RATE.
//                                System.out.println("[ERROR] grab image operation is too slow to encode, skip grab image at frame = " + frame + ", waitMillis = " + waitMillis);
//                                recorder.record(capturedFrame);  // use same capturedFrame for fast processing.
//                                frame++;
//                                waitMillis = 1000 * frame / FRAME_RATE - (System.currentTimeMillis() - startTime);
//                            }
//                            //System.out.println("frame " + frame + ", System.currentTimeMillis() = " + System.currentTimeMillis() + ", waitMillis = " + waitMillis);
//                            Thread.sleep(waitMillis);
//                        }
//                    } catch (InterruptedException ex) { 
//                    Logger.getLogger(TelaController1.class.getName()).log(Level.SEVERE, null, ex);
//                }  catch (FrameRecorder.Exception ex) {
//                    Logger.getLogger(TelaController1.class.getName()).log(Level.SEVERE, null, ex);
//                } 

                //}//end of while
            }
        }
    }
    
    
    
    @FXML
    public void actionParar(){
        
            runnable = false;
            
        if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
}
        
    }
    
    

    	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
    
    



    
    
}
