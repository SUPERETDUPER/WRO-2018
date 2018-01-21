package hardware;

import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;
import utils.Config;

class DistanceSensor {

    private final static SampleProvider ultrasonicSampleProvider = new EV3UltrasonicSensor(Config.PORT_SENSOR_ULTRASONIC).getDistanceMode();

    public static float getDistance() {
        float[] distanceValues = new float[ultrasonicSampleProvider.sampleSize()];

        ultrasonicSampleProvider.fetchSample(distanceValues, 0);

        return distanceValues[0];
    }
}