import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class Phoenix extends JPanel implements ActionListener {
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

    // Scene 4: White explosion flash → sky → phoenix flight
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
            float p = (float)((dt - 2.0) / 1.0);
            GradientPaint sky = new GradientPaint(0,0,new Color(80,180,255),
                                                0,HEIGHT,new Color(200,240,255));
            g2.setPaint(sky);
            g2.fillRect(0,0,WIDTH,HEIGHT);
            
            // fade in ท้องฟ้า
            g2.setColor(new Color(1f,1f,1f,1f-p));
            g2.fillRect(0,0,WIDTH,HEIGHT);
        } else {
            // ท้องฟ้าเต็มรูปแบบ + นกฟินิกซ์
            GradientPaint sky = new GradientPaint(0,0,new Color(80,180,255),
                                                0,HEIGHT,new Color(200,240,255));
            g2.setPaint(sky);
            g2.fillRect(0,0,WIDTH,HEIGHT);
            drawPhoenix(g2, dt - 2.5);
        }
    }

    // เมธอดใหม่สำหรับลูกบอลไฟลอยขึ้น
    private void drawFireballRising(Graphics2D g2, double dt) {
        // ตำแหน่งลูกบอลไฟลอยขึ้น
        int ballY = (int)(HEIGHT - dt * 250); // ลอยขึ้นจากล่างสู่บน
        int ballX = WIDTH / 2;
        double fireFlicker = Math.sin(dt * 15) * 3; // ไฟกระพริบ
            
            // ออร่าไฟรอบลูกบอล
            for (int i = 3; i >= 1; i--) {
                float alpha = 0.6f - (i * 0.15f);
                int radius = 30 + i * 15 + (int)fireFlicker;
                
                RadialGradientPaint aura = new RadialGradientPaint(
                    new Point2D.Float(ballX, ballY), radius,
                    new float[]{0f, 0.7f, 1f},
                    new Color[]{
                        new Color(255, 80, 0, (int)(alpha * 255)),
                        new Color(255, 150, 0, (int)(alpha * 200)),
                        new Color(255, 220, 100, 0)
                    }
                );
                g2.setPaint(aura);
                g2.fillOval(ballX - radius, ballY - radius, radius * 2, radius * 2);
            }
            
            //ลูกบอลไฟหลัก
            int ballRadius = 25 + (int)fireFlicker;
            drawMidCircle(g2, ballX, ballY, ballRadius, new Color(255, 120, 0));
            drawMidCircleOutline(g2, ballX, ballY, ballRadius);
            
            // ประกายไฟรอบลูกบอล
            // for (int i = 0; i < 8; i++) {
            //     double angle = (dt * 5 + i * Math.PI / 4) % (Math.PI * 2);
            //     int sparkX = ballX + (int)(Math.cos(angle) * (40 + fireFlicker));
            //     int sparkY = ballY + (int)(Math.sin(angle) * (40 + fireFlicker));
            //     drawMidCircle(g2, sparkX, sparkY, 3, new Color(255, 200, 0, 180));
            // }
            
            // หางไฟที่ตามหลัง
            // Path2D trail = new Path2D.Double();
            // trail.moveTo(ballX, ballY + ballRadius);
            // trail.curveTo(ballX - 15, ballY + 60, ballX + 15, ballY + 80, ballX, ballY + 120);
            // trail.curveTo(ballX - 8, ballY + 90, ballX + 8, ballY + 70, ballX, ballY + ballRadius);
            // g2.setColor(new Color(255, 100, 0, 150));
            // g2.fill(trail);
        }

    // เมธอดใหม่สำหรับระเบิด
    private void drawExplosion(Graphics2D g2, double dt) {
        // พื้นหลังมื
        g2.setColor(new Color(30, 30, 50));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
            
        int explodeX = WIDTH / 2;
        int explodeY = (int)(HEIGHT - 1.9 * 200); // ตำแหน่งที่ลูกบอลหยุด
            
        // คลื่นระเบิดขยายออก
        double explosionRadius = dt * 400; // ขยายเร็ว
        double intensity = 1.0 - dt * 2; // ลดความเข้มลง
            
        if (intensity > 0) {
            // แสงระเบิด
            RadialGradientPaint explosion = new RadialGradientPaint(
                new Point2D.Float(explodeX, explodeY), (float)explosionRadius,
                new float[]{0f, 0.3f, 0.6f, 1f},
                new Color[]{
                    new Color(255, 255, 255, (int)(intensity * 255)),
                    new Color(255, 200, 0, (int)(intensity * 200)),
                    new Color(255, 100, 0, (int)(intensity * 100)),
                    new Color(255, 50, 0, 0)
                    }
                );
            g2.setPaint(explosion);
            g2.fillOval((int)(explodeX - explosionRadius), (int)(explodeY - explosionRadius),
                    (int)(explosionRadius * 2), (int)(explosionRadius * 2));
                
            // ประกายไฟกระจาย
            for (int i = 0; i < 20; i++) {
                double angle = i * Math.PI * 2 / 20;
                double sparkDist = explosionRadius * 0.8;
                int sparkX = explodeX + (int)(Math.cos(angle) * sparkDist);
                int sparkY = explodeY + (int)(Math.sin(angle) * sparkDist);
                    
                if (sparkX >= 0 && sparkX < WIDTH && sparkY >= 0 && sparkY < HEIGHT) {
                    drawMidCircle(g2, sparkX, sparkY, (int)(intensity * 8), 
                                new Color(255, 150, 0, (int)(intensity * 200)));
                }
            }
        }
    }

    private void drawPhoenix(Graphics2D g2, double dt) {
        // คำนวณตำแหน่งบินรูป infinity (∞)
        double t = dt * 1.5;
        double a = 150;
        int cx = (int)(a * Math.sin(t) / (1 + Math.cos(t) * Math.cos(t)) + WIDTH/2);
        int cy = (int)(a * Math.sin(t) * Math.cos(t) / (1 + Math.cos(t) * Math.cos(t)) + HEIGHT/2);
        
        double flap = 20 * Math.sin(dt * 6);
        double fireEffect = Math.sin(dt * 8) * 5;

        // วาดการตกแต่งท้องฟ้า
        drawSkyDecorations(g2, dt);
        
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // วาดออร่าไฟรอบตัวนก (สีแดง-ส้ม-เหลือง)
        for (int i = 3; i >= 1; i--) {
            float alpha = 0.4f - (i * 0.1f);
            int radius = 50 + i * 20 + (int)fireEffect;
            
            RadialGradientPaint aura = new RadialGradientPaint(
                new Point2D.Float(cx, cy - 30), radius,
                new float[]{0f, 0.6f, 1f},
                new Color[]{
                    new Color(255, 50, 0, (int)(alpha * 255)),   // แดงเข้ม
                    new Color(255, 120, 0, (int)(alpha * 200)),  // ส้ม  
                    new Color(255, 200, 100, 0)                  // เหลืองอ่อน
                }
            );
            g2.setPaint(aura);
            g2.fillOval(cx - radius, cy - 30 - radius, radius * 2, radius * 2);
        }

        // ประกายไฟรอบตัว (สีแดง-ส้ม)
        for (int i = 0; i < 12; i++) {
            double angle = (dt * 3 + i * Math.PI / 6) % (Math.PI * 2);
            int sparkX = cx + (int)(Math.cos(angle) * (60 + fireEffect));
            int sparkY = cy - 30 + (int)(Math.sin(angle) * (40 + fireEffect));
            
            // สลับสีประกาย
            Color sparkColor = (i % 2 == 0) ? 
                new Color(255, 80, 0, 220) :    // แดงส้ม
                new Color(255, 150, 0, 200);    // ส้มทอง
            
            drawMidCircle(g2, sparkX, sparkY, 4 + (int)(fireEffect/2), sparkColor);
        }

            // หางไฟ (สีแดงไล่ส้ม)
            Path2D flameTail = new Path2D.Double();
            flameTail.moveTo(cx, cy + 10);
            flameTail.curveTo(cx - 25, cy + 35 + fireEffect, cx + 25, cy + 50 + fireEffect, cx, cy + 80 + fireEffect);
            flameTail.curveTo(cx - 15, cy + 55 + fireEffect, cx + 15, cy + 30 + fireEffect, cx, cy + 10);
            
            // ไล่สีหางไฟ
            GradientPaint flameGrad = new GradientPaint(
                cx, cy + 10, new Color(255, 200, 50),    // ทองที่โคนหาง
                cx, cy + 80 + (int)fireEffect, new Color(255, 50, 0)  // แดงที่ปลายหาง
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
            g2.setColor(new Color(200, 120, 30));  // ทองเข้มคล้ายในรูป
            g2.fill(body);
            g2.setColor(new Color(150, 80, 0));    // ขอบทองเข้มกว่า
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
                cx - 10, cy - 80 + (int)flap, new Color(255, 150, 50),  // ทองที่โคนปีก
                cx - 140, cy - 30 + (int)flap, new Color(200, 50, 0)    // แดงที่ปลายปีก
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
                cx + 10, cy - 80 + (int)flap, new Color(255, 150, 50),
                cx + 140, cy - 30 + (int)flap, new Color(200, 50, 0)
            );
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
            int cloudX = (int)((i * 150 + dt * 20) % (WIDTH + 100)) - 50;
            int cloudY = 80 + i * 30;
            drawCloud(g2, cloudX, cloudY);
        }
        
        // ดาวเล็ก ๆ กระพริบ
        for (int i = 0; i < 8; i++) {
            double twinkle = Math.sin(dt * 4 + i) * 0.5 + 0.5;
            int starX = 50 + i * 70;
            int starY = 50 + (int)(Math.sin(dt + i) * 20);
            g2.setColor(new Color(255, 255, 200, (int)(twinkle * 180)));
            drawStar(g2, starX, starY, 10);
        }
        
        // แสงแดดกระจาย
        g2.setColor(new Color(255, 255, 150, 50));
        for (int i = 0; i < 12; i++) {
            double angle = (dt * 0.5 + i * Math.PI / 6) % (Math.PI * 2);
            int rayX = WIDTH/2 + (int)(Math.cos(angle) * 300);
            int rayY = HEIGHT/4 + (int)(Math.sin(angle) * 200);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(WIDTH/2, HEIGHT/4, rayX, rayY);
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
            xPoints[i] = x + (int)(Math.cos(angle) * radius);
            yPoints[i] = y + (int)(Math.sin(angle) * radius);
        }
        g2.fillPolygon(xPoints, yPoints, 10);
    }

    // Midpoint circle fill
    private void drawMidCircle(Graphics2D g2,int cx,int cy,int r,Color col){
        g2.setColor(col);
        int x=0,y=r,d=1-r;
        while(x<=y){
            fillOct(g2,cx,cy,x,y);
            if(d<0) d+=2*x+3;
            else{ d+=2*(x-y)+5; y--; }
            x++;
        }
    }
    private void fillOct(Graphics2D g2,int cx,int cy,int x,int y){
        g2.drawLine(cx-x,cy-y,cx+x,cy-y);
        g2.drawLine(cx-x,cy+y,cx+x,cy+y);
        g2.drawLine(cx-y,cy-x,cx+y,cy-x);
        g2.drawLine(cx-y,cy+x,cx+y,cy+x);
    }

    // Midpoint circle outline
    private void drawMidCircleOutline(Graphics2D g2,int cx,int cy,int r){
        g2.setColor(Color.WHITE);
        int x=0,y=r,d=1-r;
        while(x<=y){
            plotPts(g2,cx,cy,x,y);
            if(d<0) d+=2*x+3;
            else { d+=2*(x-y)+5; y--; }
            x++;
        }
    }
    private void plotPts(Graphics2D g2,int cx,int cy,int x,int y){
        g2.drawLine(cx+x,cy+y,cx+x,cy+y);
        g2.drawLine(cx-x,cy+y,cx-x,cy+y);
        g2.drawLine(cx+x,cy-y,cx+x,cy-y);
        g2.drawLine(cx-x,cy-y,cx-x,cy-y);
        g2.drawLine(cx+y,cy+x,cx+y,cy+x);
        g2.drawLine(cx-y,cy+x,cx-y,cy+x);
        g2.drawLine(cx+y,cy-x,cx+y,cy-x);
        g2.drawLine(cx-y,cy-x,cx-y,cy-x);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("WHAT IF I REBORNED");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new Phoenix());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
