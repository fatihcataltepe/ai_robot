
package search;

import java.util.*;

import problem.ArmConfig;
import problem.Obstacle;
import tester.Tester;

/**
 * Created by fatihcataltepe on 06/09/15.
 */
public class DFS {
   private ArmConfig                           root;
   private ArmConfig                           goal;

   private HashMap<ArmConfig, List<ArmConfig>> map;

   private HashSet<ArmConfig>                  visited;
   private Stack<ArmConfig>                    path;
   private List<Obstacle>                      obstacles;
   private Connector                           connector;

   public DFS(ArmConfig root, ArmConfig goal, HashMap<ArmConfig, List<ArmConfig>> map, List<Obstacle> obstacles) {
      this.root = root;
      this.goal = goal;
      this.map = map;
      this.obstacles = obstacles;
      this.connector = new Connector(obstacles, Tester.MAX_BASE_STEP * 100, Tester.MAX_JOINT_STEP * 100);

      visited = new HashSet<>();
      path = new Stack<>();
   }

   public List<ArmConfig> search() {

      if(recursion(root) == false){
       return null;
      }
      return makePathShorter();
   }

   private boolean recursion(ArmConfig curr) {
      if (path.isEmpty()) {
         path.push(curr);
      }
      if (!visited.contains(curr) && chechPathUsingConnector(path.peek(), curr)) {
            visited.add(curr);
            path.push(curr);

         if (goal.equals(curr)) {
            return true;
         }

         for (ArmConfig child : map.get(curr)) {
            if (recursion(child))
               return true;
         }
         path.pop();
      }
      return false;

   }

   private List<ArmConfig> makePathShorter() {
      List<ArmConfig> originalPath = path;
      List<ArmConfig> shorterPath = new ArrayList<>();

      for (int i = 0; i < originalPath.size(); i++) {
         ArmConfig curr = originalPath.get(i);
         List<ArmConfig> currChildren = map.get(curr);

         shorterPath.add(curr);
         for (int j = originalPath.size() - 1; j > i; j--) {
            ArmConfig test = originalPath.get(j);
            if (currChildren.contains(test) && chechPathUsingConnector(curr,test)) {
               i = j - 1;
               break;
            }
         }
      }
      return shorterPath;

   }

   public boolean chechPathUsingConnector(ArmConfig a, ArmConfig b) {
      ArrayList<ArmConfig> list = new ArrayList<>();
      list.add(a);
      list.add(b);
      Connector connector = new Connector(obstacles, Tester.MAX_BASE_STEP * 5, Tester.MAX_JOINT_STEP * 5);
      if (connector.connectPath(list) == null) {
         return false;
      }
      return true;

   }
}
