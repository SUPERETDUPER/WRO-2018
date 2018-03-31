/*
 * Copyright (c) [2018] [Jonathan McIntosh, Martin Staadecker, Ryan Zazo]
 */

package ev3.communication;

import common.TransmittableType;
import common.logger.Logger;
import lejos.robotics.Transmittable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class PCDataSender implements DataSender {
    private static final String LOG_TAG = PCDataSender.class.getSimpleName();

    @Nullable
    private DataOutputStream dos;

    public PCDataSender(OutputStream outputStream) {
        dos = new DataOutputStream(outputStream);
    }

    public synchronized void sendLogMessage(@NotNull String message) {
        if (dos != null) {
            try {
                dos.writeByte(TransmittableType.LOG.ordinal());
                dos.writeUTF(message);
                dos.flush();
            } catch (IOException e) {
                close();
                Logger.error(LOG_TAG, "Failed to send log message");
            }
        }
    }

    public synchronized void sendTransmittable(@NotNull TransmittableType eventType, @NotNull Transmittable transmittable) {
        if (dos != null) {
            try {
                dos.writeByte(eventType.ordinal());
                transmittable.dumpObject(dos);
                dos.flush();
            } catch (IOException e) {
                close();
                Logger.error(LOG_TAG, "Failed to send transmittable type : " + eventType.name());
            }
        }
    }

    @Override
    public void close() {
        if (dos != null) {
            try {
                dos.close();
            } catch (IOException e) {
                Logger.error(LOG_TAG, "Could not close data output stream");
            }
        }
    }
}