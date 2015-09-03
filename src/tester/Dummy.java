
package tester;

import java.awt.*;
import java.awt.geom.Line2D;

/**
 * Created by fatihcataltepe on 24/08/15.
 */
public class Dummy {
   public static void main(String[] args) {
      Point a = new Point(0, 0);
      Point b = new Point(0, 9);

      Point c = new Point(-2, 2);
      Point d = new Point(3, 2);

      Rectangle r1 = new Rectangle(0, 0, 100, 50);
      Line2D l1 = new Line2D.Float(25, -20, 25, 100);
      System.out.println("l1.intsects(r1) = " + l1.intersects(r1));
//      System.out.println(checkIntersection(a, b, c, d));
   }

   public static boolean checkIntersection(Point a, Point b, Point c, Point d) {
      double dif = b.getX() - a.getX();
      if(dif == 0) dif = 0.000001;
      double m1 = (b.getY() - a.getY()) / (dif);
      double c1 = a.getY() - (a.getX() * m1);

      dif = d.getX() - c.getX();
      if(dif == 0) dif = 0.000001;
      double m2 = (d.getY() - c.getY()) / (dif);
      double c2 = c.getY() - (c.getX() * m2);

      double dm = m2 - m1;
      double dc = c1 - c2;

      double res = dc / dm;
      System.out.println(m1 + " " + c1);
      System.out.println(m2 + " " + c2);
      System.out.println(res);

      if (res > Math.min(a.getX(), b.getX()) && res < Math.max(a.getX(), b.getX()) && res > Math.min(c.getX(), d.getX()) && res < Math.max(c.getX(),
                                                                                                                                           d.getX()))
         return true;
      return false;
   }
}
