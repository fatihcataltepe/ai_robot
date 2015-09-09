
package model;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;

import problem.ArmConfig;
import problem.Obstacle;

/**
 * Created by fatihcataltepe on 01/09/15.
 */
public class Graph {
   private HashMap<ArmConfig, List<ArmConfig>> map;
   private List<Obstacle>                      obstacles;
   private final int                           numOfEdges = 30;

   public Graph(List<ArmConfig> randomArms, List<Obstacle> obstacles) {
      this.obstacles = obstacles;

      map = new HashMap<>();

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
               if (!checkObstacles(curr.getBase(), arm.getBase())) {
                  counter++;
                  map.get(curr).add(arm);
               }
            }
            if (counter >= numOfEdges)
               break;
         }
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

   public HashMap<ArmConfig, List<ArmConfig>> getMap() {
      return map;
   }

}
