/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sensor;

//import java.awt.Graphics;
//import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Writer;
import static java.lang.Thread.sleep;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Scanner;
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

import jssc.SerialPort;
import static jssc.SerialPort.MASK_RXCHAR;
import jssc.SerialPortEvent;
import jssc.SerialPortException;
import jssc.SerialPortList;

import org.opencv.imgproc.Imgproc;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import static javax.swing.WindowConstants.HIDE_ON_CLOSE;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

import static org.opencv.videoio.VideoWriter.fourcc;
import org.opencv.videoio.Videoio;


import utils.Utils;
//import org.bytedeco.javacpp.opencv_core;
//import org.bytedeco.javacv.FFmpegFrameGrabber;
//import org.bytedeco.javacv.FFmpegFrameRecorder;
//import org.bytedeco.javacv.Frame;
//import org.bytedeco.javacv.FrameGrabber;
//import org.bytedeco.javacv.FrameRecorder;
//import org.bytedeco.javacv.Java2DFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameConverter;
//import org.bytedeco.javacv.OpenCVFrameGrabber;

/**
 * FXML Controller class
 *
 * @author rodri
 */
public class TelaController implements Initializable {

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
//    private static FFmpegFrameRecorder recorder = null;
//    
//    private static OpenCVFrameGrabber grabber = null;
//    private static final int WEBCAM_DEVICE_INDEX = 0;
//    private static final int CAPTUREWIDTH = 600;
//    private static final int CAPTUREHRIGHT = 600;
//    private static final int FRAME_RATE = 30;
//    private static final int GOP_LENGTH_IN_FRAMES = 60;
//    private volatile boolean runnable = true;
//    private static final long serialVersionUID = 1L;
    
   

    
    Mat frame = new Mat();
    private Canvas canvas;
    private Catcher cat;
    private volatile boolean runnable = true;
    private Thread catcher;
//    private Tela telas;
    
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    //private VideoCapture capture = new VideoCapture();
     private VideoCapture capture = new VideoCapture(); 
      
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static int cameraId = 0;
   // private VideoWriter writer =   new  VideoWriter("C:\\test.avi", VideoWriter.fourcc ( 'X' ,  'V' ,  'I' ,  'D' ),33, new  Size ( 1280 ,  720 ) ,true);
     VideoWriter writer = new VideoWriter("test.avi", VideoWriter.fourcc('X', 'V', 'I', 'D'), 15, new Size(1280, 720), true);
    
    SerialPort arduinoPort = null;
    ObservableList<String> portList;
    
    Label labelValue;
    final int NUM_OF_POINT = 50;
    @FXML
    private XYChart.Series series;
    
        private void detectPort(){
         
        portList = FXCollections.observableArrayList();
 
        String[] serialPortNames = SerialPortList.getPortNames();
        for(String name: serialPortNames){
            System.out.println(name);
            portList.add(name);
        }
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
//        grabber = new OpenCVFrameGrabber(WEBCAM_DEVICE_INDEX);
//        cat = new Catcher();
//        runnable = true;
      // writer.set( "c:/test.avi" , VideoWriter.fourcc ( 'X' ,  'V' ,  'I' ,  'D' ),  15 ,  new  Size ( 1,280 ,  720 ),  true ) ;  
//          VideoWriter outputVideo;
//Size S = Size((int) inputVideo.get(CV_CAP_PROP_FRAME_WIDTH),    //Acquire input size
//              (int) inputVideo.get(CV_CAP_PROP_FRAME_HEIGHT));
//outputVideo.open(NAME , ex, inputVideo.get(CV_CAP_PROP_FPS),S, true);
        //VideoWriter writer;
       // writer.open("C:\\test.avi", VideoWriter.fourcc ( 'X' ,  'V' ,  'I' ,  'D' ),33, new  Size ( 1280 ,  720 ) ,true);
       
        capture.set(Videoio.CV_CAP_PROP_FRAME_WIDTH,  1280 );  
        capture.set (Videoio.CV_CAP_PROP_FRAME_HEIGHT,  720 );
        cat = new Catcher();
        
        detectPort();
        final ComboBox comboBoxPorts = new ComboBox(portList);
        comboBoxPorts.valueProperty()
                .addListener(new ChangeListener<String>() {

            public void changed(ObservableValue<? extends String> observable, 
                    String oldValue, String newValue) {

                System.out.println(newValue);
                disconnectArduino();
                connectArduino(newValue);
            }
         });

    }
    
            public void shiftSeriesData(float newValue)
    {
        for(int i=0; i<NUM_OF_POINT-1; i++){
            XYChart.Data<String, Number> ShiftDataUp = 
                    (XYChart.Data<String, Number>)series.getData().get(i+1);
            Number shiftValue = ShiftDataUp.getYValue();
            XYChart.Data<String, Number> ShiftDataDn = 
                    (XYChart.Data<String, Number>)series.getData().get(i);
            ShiftDataDn.setYValue(shiftValue);
        }
        XYChart.Data<String, Number> lastData = 
            (XYChart.Data<String, Number>)series.getData().get(NUM_OF_POINT-1);
        lastData.setYValue(newValue);
    }
    
        public boolean connectArduino(String port){
        
        System.out.println("connectArduino");
        
        boolean success = false;
        SerialPort serialPort = new SerialPort(port);
        try {
            serialPort.openPort();
            serialPort.setParams(
                    SerialPort.BAUDRATE_9600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setEventsMask(MASK_RXCHAR);
            serialPort.addEventListener((SerialPortEvent serialPortEvent) -> {
                if(serialPortEvent.isRXCHAR()){
                    try {
                        
                        byte[] b = serialPort.readBytes();
                        int value = b[0] & 0xff;    //convert to int
                        String st = String.valueOf(value);

                        //Update label in ui thread
                        Platform.runLater(() -> {
                            labelValue.setText(st);
                            shiftSeriesData((float)value * 5/255); //in 5V scale
                        });
                        
                    } catch (SerialPortException ex) {
                        Logger.getLogger(TelaController.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                    
                }
            });
            
            arduinoPort = serialPort;
            success = true;
        } catch (SerialPortException ex) {
            Logger.getLogger(TelaController.class.getName())
                    .log(Level.SEVERE, null, ex);
            System.out.println("SerialPortException: " + ex.toString());
        }

        return success;
    }
    
    public void disconnectArduino(){
        
        System.out.println("disconnectArduino()");
        if(arduinoPort != null){
            try {
                arduinoPort.removeEventListener();
                
                if(arduinoPort.isOpened()){
                    arduinoPort.closePort();
                }
                
            } catch (SerialPortException ex) {
                Logger.getLogger(TelaController.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
        }
    }    
    public void stop() throws Exception {
        disconnectArduino();
        //super.stop();
    }
        

    @FXML
    public void actionConectar(){
        
    }
    
    
    @FXML
    public void actionGravar(){
            
        
    }
    
    @FXML
	protected void startCamera()
	{
		if (!this.cameraActive)
		{
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
                                                
						// convert and show the frame
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(currentFrame, imageToShow);
                                                
                                                
                                                
                                                
                                                
                                                
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
                        writer.release();  
//        
//                        capture.release(); 
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
    
//    class Tela implements Runnable {
//        
//    }
    
    
    class Catcher implements Runnable {
        
        @Override
        public void run() {
            
            synchronized (this) {
               // while (runnable) {
               
                Thread thread = new Thread(){
                    
                            @Override public void run() {
                                int  index =  0 ;
                                Mat frame = grabFrame();
                                
                                while ( true ) {  
                                    
                                if  (capture.read (frame)) {  
                                    
                                    System.out.println ( "Captured the Frame the Width"  + frame.width () +  "the Height"  + frame.height ());  
                                    writer.write(frame);  

                                        try { 
                                            Thread.currentThread().sleep(66); 
                            } catch (InterruptedException ex) {
                                System.out.println("algo errado");
                                Logger.getLogger(TelaController.class.getName()).log(Level.SEVERE, null, ex);
                            }

                            index ++;  
                        }  

                        if  (index>  200 ) {  
                            break;  
                        }  

                                frame.release ();  
                            } 
                                
                            }
                        };
                    thread.start();   
                    


                //}//end of while
            }
        }
    }
//    
    
    
    
    
    
    @FXML
    public void actionParar(){
        
          
            
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
