/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package EV3.localization;

import Common.Logger;
import Common.Particles.Particle;
import EV3.navigation.Readings;
import lejos.robotics.navigation.Move;
import lejos.robotics.navigation.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used by the Pose Provider to run operations on its set of particles
 */
class ParticleSet {
    private static final String LOG_TAG = ParticleSet.class.getSimpleName();

    private static final float STARTING_RADIUS_NOISE = 4;
    private static final float STARTING_HEADING_NOISE = 3;

    private static final float DISTANCE_NOISE_FACTOR = 0.008F;
    private static final float ANGLE_NOISE_FACTOR = 0.04F;

    private static final int NUM_PARTICLES = 200;
    private static final int MAX_RESAMPLE_ITERATIONS = 1000;

    private static final float VALUE_OF_PREVIOUS_WEIGHT = 0.1F;

    private static final Random random = new Random();

    private List<Particle> particles;

    ParticleSet(@NotNull Pose startingPose) {
        this.particles = ParticleSet.getNewParticleSet(startingPose);
    }

    List<Particle> getParticles() {
        return particles;
    }

    synchronized void moveParticles(@NotNull Move move) {
        List<Particle> newParticles = new ArrayList<>(NUM_PARTICLES);

        for (Particle particle : particles) {
            Pose newPose = Util.movePose(particle.getPose(), move, ANGLE_NOISE_FACTOR, DISTANCE_NOISE_FACTOR);
            newParticles.add(new Particle(newPose, particle.getWeight()));

        }

        particles = newParticles;
    }

    synchronized void weightParticles(@NotNull Readings readings) {
        List<Particle> newParticles = new ArrayList<>(NUM_PARTICLES);

        for (Particle particle : particles) {
            float readingWeight = readings.calculateWeight(particle.getPose());
            float newWeight = VALUE_OF_PREVIOUS_WEIGHT * particle.getWeight() + (1 - VALUE_OF_PREVIOUS_WEIGHT) * readingWeight;
            newParticles.add(new Particle(particle.getPose(), newWeight));
        }

        particles = newParticles;
    }

    synchronized void resample() {
        ArrayList<Particle> newParticles = new ArrayList<>(ParticleSet.NUM_PARTICLES);

        for (int i = 0; i < MAX_RESAMPLE_ITERATIONS; i++) {

            //Copy particles with weight higher than random
            for (Particle particle : particles) {
                if (particle.getWeight() >= Math.random()) {
                    newParticles.add(particle);

                    if (newParticles.size() == ParticleSet.NUM_PARTICLES) {
                        break;
                    }
                }
            }

            if (newParticles.size() == ParticleSet.NUM_PARTICLES) {
                break;
            }
        }

        if (newParticles.size() == 0) {
            Logger.error(LOG_TAG, "Bad resample ; totally lost");
        } else if (newParticles.size() < ParticleSet.NUM_PARTICLES) {
            for (int i = newParticles.size(); i < ParticleSet.NUM_PARTICLES; i++) {
                newParticles.add(newParticles.get(i % newParticles.size()));
            }

            Logger.warning(LOG_TAG, "Bad resample; had to duplicate existing particles");
        }

        particles = newParticles;
    }

    /**
     * Estimate currentPose from weighted average of the particles
     * Calculate statistics
     */
    @NotNull
    synchronized Pose estimateCurrentPose() {
        float totalWeights = 0;

        float estimatedX = 0;
        float estimatedY = 0;
        float estimatedAngle = 0;

        for (Particle particle : particles) {
            estimatedX += (particle.getPose().getX() * particle.getWeight());
            estimatedY += (particle.getPose().getY() * particle.getWeight());
            estimatedAngle += (particle.getPose().getHeading() * particle.getWeight());

            totalWeights += particle.getWeight();
        }

        estimatedX /= totalWeights;
        estimatedY /= totalWeights;
        estimatedAngle /= totalWeights;

        // Normalize angle
        while (estimatedAngle > 180) estimatedAngle -= 360;
        while (estimatedAngle < -180) estimatedAngle += 360;

        return new Pose(estimatedX, estimatedY, estimatedAngle);
    }

    /**
     * Generates a new particle set around a specific point with weights 0.5
     */
    @NotNull
    private static ArrayList<Particle> getNewParticleSet(@NotNull Pose centerPose) {
        ArrayList<Particle> newParticles = new ArrayList<>(NUM_PARTICLES);

        for (int i = 0; i < NUM_PARTICLES; i++) {
            float radiusFromCenter = STARTING_RADIUS_NOISE * (float) random.nextGaussian();

            float thetaInRad = (float) (2 * Math.PI * Math.random());  //Random angle between 0 and 2pi

            float x = centerPose.getX() + radiusFromCenter * (float) Math.cos(thetaInRad);
            float y = centerPose.getY() + radiusFromCenter * (float) Math.sin(thetaInRad);

            float heading = centerPose.getHeading() + STARTING_HEADING_NOISE * (float) random.nextGaussian();

            newParticles.add(new Particle(x, y, heading, 0.5F));
        }

        return newParticles;
    }
}