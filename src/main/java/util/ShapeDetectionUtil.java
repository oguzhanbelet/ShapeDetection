package util;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public final class ShapeDetectionUtil {
    private ShapeDetectionUtil(){

    }

    public static Mat processImage(final Mat mat){
        final Mat processed = new Mat(mat.height(), mat.width(), mat.type());

        Imgproc.GaussianBlur(mat, processed, new Size(7,7), 1);

        Imgproc.cvtColor(processed, processed, Imgproc.COLOR_RGB2GRAY);

        Imgproc.Canny(processed, processed, 200, 20);

        Imgproc.dilate(processed, processed, new Mat(), new Point(-1,-1), 1);

        return processed;
    }

    public static void markOuterContour(final Mat processedImage, final Mat originalImage){
        final List<MatOfPoint> allContours = new ArrayList<>();
        Imgproc.findContours(processedImage, allContours, new Mat(processedImage.size(), processedImage.type()), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        final List<MatOfPoint> filteredContours = allContours.stream()
                .filter(contour -> {
                    final double value = Imgproc.contourArea(contour);
                    final Rect rect = Imgproc.boundingRect(contour);

                    final boolean isNotNoise = value > 750;

                    if (isNotNoise){
                        Imgproc.putText(
                                originalImage,
                                "Area: " + (int) value,
                                new Point(rect.x + rect.width, rect.y + rect.height),
                                2,
                                0.5,
                                new Scalar(124, 252, 0),
                                1
                        );

                        MatOfPoint2f dst = new MatOfPoint2f();
                        contour.convertTo(dst, CvType.CV_32F);
                        Imgproc.approxPolyDP(dst, dst, 0.02 * Imgproc.arcLength(dst, true), true);
                        Imgproc.putText(
                                originalImage,
                                "Points: " + dst.toArray().length,
                                new Point(rect.x + rect.height, rect.y + rect.height + 15),
                                    2,
                                    0.5,
                                    new Scalar(124, 252, 0),
                                    1
                        );
                    }
                    return isNotNoise;
                }).toList();

        Imgproc.drawContours(originalImage, filteredContours, -1, new Scalar(124, 252, 0), 1);
    }

    public static void createJFrame(final JPanel... panels){
        final JFrame window = new JFrame("Shape Detection");
        window.setSize(new Dimension(panels.length * 800, 550));
        window.setLocationRelativeTo(null);
        window.setResizable(true);
        window.setLayout(new GridLayout(1, panels.length));

        for (final JPanel panel : panels){
            window.add(panel);
        }

        window.setVisible(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public static void drawImage(final Mat mat, final JPanel panel){
       final BufferedImage image = ShapeDetectionUtil.convertMatToBufferedImage(mat);

       final Graphics graphics = panel.getGraphics();
       graphics.drawImage(image, 0, 0, panel);
    }

    private static BufferedImage convertMatToBufferedImage(final Mat mat){
        final BufferedImage bufferedImage = new BufferedImage(mat.width(), mat.height(), mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR);

        final WritableRaster raster = bufferedImage.getRaster();
        final DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
        mat.get(0,0,dataBuffer.getData());
        return bufferedImage;
    }
}
