
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

   public Graph(List<ArmConfig> freeStates, List<Obstacle> obstacles) {
      this.obstacles = obstacles;

      map = new HashMap<>();

      for (int i = 0; i < freeStates.size() - 1; i++) {
         ArmConfig curr = freeStates.get(i);
         if (map.get(curr) == null)
            map.put(curr, new ArrayList<>());

         for (int j = i + 1; j < freeStates.size(); j++) {
            ArmConfig child = freeStates.get(j);
            if (!checkObstacles(curr.getBase(), child.getBase())) {
               map.get(curr).add(child);
            }
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

   public static void printMap(Map mp) {
      Iterator it = mp.entrySet().iterator();
      while (it.hasNext()) {
         Map.Entry<ArmConfig, List<ArmConfig>> pair = (Map.Entry) it.next();
         System.out.println(pair.getKey() + " = " + pair.getValue().size());
         it.remove(); // avoids a ConcurrentModificationException
      }
   }

   public HashMap<ArmConfig, List<ArmConfig>> getMap() {
      return map;
   }
}
