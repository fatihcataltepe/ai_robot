
package problem;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import model.Graph;
import search.Connector;
import search.DFS;
import tester.Tester;

/**
 * This class represents the specifications of a given problem and solution; that is, it provides a structured representation of the
 * contents of a problem text file and associated solution text file, as described in the assignment specifications. This class
 * doesn't do any validity checking - see the code in tester.Tester for this.
 */
public class ProblemSpec {

   /**
    * True iff a problem is currently loaded
    */
   private boolean                             problemLoaded          = false;
   /**
    * True iff a solution is currently loaded
    */
   private boolean                             solutionLoaded         = false;

   /**
    * The number of joints
    */
   private int                                 jointCount;
   /**
    * The initial configuration
    */
   private ArmConfig                           initialState;
   /**
    * The goal configuration
    */
   private ArmConfig                           goalState;
   /**
    * The obstacles
    */
   private List<Obstacle>                      obstacles;

   /**
    * The path taken in the solution
    */
   private List<ArmConfig>                     path;

   private List<ArmConfig>                     randomArms;
   private HashMap<ArmConfig, List<ArmConfig>> map;

   private static final int                    numOfTrialBtwObstacles = 1;    // 10
   private static int                          numOfBaseBtwObstacles  = 15;   // 20
   private static final int                    numOfTrialOnBase       = 30;   // 5
   private static final int                    numOfUniformBase       = 150;  // 50

   /**
    * Loads a problem from a problem text file.
    *
    * @param filename
    *           the path of the text file to load.
    * @throws IOException
    *            if the text file doesn't exist or doesn't meet the assignment specifications.
    */
   public void loadProblem(String filename) throws IOException {
      problemLoaded = false;
      solutionLoaded = false;
      BufferedReader input = new BufferedReader(new FileReader(filename));
      String line;
      int lineNo = 0;
      Scanner s;
      try {
         line = input.readLine();
         lineNo++;
         initialState = new ArmConfig(line);

         line = input.readLine();
         lineNo++;
         goalState = new ArmConfig(line);

         if (initialState.getJointCount() != goalState.getJointCount()) {
            throw new IOException("Mismatch between initial and goal joint count.");
         }

         jointCount = initialState.getJointCount();
         line = input.readLine();
         lineNo++;
         s = new Scanner(line);
         int numObstacles = s.nextInt();
         s.close();

         obstacles = new ArrayList<Obstacle>();
         for (int i = 0; i < numObstacles; i++) {
            line = input.readLine();
            lineNo++;
            obstacles.add(new Obstacle(line));
         }

         problemLoaded = true;
      }
      catch (InputMismatchException e) {
         throw new IOException(String.format("Invalid number format on line %d: %s", lineNo, e.getMessage()));
      }
      catch (NoSuchElementException e) {
         throw new IOException(String.format("Not enough tokens on line %d", lineNo));
      }
      catch (NullPointerException e) {
         throw new IOException(String.format("Line %d expected, but file ended.", lineNo));
      }
      finally {
         input.close();
      }

      // solve the problem
      long startTime = System.currentTimeMillis();
      int counter = 0;
      while (randomArms == null || randomArms.size() == 0) {
         System.out.println(String.format("%d th trial in progress", ++counter));

          if (numOfBaseBtwObstacles == 30)
          numOfBaseBtwObstacles = 15;
          else
          numOfBaseBtwObstacles = 30;

         solveProblem();
         if (System.currentTimeMillis() - startTime > 60000) {
            System.out.println("TIME IS UP, NO VALID PATH FOUND!");
            System.exit(0);
         }

      }

      System.out.println("found in: " + (System.currentTimeMillis() - startTime) + " millseconds");
   }

   /**
    * Loads a solution from a solution text file.
    *
    * @param filename
    *           the path of the text file to load.
    * @throws IOException
    *            if the text file doesn't exist or doesn't meet the assignment specifications.
    */
   public void loadSolution(String filename) throws IOException {
      if (!problemLoaded) {
         return;
      }
      solutionLoaded = false;
      BufferedReader input = new BufferedReader(new FileReader(filename));
      String line;
      int lineNo = 0;
      Scanner s;
      try {
         line = input.readLine();
         lineNo++;
         s = new Scanner(line);
         int pathLength = s.nextInt() + 1;
         s.close();

         path = new ArrayList<ArmConfig>();
         for (int i = 0; i < pathLength; i++) {
            line = input.readLine();
            lineNo++;
            path.add(new ArmConfig(line));
         }
         solutionLoaded = true;
      }
      catch (InputMismatchException e) {
         throw new IOException(String.format("Invalid number format on line %d: %s", lineNo, e.getMessage()));
      }
      catch (NoSuchElementException e) {
         throw new IOException(String.format("Not enough tokens on line %d", lineNo));
      }
      catch (NullPointerException e) {
         throw new IOException(String.format("Line %d expected, but file ended.", lineNo));
      }
      finally {
         input.close();
      }
   }

   /**
    * Saves the current solution to a solution text file.
    *
    * @param filename
    *           the path of the text file to save to.
    * @throws IOException
    *            if the text file doesn't exist or doesn't meet the assignment specifications.
    */
   public void saveSolution(String filename) throws IOException {
      if (!problemLoaded || !solutionLoaded) {
         return;
      }
      String ls = System.getProperty("line.separator");
      FileWriter output = new FileWriter(filename);
      output.write(String.format("%d%s", path.size() - 1, ls));
      for (ArmConfig cfg : path) {
         output.write(cfg + ls);
      }
      output.close();
   }

   /**
    * Assumes that a path can be taken directly from the initial configuration to the goal.
    */
   public void assumeDirectSolution() {
      if (!problemLoaded) {
         return;
      }
      path = new ArrayList<ArmConfig>();
      path.add(initialState);
      path.add(goalState);
      solutionLoaded = true;
   }

   /**
    * Returns the true total cost of the currently loaded solution.
    *
    * @return the true total cost of the currently loaded solution.
    */
   public double calculateTotalCost() {
      double cost = 0;
      ArmConfig c0 = path.get(0);
      for (int i = 1; i < path.size(); i++) {
         ArmConfig c1 = path.get(i);
         cost += c0.totalDistance(c1);
         c0 = c1;
      }
      return cost;
   }

   /**
    * Returns the number of joints in each configuration.
    *
    * @return the number of joints in each configuration.
    */
   public int getJointCount() {
      return jointCount;
   }

   /**
    * Returns the initial configuration.
    *
    * @return the initial configuration.
    */
   public ArmConfig getInitialState() {
      return initialState;
   }

   /**
    * Returns the goal configuration.
    *
    * @return the goal configuration.
    */
   public ArmConfig getGoalState() {
      return goalState;
   }

   /**
    * Returns the list of obstacles.
    *
    * @return the list of obstacles.
    */
   public List<Obstacle> getObstacles() {
      return new ArrayList<Obstacle>(obstacles);
   }

   /**
    * Sets the path.
    *
    * @param path
    *           the new path.
    */
   public void setPath(List<ArmConfig> path) {
      if (!problemLoaded) {
         return;
      }
      this.path = new ArrayList<ArmConfig>(path);
      solutionLoaded = true;
   }

   /**
    * Returns the solution path.
    *
    * @return the solution path.
    */
   public List<ArmConfig> getPath() {
      return new ArrayList<ArmConfig>(path);
   }

   /**
    * Returns whether a problem is currently loaded.
    *
    * @return whether a problem is currently loaded.
    */
   public boolean problemLoaded() {
      return problemLoaded;
   }

   /**
    * Returns whether a solution is currently loaded.
    *
    * @return whether a solution is currently loaded.
    */
   public boolean solutionLoaded() {
      return solutionLoaded;
   }

   private void solveProblem() {
      Tester t = new Tester();
      randomArms = new ArrayList<>();
      System.out.println("Creating random confs between obstacles...");

      // creates random points between obstacles
      for (int k = 0; k < numOfTrialBtwObstacles; k++) {
         for (int i = 0; i < obstacles.size() - 1; i++) {
            Obstacle curr = obstacles.get(i);

            for (int j = i + 1; j < obstacles.size(); j++) {
               Obstacle next = obstacles.get(j);
               int collisions = getNumberOfCollision(curr.getCenter(), next.getCenter());

               if (collisions <= 2) {
                  // selects 20 points per each pair of obstacle
                  for (int m = 0; m < numOfBaseBtwObstacles; m++) {
                     Point2D.Double p1 = curr.createRandomPointInBetween();
                     Point2D.Double p2 = next.createRandomPointInBetween();

                     Point2D.Double p3 = new Point2D.Double((p1.getX() + p2.getX()) / 2, (p1.getY() + p2.getY()) / 2);

                     // selects 5 configurations per a base between two obstacles
                     for (int l = 0; l < numOfTrialOnBase; l++) {
                        ArmConfig temp = ArmConfig.factory(p3, jointCount);
                        if (!t.hasCollision(temp, obstacles) && t.fitsBounds(temp) && !t.hasSelfCollision(temp)) {
                           randomArms.add(temp);
                           break;
                        }

                     }
                  }
               }

            }
         }
      }

      // creates uniformly distributed random configurations
      System.out.println("Creating random confs in free space...");

      int counter = 0;
      while (counter < numOfUniformBase) {
         ArmConfig temp = ArmConfig.factory(jointCount);
         if (!t.hasCollision(temp, obstacles) && t.fitsBounds(temp) && !t.hasSelfCollision(temp)) {
            randomArms.add(temp);
            counter++;
         }
      }

      randomArms.add(initialState);
      randomArms.add(goalState);

      System.out.println("creating graph...");

      Graph graph = new Graph(randomArms, obstacles);
      map = graph.getMap();

      System.out.println("implementing dfs...");

      DFS dfs = new DFS(initialState, goalState, map, obstacles);
      randomArms = dfs.search();

      System.out.println("creating primitive steps");

      Connector connector = new Connector(obstacles);
      List<ArmConfig> solution = connector.connectPath(randomArms);

      if (solution != null) {
         setPath(solution);
         try {
            saveSolution("solution");
         }
         catch (IOException e) {
            e.printStackTrace();
         }
      }

      else {
         solution = null;
         randomArms = null;
      }

   }


   public HashMap<ArmConfig, List<ArmConfig>> getMap() {
      return map;
   }

   private int getNumberOfCollision(Point2D curr, Point2D child) {
      int counter = 0;
      for (Obstacle o : obstacles) {
         Rectangle2D r = o.getRect();
         Line2D l = new Line2D.Double(curr.getX(), curr.getY(), child.getX(), child.getY());
         if (l.intersects(r))
            counter++;
      }
      return counter;
   }
}
