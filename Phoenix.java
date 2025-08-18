import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Phoenix1 extends JPanel implements ActionListener {
    
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 600;
    private static final int FRAME_RATE = 16; 
    private final Timer animationTimer;
    private final long startTime;
    
    public Phoenix1() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.WHITE);
        startTime = System.currentTimeMillis();
        animationTimer = new Timer(FRAME_RATE, this);
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        double elapsedTime = (System.currentTimeMillis() - startTime) / 1000.0;
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                           RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawAnimationSequence(g2d, elapsedTime);
    }
    
    /**
     * Controls the main animation sequence
     * Scene progression: Fireball → Explosion → Sky transition → Phoenix flight
     */
    private void drawAnimationSequence(Graphics2D g2d, double elapsedTime) {
        if (elapsedTime < 1.5) {
            // Scene 1: Dark sky with rising fireball
            drawDarkSky(g2d);
            drawRisingFireball(g2d, elapsedTime - 0.1);
            
        } else if (elapsedTime < 2.0) {
            // Scene 2: Explosion effect
            drawExplosionEffect(g2d, elapsedTime - 1.5);
            
        } else if (elapsedTime < 2.5) {
            // Scene 3: Transition to day sky
            drawSkyTransition(g2d, elapsedTime - 2.0);
            
        } else {
            // Scene 4: Phoenix flying in day sky
            drawDaySky(g2d);
            drawPhoenixFlight(g2d, elapsedTime - 2.5);
        }
    }
    
    // ======= Algorithm Implementations =======
    
    /**
     * Bresenham Line Algorithm - for drawing straight lines
     * Used for: Sun rays, phoenix claws, feather rachis
     */
    private List<Point> calculateBresenhamLine(int startX, int startY, int endX, int endY) {
        List<Point> linePoints = new ArrayList<>();
        
        int deltaX = Math.abs(endX - startX);
        int deltaY = -Math.abs(endY - startY);
        int stepX = startX < endX ? 1 : -1;
        int stepY = startY < endY ? 1 : -1;
        int error = deltaX + deltaY;
        
        int currentX = startX;
        int currentY = startY;
        
        while (true) {
            linePoints.add(new Point(currentX, currentY));
            
            if (currentX == endX && currentY == endY) break;
            
            int doubleError = 2 * error;
            if (doubleError >= deltaY) { 
                error += deltaY; 
                currentX += stepX; 
            }
            if (doubleError <= deltaX) { 
                error += deltaX; 
                currentY += stepY; 
            }
        }
        
        return linePoints;
    }
    
    /**
     * Midpoint Circle Algorithm - for drawing circles
     * Used for: Phoenix head, eyes, explosion, fire aura
     */
    private Polygon createMidpointCircle(int centerX, int centerY, int radius) {
        Polygon circlePolygon = new Polygon();
        List<Point> circlePoints = new ArrayList<>();
        
        int x = radius;
        int y = 0;
        int decisionParameter = 1 - radius;
        
        while (x >= y) {
            // Plot all 8 octants of the circle
            circlePoints.add(new Point(centerX + x, centerY + y));
            circlePoints.add(new Point(centerX + y, centerY + x));
            circlePoints.add(new Point(centerX - y, centerY + x));
            circlePoints.add(new Point(centerX - x, centerY + y));
            circlePoints.add(new Point(centerX - x, centerY - y));
            circlePoints.add(new Point(centerX - y, centerY - x));
            circlePoints.add(new Point(centerX + y, centerY - x));
            circlePoints.add(new Point(centerX + x, centerY - y));
            
            if (decisionParameter < 0) {
                decisionParameter += 2 * y + 3;
            } else {
                decisionParameter += 2 * (y - x) + 5;
                x--;
            }
            y++;
        }
        
        // Sort points by angle to create proper polygon
        circlePoints.sort((point1, point2) -> {
            double angle1 = Math.atan2(point1.y - centerY, point1.x - centerX);
            double angle2 = Math.atan2(point2.y - centerY, point2.x - centerX);
            return Double.compare(angle1, angle2);
        });
        
        for (Point point : circlePoints) {
            circlePolygon.addPoint(point.x, point.y);
        }
        
        return circlePolygon;
    }
    
    /**
     * Midpoint Ellipse Algorithm - for drawing ellipses
     * Used for: Phoenix body, clouds, neck
     */
    private Polygon createMidpointEllipse(int centerX, int centerY, int radiusA, int radiusB) {
        Polygon ellipsePolygon = new Polygon();
        List<Point> ellipsePoints = new ArrayList<>();
        
        int aSquared = radiusA * radiusA;
        int bSquared = radiusB * radiusB;
        int twoASquared = 2 * aSquared;
        int twoBSquared = 2 * bSquared;
        
        // Region 1: Where slope > -1
        int x = 0;
        int y = radiusB;
        int decisionParam1 = Math.round(bSquared - aSquared * radiusB + aSquared / 4);
        int deltaX = 0;
        int deltaY = twoASquared * y;
        
        while (deltaX <= deltaY) {
            // Plot 4 quadrants
            ellipsePoints.add(new Point(centerX + x, centerY + y));
            ellipsePoints.add(new Point(centerX - x, centerY + y));
            ellipsePoints.add(new Point(centerX + x, centerY - y));
            ellipsePoints.add(new Point(centerX - x, centerY - y));
            
            x++;
            deltaX += twoBSquared;
            decisionParam1 += deltaX + bSquared;
            
            if (decisionParam1 >= 0) {
                y--;
                deltaY -= twoASquared;
                decisionParam1 -= deltaY;
            }
        }
        
        // Region 2: Where slope < -1
        x = radiusA;
        y = 0;
        int decisionParam2 = Math.round(aSquared - bSquared * radiusA + bSquared / 4);
        deltaX = twoBSquared * x;
        deltaY = 0;
        
        while (deltaX >= deltaY) {
            // Plot 4 quadrants
            ellipsePoints.add(new Point(centerX + x, centerY + y));
            ellipsePoints.add(new Point(centerX - x, centerY + y));
            ellipsePoints.add(new Point(centerX + x, centerY - y));
            ellipsePoints.add(new Point(centerX - x, centerY - y));
            
            y++;
            deltaY += twoASquared;
            decisionParam2 += deltaY + aSquared;
            
            if (decisionParam2 >= 0) {
                x--;
                deltaX -= twoBSquared;
                decisionParam2 -= deltaX;
            }
        }
        
        // Sort points by angle and create polygon
        ellipsePoints.sort((point1, point2) -> {
            double angle1 = Math.atan2(point1.y - centerY, point1.x - centerX);
            double angle2 = Math.atan2(point2.y - centerY, point2.x - centerX);
            return Double.compare(angle1, angle2);
        });
        
        for (Point point : ellipsePoints) {
            ellipsePolygon.addPoint(point.x, point.y);
        }
        
        return ellipsePolygon;
    }
    
    /**
     * Bezier Curve Algorithm - for drawing curved lines
     * Used for: Phoenix wing feathers
     */
    private Polygon createBezierCurve(int x0, int y0, int x1, int y1, 
                                     int x2, int y2, int x3, int y3, int segments) {
        Polygon curvePolygon = new Polygon();
        
        for (int i = 0; i <= segments; i++) {
            double t = i / (double) segments;
            double tSquared = t * t;
            double tCubed = tSquared * t;
            double oneMinusT = 1 - t;
            double oneMinusTSquared = oneMinusT * oneMinusT;
            double oneMinusTCubed = oneMinusTSquared * oneMinusT;
            
            int x = (int)(oneMinusTCubed * x0 + 3 * oneMinusTSquared * t * x1 + 
                         3 * oneMinusT * tSquared * x2 + tCubed * x3);
            int y = (int)(oneMinusTCubed * y0 + 3 * oneMinusTSquared * t * y1 + 
                         3 * oneMinusT * tSquared * y2 + tCubed * y3);
            
            curvePolygon.addPoint(x, y);
        }
        
        return curvePolygon;
    }
    
    /**
     * Utility method to draw lines using Bresenham algorithm
     */
    private void drawBresenhamLine(Graphics2D g2d, int startX, int startY, int endX, int endY) {
        List<Point> linePoints = calculateBresenhamLine(startX, startY, endX, endY);
        for (Point point : linePoints) {
            g2d.fillRect(point.x, point.y, 1, 1);
        }
    }
    
    // ======= Scene Drawing Methods =======
    
    //Draws dark night sky background 
    private void drawDarkSky(Graphics2D g2d) {
        g2d.setColor(new Color(30, 30, 50));
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    
    //Draws day sky with gradient
    private void drawDaySky(Graphics2D g2d) {
        GradientPaint skyGradient = new GradientPaint(
            0, 0, new Color(80, 180, 255),
            0, WINDOW_HEIGHT, new Color(200, 240, 255)
        );
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    //Draws rising fireball animation
    private void drawRisingFireball(Graphics2D g2d, double elapsedTime) {
        int fireballY = (int)(WINDOW_HEIGHT - elapsedTime * 250);
        int fireballX = WINDOW_WIDTH / 2;
        double fireFlicker = Math.sin(elapsedTime * 15) * 3;
        
        // Draw fire aura with multiple circle layers
        for (int layer = 3; layer >= 1; layer--) {
            float transparency = 0.6f - (layer * 0.15f);
            int auraRadius = 30 + layer * 15 + (int)fireFlicker;
            g2d.setColor(new Color(255, 150, 0, (int)(transparency * 200)));
            g2d.fillPolygon(createMidpointCircle(fireballX, fireballY, auraRadius));
        }
        
        // Draw main fireball
        int fireballRadius = 25 + (int)fireFlicker;
        g2d.setColor(new Color(255, 120, 0));
        g2d.fillPolygon(createMidpointCircle(fireballX, fireballY, fireballRadius));
        g2d.setColor(Color.WHITE);
        g2d.drawPolygon(createMidpointCircle(fireballX, fireballY, fireballRadius));
    }
    
    //Draws explosion effect
    private void drawExplosionEffect(Graphics2D g2d, double elapsedTime) {
        drawDarkSky(g2d);
        
        int explosionX = WINDOW_WIDTH / 2;
        int explosionY = (int)(WINDOW_HEIGHT - 1.9 * 200);
        double explosionRadius = elapsedTime * 400;
        double intensity = 1.0 - elapsedTime * 2;
        
        if (intensity > 0) {
            // Main explosion circle
            g2d.setColor(new Color(255, 255, 255, (int)(intensity * 255)));
            g2d.fillPolygon(createMidpointCircle(explosionX, explosionY, (int)explosionRadius));
            
            // Surrounding fire sparks
            for (int i = 0; i < 20; i++) {
                double sparkAngle = i * Math.PI * 2 / 20;
                double sparkDistance = explosionRadius * 0.8;
                int sparkX = explosionX + (int)(Math.cos(sparkAngle) * sparkDistance);
                int sparkY = explosionY + (int)(Math.sin(sparkAngle) * sparkDistance);
                
                if (sparkX >= 0 && sparkX < WINDOW_WIDTH && 
                    sparkY >= 0 && sparkY < WINDOW_HEIGHT) {
                    g2d.setColor(new Color(255, 150, 0, (int)(intensity * 200)));
                    g2d.fillPolygon(createMidpointCircle(sparkX, sparkY, (int)(intensity * 8)));
                }
            }
        }
    }
    
    //Draws transition from explosion to day sky
    private void drawSkyTransition(Graphics2D g2d, double transitionProgress) {
        float fadeProgress = (float)(transitionProgress / 1.0);
        
        drawDaySky(g2d);
        
        // White fade overlay
        g2d.setColor(new Color(1f, 1f, 1f, 1f - fadeProgress));
        g2d.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
    }
    
    
    //Main phoenix drawing method with infinity flight pattern
    private void drawPhoenixFlight(Graphics2D g2d, double elapsedTime) {
        // Calculate infinity (∞) flight pattern
        double flightParameter = elapsedTime * 1.5;
        double amplitude = 150;
        
        int phoenixX = (int)(amplitude * Math.sin(flightParameter) / 
                           (1 + Math.cos(flightParameter) * Math.cos(flightParameter)) + 
                           WINDOW_WIDTH / 2);
        int phoenixY = (int)(amplitude * Math.sin(flightParameter) * Math.cos(flightParameter) / 
                           (1 + Math.cos(flightParameter) * Math.cos(flightParameter)) + 
                           WINDOW_HEIGHT / 2);
        
        double wingFlap = 25 * Math.sin(elapsedTime * 6);
        double fireEffect = Math.sin(elapsedTime * 8) * 5;
        
        drawSkyDecorations(g2d, elapsedTime);
        drawPhoenixFireAura(g2d, phoenixX, phoenixY, fireEffect);
        drawPhoenixTail(g2d, phoenixX, phoenixY, fireEffect);
        drawPhoenixWings(g2d, phoenixX, phoenixY, wingFlap, fireEffect);
        drawPhoenixBody(g2d, phoenixX, phoenixY);
        drawPhoenixHead(g2d, phoenixX, phoenixY);
        drawPhoenixClaws(g2d, phoenixX, phoenixY);
        drawPhoenixFireSparks(g2d, phoenixX, phoenixY, elapsedTime, fireEffect);
    }
    
    //Draws phoenix fire aura effect
    private void drawPhoenixFireAura(Graphics2D g2d, int phoenixX, int phoenixY, double fireEffect) {
        for (int layer = 3; layer >= 1; layer--) {
            float transparency = 0.15f - (layer * 0.03f);
            int auraRadius = 45 + layer * 15 + (int)fireEffect;
            g2d.setColor(new Color(255, 100, 0, (int)(transparency * 255)));
            g2d.fillPolygon(createMidpointCircle(phoenixX, phoenixY - 15, auraRadius));
        }
    }
    
    //Draws peacock-style phoenix tail with individual feathers
    private void drawPhoenixTail(Graphics2D g2d, int phoenixX, int phoenixY, double fireEffect) {
        for (int featherIndex = 0; featherIndex < 5; featherIndex++) {
            double tailAngle = (Math.PI / 8) * (featherIndex - 2);
            int tailTipX = phoenixX + (int)(Math.sin(tailAngle) * 30);
            int tailTipY = phoenixY + 90 + (int)fireEffect + featherIndex * 8;
            
            // Individual tail feather
            g2d.setColor(new Color(200, 30, 0, 220));
            Polygon tailFeather = new Polygon();
            tailFeather.addPoint(phoenixX, phoenixY + 15);
            tailFeather.addPoint(phoenixX + (int)(Math.sin(tailAngle) * 10), phoenixY + 40);
            tailFeather.addPoint(tailTipX, tailTipY);
            tailFeather.addPoint(phoenixX + (int)(Math.sin(tailAngle) * 8), phoenixY + 35);
            g2d.fillPolygon(tailFeather);
            
            // Feather border
            g2d.setColor(new Color(120, 15, 0));
            g2d.drawPolygon(tailFeather);
        }
    }
    
    //Draws multi-layered phoenix wings using Bezier curves
    private void drawPhoenixWings(Graphics2D g2d, int phoenixX, int phoenixY, 
                                 double wingFlap, double fireEffect) {
        drawDetailedWing(g2d, phoenixX, phoenixY, wingFlap, fireEffect, true);  // Left wing
        drawDetailedWing(g2d, phoenixX, phoenixY, wingFlap, fireEffect, false); // Right wing
    }
    
    //Draws individual wing with multiple feather layers
    private void drawDetailedWing(Graphics2D g2d, int phoenixX, int phoenixY, 
                                 double wingFlap, double fireEffect, boolean isLeftWing) {
        int direction = isLeftWing ? -1 : 1;
        int wingBaseX = phoenixX + (direction * 5);
        int wingBaseY = phoenixY - 18;
        
        // Primary feathers (outer layer)
        drawFeatherLayer(g2d, wingBaseX, wingBaseY, direction, wingFlap,
                        new int[]{120, 115, 110, 105, 100, 95},
                        new double[]{-45, -35, -25, -15, -5, 5},
                        new Color(180, 30, 0, 220), 0);
        
        // Secondary feathers (middle layer)
        drawFeatherLayer(g2d, wingBaseX, wingBaseY + 5, direction, wingFlap,
                        new int[]{100, 95, 90, 85, 80},
                        new double[]{-40, -28, -16, -4, 8},
                        new Color(255, 80, 20, 230), 1);
        
        // Tertiary feathers (inner layer)
        drawFeatherLayer(g2d, wingBaseX, wingBaseY + 10, direction, wingFlap,
                        new int[]{80, 75, 70, 65},
                        new double[]{-35, -20, -5, 10},
                        new Color(255, 160, 50, 240), 2);
        
        // Cover feathers (innermost layer)
        drawFeatherLayer(g2d, wingBaseX, wingBaseY + 15, direction, wingFlap,
                        new int[]{50, 45, 40},
                        new double[]{-25, -10, 5},
                        new Color(255, 200, 80, 250), 3);
    }
    
    
    //Draws a layer of wing feathers using Bezier curves
    private void drawFeatherLayer(Graphics2D g2d, int baseX, int baseY, int direction, 
                                 double wingFlap, int[] featherLengths, double[] featherAngles,
                                 Color featherColor, int layerIndex) {
        for (int i = 0; i < featherLengths.length; i++) {
            double angleRadians = Math.toRadians(featherAngles[i]);
            double currentFlap = wingFlap * (1.0 - layerIndex * 0.1 + i * 0.04);
            
            int featherTipX = baseX + (int)(direction * featherLengths[i] * Math.cos(angleRadians));
            int featherTipY = baseY - (int)(featherLengths[i] * Math.sin(angleRadians) + currentFlap);
            
            // Create feather using Bezier curve
            Polygon featherShape = createBezierCurve(
                baseX - direction * (5 - layerIndex), baseY,              // P0: Base left
                baseX + direction * (40 - layerIndex * 10), baseY - 20,   // P1: Control point 1
                featherTipX - direction * (15 - layerIndex * 3), featherTipY + 10, // P2: Control point 2
                featherTipX, featherTipY,                                 // P3: Tip
                25 - layerIndex * 5                                       // Segments
            );
            
            // Draw feather
            g2d.setColor(featherColor);
            g2d.fillPolygon(featherShape);
            g2d.setColor(new Color(100, 15, 0));
            g2d.drawPolygon(featherShape);
            
            // Draw feather rachis (center line)
            g2d.setColor(new Color(80, 10, 0));
            drawBresenhamLine(g2d, baseX, baseY, featherTipX, featherTipY);
        }
    }
    
    //Draws phoenix body using midpoint ellipse
    private void drawPhoenixBody(Graphics2D g2d, int phoenixX, int phoenixY) {
        // Outer body layer
        g2d.setColor(new Color(180, 100, 20));
        Polygon outerBody = createMidpointEllipse(phoenixX, phoenixY - 15, 17, 42);
        g2d.fillPolygon(outerBody);
        
        // Middle body layer
        g2d.setColor(new Color(220, 140, 40));
        Polygon middleBody = createMidpointEllipse(phoenixX, phoenixY - 15, 14, 35);
        g2d.fillPolygon(middleBody);
        
        // Inner body layer
        g2d.setColor(new Color(200, 120, 30));
        Polygon innerBody = createMidpointEllipse(phoenixX, phoenixY - 15, 10, 27);
        g2d.fillPolygon(innerBody);
        
        // Body outline
        g2d.setColor(new Color(120, 60, 0));
        g2d.drawPolygon(outerBody);
    }
    
    //Draws phoenix head, neck, eyes, and beak
    private void drawPhoenixHead(Graphics2D g2d, int phoenixX, int phoenixY) {
        int headX = phoenixX;
        int headY = phoenixY - 45;
        
        // Neck
        g2d.setColor(new Color(200, 120, 30));
        g2d.fillPolygon(createMidpointCircle(phoenixX, phoenixY - 30, 10));
        
        // Head layers
        g2d.setColor(new Color(180, 100, 20));
        g2d.fillPolygon(createMidpointCircle(headX, headY, 16));
        g2d.setColor(new Color(220, 140, 40));
        g2d.fillPolygon(createMidpointCircle(headX, headY, 13));
        g2d.setColor(new Color(120, 60, 0));
        g2d.drawPolygon(createMidpointCircle(headX, headY, 16));
        
        // Eye layers (black eyes as requested)
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillPolygon(createMidpointCircle(headX - 3, headY - 5, 6));
        g2d.setColor(new Color(20, 20, 20));
        g2d.fillPolygon(createMidpointCircle(headX - 3, headY - 5, 5));
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillPolygon(createMidpointCircle(headX - 3, headY - 5, 3));
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillPolygon(createMidpointCircle(headX - 5, headY - 7, 1));
        
        // Golden beak
        drawGoldenBeak(g2d, headX, headY);
    }
    
    
    //Draws eagle-style golden beak
    private void drawGoldenBeak(Graphics2D g2d, int headX, int headY) {
        // Upper beak
        Polygon upperBeak = new Polygon();
        upperBeak.addPoint(headX + 13, headY);
        upperBeak.addPoint(headX + 22, headY + 2);
        upperBeak.addPoint(headX + 20, headY + 6);
        upperBeak.addPoint(headX + 15, headY + 3);
        g2d.setColor(new Color(255, 215, 0));
        g2d.fillPolygon(upperBeak);
        
        // Lower beak
        Polygon lowerBeak = new Polygon();
        lowerBeak.addPoint(headX + 13, headY + 3);
        lowerBeak.addPoint(headX + 19, headY + 6);
        lowerBeak.addPoint(headX + 17, headY + 8);
        g2d.setColor(new Color(200, 170, 0));
        g2d.fillPolygon(lowerBeak);
        
        // Beak outlines
        g2d.setColor(new Color(150, 120, 0));
        g2d.drawPolygon(upperBeak);
        g2d.drawPolygon(lowerBeak);
    }
    
    //Draws phoenix claws (golden talons)
    private void drawPhoenixClaws(Graphics2D g2d, int phoenixX, int phoenixY) {
        g2d.setColor(new Color(255, 215, 0));
        
        // Left leg and talons
        drawBresenhamLine(g2d, phoenixX - 6, phoenixY + 8, phoenixX - 8, phoenixY + 25);
        for (int talon = 0; talon < 3; talon++) {
            drawBresenhamLine(g2d, phoenixX - 8 + talon * 1, phoenixY + 25, 
                            phoenixX - 10 + talon * 2, phoenixY + 30);
        }
        
        // Right leg and talons
        drawBresenhamLine(g2d, phoenixX + 6, phoenixY + 8, phoenixX + 8, phoenixY + 25);
        for (int talon = 0; talon < 3; talon++) {
            drawBresenhamLine(g2d, phoenixX + 8 - talon * 1, phoenixY + 25, 
                            phoenixX + 10 - talon * 2, phoenixY + 30);
        }
    }
    
    
    //Draws rotating fire sparks around phoenix
    private void drawPhoenixFireSparks(Graphics2D g2d, int phoenixX, int phoenixY, 
                                     double elapsedTime, double fireEffect) {
        for (int spark = 0; spark < 12; spark++) {
            double sparkAngle = (elapsedTime * 3 + spark * Math.PI / 6) % (Math.PI * 2);
            int sparkX = phoenixX + (int)(Math.cos(sparkAngle) * (50 + fireEffect * 2));
            int sparkY = phoenixY - 15 + (int)(Math.sin(sparkAngle) * (35 + fireEffect));
            
            Color sparkColor;
            switch (spark % 3) {
                case 0:
                    sparkColor = new Color(255, 215, 0, 150);  // Gold
                    break;
                case 1:
                    sparkColor = new Color(255, 80, 0, 130);   // Orange
                    break;
                default:
                    sparkColor = new Color(200, 30, 0, 110);   // Red
                    break;
            }
            
            g2d.setColor(sparkColor);
            g2d.fillPolygon(createMidpointCircle(sparkX, sparkY, 3));
        }
    }
    
    //Draws sky decorations (clouds, stars, sun rays)
    private void drawSkyDecorations(Graphics2D g2d, double elapsedTime) {
        drawMovingClouds(g2d, elapsedTime);
        drawTwinklingStars(g2d, elapsedTime);
        drawSunRays(g2d, elapsedTime);
    }
    
    //Draws moving clouds with soft overlapping ellipses
    private void drawMovingClouds(Graphics2D g2d, double elapsedTime) {
        g2d.setColor(new Color(255, 255, 255, 150));
        
        for (int cloudIndex = 0; cloudIndex < 4; cloudIndex++) {
            int cloudX = (int)((cloudIndex * 150 + elapsedTime * 20) % (WINDOW_WIDTH + 100)) - 50;
            int cloudY = 80 + cloudIndex * 30;
            drawSoftCloud(g2d, cloudX, cloudY);
        }
    }
    
    //Draws individual soft cloud with multiple transparent layers
    private void drawSoftCloud(Graphics2D g2d, int cloudX, int cloudY) {
        // Layer 1: Background (most transparent)
        g2d.setColor(new Color(255, 255, 255, 60));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 15, cloudY + 8, 40, 22));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 45, cloudY + 5, 35, 20));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 75, cloudY + 8, 38, 24));
        
        // Layer 2: Middle
        g2d.setColor(new Color(255, 255, 255, 90));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 20, cloudY + 12, 35, 18));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 50, cloudY + 8, 30, 16));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 80, cloudY + 12, 32, 20));
        
        // Layer 3: Dense (less transparent)
        g2d.setColor(new Color(255, 255, 255, 120));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 25, cloudY + 15, 28, 15));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 55, cloudY + 12, 25, 13));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 85, cloudY + 15, 28, 16));
        
        // Layer 4: Highlights (brightest)
        g2d.setColor(new Color(255, 255, 255, 150));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 30, cloudY + 18, 22, 12));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 60, cloudY + 15, 20, 11));
        
        // Base layer - connects cloud masses
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillPolygon(createMidpointEllipse(cloudX + 45, cloudY + 25, 35, 14));
    }
    
    //Draws twinkling stars
    private void drawTwinklingStars(Graphics2D g2d, double elapsedTime) {
        for (int starIndex = 0; starIndex < 8; starIndex++) {
            double twinkleEffect = Math.sin(elapsedTime * 4 + starIndex) * 0.5 + 0.5;
            int starX = 50 + starIndex * 70;
            int starY = 50 + (int)(Math.sin(elapsedTime + starIndex) * 20);
            
            g2d.setColor(new Color(255, 255, 200, (int)(twinkleEffect * 180)));
            drawStar(g2d, starX, starY, 10);
        }
    }
    
    //Draws individual star shape
    private void drawStar(Graphics2D g2d, int centerX, int centerY, int starSize) {
        Polygon starShape = new Polygon();
        
        for (int point = 0; point < 10; point++) {
            double pointAngle = point * Math.PI / 5;
            int radius = (point % 2 == 0) ? starSize : starSize / 2;
            int pointX = centerX + (int)(Math.cos(pointAngle) * radius);
            int pointY = centerY + (int)(Math.sin(pointAngle) * radius);
            starShape.addPoint(pointX, pointY);
        }
        
        g2d.fillPolygon(starShape);
    }
    
    //Draws rotating sun rays using Bresenham lines
    private void drawSunRays(Graphics2D g2d, double elapsedTime) {
        g2d.setColor(new Color(255, 255, 150, 50));
        
        for (int rayIndex = 0; rayIndex < 12; rayIndex++) {
            double rayAngle = (elapsedTime * 0.5 + rayIndex * Math.PI / 6) % (Math.PI * 2);
            int rayEndX = WINDOW_WIDTH / 2 + (int)(Math.cos(rayAngle) * 300);
            int rayEndY = WINDOW_HEIGHT / 4 + (int)(Math.sin(rayAngle) * 200);
            
            drawBresenhamLine(g2d, WINDOW_WIDTH / 2, WINDOW_HEIGHT / 4, rayEndX, rayEndY);
        }
    }
    
    // ======= Event Handling =======
    @Override
    public void actionPerformed(ActionEvent event) {
        repaint();
    }
    
    // ======= Main Method =======
    public static void main(String[] arguments) {
        JFrame mainWindow = new JFrame("WHAT IF I REBORNED");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.add(new Phoenix1());
        mainWindow.pack();
        mainWindow.setLocationRelativeTo(null);
        mainWindow.setVisible(true);
    }
}
