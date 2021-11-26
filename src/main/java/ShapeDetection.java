import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import util.ShapeDetectionUtil;

import javax.swing.*;

public class ShapeDetection {

    static{
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        final JPanel cameraFeed = new JPanel();
        final JPanel processFeed = new JPanel();
        ShapeDetectionUtil.createJFrame(cameraFeed, processFeed);

        final VideoCapture camera = new VideoCapture(0);

        ShapeDetection.startShapeDetection(cameraFeed, processFeed, camera).run();
    }

    private static Runnable startShapeDetection(final JPanel cameraFeed, final JPanel processFeed, final VideoCapture camera){
        return () -> {
            final Mat frame = new Mat();

            while (true) {
                camera.read(frame);

                final Mat processed = ShapeDetectionUtil.processImage(frame);

                ShapeDetectionUtil.markOuterContour(processed, frame);

                ShapeDetectionUtil.drawImage(frame, cameraFeed);

                ShapeDetectionUtil.drawImage(processed, processFeed);
            }
        };
    }
}
