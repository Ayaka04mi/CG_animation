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
                sunAngle += 0.003;
                break;

            case NIGHT:
                moonAngle += 0.003;
                break;

            case DARK:
                // ไม่ต้องทำอะไร
                break;
        }
        repaint();
    }

    public Assignment1_66050026_66050306() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(240, 240, 220));
        timer = new Timer(16, this); // ~60 FPS
        timer.start();

        // === สร้างดาว (ใช้จุด pixel แทน fillOval) ===
        stars = new Point[20];
        Random rand = new Random();
        int winX = 50, winY = 100, winW = 500, winH = 260;
        for (int i = 0; i < stars.length; i++) {
            int x = winX + rand.nextInt(winW);
            int y = winY + rand.nextInt(winH);
            stars[i] = new Point(x, y);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        if (sceneState == SceneState.DARK) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight()); // พื้นดำเต็มจอ
            g2d.dispose();
            return; // ❗️ หยุดการวาดทั้งหมด
        }
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        int winX = 50, winY = 100, winW = 500, winH = 260;

        // === กรอบหน้าต่างด้วย Bresenham ===
        int padding = 6; // ระยะห่างระหว่างกรอบนอกกับกรอบใน

        // === ตำแหน่งกรอบใน ===
        int innerX = winX + padding;
        int innerY = winY + padding;
        int innerW = winW - 2 * padding;
        int innerH = winH - 2 * padding;

        int offset = 2; // ระยะห่างระหว่างเส้นคู่
        // คำนวณตำแหน่งของเส้นแนวนอน 1/4 จากบน
        int horizontalY1 = winY + winH / 4 - offset;
        int horizontalY2 = winY + winH / 4 - offset;

        // === วาดกระจก ===
        drawGlass(g2d, winX + padding, winY + padding, innerW, innerH);

        // === วาดพระอาทิตย์ ===
        Shape oldClip = g2d.getClip(); // เก็บ clip เดิมไว้

        // === กำหนด clip เป็นกรอบหน้าต่าง ===
        g2d.setClip(new Rectangle(innerX, innerY, innerW, innerH));

        // === วาดพระอาทิตย์ ===
        if (sceneState == SceneState.DAY || sceneState == SceneState.TRANSITION) {
            double progress = sunAngle; // 0.0 - 1.0
            int amplitude = 60;

            int sunX = innerX + (int) (progress * innerW);
            int sunY = innerY + innerH / 2 - (int) (Math.sin(progress * Math.PI) * amplitude);

            drawSun(g2d, sunX, sunY);

            if (sunX > innerX + innerW) {
                sceneState = SceneState.NIGHT;
                nightStartTime = System.currentTimeMillis();
                moonAngle = 0;
            }

            // === วาดพระจันทร์ ===
        } else if (sceneState == SceneState.NIGHT) {
            Polygon nightGlass = new Polygon();
            nightGlass.addPoint(innerX, innerY); // มุมบนซ้าย
            nightGlass.addPoint(innerX + innerW, innerY); // มุมบนขวา
            nightGlass.addPoint(innerX + innerW, innerY + innerH); // มุมล่างขวา
            nightGlass.addPoint(innerX, innerY + innerH); // มุมล่างซ้าย

            g2d.setColor(new Color(10, 10, 30)); // สีพื้นหลังกลางคืน
            g2d.fillPolygon(nightGlass);

            double progress = moonAngle;
            int amplitude = 40;

            int moonX = innerX + (int) (progress * innerW);
            int moonY = innerY + innerH / 2 - (int) (Math.sin(progress * Math.PI) * amplitude);

            drawMoon(g2d, moonX, moonY);

            // วาดดาว
            g2d.setColor(Color.WHITE);
            for (Point star : stars) {
                g2d.fillRect(star.x, star.y, 1, 1);
            }

            if (moonX > innerX + innerW) {
                sceneState = SceneState.DARK;
            }
        } 

        // === คืน clip เดิม ===
        g2d.setClip(oldClip);

        // === กรอบนอก ===
        g2d.setColor(new Color(150, 90, 60));
        drawLineBresenham(g2d, winX, winY, winX + winW, winY); // บน
        drawLineBresenham(g2d, winX, winY + winH, winX + winW, winY + winH); // ล่าง
        drawLineBresenham(g2d, winX, winY, winX, winY + winH); // ซ้าย
        drawLineBresenham(g2d, winX + winW, winY, winX + winW, winY + winH); // ขวา
        // === กรอบใน ===
        g2d.setColor(new Color(150, 90, 60));
        drawLineBresenham(g2d, innerX, innerY, innerX + innerW, innerY); // บน
        drawLineBresenham(g2d, innerX, innerY + innerH, innerX + innerW, innerY + innerH); // ล่าง
        drawLineBresenham(g2d, innerX, innerY, innerX, innerY + innerH); // ซ้าย
        drawLineBresenham(g2d, innerX + innerW, innerY, innerX + innerW, innerY + innerH); // ขวาF

        // === เส้นแบ่งแนวตั้งตรงกลาง ===
        drawLineBresenham(g2d, winX + winW / 2 - offset, winY + padding, winX + winW / 2 - offset,
                winY + winH - padding);
        drawLineBresenham(g2d, winX + winW / 2 + offset, winY + padding, winX + winW / 2 + offset,
                winY + winH - padding);

        // === เส้นแบ่งแนวนอน 1/4 จากบน ===
        drawLineBresenham(g2d, winX + padding, winY + winH / 4 - offset, winX + winW - padding,
                winY + winH / 4 - offset);
        drawLineBresenham(g2d, winX + padding, winY + winH / 4 + offset, winX + winW - padding,
                winY + winH / 4 + offset);

        // === เส้นแบ่งแนวตั้ง 1/4 จากซ้าย (ถึงเส้นแนวนอน) ===
        int verticalX1 = winX + winW / 4;
        drawLineBresenham(g2d, verticalX1 - offset, winY + padding, verticalX1 - offset, horizontalY1);
        drawLineBresenham(g2d, verticalX1 + offset, winY + padding, verticalX1 + offset, horizontalY2);

        // === เส้นแบ่งแนวตั้ง 3/4 จากซ้าย (ถึงเส้นแนวนอน) ===
        int verticalX3 = winX + 3 * winW / 4;
        drawLineBresenham(g2d, verticalX3 - offset, winY + padding, verticalX3 - offset, horizontalY1);
        drawLineBresenham(g2d, verticalX3 + offset, winY + padding, verticalX3 + offset, horizontalY2);

        // === วาดผ้าม่าน ===
        drawCurtain(g2d, winX, winY, winW, winH, waveOffset);

        g2d.dispose();

    }

    // === Bresenham line ===
    private void drawLineBresenham(Graphics2D g2d, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;

        while (true) {
            g2d.fillRect(x0, y0, 1, 1);
            if (x0 == x1 && y0 == y1)
                break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    public void drawGlass(Graphics2D g2, int winX, int winY, int winW, int winH) {
        // ตั้งค่าสีฟ้าโปร่งแสง
        g2.setColor(new Color(135, 206, 250, 100)); // RGBA

        // สร้าง Polygon เป็นสี่เหลี่ยมกระจก
        Polygon glass = new Polygon();
        glass.addPoint(winX, winY); // มุมบนซ้าย
        glass.addPoint(winX + winW, winY); // มุมบนขวา
        glass.addPoint(winX + winW, winY + winH); // มุมล่างขวา
        glass.addPoint(winX, winY + winH); // มุมล่างซ้าย

        // วาดกระจกด้วย Polygon
        g2.fillPolygon(glass);
    }

    // === Midpoint Circle ===
    private void drawFilledCircleMidpoint(Graphics2D g2d, int xc, int yc, int r, Color color) {
        g2d.setColor(color);
        for (int y = -r; y <= r; y++) {
            for (int x = -r; x <= r; x++) {
                if (x * x + y * y <= r * r) {
                    g2d.fillRect(xc + x, yc + y, 1, 1);
                }
            }
        }
    }

    // วาดพระอาทิตย์ 3 ชั้น (Glow Effect)
    private void drawSun(Graphics2D g2d, int x, int y) {
        drawFilledCircleMidpoint(g2d, x, y, 60, new Color(255, 230, 150)); // วงนอก
        drawFilledCircleMidpoint(g2d, x, y, 50, new Color(255, 200, 100)); // วงกลาง
        drawFilledCircleMidpoint(g2d, x, y, 40, new Color(255, 160, 0)); // วงใน
    }

    // วาดพระจันทร์ 3 ชั้น (Glow Effect)
    private void drawMoon(Graphics2D g2d, int x, int y) {
        drawFilledCircleMidpoint(g2d, x, y, 55, new Color(200, 200, 220)); // วงนอก
        drawFilledCircleMidpoint(g2d, x, y, 45, new Color(180, 180, 200)); // วงกลาง
        drawFilledCircleMidpoint(g2d, x, y, 35, new Color(240, 240, 255)); // วงใน
    }

    // ฟังก์ชันวาดเส้นตรงแนวนอน (pixel-by-pixel)
    private void drawHorizontalLine(Graphics2D g2d, int x1, int x2, int y) {
        for (int x = x1; x <= x2; x++) {
            g2d.fillRect(x, y, 1, 1);
        }
    }

    // วาดผ้าม่าน
    public void drawCurtain(Graphics g, int winX, int winY, int winW, int winH, double waveOffset) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(255, 255, 255, 180));

        int curtainTop = winY + 10;
        int curtainBottom = winY + winH + 20;
        int amplitude = 10;
        int frequency = 20;
        int step = 10;

        // for (int x = winX + 10; x < winX + winW - 10; x += step) {
        // double offset = Math.sin((x / (double) frequency) + waveOffset) * amplitude;
        // drawLineBresenham(g2, x + (int) offset, curtainTop, x + (int) offset,
        // curtainBottom);
        // }
        for (int x = winX + 10; x < winX + winW - 10; x += step) {
            double offset = Math.sin((x / (double) frequency) + waveOffset) * amplitude;
            int baseX = x + (int) offset;

            // วาดเส้นม่านแบบหนา (3 พิกเซล)
            drawLineBresenham(g2, baseX - 1, curtainTop, baseX - 1, curtainBottom);
            drawLineBresenham(g2, baseX, curtainTop, baseX, curtainBottom);
            drawLineBresenham(g2, baseX + 1, curtainTop, baseX + 1, curtainBottom);
        }
    }

    // === Bezier Curve ===
    private void drawBezier(Graphics2D g2d, int[] px, int[] py) {
        double step = 0.001;
        for (double t = 0; t <= 1; t += step) {
            double x = Math.pow(1 - t, 3) * px[0] +
                    3 * t * Math.pow(1 - t, 2) * px[1] +
                    3 * (1 - t) * t * t * px[2] +
                    t * t * t * px[3];
            double y = Math.pow(1 - t, 3) * py[0] +
                    3 * t * Math.pow(1 - t, 2) * py[1] +
                    3 * (1 - t) * t * t * py[2] +
                    t * t * t * py[3];
            g2d.fillRect((int) x, (int) y, 1, 1);
        }
    }

}
