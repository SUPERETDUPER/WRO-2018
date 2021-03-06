/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package pc;

import common.TestUtils;
import common.TransmittableType;
import ev3.communication.PCDataSender;
import lejos.robotics.navigation.Pose;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

class ConnectionTest {

    @Test
    void listen() {
        final Pose poseToSend = new Pose(10, 10, 10);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PCDataSender dataSender = new PCDataSender(out);
        dataSender.sendTransmittable(TransmittableType.CURRENT_POSE, poseToSend);

        ByteArrayInputStream input = new ByteArrayInputStream(out.toByteArray());

        DataReceiver dataReceiver = new DataReceiver(input, new DataReceiver.DataReceivedListener() {
            @Override
            public void dataReceived(TransmittableType event, DataInputStream dis) throws IOException {
                Assertions.assertEquals(event, TransmittableType.CURRENT_POSE);

                Pose loadedPose = new Pose();
                loadedPose.loadObject(dis);

                TestUtils.assertPoseEqual(poseToSend, loadedPose);
            }
        });

        dataReceiver.read();
    }
}