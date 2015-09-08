
package model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import problem.ArmConfig;
import problem.Obstacle;
import search.Connector;
import tester.Tester;

/**
 * Created by fatihcataltepe on 01/09/15.
 */
public class Graph {
   private HashMap<ArmConfig, List<ArmConfig>> map;
   private List<Obstacle>                      obstacles;
   private final int                           numOfEdges         = 10;
   private int                                 numOfSamplesOnPath = 10;
   Connector connector;

   public Graph(List<ArmConfig> randomArms, List<Obstacle> obstacles) {
      this.obstacles = obstacles;

      map = new HashMap<>();

      System.out.println("Creating graph between confs...");

      for (int i = 0; i < randomArms.size(); i++) {
         ArmConfig curr = randomArms.get(i);
         TreeMap<Double, ArmConfig> treeMap = new TreeMap<>();

         if (map.get(curr) == null)
            map.put(curr, new ArrayList<>());

         for (int j = 0; j < randomArms.size(); j++) {
            ArmConfig child = randomArms.get(j);
            treeMap.put(curr.getBase().distance(child.getBase()), child);
         }

         SortedSet<Double> sortedSet = new TreeSet<Double>(treeMap.keySet());

         // creates a -> b edges at number of edges
         int counter = 0;
         for (Double d : sortedSet) {
            if (d != 0) {
               ArmConfig arm = treeMap.get(d);
               boolean checkPath = checkSamplesOnPath(connectTwoConf(curr, arm));

               if (!checkObstacles(curr.getBase(), arm.getBase()) && !checkPath) {
                  counter++;
                  map.get(curr).add(arm);
               }
            }
            if (counter >= numOfEdges)
               break;
         }
         // System.out.println("collision: " + counterWrong);
         // System.out.println("collision free: " + counterRight);
         // printMap(map);
      }
   }

   private boolean checkObstacles(Point2D curr, Point2D child) {
      for (Obstacle o : obstacles) {
         Rectangle2D r = o.getRect();
         Line2D l = new Line2D.Double(curr.getX(), curr.getY(), child.getX(), child.getY());
         if (l.intersects(r))
            return true;
      }
      return false;
   }

   public static void printMap(Map mp) {
      Iterator it = mp.entrySet().iterator();
      while (it.hasNext()) {
         Map.Entry<ArmConfig, List<ArmConfig>> pair = (Map.Entry) it.next();
         System.out.println(pair.getKey() + " = " + pair.getValue().size());
         it.remove(); // avoids a ConcurrentModificationException
      }
   }

   public boolean checkSamplesOnPath(List<ArmConfig> samples) {

      Tester t = new Tester();
      for (ArmConfig a : samples) {
         if (t.hasCollision(a, obstacles) || t.hasSelfCollision(a) || !t.fitsBounds(a)) {
            return true;
         }
      }
      return false;
   }

   public HashMap<ArmConfig, List<ArmConfig>> getMap() {
      return map;
   }

   private List<ArmConfig> connectTwoConf(ArmConfig a, ArmConfig b) {
       double MAX_BASE = Tester.MAX_BASE_STEP*80;
       double MAX_JOINT = Tester.MAX_JOINT_STEP*80;


      // Decides primitive steps of base
      double primitiveX = b.getBase().getX() - a.getBase().getX() < 0 ? -1 * MAX_BASE : MAX_BASE;
      double primitiveY = b.getBase().getY() - a.getBase().getY() < 0 ? -1 * MAX_BASE : MAX_BASE;

      // decides primitive steps of joints
      List<Double> primitiveJoint = new ArrayList<>();
      for (int i = 0; i < a.getJointAngles().size(); i++) {
         if (b.getJointAngles().get(i) - a.getJointAngles().get(i) < 0) {
            primitiveJoint.add(-1 * MAX_JOINT);
         }
         else {
            primitiveJoint.add(MAX_JOINT);
         }
      }

      boolean isFinished = false;
      ArmConfig nextArm;
      List<ArmConfig> pathBetween = new ArrayList<>();
      pathBetween.add(a);

      while (!isFinished) {
         isFinished = true;
         nextArm = pathBetween.get(pathBetween.size() - 1);


         double nextX, nextY;
         // calculate new X
         if (Math.abs(b.getBase().getX() - nextArm.getBase().getX()) > MAX_BASE) {
            isFinished = false;
            nextX = nextArm.getBase().getX() + primitiveX;
         }
         else {
            nextX = b.getBase().getX();
         }

         // calculate new y
         if (Math.abs(b.getBase().getY() - nextArm.getBase().getY()) > MAX_BASE) {
            isFinished = false;
            nextY = nextArm.getBase().getY() + primitiveY;
         }
         else {
            nextY = b.getBase().getY();
         }

         List<Double> nextJointAngles = new ArrayList<>();
         for (int i = 0; i < nextArm.getJointAngles().size(); i++) {
            if (Math.abs(b.getJointAngles().get(i) - nextArm.getJointAngles().get(i)) > MAX_JOINT) {
               isFinished = false;
               nextJointAngles.add(nextArm.getJointAngles().get(i) + primitiveJoint.get(i));
            }
            else {
               nextJointAngles.add(b.getJointAngles().get(i));
            }
         }

         // adds new configuration to the path
         pathBetween.add(new ArmConfig(new Point2D.Double(nextX, nextY), nextJointAngles));
      }
      return pathBetween;

   }
}
