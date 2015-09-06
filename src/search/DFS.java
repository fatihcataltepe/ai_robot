
package search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import problem.ArmConfig;

/**
 * Created by fatihcataltepe on 06/09/15.
 */
public class DFS {
   private ArmConfig                           root;
   private ArmConfig                           goal;

   private HashMap<ArmConfig, List<ArmConfig>> map;

   private HashSet<ArmConfig>                  visited;
   private Stack<ArmConfig>                    path;

   public DFS(ArmConfig root, ArmConfig goal, HashMap<ArmConfig, List<ArmConfig>> map) {
      this.root = root;
      this.goal = goal;
      this.map = map;

      visited = new HashSet<>();
      path = new Stack<>();
   }

   public List<ArmConfig> search() {
      recursion(root);
      System.out.println("searching is finished");
      System.out.println("path size is: " + path.size());
      return path;
   }

   public boolean recursion(ArmConfig curr) {
      if (!visited.contains(curr)) {
         // System.out.println("searching..." + path.size());
         visited.add(curr);
         path.push(curr);
         if (goal.equals(curr)) {
            System.out.println("found!!");
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
}
