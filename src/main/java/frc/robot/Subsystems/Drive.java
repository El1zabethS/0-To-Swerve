// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.Subsystems;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import edu.wpi.first.math.util.Units;

public class Drive extends SubsystemBase {
  public static double WHEEL_RADIUS_METERS = Units.inchesToMeters(1.5);
  public static double WHEEL_DIAMETER_METERS = Units.inchesToMeters(3);
  public static double[] [] ORIGIN_POINT = new double[4] [2]; //x,y coordinate of module point
  public static double ANGLE_RADIANS = 0;
  public static double[] VELOCITY_M_PER_SEC = new double [2];
  public static double ANGLE_VELOCITY_DEGREES_PER_SEC = 0;

  public static double[] CURRENT_STEERING_ANGLE_DEGREES = new double[4];
  public static double[] CURRENT_WHEEL_ANGULAR_VELOCITY_DEGREES_PER_SEC = new double[4];

  public static double[] CALCULATED_STEERING_ANGLES_DEG = new double[4];
  public static double[] CALCULATED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC = new double[4];

  public static double[] RECOMMENDED_STEERING_ANGLES_DEG = new double[4];
  public static double[] RECOMMENDED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC = new double[4];
  /** Creates a new Drive. */
  public Drive() {
  }
  
  
  public void ASSIGN_VEHICLE_VALUES_FOR_SQUARE_VEHICLE(double d, double wheel_d){
    this.WHEEL_DIAMETER_METERS = wheel_d; //set wheel diameter
    this.ORIGIN_POINT[0][0] = d; //mod. 0 is in +x +y
    this.ORIGIN_POINT[0][1] = d;

    this.ORIGIN_POINT[1][0] = -d; //mod. 1 is in -x +y
    this.ORIGIN_POINT[1][1] = d;

    this.ORIGIN_POINT[2][0] = -d; //mod. 2 is in -x -y
    this.ORIGIN_POINT[2][1] = -d;

    this.ORIGIN_POINT[3][0] = d; //mod. 3 is in +x -y
    this.ORIGIN_POINT[3][1] = d;
  }

  public void initializeSteeringAnglesAndWheelSpeeds(double[] STEERING_ANG_DEG,
  double[]WHEEL_SPEEDS_DEG_PER_SEC){
    int i;
    for(i=0; i<4; ++i)
    {
      this.CURRENT_STEERING_ANGLE_DEGREES[i] = STEERING_ANG_DEG[i];
    }
  }
  public void CALCULATION_SWERVE_STEERING_AND_SPEED()
  {
    int i;
    double sv, cv;

    cv = Math.cos(this.ANGLE_RADIANS);
    sv = Math.sin(this.ANGLE_RADIANS);

    //calc veh vel in veh coord sys
    double[] V0_IN_VEH = new double[2];
    V0_IN_VEH[0] = cv* this.VELOCITY_M_PER_SEC[0] + sv* this.VELOCITY_M_PER_SEC[1];
    V0_IN_VEH[1] = -sv* this.VELOCITY_M_PER_SEC[0] + cv* this.VELOCITY_M_PER_SEC[1];

      double[]v_i = new double[2];
      double speed;

      for (i=0; i<4; ++i)
      {
        v_i[0] = V0_IN_VEH[0] - Units.degreesToRadians(this.ANGLE_VELOCITY_DEGREES_PER_SEC) * this.ORIGIN_POINT[i][1];
        v_i[1] = V0_IN_VEH[1] + Units.degreesToRadians(this.ANGLE_VELOCITY_DEGREES_PER_SEC) * this.ORIGIN_POINT[1][0];

          speed = Math.sqrt(v_i[0]*v_i[0] + v_i[1]*v_i[1]);
          this.CALCULATED_STEERING_ANGLES_DEG[i] = Math.atan2(v_i[1], v_i[0]) * Units.degreesToRadians(speed);
          this.CALCULATED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i] = Units.degreesToRadians(speed / (this.WHEEL_DIAMETER_METERS/2.0));
      }
      }

      public void RECOMMENDED_SWERVE_STEERING_AND_SPEED()
      {
        //Compares calculated steering angles w/ current steering angles
        //and make a recommendation, Hetter to steer 180 degrees opposite
        //from recommended value then spin the wheel in the negative direction.

        int i;
        double ANGLE_DIFF_DEG;

        for (i=0; i<4; ++i)
        {
          ANGLE_DIFF_DEG = this.CALCULATED_STEERING_ANGLES_DEG[i]
          - this.CURRENT_STEERING_ANGLE_DEGREES[i];

            while (ANGLE_DIFF_DEG < 180.0)
              ANGLE_DIFF_DEG += 360.0;
            while (ANGLE_DIFF_DEG > 180.0)
              ANGLE_DIFF_DEG -= 360.0;

              if (Math.abs(ANGLE_DIFF_DEG) < 90.0)
              {
                this.RECOMMENDED_STEERING_ANGLES_DEG[i] =
                this.CURRENT_STEERING_ANGLE_DEGREES[i] + ANGLE_DIFF_DEG;
                this.RECOMMENDED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i] =
                this.CALCULATED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i];
              }
              else
              {
                this.RECOMMENDED_STEERING_ANGLES_DEG[i] =
                  this.CURRENT_STEERING_ANGLE_DEGREES[i] + ANGLE_DIFF_DEG - 180.0; //or + 180.0
                this.RECOMMENDED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i] =
                  - this.CALCULATED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i];
                while(this.RECOMMENDED_STEERING_ANGLES_DEG[i] - this.CURRENT_STEERING_ANGLE_DEGREES[i] > 180.0)
                  this.RECOMMENDED_STEERING_ANGLES_DEG[i] -= 360.0;
                while(this.RECOMMENDED_STEERING_ANGLES_DEG[i] - this.CURRENT_STEERING_ANGLE_DEGREES[i] < -180.0)
                  this.RECOMMENDED_STEERING_ANGLES_DEG[i] += 360.0;
              }
        }

          
      }

      void print_result()
      {
        int i;
        System.out.println("------------------");
        System.out.println("\nVehicle Velocity Inputs (from joystick input):");
        System.out.printf("\tvelocity of point 0 = %.4f, %.4 m/sec\n",
        this.VELOCITY_M_PER_SEC[0], this.VELOCITY_M_PER_SEC[1]);
        System.out.printf("\tangular velocity = %.2f deg/sec\n", this.ANGLE_VELOCITY_DEGREES_PER_SEC);
        System.out.printf("current steering ang");
        for (i=0; i<4; ++i)
        {
          System.out.printf("\t\t%.2f", this.CURRENT_STEERING_ANGLE_DEGREES[i]);
        }
            System.out.printf("\tdeg\n");
        
      System.out.printf("calculated steering ang");
      for (i=0 ; i<4 ; ++i)
      {
          System.out.printf("\t\t%.2f", this.CALCULATED_STEERING_ANGLES_DEG[i]) ;  
      }
      System.out.printf("\tdeg\n");
      
      System.out.printf("recommend steering ang");
      for (i=0 ; i<4 ; ++i)
      {
          System.out.printf("\t\t%.2f", this.RECOMMENDED_STEERING_ANGLES_DEG[i]) ;  
      }
      System.out.printf("\tdeg\n\n");
      
      System.out.printf("current wheel speed");
      for (i=0 ; i<4 ; ++i)
      {
          System.out.printf("\t\t%.1f", this.CURRENT_WHEEL_ANGULAR_VELOCITY_DEGREES_PER_SEC[i]) ;  
      }
      System.out.printf("\tdeg/sec\n");
      
      System.out.printf("calculated wheel speed");
      for (i=0 ; i<4 ; ++i)
      {
          System.out.printf("\t\t%.1f", this.CALCULATED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i]) ;  
      }
      System.out.printf("\tdeg/sec\n");
      
      System.out.printf("recommend wheel speed");
      for (i=0 ; i<4 ; ++i)
      {
          System.out.printf("\t\t%.1f", this.RECOMMENDED_WHEEL_ANGULAR_VELOCITY_DEG_PER_SEC[i]) ;  
      }
      System.out.printf("\tdeg/sec\n") ;

  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
  }   
}
