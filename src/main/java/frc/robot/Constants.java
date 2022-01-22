// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;

/**
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants {

    public static final double ksVolts = 0.131;
    public static final double ksVoltSecondsPerMeter =  4.03;
    public static final double kaVoltSecondsSquaredPerMeter = 0.521;

    public static final double kTrackWidthMeters = 0.702;
    public static final DifferentialDriveKinematics kDriveKinematics = new DifferentialDriveKinematics(kTrackWidthMeters);

    public static final double kMaxSpeedMetersPerSecond = 2.5;
    public static final double kMaxAccelerationMetersPerSecondSquared = 19;

    public static final double kRamseteB = 2;
    public static final double kRamseteZeta = 0.7;

    public static final double kP = 1.89;

    public static final double kDistPerRot = (3.072/100);

}
