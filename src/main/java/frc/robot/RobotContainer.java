// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.xml.stream.events.DTD;

import org.opencv.ml.DTrees;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.TrajectoryUtil;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.commands.ExampleCommand;
import frc.robot.subsystems.ExampleSubsystem;
import frc.robot.subsystems.drivetrain.Drivetrain;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems and commands are defined here...
  private final ExampleSubsystem m_exampleSubsystem = new ExampleSubsystem();

  private final ExampleCommand m_autoCommand = new ExampleCommand(m_exampleSubsystem);
  private final Drivetrain dt = new Drivetrain();
  private Trajectory trajectory = new Trajectory();
  //private String trajectoryJSON = "paths/MyPath.wpilib.json";
  private RobotContainer m_robotContainer;
  private XboxController xboxController = new XboxController(0);
  private PIDController rightPID= new PIDController(Constants.kP, 0, 0);
  private PIDController leftPID= new PIDController(Constants.kP, 0, 0);
  private Field2d m_field = new Field2d();
  
  
  

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    

    // Configure the button bindings
    configureButtonBindings();
    dt.setDefaultCommand(
      // A split-stick arcade command, with forward/backward controlled by the left
      // hand, and turning controlled by the right.
      new RunCommand(
          () ->
              dt.arcadeDrive(xboxController.getLeftY(), xboxController.getRightX()),
          dt));
}
   

  /**
   * Use this method to define your button->command mappings. Buttons can be created by
   * instantiating a {@link GenericHID} or one of its subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
   * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
   */
  private void configureButtonBindings() {

  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    //The voltage constraint makes sure the robot doesn't exceed a certain voltage during runtime.
    var autoVoltageConstraint = 
      new DifferentialDriveVoltageConstraint(
        new SimpleMotorFeedforward(
          Constants.ksVolts, 
          Constants.ksVoltSecondsPerMeter,
          Constants.kaVoltSecondsSquaredPerMeter),
        Constants.kDriveKinematics, 10);

    //Gives the trajectory the constants determined in characterization.
    TrajectoryConfig config = 
      new TrajectoryConfig(
        Constants.kMaxSpeedMetersPerSecond,
        Constants.kMaxAccelerationMetersPerSecondSquared)
        .setKinematics(Constants.kDriveKinematics )
        .addConstraint(autoVoltageConstraint);    

    //Generates the actual path with given points.
    trajectory = TrajectoryGenerator.generateTrajectory(
      new Pose2d(0, 0, new Rotation2d(0)),
            // Pass through these two interior waypoints, making an 's' curve path
            List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
            // End 3 meters straight ahead of where we started, facing forward
            new Pose2d(3, 0, new Rotation2d(0)),
            // Pass config
            config);
    
    /*
    try {
      Path trajectoryPath = Filesystem.getDeployDirectory().toPath().resolve(trajectoryJSON); 

      trajectory = TrajectoryUtil.fromPathweaverJson(trajectoryPath);
    } catch (IOException ex) {
      DriverStation.reportError("Unable to open trajectory: " + trajectoryJSON, ex.getStackTrace());
    }*/
   
    



    //Ramsete is a trajectory tracker and auto corrector. We feed it parameters into a ramsete command
    //so that it constantly updates and corrects the trajectory auto.
    RamseteCommand ramseteCommand = new RamseteCommand(
      trajectory, 
      dt::getPose, //Gets the translational and rotational position of the robot.
        new RamseteController(Constants.kRamseteB, Constants.kRamseteZeta),//Uses constants of 2.0 and 0.7
        new SimpleMotorFeedforward( //Feedforward controller to control the motors before they move
          Constants.ksVolts,  
          Constants.ksVoltSecondsPerMeter,
          Constants.kaVoltSecondsSquaredPerMeter),
          Constants.kDriveKinematics, 
          dt::getWheelSpeeds,
          leftPID,
          rightPID,
          dt::tankDriveVolts,
          dt);

    dt.resetOdometry(trajectory.getInitialPose());
    return ramseteCommand.andThen(() -> dt.tankDriveVolts(0,0));
  }

  public void update() {
    SmartDashboard.putNumber("Encoder", dt.getAverageEncoderDistance());
    SmartDashboard.putNumber("Heading", dt.getHeading());
    SmartDashboard.putNumber("Left Voltage", dt.getLeftVoltage());
    SmartDashboard.putNumber("Right Voltage", dt.getRightVoltage());
    SmartDashboard.putData("Right PID Controller",  rightPID);
    SmartDashboard.putData("Left PID Controller", leftPID);
    
  }

  
 
}
