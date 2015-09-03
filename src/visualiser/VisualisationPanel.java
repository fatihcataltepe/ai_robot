
package visualiser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import problem.ArmConfig;
import problem.Obstacle;
import problem.ProblemSpec;

public class VisualisationPanel extends JComponent {
   /** UID, as required by Swing */
   private static final long serialVersionUID   = -4286532773714402501L;

   private ProblemSpec       problemSetup       = new ProblemSpec();
   private Visualiser        visualiser;

   private AffineTransform   translation        = AffineTransform.getTranslateInstance(0, -1);
   private AffineTransform   transform          = null;

   private ArmConfig         currentState;
   private boolean           animating          = false;
   private boolean           displayingSolution = false;
   private Timer             animationTimer;
   private int               framePeriod        = 20;                                         // 50 FPS
   private Integer           frameNumber        = null;
   private int               maxFrameNumber;

   private int               samplingPeriod     = 100;

   public VisualisationPanel(Visualiser visualiser) {
      super();
      this.setBackground(Color.WHITE);
      this.setOpaque(true);
      this.visualiser = visualiser;
   }

   public void setDisplayingSolution(boolean displayingSolution) {
      this.displayingSolution = displayingSolution;
      repaint();
   }

   public boolean isDisplayingSolution() {
      return displayingSolution;
   }

   public void setFramerate(int framerate) {
      this.framePeriod = 1000 / framerate;
      if (animationTimer != null) {
         animationTimer.setDelay(framePeriod);
      }
   }

   public void initAnimation() {
      if (!problemSetup.solutionLoaded()) {
         return;
      }
      if (animationTimer != null) {
         animationTimer.stop();
      }
      animating = true;
      gotoFrame(0);
      maxFrameNumber = problemSetup.getPath().size() - 1;
      animationTimer = new Timer(framePeriod, new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent arg0) {
            int newFrameNumber = frameNumber + 1;
            if (newFrameNumber >= maxFrameNumber) {
               animationTimer.stop();
               visualiser.setPlaying(false);
            }
            if (newFrameNumber <= maxFrameNumber) {
               gotoFrame(newFrameNumber);
            }
         }
      });
      visualiser.setPlaying(false);
      visualiser.updateMaximum();
   }

   public void gotoFrame(int frameNumber) {
      if (!animating || (this.frameNumber != null && this.frameNumber == frameNumber)) {
         return;
      }
      this.frameNumber = frameNumber;
      visualiser.setFrameNumber(frameNumber);
      currentState = problemSetup.getPath().get(frameNumber);
      repaint();
   }

   public int getFrameNumber() {
      return frameNumber;
   }

   public void playPauseAnimation() {
      if (animationTimer.isRunning()) {
         animationTimer.stop();
         visualiser.setPlaying(false);
      }
      else {
         if (frameNumber >= maxFrameNumber) {
            gotoFrame(0);
         }
         animationTimer.start();
         visualiser.setPlaying(true);
      }
   }

   public void stopAnimation() {
      if (animationTimer != null) {
         animationTimer.stop();
      }
      animating = false;
      visualiser.setPlaying(false);
      frameNumber = null;
   }

   public ProblemSpec getProblemSetup() {
      return problemSetup;
   }

   public void calculateTransform() {
      transform = AffineTransform.getScaleInstance(getWidth(), -getHeight());
      transform.concatenate(translation);
   }

   public void paintState(Graphics2D g2, ArmConfig s) {
      if (s == null) {
         return;
      }
      Path2D.Float path = new Path2D.Float();

      List<Line2D> links = s.getLinks();
      Point2D p = s.getBase();
      path.moveTo(p.getX(), p.getY());
      for (int i = 0; i < links.size(); i++) {
         p = links.get(i).getP2();
         path.lineTo(p.getX(), p.getY());
      }
      path.transform(transform);
      g2.draw(path);
      if (animating || !displayingSolution) {
         p = transform.transform(s.getBase(), null);
         Color color = g2.getColor();
         Stroke stroke = g2.getStroke();
         g2.setColor(Color.BLACK);
         g2.setStroke(new BasicStroke(1));
         g2.draw(new Ellipse2D.Double(p.getX() - 4, p.getY() - 4, 8, 8));
         g2.setColor(color);
         g2.setStroke(stroke);
      }
   }

   public void setSamplingPeriod(int samplingPeriod) {
      this.samplingPeriod = samplingPeriod;
      repaint();
   }

   public void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);
      if (!problemSetup.problemLoaded()) {
         return;
      }
      calculateTransform();
      Graphics2D g2 = (Graphics2D) graphics;
      g2.setColor(Color.WHITE);
      g2.fillRect(0, 0, getWidth(), getHeight());

      // g2.setColor(Color.black);
      // for (int i = 0; i < getWidth(); i = i + (getWidth() / 100)) {
      // for (int j = 0; j < getHeight(); j = j + (getHeight() / 100)) {
      // System.out.println("asdasdasd");
      // g2.fillRect(i, j, 3, 3);
      // }
      // }

      List<Obstacle> obstacles = problemSetup.getObstacles();
      if (obstacles != null) {
         g2.setColor(Color.red);
         for (Obstacle obs : problemSetup.getObstacles()) {
            Shape transformed = transform.createTransformedShape(obs.getRect());
            g2.fill(transformed);
         }
      }

      g2.setStroke(new BasicStroke(2));
      if (!animating) {
         if (displayingSolution) {
            List<ArmConfig> path = problemSetup.getPath();
            int lastIndex = path.size() - 1;
            for (int i = 0; i < lastIndex; i += samplingPeriod) {
               float t = (float) i / lastIndex;
               g2.setColor(new Color(0, t, 1 - t));
               paintState(g2, path.get(i));
            }
            g2.setColor(Color.green);
            paintState(g2, path.get(lastIndex));
         }
         else {
            g2.setColor(Color.blue);
            paintState(g2, problemSetup.getInitialState());

            g2.setColor(Color.green);
            paintState(g2, problemSetup.getGoalState());

            // --------------------------------------------------------------------------------
            Iterator it = problemSetup.getMap().entrySet().iterator();
            while (it.hasNext()) {
               Map.Entry<ArmConfig, List<ArmConfig>> pair = (Map.Entry) it.next();
               ArmConfig curr = pair.getKey();
               ArrayList<ArmConfig> children = (ArrayList<ArmConfig>) pair.getValue();

               int counter = 0;
               for (ArmConfig c : children) {
                  g2.setColor(Color.black);
                  g2.drawLine((int) (curr.getBase().getX() * getWidth()),
                              (int) (getHeight() - getHeight() * curr.getBase().getY()),
                              (int) (c.getBase().getX() * getWidth()),
                              (int) (getHeight() - getHeight() * c.getBase().getY()));

               }

               it.remove(); // avoids a ConcurrentModificationException
            }

            int cun = 0;
            for (Point2D.Double f : problemSetup.getFreeSpace()) {
               System.out.println(cun++);
               g2.setColor(Color.green);
               // g2.drawRect((int) (f.getX() * getWidth()), (int) (getHeight() - f.getY() * getHeight()), 3, 3);
               // paintState(g2, new ArmConfig(f, new ArrayList<Double>()));
            }

            for (ArmConfig a : problemSetup.getRandomArms()) {
//               paintState(g2, a);
            }
            // --------------------------------------------------------------------------------
         }
      }
      else {
         g2.setColor(Color.blue);
         paintState(g2, currentState);

      }
   }
}
