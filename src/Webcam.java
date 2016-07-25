import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_highgui;
import org.bytedeco.javacpp.opencv_videoio;
import org.bytedeco.javacpp.opencv_videoio.CvCapture;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgproc.*;



public class Webcam {
   public static void main(String[] args) throws Exception {
	   
       CvCapture capture = opencv_videoio.cvCreateCameraCapture(0);
       
       IplImage img1, imghsv, imgbin;
       //For blue
       CvScalar minc = cvScalar(95,125,75,0), maxc = cvScalar(145,255,255,0);
       CvSeq contour1 = new CvSeq(), contour2;
       CvMemStorage storage = CvMemStorage.create();
       CvMoments moments = new CvMoments(Loader.sizeof(CvMoments.class));
       double areaMax = 1000, areaC = 0;
       double m01, m10, pixel_area, focal, pixel_width, obj_width, obj_height, ratio, blank_area;
       double distance;
       
       //focal is (pixel width * distance in inches) / object width 
       focal = 816.8735384615385;
       //Real objects width in inches
       obj_width = 11.75;
       //Real objects height in inches
       obj_height = 9.75;
       //Real blank spot in U area
       blank_area = 64.90625;
       
       int posX=0, posY=0;
       
       int cRad = 100;
       
       while(true) {
       
    	   img1 = opencv_videoio.cvQueryFrame(capture);
    	   opencv_imgproc. cvSmooth(img1, img1, CV_MEDIAN, 15, 0, 0, 0);
    	   imgbin = IplImage.create(cvGetSize(img1), 8, 1);
    	   imghsv = IplImage.create(cvGetSize(img1), 8, 3);
       
    	   if(img1 == null) break;
    	   cvCvtColor(img1, imghsv, CV_BGR2HSV);
    	   cvInRangeS(imghsv, minc, maxc, imgbin);
       
    	   contour1 = new CvSeq();
    	   areaMax = 1000;
       
    	   cvFindContours(imgbin, storage, contour1, Loader.sizeof(CvContour.class), CV_RETR_LIST, CV_LINK_RUNS, cvPoint(0,0));
       
    	   contour2 = contour1;
       
    	   while(contour1 != null && !contour1.isNull()) {
    		   
    		   areaC = cvContourArea(contour1, CV_WHOLE_SEQ, 1);
           
    		   if(areaC > areaMax)
    		   {
    			   areaMax = areaC;
    		   }
           
    		   contour1 = contour1.h_next();
    	   }
       
    	   while(contour2 != null && !contour2.isNull()) {
    		   
    		   areaC = cvContourArea(contour2, CV_WHOLE_SEQ, 1);
           
    		   if(areaC < areaMax) {
    			   
    			   cvDrawContours(imgbin, contour2, CV_RGB(0,0,0),CV_RGB(0,0,0),0,CV_FILLED,8,cvPoint(0,0));
    		   }
    		   
    		   contour2 = contour2.h_next();
    	   }
       
    	   cvMoments(imgbin, moments, 1);
    	   m10 = cvGetSpatialMoment(moments, 1, 0);
    	   m01 = cvGetSpatialMoment(moments, 0, 1);
    	   pixel_area = cvGetCentralMoment(moments, 0, 0);
       
    	   posX = (int) (m10/pixel_area);
    	   posY = (int) (m01/pixel_area);
       
    	   if(posX > 0 && posY > 0) {
           
    		   cRad = (int) (100 / (5000/pixel_area));
    		   cvCircle(img1, cvPoint(posX, posY), 5, cvScalar(0,255,0,0), 9, 0, 0);
    	   }
    	   
    	   //For rectangle delete (- blank_area) 
    	   //For U shaped add after obj_width (- blank_area);
    	   ratio =  java.lang.Math.sqrt((pixel_area/(obj_height*obj_width - blank_area)));
    	   pixel_width = ratio * obj_width;
    	   distance = (obj_width * focal) / pixel_width;
       
    	   cvFlip(img1, img1, 1);
    	   cvFlip(imgbin, imgbin , 1);
       
    	   opencv_highgui.cvShowImage("Color",img1);
    	   opencv_highgui.cvShowImage("CF",imgbin);
    	   char c = (char) opencv_highgui.cvWaitKey(15);
    	   
    	   if(c == 27) break;
    	   
    	   if(c == 'q') {
    		   
    		   System.out.print("Width in pixels ");
    		   System.out.println(pixel_width);
    		   System.out.print("Distance in inches ");
    		   System.out.println(distance);
           
    	   }
       }
       
   }
}
