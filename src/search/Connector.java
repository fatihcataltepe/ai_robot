
package search;

import java.util.ArrayList;
import java.util.List;

import problem.ArmConfig;
import problem.Obstacle;
import tester.Tester;

/**
 * Created by fatihcataltepe on 08/09/15.
 */
public class Connector {
   private List<ArmConfig> pathBetween;
   private Tester          tester;
   private List<Obstacle>  obstacles;
   private Double MAX_BASE_STEP;
   private Double MAX_JOINT_STEP;

   public Connector() {
   }

   public Connector(List<Obstacle> obstacles) {
      this.pathBetween = new ArrayList<>();
      this.tester = new Tester();
      this.obstacles = obstacles;
      this.MAX_BASE_STEP = Tester.MAX_BASE_STEP;
      this.MAX_JOINT_STEP = Tester.MAX_JOINT_STEP;

   }
   public Connector( List<Obstacle> obstacles, double max_base, double max_joint) {
      this.pathBetween = new ArrayList<>();
      this.tester = new Tester();
      this.obstacles = obstacles;
      this.MAX_BASE_STEP = max_base;
      this.MAX_JOINT_STEP = max_joint;

   }

   public List<ArmConfig> connectPath(List<ArmConfig> path) {
      if (path == null || path.size() == 0)
         return path;

      pathBetween.clear();
      pathBetween.add(path.get(0));
//      System.out.println("size of path: " + path.size());

      for (int i = 0; i < path.size() - 1; i++) {
         if (!connectTwoConf(path.get(i + 1))) {
//            System.out.println("Exitting connecting path... " + i);
            return null;
         }
      }
      pathBetween.add(path.get(path.size() - 1));
//      System.out.println("connected path size: " + pathBetween.size());
      return pathBetween;
   }

   private boolean connectTwoConf(ArmConfig b) {

      while (!pathBetween.get(pathBetween.size() - 1).isSimilar(b)) {
         boolean b1 = moveX(b);
         boolean b2 = moveY(b);
         boolean b3 = moveAngles(b);
//         System.out.println("in the while loop" + " " + b1 + " " + b2 + " " + b3);
         if (!b1 && !b2 && !b3) {
//            System.out.println("no available path");
            return false;
         }
      }

      return true;

   }


   private boolean moveX(ArmConfig b) {
      ArmConfig nextArm = new ArmConfig(pathBetween.get(pathBetween.size() - 1));
      double primitiveX = b.getBase().getX() - nextArm.getBase().getX() < 0 ? -1 * this.MAX_BASE_STEP : this.MAX_BASE_STEP;

      if (Math.abs(b.getBase().getX() - nextArm.getBase().getX()) > this.MAX_BASE_STEP) {
         nextArm.changeX(nextArm.getBase().getX() + primitiveX);

      }
      else if (b.getBase().getX() != nextArm.getBase().getX()) {
         nextArm.changeX(b.getBase().getX());
      }
      else {
         return false;
      }

      if (tester.validConfiguration(nextArm,obstacles)) {
         pathBetween.add(nextArm);
         return true;
      }
      return false;

   }

   private boolean moveY(ArmConfig b) {
      ArmConfig nextArm = new ArmConfig(pathBetween.get(pathBetween.size() - 1));
      double primitiveY = b.getBase().getY() - nextArm.getBase().getY() < 0 ? -1 * this.MAX_BASE_STEP : this.MAX_BASE_STEP;

      if (Math.abs(b.getBase().getY() - nextArm.getBase().getY()) > this.MAX_BASE_STEP) {
         nextArm.changeY(nextArm.getBase().getY() + primitiveY);
      }
      else if (nextArm.getBase().getY() != b.getBase().getY()) {
         nextArm.changeY(b.getBase().getY());
      }
      else {
         return false;
      }

      if (tester.validConfiguration(nextArm,obstacles)) {
         pathBetween.add(nextArm);
         return true;
      }
      return false;

   }

   private boolean moveAngles(ArmConfig b) {
      ArmConfig nextArm = new ArmConfig(pathBetween.get(pathBetween.size() - 1));

      // decides primitive steps of joints
      List<Double> primitiveJoint = new ArrayList<>();
      for (int i = 0; i < nextArm.getJointAngles().size(); i++) {
         if (b.getJointAngles().get(i) - nextArm.getJointAngles().get(i) < 0) {
            primitiveJoint.add(-1 * this.MAX_JOINT_STEP);
         }
         else {
            primitiveJoint.add(this.MAX_JOINT_STEP);
         }
      }

      boolean isChanged = false;
      for (int i = 0; i < nextArm.getJointAngles().size(); i++) {
         double initialJoint = nextArm.getJointAngles().get(i);
         if (Math.abs(b.getJointAngles().get(i) - nextArm.getJointAngles().get(i)) > this.MAX_JOINT_STEP) {
            nextArm.changeJoint(i, nextArm.getJointAngles().get(i) + primitiveJoint.get(i));
            if (tester.validConfiguration(nextArm,obstacles)) {
               isChanged = true;
            }
            else {
               nextArm.changeJoint(i, initialJoint);
            }
         }
         else if (nextArm.getJointAngles().get(i) != b.getJointAngles().get(i)) {
            nextArm.changeJoint(i, b.getJointAngles().get(i));
            if (tester.validConfiguration(nextArm,obstacles)) {
               isChanged = true;
            }

            else {
               nextArm.changeJoint(i, initialJoint);
            }
         }

      }

      // adds new configuration to the path
      if (isChanged) {
         pathBetween.add(nextArm);
         return true;
      }
      return false;

   }
}
