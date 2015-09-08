
package search;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import problem.ArmConfig;
import tester.Tester;

/**
 * Created by fatihcataltepe on 08/09/15.
 */
public class Connector {
   private List<ArmConfig> path;
   private List<ArmConfig> pathBetween;
   private Tester          tester;

   public Connector() {
   }

   public Connector(List<ArmConfig> path) {
      this.path = path;
      this.pathBetween = new ArrayList<>();
      this.tester = new Tester();

   }

   public List<ArmConfig> connectPath() {
      if (path == null || path.size() == 0)
         return path;

      pathBetween.add(path.get(0));

      for (int i = 0; i < path.size() - 1; i++) {
         connectTwoConf(path.get(i), path.get(i + 1));
      }

      System.out.println("connected path size: " + pathBetween.size());
      return pathBetween;
   }

   private void connectTwoConf(ArmConfig a, ArmConfig b) {

      // Decides primitive steps of base
      double primitiveX = b.getBase().getX() - a.getBase().getX() < 0 ? -1 * Tester.MAX_BASE_STEP : Tester.MAX_BASE_STEP;
      double primitiveY = b.getBase().getY() - a.getBase().getY() < 0 ? -1 * Tester.MAX_BASE_STEP : Tester.MAX_BASE_STEP;

      // decides primitive steps of joints
      List<Double> primitiveJoint = new ArrayList<>();
      for (int i = 0; i < a.getJointAngles().size(); i++) {
         if (b.getJointAngles().get(i) - a.getJointAngles().get(i) < 0) {
            primitiveJoint.add(-1 * Tester.MAX_JOINT_STEP);
         }
         else {
            primitiveJoint.add(Tester.MAX_JOINT_STEP);
         }
      }

      boolean isFinished = false;
      ArmConfig nextArm;

      while (!isFinished) {
         isFinished = true;
         nextArm = pathBetween.get(pathBetween.size() - 1);

         double nextX, nextY;
         // calculate new X
         if (Math.abs(b.getBase().getX() - nextArm.getBase().getX()) > Tester.MAX_BASE_STEP) {
            isFinished = false;
            nextX = nextArm.getBase().getX() + primitiveX;
         }
         else {
            nextX = b.getBase().getX();
         }

         // calculate new y
         if (Math.abs(b.getBase().getY() - nextArm.getBase().getY()) > Tester.MAX_BASE_STEP) {
            isFinished = false;
            nextY = nextArm.getBase().getY() + primitiveY;
         }
         else {
            nextY = b.getBase().getY();
         }

         List<Double> nextJointAngles = new ArrayList<>();
         for (int i = 0; i < nextArm.getJointAngles().size(); i++) {
            if (Math.abs(b.getJointAngles().get(i) - nextArm.getJointAngles().get(i)) > Tester.MAX_JOINT_STEP) {
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

   }
}
