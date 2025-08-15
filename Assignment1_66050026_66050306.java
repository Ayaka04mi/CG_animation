import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Assignment1_66050026_66050306 extends JPanel implements ActionListener {
    private Timer timer;
    private double waveOffset = 0;

    private enum SceneState {
        DAY, // พระอาทิตย์กำลังเคลื่อน
        NIGHT, // พระจันทร์กำลังเคลื่อน
        DARK, // มืดหมด
        TRANSITION // กลับไปพระอาทิตย์
    }

    private SceneState sceneState = SceneState.DAY;

    private double sunAngle = 0;
    private double moonAngle = 0;
    private long nightStartTime = 0;

    // เก็บตำแหน่งดาว
    private Point[] stars;

    public static void main(String[] args) {
        JFrame f = new JFrame("WHAT IF I REBORNED");
        Assignment1_66050026_66050306 p = new Assignment1_66050026_66050306();
        f.add(p);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        waveOffset += 0.05;
        switch (sceneState) {
            case DAY:
                sunAngle += 0.003; // ความเร็วพระอาทิตย์
                if (sunAngle >= Math.PI) {
                    sceneState = SceneState.NIGHT;
                    nightStartTime = System.currentTimeMillis();
                    moonAngle = 0;
                }
                break;
            case NIGHT:
                moonAngle += 0.003; // ความเร็วพระจันทร์
                if (moonAngle >= Math.PI) {
                    sceneState = SceneState.DARK;
                    nightStartTime = System.currentTimeMillis();
                }
                break;

            case DARK:

                break;
        }
        repaint();
    }

    public Assignment1_66050026_66050306() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(240, 240, 220));
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
        // สร้างดาวคงที่ (น้อยลง)
        stars = new Point[20];
        Random rand = new Random();
        int winX = 150, winY = 100, winW = 1000, winH = 400;
        for (int i = 0; i < stars.length; i++) {
            int x = winX + rand.nextInt(winW);
            int y = winY + rand.nextInt(winH);
            stars[i] = new Point(x, y);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int winX = 50, winY = 100, winW = 500, winH = 260;
        int radius = 300;

        // กรอบหน้าต่าง
        g2d.setColor(new Color(100, 60, 40));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawRect(winX, winY, winW, winH);

        // เส้นแบ่ง
        g2d.drawLine(winX + winW / 2, winY, winX + winW / 2, winY + winH);
        g2d.drawLine(winX, winY + winH / 4, winX + winW, winY + winH / 4);
        g2d.drawLine(winX + winW / 4, winY, winX + winW / 4,  winH - winY);
        g2d.drawLine(winX + winW/2 + winW / 4, winY, winX + winW/2 + winW / 4,  winH - winY);

        if (sceneState == SceneState.DAY) {
            // กลางวัน
            g2d.setColor(new Color(180, 220, 255, 150));
            g2d.fillRect(winX, winY, winW, winH);

            int sunX = (int) (winX + winW / 2 + radius * Math.cos(sunAngle - Math.PI/ 2));
            int sunY = (int) (winY + winH + radius * Math.sin(sunAngle - Math.PI / 2));
            drawSunWithGlowClipped(g2d, winX, winY, winW, winH, sunX, sunY, 40);

        } else if (sceneState == SceneState.NIGHT) {
            // กลางคืน
            g2d.setColor(new Color(10, 10, 40, 200));
            g2d.fillRect(winX, winY, winW, winH);

            // ดาวคงที่
            g2d.setColor(Color.WHITE);
            for (Point star : stars) {
                g2d.fillOval(star.x, star.y, 2, 2);
            }

            int moonX = (int) (winX + winW / 2 + radius * Math.cos(moonAngle - Math.PI / 2));
            int moonY = (int) (winY + winH + radius * Math.sin(moonAngle - Math.PI / 2));
            drawMoonWithGlowClipped(g2d, winX, winY, winW, winH, moonX, moonY, 35);

        } else if (sceneState == SceneState.DARK) {
            // พื้นหลังดำสนิท
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // วาดผ้าม่าน (ยกเว้นตอนมืดสนิท)
        if (sceneState != SceneState.DARK) {
            drawCurtain(g2d, winX, winY, winW, winH);
        }

    }

    private void drawCurtain(Graphics2D g2d, int winX, int winY, int winW, int winH) {
        g2d.setColor(new Color(245, 245, 220, 200));
        g2d.setStroke(new BasicStroke(2));

        int topY = winY - 20;
        int bottomY = winY + winH + 50;

        for (int i = 0; i <= 10; i++) {
            double wave = Math.sin((i * 0.5) + waveOffset) * 20;
            QuadCurve2D q = new QuadCurve2D.Double(
                    winX + i * (winW / 10.0), topY,
                    winX + i * (winW / 10.0) + wave, (topY + bottomY) / 2.0,
                    winX + i * (winW / 10.0), bottomY);
            g2d.draw(q);
        }
    }

    private void drawSunWithGlowClipped(Graphics2D g2d, int winX, int winY, int winW, int winH, int xc, int yc, int r) {
        BufferedImage glowImage = new BufferedImage(winW, winH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gGlow = glowImage.createGraphics();
        gGlow.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 5; i >= 1; i--) {
            int radius = r + i * 10;
            int alpha = (int) (25 * i);
            gGlow.setColor(new Color(255, 220, 120, Math.min(alpha, 255)));
            gGlow.fillOval((xc - winX) - radius, (yc - winY) - radius, radius * 2, radius * 2);
        }

        gGlow.setColor(new Color(255, 204, 80));
        drawMidpointCircleFilled(gGlow, (xc - winX), (yc - winY), r);

        gGlow.setColor(new Color(255, 230, 120));
        gGlow.fillOval((xc - winX) - (r - 4), (yc - winY) - (r - 4), (r - 4) * 2, (r - 4) * 2);

        gGlow.dispose();
        Shape oldClip = g2d.getClip();
        g2d.setClip(new Rectangle(winX, winY, winW, winH));
        g2d.drawImage(glowImage, winX, winY, null);
        g2d.setClip(oldClip);
    }

    private void drawMoonWithGlowClipped(Graphics2D g2d, int winX, int winY, int winW, int winH, int xc, int yc, int r) {
        BufferedImage glowImage = new BufferedImage(winW, winH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gGlow = glowImage.createGraphics();
        gGlow.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 3; i >= 1; i--) {
            int radius = r + i * 8;
            int alpha = (int) (20 * i);
            gGlow.setColor(new Color(200, 200, 200, Math.min(alpha, 255)));
            gGlow.fillOval((xc - winX) - radius, (yc - winY) - radius, radius * 2, radius * 2);
        }

        gGlow.setColor(new Color(230, 230, 230));
        drawMidpointCircleFilled(gGlow, (xc - winX), (yc - winY), r);

        gGlow.dispose();
        Shape oldClip = g2d.getClip();
        g2d.setClip(new Rectangle(winX, winY, winW, winH));
        g2d.drawImage(glowImage, winX, winY, null);
        g2d.setClip(oldClip);
    }

    // Midpoint Circle Filled
    private void drawMidpointCircleFilled(Graphics2D g, int xc, int yc, int r) {
        for (int yy = -r; yy <= r; yy++) {
            int rowY = yc + yy;
            int xMax = (int) Math.floor(Math.sqrt(r * r - yy * yy));
            g.fillRect(xc - xMax, rowY, xMax * 2 + 1, 1);
        }
    }

}
