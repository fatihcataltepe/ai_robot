
package model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import problem.ArmConfig;
import problem.Obstacle;
import tester.Tester;

/**
 * Created by fatihcataltepe on 01/09/15.
 */
public class Graph {
   private HashMap<ArmConfig, List<ArmConfig>> map;
   private List<Obstacle>                      obstacles;
   private final int                           numOfEdges         = 10;
   private int                                 numOfSamplesOnPath = 10;

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
               boolean checkPath = checkSamplesOnPath(getArmsOnPath(curr, arm));

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

   public List<ArmConfig> getArmsOnPath(ArmConfig a, ArmConfig b) {
      List<ArmConfig> retVal = new ArrayList<>();

      double deltaX = (b.getBase().getX() - a.getBase().getX()) / numOfSamplesOnPath;
      double deltaY = (b.getBase().getY() - a.getBase().getY()) / numOfSamplesOnPath;

      List<Double> deltaAngles = new ArrayList<>();
      for (int i = 0; i < a.getJointAngles().size(); i++) {
         deltaAngles.add((b.getJointAngles().get(i) - a.getJointAngles().get(i)) / numOfSamplesOnPath);
      }

      for (int i = 0; i < numOfSamplesOnPath; i++) {
         double x = a.getBase().getX() + (i * deltaX);
         double y = a.getBase().getY() + (i * deltaY);

         List<Double> jointAngles = new ArrayList<>();
         for (int j = 0; j < a.getJointAngles().size(); j++) {
            jointAngles.add(a.getJointAngles().get(j) + (i * deltaAngles.get(j)));
         }

         retVal.add(new ArmConfig(new Point2D.Double(x, y), jointAngles));
      }
      return retVal;
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
}
