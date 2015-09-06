
package tester;

import problem.ArmConfig;

/**
 * Created by fatihcataltepe on 24/08/15.
 */
public class Dummy {
   public static void main(String[] args) {
      ArmConfig a = new ArmConfig("0.7 0.7 0.6 0.2 0.2 0.2");
      ArmConfig b = new ArmConfig("0.7 0.7 0.6 0.2 0.2 0.2");

      System.out.println(a.equals(b));

   }

}
