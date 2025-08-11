import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;

public class Assignment1_66050026_66050306 extends JPanel implements ActionListener {
    private Timer timer;
    private double waveOffset = 0;

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
        repaint();
    }

    public Assignment1_66050026_66050306() {
        setPreferredSize(new Dimension(600, 600));
        setBackground(new Color(240, 240, 220));
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        // ขนาดหน้าต่าง
        int winX = 150, winY = 100, winW = 300, winH = 400;

        // กรอบหน้าต่าง
        g2d.setColor(new Color(100, 60, 40));
        g2d.setStroke(new BasicStroke(6));
        g2d.drawRect(winX, winY, winW, winH);

        // วาดเส้นแบ่งกระจก
        g2d.drawLine(winX + winW / 2, winY, winX + winW / 2, winY + winH);
        

        // วาดกระจกใส
        g2d.setColor(new Color(180, 220, 255, 150));
        g2d.fillRect(winX, winY, winW, winH);

        // วาดดวงอาทิตย์ด้วย Midpoint Circle Algorithm
        drawSunWithGlowClipped(g2d, winX, winY, winW, winH, winX + 220, winY + 120, 40);

        // วาดผ้าม่านปลิว
        drawCurtain(g2d, winX, winY, winW, winH);

         // วาดเตียงและคนไข้
        
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
                winX + i * (winW / 10.0), bottomY
            );
            g2d.draw(q);
        }
    }

    private void drawSunWithGlowClipped(Graphics2D g2d, int winX, int winY, int winW, int winH, int xc, int yc, int r) {
        BufferedImage glowImage = new BufferedImage(winW, winH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gGlow = glowImage.createGraphics();
        gGlow.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // วาด glow ลงใน buffer
        for (int i = 5; i >= 1; i--) {
            int radius = r + i * 10;
            int alpha = (int)(25 * i);
            gGlow.setColor(new Color(255, 220, 120, Math.min(alpha, 255)));
            gGlow.fillOval((xc - winX) - radius, (yc - winY) - radius, radius * 2, radius * 2);
        }

        // วาดวงกลมตรงกลางด้วย midpoint
        gGlow.setColor(new Color(255, 204, 80));
        drawMidpointCircleFilled(gGlow, (xc - winX), (yc - winY), r);

        gGlow.setColor(new Color(255, 230, 120));
        gGlow.fillOval((xc - winX) - (r - 4), (yc - winY) - (r - 4), (r - 4) * 2, (r - 4) * 2);

        gGlow.dispose();

        // Clip ให้อยู่ในขอบหน้าต่าง
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
