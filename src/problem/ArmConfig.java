
package problem;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import tester.Tester;

/**
 * Represents a configuration of the arm, i.e. base x-y coordinates and joint angles. This class doesn't do any validity checking.
 */
public class ArmConfig {

    /**
     * Length of each link
     */
    public static final double LINK_LENGTH = 0.05;

    /**
     * Base x and y coordinates
     */
    private Point2D base;
    /**
     * Joint angles in radians
     */
    private List<Double> jointAngles;
    /**
     * Links as Line2D
     */
    private List<Line2D> links;

    /**
     * Constructor
     *
     * @param base        Base coordinates (Point2D)
     * @param jointAngles Joint angles in radians
     */
    public ArmConfig(Point2D base, List<Double> jointAngles) {
        this.base = new Point2D.Double(base.getX(), base.getY());
        this.jointAngles = new ArrayList<Double>(jointAngles);

        generateLinks();
    }

    /**
     * Constructs an ArmConfig from a space-separated string. The first two numbers are the x and y coordinates asasof the robot's
     * base. The subsequent numbers are the joint angles in sequential order defined in radians.
     *
     * @param str The String containing the values
     * @throws InputMismatchException
     */
    public ArmConfig(String str) throws InputMismatchException {
        Scanner s = new Scanner(str);
        base = new Point2D.Double(s.nextDouble(), s.nextDouble());
        jointAngles = new ArrayList<Double>();
        while (s.hasNextDouble()) {
            jointAngles.add(s.nextDouble());
        }
        s.close();
        generateLinks();
    }

    /**
     * Copy constructor.
     *
     * @param cfg the configuration to copy.
     */
    public ArmConfig(ArmConfig cfg) {
        base = cfg.getBase();
        jointAngles = cfg.getJointAngles();
        links = cfg.getLinks();
    }

    public static ArmConfig factory(int numOfJoint) {
        Random rand = new Random();
        return factory(new Point2D.Double(rand.nextDouble(), rand.nextDouble()), numOfJoint);
    }

    public static ArmConfig factory(Point2D base, int numOfJoint) {
        List<Double> jointAngles = new ArrayList<>();
        for (int i = 0; i < numOfJoint; i++) {
            jointAngles.add(new Random().nextDouble() * Tester.MAX_JOINT_ANGLE * 2 - Tester.MAX_JOINT_ANGLE);
        }
        return new ArmConfig(base, jointAngles);

    }

    /**
     * Returns a space-separated string representation of this configuration.
     *
     * @return a space-separated string representation of this configuration.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(base.getX());
        sb.append(" ");
        sb.append(base.getY());
        for (Double angle : jointAngles) {
            sb.append(" ");
            sb.append(angle);
        }
        return sb.toString();
    }

    /**
     * Returns the number of joints in this configuration.
     *
     * @return the number of joints in this configuration.
     */
    public int getJointCount() {
        return jointAngles.size();
    }

    /**
     * Returns the base position.
     *
     * @return the base position.
     */
    public Point2D.Double getBase() {
        return new Point2D.Double(base.getX(), base.getY());
    }

    /**
     * Returns the list of joint angles in radians.
     *
     * @return the list of joint angles in radians.
     */
    public List<Double> getJointAngles() {
        return new ArrayList<Double>(jointAngles);
    }

    /**
     * Returns the list of links as Line2D.
     *
     * @return the list of links as Line2D.
     */
    public List<Line2D> getLinks() {
        return new ArrayList<Line2D>(links);
    }

    /**
     * Returns the maximum straight-line distance between the link endpoints in this state vs. the other state, or -1 if the link
     * counts don't match.
     *
     * @param otherState the other state to compare.
     * @return the maximum straight-line distance for any link endpoint.
     */
    public double maxDistance(ArmConfig otherState) {
        if (this.getJointCount() != otherState.getJointCount()) {
            return -1;
        }
        double maxDistance = base.distance(otherState.getBase());
        List<Line2D> otherLinks = otherState.getLinks();
        for (int i = 0; i < links.size(); i++) {
            double distance = links.get(i).getP2().distance(otherLinks.get(i).getP2());
            if (distance > maxDistance) {
                maxDistance = distance;
            }
        }
        return maxDistance;
    }

    /**
     * Returns the total straight-line distance between the link endpoints in this state vs. the other state, or -1 if the link
     * counts don't match.
     *
     * @param otherState the other state to compare.
     * @return the total straight-line distance over all link endpoints.
     */
    public double totalDistance(ArmConfig otherState) {
        if (this.getJointCount() != otherState.getJointCount()) {
            return -1;
        }
        double totalDist = base.distance(otherState.getBase());
        List<Line2D> otherLinks = otherState.getLinks();
        for (int i = 0; i < links.size(); i++) {
            totalDist += links.get(i).getP2().distance(otherLinks.get(i).getP2());
        }
        return totalDist;
    }

    /**
     * Returns the maximum difference in angle between the joints in this state vs. the other state, or -1 if the joint counts don't
     * match.
     *
     * @param otherState the other state to compare.
     * @return the maximum joint angle change for any joint.
     */
    public double maxAngleDiff(ArmConfig otherState) {
        if (this.getJointCount() != otherState.getJointCount()) {
            return -1;
        }
        List<Double> otherJointAngles = otherState.getJointAngles();
        double maxDiff = 0;
        for (int i = 0; i < jointAngles.size(); i++) {
            double diff = Math.abs(jointAngles.get(i) - otherJointAngles.get(i));
            if (diff > maxDiff) {
                maxDiff = diff;
            }
        }
        return maxDiff;
    }

    /**
     * Generates links from joint angles
     */
    private void generateLinks() {
        links = new ArrayList<Line2D>();
        double x1 = base.getX();
        double y1 = base.getY();
        double totalAngle = 0;
        for (Double angle : jointAngles) {
            totalAngle += angle;
            double x2 = x1 + LINK_LENGTH * Math.cos(totalAngle);
            double y2 = y1 + LINK_LENGTH * Math.sin(totalAngle);
            links.add(new Line2D.Double(x1, y1, x2, y2));
            x1 = x2;
            y1 = y2;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ArmConfig armConfig = (ArmConfig) o;

        if (!base.equals(armConfig.base))
            return false;
        return jointAngles.equals(armConfig.jointAngles);

    }

    @Override
    public int hashCode() {
        int result = base.hashCode();
        result = 31 * result + jointAngles.hashCode();
        return result;
    }

    public boolean isSimilar(ArmConfig a) {
        if (Math.abs(a.getBase().getX() - getBase().getX()) > Tester.MAX_BASE_STEP) return false;
        if (Math.abs(a.getBase().getY() - getBase().getY()) > Tester.MAX_BASE_STEP) return false;
        if (a.getJointCount() != getJointCount()) return false;

        for (int i = 0; i < getJointAngles().size(); i++) {
            if (Math.abs(getJointAngles().get(i) - a.getJointAngles().get(i)) > Tester.MAX_JOINT_STEP) return false;
        }
        return true;
    }

    public void changeX(Double x) {
        base.setLocation(x, base.getY());
        generateLinks();
    }

    public void changeY(Double y) {
        base.setLocation(base.getX(), y);
        generateLinks();
    }

    public void changeJoint(int index, Double angle) {
        jointAngles.set(index, angle);
        generateLinks();
    }
}
