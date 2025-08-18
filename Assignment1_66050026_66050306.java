import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Random;

public class Assignment1_66050026_66050306 {

    public static void main(String[] args) {
        JFrame f = new JFrame("WHAT IF I REBORNED");
        Glass_window p = new Glass_window(f);
        f.add(p);
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);

    }

}

// ================= Scene 1 =================
class Glass_window extends JPanel implements ActionListener {
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
    boolean launchedPhoenix = false;

    // เก็บตำแหน่งดาว
    private Point[] stars;

    private JFrame parentFrame; // เก็บ reference ไปที่ JFrame

    Glass_window(JFrame frame) {
        this.parentFrame = frame;

        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(240, 240, 220));
        Timer timer = new Timer(16, this); 
        timer.start();

        // === สร้างดาว ===
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
    public void actionPerformed(ActionEvent e) {
        waveOffset += 0.05;

        switch (sceneState) {
            case DAY:
                sunAngle += 0.006;
                if (sunAngle >= Math.PI) {
                    sceneState = SceneState.NIGHT;
                    moonAngle = 0;
                }
                break;
            case NIGHT:
                moonAngle += 0.006;
                if (moonAngle >= Math.PI) {
                    sceneState = SceneState.DARK;
                }
                break;
            case DARK:
                if (!launchedPhoenix) {
                    launchedPhoenix = true;
                    SwingUtilities.invokeLater(() -> {
                        parentFrame.getContentPane().removeAll(); // ลบของเก่าออก
                        parentFrame.add(new Phoenix());           // ใส่ Phoenix แทน
                        parentFrame.revalidate();
                        parentFrame.repaint();
                    });
                }
                break;
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        if (sceneState == SceneState.DARK) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight()); // พื้นดำเต็มจอ
            g2d.dispose();
            return; // หยุดการวาดทั้งหมด
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
        } else if (sceneState == SceneState.DARK) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return; // ไม่ต้อง dispose()
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

// ================= Scene 2 =================
class Phoenix extends JPanel implements ActionListener {
    private static int WIDTH = 600, HEIGHT = 600;
    private final Timer timer;
    private final long startTime;

    public Phoenix() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        startTime = System.currentTimeMillis();
        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        double t = (System.currentTimeMillis() - startTime) / 1000.0;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawScene4_ExplosionToSky(g2, t);

    }

    private void drawScene4_ExplosionToSky(Graphics2D g2, double dt) {
        if (dt < 1.5) {
            // ลูกบอลไฟลอยขึ้นมา
            g2.setColor(new Color(30, 30, 50));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            drawFireballRising(g2, dt - 0.1);
        } else if (dt < 2.0) {
            // ระเบิดตู้ม
            drawExplosion(g2, dt - 1.5);
        } else if (dt < 2.5) {
            // fade จากระเบิดเป็นท้องฟ้า
            float p = (float) ((dt - 2.0) / 1.0);
            GradientPaint sky = new GradientPaint(0, 0, new Color(80, 180, 255),
                    0, HEIGHT, new Color(200, 240, 255));
            g2.setPaint(sky);
            g2.fillRect(0, 0, WIDTH, HEIGHT);

            // fade in ท้องฟ้า
            g2.setColor(new Color(1f, 1f, 1f, 1f - p));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        } else {
            // ท้องฟ้าเต็มรูปแบบ + นกฟินิกซ์
            GradientPaint sky = new GradientPaint(0, 0, new Color(80, 180, 255),
                    0, HEIGHT, new Color(200, 240, 255));
            g2.setPaint(sky);
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            drawPhoenix(g2, dt - 2.5);
        }
    }

    // เมธอดใหม่สำหรับลูกบอลไฟลอยขึ้น
    private void drawFireballRising(Graphics2D g2, double dt) {
        // ตำแหน่งลูกบอลไฟลอยขึ้น
        int ballY = (int) (HEIGHT - dt * 250); // ลอยขึ้นจากล่างสู่บน
        int ballX = WIDTH / 2;
        double fireFlicker = Math.sin(dt * 15) * 3; // ไฟกระพริบ

        // ออร่าไฟรอบลูกบอล
        for (int i = 3; i >= 1; i--) {
            float alpha = 0.6f - (i * 0.15f);
            int radius = 30 + i * 15 + (int) fireFlicker;

            RadialGradientPaint aura = new RadialGradientPaint(
                    new Point2D.Float(ballX, ballY), radius,
                    new float[] { 0f, 0.7f, 1f },
                    new Color[] {
                            new Color(255, 80, 0, (int) (alpha * 255)),
                            new Color(255, 150, 0, (int) (alpha * 200)),
                            new Color(255, 220, 100, 0)
                    });
            g2.setPaint(aura);
            g2.fillOval(ballX - radius, ballY - radius, radius * 2, radius * 2);
        }

        // ลูกบอลไฟหลัก
        int ballRadius = 25 + (int) fireFlicker;
        drawMidCircle(g2, ballX, ballY, ballRadius, new Color(255, 120, 0));
        drawMidCircleOutline(g2, ballX, ballY, ballRadius);

        // ประกายไฟรอบลูกบอล
        // for (int i = 0; i < 8; i++) {
        // double angle = (dt * 5 + i * Math.PI / 4) % (Math.PI * 2);
        // int sparkX = ballX + (int)(Math.cos(angle) * (40 + fireFlicker));
        // int sparkY = ballY + (int)(Math.sin(angle) * (40 + fireFlicker));
        // drawMidCircle(g2, sparkX, sparkY, 3, new Color(255, 200, 0, 180));
        // }

        // หางไฟที่ตามหลัง
        // Path2D trail = new Path2D.Double();
        // trail.moveTo(ballX, ballY + ballRadius);
        // trail.curveTo(ballX - 15, ballY + 60, ballX + 15, ballY + 80, ballX, ballY +
        // 120);
        // trail.curveTo(ballX - 8, ballY + 90, ballX + 8, ballY + 70, ballX, ballY +
        // ballRadius);
        // g2.setColor(new Color(255, 100, 0, 150));
        // g2.fill(trail);
    }

    // เมธอดใหม่สำหรับระเบิด
    private void drawExplosion(Graphics2D g2, double dt) {
        // พื้นหลังมื
        g2.setColor(new Color(30, 30, 50));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int explodeX = WIDTH / 2;
        int explodeY = (int) (HEIGHT - 1.9 * 200); // ตำแหน่งที่ลูกบอลหยุด

        // คลื่นระเบิดขยายออก
        double explosionRadius = dt * 400; // ขยายเร็ว
        double intensity = 1.0 - dt * 2; // ลดความเข้มลง

        if (intensity > 0) {
            // แสงระเบิด
            RadialGradientPaint explosion = new RadialGradientPaint(
                    new Point2D.Float(explodeX, explodeY), (float) explosionRadius,
                    new float[] { 0f, 0.3f, 0.6f, 1f },
                    new Color[] {
                            new Color(255, 255, 255, (int) (intensity * 255)),
                            new Color(255, 200, 0, (int) (intensity * 200)),
                            new Color(255, 100, 0, (int) (intensity * 100)),
                            new Color(255, 50, 0, 0)
                    });
            g2.setPaint(explosion);
            g2.fillOval((int) (explodeX - explosionRadius), (int) (explodeY - explosionRadius),
                    (int) (explosionRadius * 2), (int) (explosionRadius * 2));

            // ประกายไฟกระจาย
            for (int i = 0; i < 20; i++) {
                double angle = i * Math.PI * 2 / 20;
                double sparkDist = explosionRadius * 0.8;
                int sparkX = explodeX + (int) (Math.cos(angle) * sparkDist);
                int sparkY = explodeY + (int) (Math.sin(angle) * sparkDist);

                if (sparkX >= 0 && sparkX < WIDTH && sparkY >= 0 && sparkY < HEIGHT) {
                    drawMidCircle(g2, sparkX, sparkY, (int) (intensity * 8),
                            new Color(255, 150, 0, (int) (intensity * 200)));
                }
            }
        }
    }

    private void drawPhoenix(Graphics2D g2, double dt) {
        // คำนวณตำแหน่งบินรูป infinity (∞)
        double t = dt * 1.5;
        double a = 150;
        int cx = (int) (a * Math.sin(t) / (1 + Math.cos(t) * Math.cos(t)) + WIDTH / 2);
        int cy = (int) (a * Math.sin(t) * Math.cos(t) / (1 + Math.cos(t) * Math.cos(t)) + HEIGHT / 2);

        double flap = 20 * Math.sin(dt * 6);
        double fireEffect = Math.sin(dt * 8) * 5;

        // วาดการตกแต่งท้องฟ้า
        drawSkyDecorations(g2, dt);

        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // วาดออร่าไฟรอบตัวนก (สีแดง-ส้ม-เหลือง)
        for (int i = 3; i >= 1; i--) {
            float alpha = 0.4f - (i * 0.1f);
            int radius = 50 + i * 20 + (int) fireEffect;

            RadialGradientPaint aura = new RadialGradientPaint(
                    new Point2D.Float(cx, cy - 30), radius,
                    new float[] { 0f, 0.6f, 1f },
                    new Color[] {
                            new Color(255, 50, 0, (int) (alpha * 255)), // แดงเข้ม
                            new Color(255, 120, 0, (int) (alpha * 200)), // ส้ม
                            new Color(255, 200, 100, 0) // เหลืองอ่อน
                    });
            g2.setPaint(aura);
            g2.fillOval(cx - radius, cy - 30 - radius, radius * 2, radius * 2);
        }

        // ประกายไฟรอบตัว (สีแดง-ส้ม)
        for (int i = 0; i < 12; i++) {
            double angle = (dt * 3 + i * Math.PI / 6) % (Math.PI * 2);
            int sparkX = cx + (int) (Math.cos(angle) * (60 + fireEffect));
            int sparkY = cy - 30 + (int) (Math.sin(angle) * (40 + fireEffect));

            // สลับสีประกาย
            Color sparkColor = (i % 2 == 0) ? new Color(255, 80, 0, 220) : // แดงส้ม
                    new Color(255, 150, 0, 200); // ส้มทอง

            drawMidCircle(g2, sparkX, sparkY, 4 + (int) (fireEffect / 2), sparkColor);
        }

        // หางไฟ (สีแดงไล่ส้ม)
        Path2D flameTail = new Path2D.Double();
        flameTail.moveTo(cx, cy + 10);
        flameTail.curveTo(cx - 25, cy + 35 + fireEffect, cx + 25, cy + 50 + fireEffect, cx, cy + 80 + fireEffect);
        flameTail.curveTo(cx - 15, cy + 55 + fireEffect, cx + 15, cy + 30 + fireEffect, cx, cy + 10);

        // ไล่สีหางไฟ
        GradientPaint flameGrad = new GradientPaint(
                cx, cy + 10, new Color(255, 200, 50), // ทองที่โคนหาง
                cx, cy + 80 + (int) fireEffect, new Color(255, 50, 0) // แดงที่ปลายหาง
        );
        g2.setPaint(flameGrad);
        g2.fill(flameTail);
        g2.setColor(new Color(200, 50, 0));
        g2.setStroke(new BasicStroke(2f));
        g2.draw(flameTail);

        // ลำตัวหลัก (สีทองเข้ม)
        Path2D body = new Path2D.Double();
        body.moveTo(cx, cy);
        body.curveTo(cx - 25, cy - 60, cx + 25, cy - 60, cx, cy);
        g2.setColor(new Color(200, 120, 30)); // ทองเข้มคล้ายในรูป
        g2.fill(body);
        g2.setColor(new Color(150, 80, 0)); // ขอบทองเข้มกว่า
        g2.setStroke(new BasicStroke(3f));
        g2.draw(body);

        // หัว (สีทองเข้ม)
        int hx = cx;
        int hy = cy - 60;
        drawMidCircle(g2, hx, hy, 15, new Color(200, 120, 30));
        g2.setColor(new Color(150, 80, 0));
        drawMidCircleOutline(g2, hx, hy, 15);

        // ตาเรืองแสง (สีแดง-ส้ม)
        g2.setColor(new Color(255, 100, 0));
        g2.fillOval(hx - 8, hy - 10, 12, 12);
        g2.setColor(new Color(200, 50, 0));
        g2.fillOval(hx - 6, hy - 8, 6, 6);
        g2.setColor(new Color(255, 200, 100));
        g2.fillOval(hx - 4, hy - 6, 3, 3);

        // จมูก/ปาก (สีส้มเข้ม)
        Path2D beak = new Path2D.Double();
        beak.moveTo(hx + 12, hy + 2);
        beak.lineTo(hx + 20, hy + 5);
        beak.lineTo(hx + 12, hy + 8);
        beak.closePath();
        g2.setColor(new Color(220, 100, 0));
        g2.fill(beak);
        g2.setColor(new Color(180, 60, 0));
        g2.draw(beak);

        // ปีกซ้าย พร้อมเอฟเฟกต์ไฟ (สีแดง-ทอง)
        Path2D leftWing = new Path2D.Double();
        leftWing.moveTo(cx - 10, cy - 10);
        leftWing.curveTo(cx - 100, cy - 80 + flap, cx - 140, cy - 30 + flap, cx - 80, cy + 10);
        leftWing.curveTo(cx - 110, cy - 20 + flap, cx - 90, cy + 30 + flap, cx - 60, cy + 10);
        leftWing.curveTo(cx - 90, cy, cx - 50, cy + 40, cx - 20, cy + 10);

        // ไฟตามขอบปีก (สีแดงส้ม)
        g2.setColor(new Color(255, 80, 0, 120));
        g2.setStroke(new BasicStroke(10f));
        g2.draw(leftWing);

        // ไล่สีปีก
        GradientPaint wingGrad = new GradientPaint(
                cx - 10, cy - 80 + (int) flap, new Color(255, 150, 50), // ทองที่โคนปีก
                cx - 140, cy - 30 + (int) flap, new Color(200, 50, 0) // แดงที่ปลายปีก
        );
        g2.setPaint(wingGrad);
        g2.fill(leftWing);
        g2.setColor(new Color(150, 40, 0));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(leftWing);

        // ปีกขวา พร้อมเอฟเฟกต์ไฟ (สีแดง-ทอง)
        Path2D rightWing = new Path2D.Double();
        rightWing.moveTo(cx + 10, cy - 10);
        rightWing.curveTo(cx + 100, cy - 80 + flap, cx + 140, cy - 30 + flap, cx + 80, cy + 10);
        rightWing.curveTo(cx + 110, cy - 20 + flap, cx + 90, cy + 30 + flap, cx + 60, cy + 10);
        rightWing.curveTo(cx + 90, cy, cx + 50, cy + 40, cx + 20, cy + 10);

        // ไฟตามขอบปีก
        g2.setColor(new Color(255, 80, 0, 120));
        g2.setStroke(new BasicStroke(10f));
        g2.draw(rightWing);

        // ไล่สีปีก
        GradientPaint wingGradR = new GradientPaint(
                cx + 10, cy - 80 + (int) flap, new Color(255, 150, 50),
                cx + 140, cy - 30 + (int) flap, new Color(200, 50, 0));
        g2.setPaint(wingGradR);
        g2.fill(rightWing);
        g2.setColor(new Color(150, 40, 0));
        g2.setStroke(new BasicStroke(3f));
        g2.draw(rightWing);
    }

    // เพิ่มเมธอดใหม่สำหรับตกแต่งท้องฟ้า
    private void drawSkyDecorations(Graphics2D g2, double dt) {
        // เมฆลอยช้า ๆ
        g2.setColor(new Color(255, 255, 255, 150));
        for (int i = 0; i < 4; i++) {
            int cloudX = (int) ((i * 150 + dt * 20) % (WIDTH + 100)) - 50;
            int cloudY = 80 + i * 30;
            drawCloud(g2, cloudX, cloudY);
        }

        // ดาวเล็ก ๆ กระพริบ
        for (int i = 0; i < 8; i++) {
            double twinkle = Math.sin(dt * 4 + i) * 0.5 + 0.5;
            int starX = 50 + i * 70;
            int starY = 50 + (int) (Math.sin(dt + i) * 20);
            g2.setColor(new Color(255, 255, 200, (int) (twinkle * 180)));
            drawStar(g2, starX, starY, 10);
        }

        // แสงแดดกระจาย
        g2.setColor(new Color(255, 255, 150, 50));
        for (int i = 0; i < 12; i++) {
            double angle = (dt * 0.5 + i * Math.PI / 6) % (Math.PI * 2);
            int rayX = WIDTH / 2 + (int) (Math.cos(angle) * 300);
            int rayY = HEIGHT / 4 + (int) (Math.sin(angle) * 200);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(WIDTH / 2, HEIGHT / 4, rayX, rayY);
        }
    }

    // เมธอดวาดเมฆ
    private void drawCloud(Graphics2D g2, int x, int y) {
        g2.fillOval(x, y, 40, 25);
        g2.fillOval(x + 15, y - 8, 35, 30);
        g2.fillOval(x + 30, y, 40, 25);
        g2.fillOval(x + 20, y + 8, 30, 20);
    }

    // เมธอดวาดดาว
    private void drawStar(Graphics2D g2, int x, int y, int size) {
        int[] xPoints = new int[10];
        int[] yPoints = new int[10];
        for (int i = 0; i < 10; i++) {
            double angle = i * Math.PI / 5;
            int radius = (i % 2 == 0) ? size : size / 2;
            xPoints[i] = x + (int) (Math.cos(angle) * radius);
            yPoints[i] = y + (int) (Math.sin(angle) * radius);
        }
        g2.fillPolygon(xPoints, yPoints, 10);
    }

    // Midpoint circle fill
    private void drawMidCircle(Graphics2D g2, int cx, int cy, int r, Color col) {
        g2.setColor(col);
        int x = 0, y = r, d = 1 - r;
        while (x <= y) {
            fillOct(g2, cx, cy, x, y);
            if (d < 0)
                d += 2 * x + 3;
            else {
                d += 2 * (x - y) + 5;
                y--;
            }
            x++;
        }
    }

    private void fillOct(Graphics2D g2, int cx, int cy, int x, int y) {
        g2.drawLine(cx - x, cy - y, cx + x, cy - y);
        g2.drawLine(cx - x, cy + y, cx + x, cy + y);
        g2.drawLine(cx - y, cy - x, cx + y, cy - x);
        g2.drawLine(cx - y, cy + x, cx + y, cy + x);
    }

    // Midpoint circle outline
    private void drawMidCircleOutline(Graphics2D g2, int cx, int cy, int r) {
        g2.setColor(Color.WHITE);
        int x = 0, y = r, d = 1 - r;
        while (x <= y) {
            plotPts(g2, cx, cy, x, y);
            if (d < 0)
                d += 2 * x + 3;
            else {
                d += 2 * (x - y) + 5;
                y--;
            }
            x++;
        }
    }

    private void plotPts(Graphics2D g2, int cx, int cy, int x, int y) {
        g2.drawLine(cx + x, cy + y, cx + x, cy + y);
        g2.drawLine(cx - x, cy + y, cx - x, cy + y);
        g2.drawLine(cx + x, cy - y, cx + x, cy - y);
        g2.drawLine(cx - x, cy - y, cx - x, cy - y);
        g2.drawLine(cx + y, cy + x, cx + y, cy + x);
        g2.drawLine(cx - y, cy + x, cx - y, cy + x);
        g2.drawLine(cx + y, cy - x, cx + y, cy - x);
        g2.drawLine(cx - y, cy - x, cx - y, cy - x);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public static void startScene2() {
        JFrame frame = new JFrame("WHAT IF I REBORNED - Scene 2");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Phoenix());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
