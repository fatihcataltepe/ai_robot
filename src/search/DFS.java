
package search;

import java.util.*;

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
      System.out.println("Looking for a proper path...");

      recursion(root);
      System.out.println("searching is finished");
      System.out.println("path size is: " + path.size());
      return makePathShorter();
   }

   private boolean recursion(ArmConfig curr) {
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

   private List<ArmConfig> makePathShorter(){
      List<ArmConfig> originalPath = path;
      List<ArmConfig> shorterPath = new ArrayList<>();

      for (int i = 0; i < originalPath.size(); i++) {
         ArmConfig curr = originalPath.get(i);
         List<ArmConfig> currChildren = map.get(curr);

         shorterPath.add(curr);
         for (int j = originalPath.size()-1; j > i; j--) {
            ArmConfig test = originalPath.get(j);
            if(currChildren.contains(test)){
               i = j - 1;
               break;
            }
         }
      }
      System.out.println("path size reduced to: "+ shorterPath.size() );
      return shorterPath;

    }
}