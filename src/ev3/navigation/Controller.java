/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package ev3.navigation;

import common.TransmittableType;
import common.logger.Logger;
import ev3.communication.ComManager;
import lejos.robotics.navigation.*;
import lejos.robotics.pathfinding.Path;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Acts as a bridge to the navigator and provides helpful methods for the robot.
 */
public final class Controller implements MoveListener, NavigationListener {
    private static final String LOG_TAG = Controller.class.getSimpleName();

    private final MyNavigator navigator;

    public Controller(@NotNull MyNavigator navigator) {
        this.navigator = navigator;

        this.navigator.addNavigationListener(this);
        this.navigator.getMoveController().addMoveListener(this);
    }

    public void moveForward() {
        navigator.getMoveController().travel(57, false);
    }

    public void followPath(@NotNull Path path, @Nullable Offset offset) {
        Path newPath = new Path();

        for (Waypoint waypoint : path) {
            if (offset != null) {
                if (waypoint.isHeadingRequired()) {
                    //TODO Think about issue with no heading required and find solution
                    waypoint.setLocation(offset.reverseOffset(waypoint.getPose()));
                } else {
                    Logger.warning(LOG_TAG, "Could not offset because no heading defined");
                }
            }

            if (waypoint.isHeadingRequired()) {
                waypoint = new Waypoint(waypoint.x, waypoint.y, normalize(waypoint.getHeading()));
            }

            newPath.add(waypoint);
        }

        navigator.followPath(newPath);

        ComManager.sendTransmittable(TransmittableType.PATH, navigator.getPath());

        waitForStop();
    }

    public void followPath(@NotNull Path path) {
        followPath(path, null);
    }

    private void waitForStop() {
        while (navigator.isMoving()) {
            ComManager.sendTransmittable(TransmittableType.CURRENT_POSE, navigator.getPoseProvider().getPose());

            Thread.yield();
        }
    }

    //TODO Consider removing and instead working directly with the Navigator
    @Contract(pure = true)
    private static double normalize(double heading) {
        while (heading > 180) heading -= 360;
        while (heading <= -180) heading += 360;
        return heading;
    }

    @Contract(pure = true)
    @NotNull
    public Pose getPose() {
        return navigator.getPoseProvider().getPose();
    }

    @Contract(pure = true)
    public MyNavigator getNavigator() {
        return navigator;
    }

    @Override
    public void moveStarted(Move move, MoveProvider moveProvider) {
        Logger.info(LOG_TAG, "Started : " + move.toString());
    }

    @Override
    public void moveStopped(Move move, MoveProvider moveProvider) {
        Logger.info(LOG_TAG, "Stopped : " + move.toString());
    }

    @Override
    public void atWaypoint(Waypoint waypoint, Pose pose, int i) {
        Logger.info(LOG_TAG, "At waypoint : " + waypoint + " pose : " + pose.toString());
    }

    @Override
    public void pathComplete(Waypoint waypoint, Pose pose, int i) {
//        Logger.info(LOG_TAG, "Path complete : " + waypoint + " pose : " + pose.toString());
    }

    @Override
    public void pathInterrupted(Waypoint waypoint, Pose pose, int i) {
        Logger.info(LOG_TAG, "Path interrupted : " + waypoint + " pose : " + pose.toString());
    }
}