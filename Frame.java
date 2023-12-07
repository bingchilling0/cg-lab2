import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Frame extends JFrame {
    protected File file;
    protected BufferedImage image, contrastImage, histogramImage;
    private final JLabel imageLabel;
    private final JLabel contrastLabel, histogramLabel;
    public Frame(){
        super("");

        JMenuBar menuBar;
        JMenu fileMenu;
        JMenuItem menuItem;

        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_1);
        menuBar.add(fileMenu);

        menuItem = new JMenuItem("Open",
                KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadFile();
            }
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("LC",
                KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                linearContrast(1.5f, 70f);
            }
        });
        fileMenu.add(menuItem);

        menuItem = new JMenuItem("EH",
                KeyEvent.VK_L);
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                equalizeHistogram();
            }
        });
        fileMenu.add(menuItem);

        this.setJMenuBar(menuBar);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 3));

        imageLabel = new JLabel();
        panel.add(imageLabel);
        contrastLabel = new JLabel();
        panel.add(contrastLabel);
        histogramLabel = new JLabel();
        panel.add(histogramLabel);
        this.add(panel);
    }

    private File getFileFromDialog(int mode){
        FileDialog fileDialog = new FileDialog(this, "Open file", mode);
        fileDialog.setDirectory("./src");
        fileDialog.setVisible(true);
        try {
            this.file = new File(fileDialog.getDirectory(), fileDialog.getFile());
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(null, "No file selected", "", JOptionPane.ERROR_MESSAGE);
        }

        return new File(fileDialog.getDirectory(), fileDialog.getFile());
    }

    private void loadFile() {
        this.file = getFileFromDialog(FileDialog.LOAD);
        StringBuilder sb = new StringBuilder();
        try {
            image = ImageIO.read(file);
            contrastImage = ImageIO.read(file);
            histogramImage = ImageIO.read(file);
        } catch (FileNotFoundException | NullPointerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.repaint();
    }

    private void linearContrast(float a, float b){
        int width = image.getWidth(), height = image.getHeight();
        Color c;
        ArrayList<float[]> hsbs = new ArrayList<>();
        float[] hsb;
        float max = 0, min = 256;
        for(int p:image.getRaster().getPixels(0, 0, width, height, (int[]) null)){
            c = new Color(p, true);
            hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            hsb[2] = hsb[2]*a+b;
            hsbs.add(hsb);
            if(hsb[2] > max){
                max = hsb[2];
            }
            if(hsb[2] < min){
                min = hsb[2];
            }
        }

        int[] rgbs = new int[hsbs.size()];
        for(int i = 0;i< hsbs.size();i++){
            hsb = hsbs.get(i);
            hsb[2] = (hsb[2]- min)/(max - min);
            hsbs.set(i, hsb);
            rgbs[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        }

        contrastImage.getRaster().setPixels(0, 0, width, height, rgbs);


        repaint();
    }

    void equalizeHistogram(){
        int width = image.getWidth(), height = image.getHeight();
        double[] hist = new double[256];
        int[] pixels = image.getRaster().getPixels(0, 0, width, height, (int[]) null);

        for(int p:pixels){
            Color c = new Color(p, true);
            hist[(int)(Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[2]*255)]++;
        }

        for(int i=0;i<256;i++){
            hist[i] /= (height*width);
        }

        double[] sh = new double[256];

        for(int i=0;i<256;i++){
            for(int j=1;j<=i;j++){
                sh[i] += hist[j];
            }
        }

        for(int i = 0;i<pixels.length;i++){
            Color c = new Color(pixels[i], true);
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            hsb[2] = (float) (sh[(int) (hsb[2]*255)]/3);
            pixels[i] = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        }

        histogramImage.getRaster().setPixels(0, 0, width, height, pixels);

        repaint();
    }

    public void repaint(){
        ImageIcon icon = new ImageIcon(image);
        imageLabel.setIcon(icon);
        imageLabel.repaint();
        ImageIcon icon2 = new ImageIcon(contrastImage);
        contrastLabel.setIcon(icon2);
        contrastLabel.repaint();
        ImageIcon icon3 = new ImageIcon(histogramImage);
        histogramLabel.setIcon(icon3);
        histogramLabel.repaint();
    }
}
