package ftc.vision;


import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by robotics on 1/5/2016.
 */
public class BlueFinder implements ImageProcessor<BlueResult> {
    //Outputs
    private Mat blurOutput;
    private Mat rgbThresholdOutput;
    private MatOfKeyPoint findBlobsOutput;
    private static final String TAG = "BlueFinder";
    //Sources
    private Mat source0;

    /**
     * This constructor sets up the pipeline
     */
    public BlueFinder() {
    }

    /**
     * This is the primary method that runs the entire pipeline and updates the outputs.
     */
    @Override
    public ImageProcessorResult<BlueResult> process(long startTime, Mat rgbaFrame, boolean saveImages) {

        //Step  Blur0:
        if (saveImages) {
            ImageUtil.saveImage(TAG, rgbaFrame, Imgproc.COLOR_RGBA2BGR, "0_camera", startTime);
        }
        blurOutput = new Mat();
        rgbThresholdOutput = new Mat();
        findBlobsOutput = new MatOfKeyPoint();
        setsource0(rgbaFrame);
        Mat blurInput = source0;
        BlurType blurType = BlurType.get("Median Filter");
        double blurRadius = 3;
        blur(blurInput, blurType, blurRadius, blurOutput);
        Imgproc.cvtColor(blurOutput(), blurOutput(), Imgproc.COLOR_RGB2HSV);
        List<Scalar> hsvMin = new ArrayList<>();
        List<Scalar> hsvMax = new ArrayList<>();

        //hsvMin.add(new Scalar(  H,   S,   V  ));
        hsvMin.add(new Scalar(300/2,  50, 150)); //red min
        hsvMax.add(new Scalar( 60/2, 255, 255)); //red max

        hsvMin.add(new Scalar( 60/2,  50, 150)); //green min
        hsvMax.add(new Scalar(120/2, 255, 255)); //green max

        hsvMin.add(new Scalar(120/2,  100, 50)); //blue min
        hsvMax.add(new Scalar(300/2, 255, 255)); //blue max

        // make a list of channels that are blank (used for combining binary images)
        List<Mat> rgbaChannels = new ArrayList<>();



        // These variables are used inside the loop:
        Mat maskedImage;
        Mat colSum = new Mat();


        //loop through the filters
        for(int i=0; i<3; i++) {
            //apply HSV thresholds
            maskedImage = new Mat();
            ImageUtil.hsvInRange(blurOutput(), hsvMin.get(i), hsvMax.get(i), maskedImage);

            //copy the binary image to a channel of rgbaChannels
            rgbaChannels.add(maskedImage);

            //apply a column sum to the (unscaled) binary image
            Core.reduce(maskedImage, colSum, 0, Core.REDUCE_SUM, 4);

            if (saveImages) {
                ImageUtil.saveImage(TAG, rgbaFrame, Imgproc.COLOR_RGBA2BGR, "1_binary", startTime);
            }
        }
        //add empty alpha channel
        rgbaChannels.add(Mat.zeros(blurOutput.size(), CvType.CV_8UC1));
        //merge the 3 binary images and 1 alpha channel into one image
        Core.merge(rgbaChannels, rgbaFrame);

        //Step  RGB_Threshold0:
        Mat rgbThresholdInput = rgbaFrame;
        double[] rgbThresholdRed = {0, 0};
        double[] rgbThresholdGreen = {0, 0};
        double[] rgbThresholdBlue = {0, 255};
        rgbThreshold(rgbThresholdInput, rgbThresholdRed, rgbThresholdGreen, rgbThresholdBlue, rgbThresholdOutput);


        //Step  Find_Blobs0:
        Mat findBlobsInput = rgbThresholdOutput;
        double findBlobsMinArea = 10;
        double[] findBlobsCircularity = {.5, 1.0};
        boolean findBlobsDarkBlobs = true;
        findBlobs(findBlobsInput, findBlobsMinArea, findBlobsCircularity, findBlobsDarkBlobs, findBlobsOutput);
        KeyPoint[] blobs = findBlobsOutput().toArray();
        if(blobs.length > 0) {
            for (KeyPoint b: blobs)Imgproc.rectangle(rgbThresholdOutput(), new Point(b.pt.x, b.pt.y), new Point(b.pt.x + rgbaFrame.height()/30, b.pt.y + rgbaFrame.height()/30), ImageUtil.BLUE);


            return new ImageProcessorResult<BlueResult>(startTime, rgbaFrame, new BlueResult((int)(blobs[0].pt.x),(int) (blobs[0].pt.y)));
        }
        else return new ImageProcessorResult<BlueResult>(startTime, blurOutput(), new BlueResult(0,0));
    }

    /**
     * This method is a generated setter for source0.
     * @param source0 the Mat to set
     */
    public void setsource0(Mat source0) {
        this.source0 = source0;
    }

    /**
     * This method is a generated getter for the output of a Blur.
     * @return Mat output from Blur.
     */
    public Mat blurOutput() {
        return blurOutput;
    }

    /**
     * This method is a generated getter for the output of a RGB_Threshold.
     * @return Mat output from RGB_Threshold.
     */
    public Mat rgbThresholdOutput() {
        return rgbThresholdOutput;
    }

    /**
     * This method is a generated getter for the output of a Find_Blobs.
     * @return MatOfKeyPoint output from Find_Blobs.
     */
    public MatOfKeyPoint findBlobsOutput() {
        return findBlobsOutput;
    }


    /**
     * An indication of which type of filter to use for a blur.
     * Choices are BOX, GAUSSIAN, MEDIAN, and BILATERAL
     */
    enum BlurType{
        BOX("Box Blur"), GAUSSIAN("Gaussian Blur"), MEDIAN("Median Filter"),
        BILATERAL("Bilateral Filter");

        private final String label;

        BlurType(String label) {
            this.label = label;
        }

        public static BlurType get(String type) {
            if (BILATERAL.label.equals(type)) {
                return BILATERAL;
            }
            else if (GAUSSIAN.label.equals(type)) {
                return GAUSSIAN;
            }
            else if (MEDIAN.label.equals(type)) {
                return MEDIAN;
            }
            else {
                return BOX;
            }
        }

        @Override
        public String toString() {
            return this.label;
        }
    }

    /**
     * Softens an image using one of several filters.
     * @param input The image on which to perform the blur.
     * @param type The blurType to perform.
     * @param doubleRadius The radius for the blur.
     * @param output The image in which to store the output.
     */
    private void blur(Mat input, BlurType type, double doubleRadius,
                      Mat output) {
        int radius = (int)(doubleRadius + 0.5);
        int kernelSize;
        switch(type){
            case BOX:
                kernelSize = 2 * radius + 1;
                Imgproc.blur(input, output, new Size(kernelSize, kernelSize));
                break;
            case GAUSSIAN:
                kernelSize = 6 * radius + 1;
                Imgproc.GaussianBlur(input,output, new Size(kernelSize, kernelSize), radius);
                break;
            case MEDIAN:
                kernelSize = 2 * radius + 1;
                Imgproc.medianBlur(input, output, kernelSize);
                break;
            case BILATERAL:
                Imgproc.bilateralFilter(input, output, -1, radius, radius);
                break;
        }
    }

    /**
     * Segment an image based on color ranges.
     * @param input The image on which to perform the RGB threshold.
     * @param red The min and max red.
     * @param green The min and max green.
     * @param blue The min and max blue.
     * @param out The image in which to store the output.
     */
    private void rgbThreshold(Mat input, double[] red, double[] green, double[] blue,
                              Mat out) {
        Imgproc.cvtColor(input, out, Imgproc.COLOR_BGR2RGB);
        Core.inRange(out, new Scalar(red[0], green[0], blue[0]),
                new Scalar(red[1], green[1], blue[1]), out);
    }

    /**
     * Detects groups of pixels in an image.
     * @param input The image on which to perform the find blobs.
     * @param minArea The minimum size of a blob that will be found
     * @param circularity The minimum and maximum circularity of blobs that will be found
     * @param darkBlobs The boolean that determines if light or dark blobs are found.
     * @param blobList The output where the MatOfKeyPoint is stored.
     */
    private void findBlobs(Mat input, double minArea, double[] circularity,
                           Boolean darkBlobs, MatOfKeyPoint blobList) {
        FeatureDetector blobDet = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
        try {
            File tempFile = File.createTempFile("config", ".xml");

            StringBuilder config = new StringBuilder();

            config.append("<?xml version=\"1.0\"?>\n");
            config.append("<opencv_storage>\n");
            config.append("<thresholdStep>10.</thresholdStep>\n");
            config.append("<minThreshold>50.</minThreshold>\n");
            config.append("<maxThreshold>220.</maxThreshold>\n");
            config.append("<minRepeatability>2</minRepeatability>\n");
            config.append("<minDistBetweenBlobs>10.</minDistBetweenBlobs>\n");
            config.append("<filterByColor>1</filterByColor>\n");
            config.append("<blobColor>");
            config.append((darkBlobs ? 0 : 255));
            config.append("</blobColor>\n");
            config.append("<filterByArea>1</filterByArea>\n");
            config.append("<minArea>");
            config.append(minArea);
            config.append("</minArea>\n");
            config.append("<maxArea>");
            config.append(3000);
            config.append("</maxArea>\n");
            config.append("<filterByCircularity>1</filterByCircularity>\n");
            config.append("<minCircularity>");
            config.append(circularity[0]);
            config.append("</minCircularity>\n");
            config.append("<maxCircularity>");
            config.append(circularity[1]);
            config.append("</maxCircularity>\n");
            config.append("<filterByInertia>0</filterByInertia>\n");
            config.append("<filterByConvexity>0</filterByConvexity>\n");
            config.append("</opencv_storage>\n");
            FileWriter writer;
            writer = new FileWriter(tempFile, false);
            writer.write(config.toString());
            writer.close();
            blobDet.read(tempFile.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        blobDet.detect(input, blobList);
    }


}
