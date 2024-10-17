// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems.drive;

import frc.robot.Constants;
import frc.robot.Constants.RobotConstants.DrivetrainConstants;
import edu.wpi.first.math.util.Units;

/** Add your docs here. */
public class Drive {
    // TODO:change to english(it is in spanish)

    // chassis

    double[][] swervemodule_positions = new double[4][2]; // x,y coord of module point in vehicle coord sys
    // module 0 is in +x,+y quadrant
    // module 1 is in -x,+y quadrant
    // module 2 is in -x,-y quadrant
    // module 3 is in +x,-y quadrant

    public double angle;// angle of the rotation of the robot, the actual angle relative to the 0 degrees of the field 
                        //  in radiants(gyro)(rad)
    public double[] centerVel = new double[2];// Velocity where the center of the robot is going in m/s from the center (in the direction where the robot is x and y component)
                                             
    public double angularVel;// veloity in degrees/s of how fast the robot rotates

    // wheel
    public double[] steeringAngle = new double[4];//same thing for angle but for each wheel instead (in degrees) for 4 modules 
    public double[] wheelAngularVel = new double[4]; // velocity of the wheel to rotate in degrees per sec for 4 modules 
                                                   

    public double[] lastSteeringAngle = new double[4];// Last Steering Angle. previous angles for 4 modules 
    public double[] lastWheelAngularVel = new double[4]; // Last wheel angular velocity in degrees/secxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx for 4 modules 

    public double[] RecommendedSteeringAngles = new double[4];// angle in degrees for 4 modules, the angle that we wamt to move on the wheel
    public double[] RecommendedWheelAngularVel = new double[4];// degrees/secxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx  for 4 modules 

    public void assign_veh_values_for_square_vehicle(double d, double wheel_d) { // Distaqnce between wheel pivot and teh center of the robot, wheel_d is wheel diameter
        // set the wheel diameter
        DrivetrainConstants.WHEEL_RADIUS_METERS = wheel_d;

        // define the locations of the four swerve modules in the xv, yv coord sy
        //             # : 0 is x and y is 1 
        this.origin_pt[0][0] = d; // module 0 is in the +x,+y quadrant
        this.origin_pt[0][1] = d;

        this.origin_pt[1][0] = -d; // module 1 is in the -x,+y quadrant
        this.origin_pt[1][1] = d;

        this.origin_pt[2][0] = -d; // module 2 is in the -x,-y quadrant
        this.origin_pt[2][1] = -d;

        this.origin_pt[3][0] = d; // module 3 is in the +x,-y quadrant
        this.origin_pt[3][1] = -d;
    }

    public void initialize_steering_angles_and_wheel_speeds(double[] steering_ang_deg,double[] wheel_speeds_deg_per_sec) { // for testing
        int i;
        for (i = 0; i < 4; ++i) {
            this.steeringAngle[i] = steering_ang_deg[i];
            this.wheelAngularVel[i] = wheel_speeds_deg_per_sec[i];
        }
    }

    public void calc_swerve_steering_and_speed() {
        int i;
        double sinValue, cosValue;

        cosValue = Math.cos(this.angle); // cos of the angle between where is the robot looking at and the 0 degrees (this amgle is from the gyro)
        sinValue = Math.sin(this.angle); // cos of the angle between where is the robot looking at and the 0 degrees (this angle is from the gyro)

        // calculate the robot velocity in the vehicle coord sys
        double[] initialVeloity = new double[2];// in m/s

        initialVeloity[0] = cosValue * this.centerVel[0] + sinValue * this.centerVel[1];// x component for the velocity here we are getting Vx 
        initialVeloity[1] = -sinValue * this.centerVel[0] + cosValue * this.centerVel[1];// y velocity for the velocity here we are getting Vy

        double[] NewVel = new double[2]; // x(0) and y(1) component
        double speed;
        

        for (i = 0; i < 4; ++i) {
            NewVel[0] = initialVeloity[0] - Units.degreesToRadians(this.angularVel) * this.origin_pt[i][1]; // x component of the velocity * our angular velocity in radiants * our distance 
            NewVel[1] = initialVeloity[1] + Units.degreesToRadians(this.angularVel) * this.origin_pt[i][0]; // rad per sec

            speed = Math.sqrt(NewVel[0] * NewVel[0] + NewVel[1] * NewVel[1]);//calculate the speed with pythagorean theorem
            this.lastSteeringAngle[i] = Units.radiansToDegrees(Math.atan2(NewVel[1], NewVel[0]));
            this.lastWheelAngularVel[i] = Units.radiansToDegrees(speed / (0.1016 / 2)); //0.1016 is the wheel diameter in meters  converting from rad per meter to degrees per meter 
            //TODO: CHANGE YOUR 0.1016/2 TO A VARIABLE OR CONSTANT CALLED RADIUS 
        }
    }

    public void recommended_swerve_stering_and_speed() {
        int i;
        double differentialAngle;// the angle of difference in degress

        for (i = 0; i < 4; ++i) {
            differentialAngle = this.lastSteeringAngle[i] - this.steeringAngle[i];// the angle that we want - the angle that we have in order to get the angle that we need to translate 
            // bound the difference between -180 and +180 degrees to find the most efficient move 
            while (differentialAngle < 180.0) {
                differentialAngle += 360.0;
            }
            while (differentialAngle > 180.0) {
                differentialAngle -= 360.0;
            }

            if (Math.abs(differentialAngle) < 90.0) {
                //to find the best and most efficient way to ratate wthe wheel 
                this.RecommendedSteeringAngles[i] = this.steeringAngle[i] + differentialAngle;
                this.RecommendedWheelAngularVel[i] = this.lastWheelAngularVel[i];
            } 
            else 
            {
                // will add 180 to calculated steering angle and rotate the wheel in the negaive
                // direction
                this.RecommendedSteeringAngles[i] = this.steeringAngle[i] + differentialAngle - 180;// it can be - it doesn't matter

                this.RecommendedWheelAngularVel[i] = - this.lastWheelAngularVel[i];

                while (this.RecommendedSteeringAngles[i] - this.steeringAngle[i] > 180.0){
                    this.RecommendedSteeringAngles[i] -= 360;
                }
                while (this.RecommendedSteeringAngles[i] - this.steeringAngle[i] < -180.0){
                    this.RecommendedSteeringAngles[i] += 360;
                }



                
            }
        }
    }



        
    public void print_results()
    {
        int i ;
        System.out.println("------------------") ;
        System.out.println("\nVehicle Velocity Inputs (from joystick input):") ;
        System.out.printf("\tvelocity of point 0 = %.4f, %.4f m/sec\n",
                           this.centerVel[0], this.centerVel[1]) ;
        System.out.printf("\tangular velocity = %.2f deg/sec\n", this.angularVel) ;
        System.out.printf("vehicle ang = %.2f degrees (from IMU sensor)\n\n", Units.radiansToDegrees(angle)) ;
        
        System.out.println("\t\t\t\tmodule 0\tmodule 1\tmodule 2\tmodule3") ;
        System.out.printf("current steering ang") ;
        for (i=0 ; i<4 ; ++i)
        {
            System.out.printf("\t\t%.2f", this.steeringAngle[i]) ;  
        }
        System.out.printf("\tdeg\n") ;
        
        System.out.printf("calculated steering ang") ;
        for (i=0 ; i<4 ; ++i)
        {
            System.out.printf("\t\t%.2f", this.lastSteeringAngle[i]) ;  
        }
        System.out.printf("\tdeg\n") ;
        
        System.out.printf("recommend steering ang") ;
        for (i=0 ; i<4 ; ++i)
        {
            System.out.printf("\t\t%.2f", this.RecommendedSteeringAngles[i]) ;  
        }
        System.out.printf("\tdeg\n\n") ;
        
        System.out.printf("current wheel speed") ;
        for (i=0 ; i<4 ; ++i)
        {
            System.out.printf("\t\t%.1f", this.wheelAngularVel[i]) ;  
        }
        System.out.printf("\tdeg/sec\n") ;
        
        System.out.printf("calculated wheel speed") ;
        for (i=0 ; i<4 ; ++i)
        {
            System.out.printf("\t\t%.1f", this.lastWheelAngularVel[i]) ;  
        }
        System.out.printf("\tdeg/sec\n") ;
        
        System.out.printf("recommend wheel speed") ;
        for (i=0 ; i<4 ; ++i)
        {
            System.out.printf("\t\t%.1f", this.RecommendedWheelAngularVel[i]) ;  
        }
        System.out.printf("\tdeg/sec\n") ;

    }
}

class main {
    public static void notmain( String args[] ) {
        
        Drive veh = new Drive();
        veh.assign_veh_values_for_square_vehicle(0.3302, 0.1016) ;
        
        double[] initial_steering_deg = {42.5, 33.6, 18.1, 21.9} ;
        double[] initial_wheel_speed_deg_per_sec = {1.56, 11.3, 4.2, 10.4} ;
        veh.initialize_steering_angles_and_wheel_speeds(initial_steering_deg,
                                            initial_wheel_speed_deg_per_sec) ;
        
        // Example Case 1:
        // set the current vehicle angle
        veh.angle = Units.degreesToRadians( 30.0)  ;
        
        // set the desired velocity (linear and angular for the vehicle)
        veh.centerVel[0] =  0.5 ;
        veh.centerVel[1] =  1.1 ;
        veh.angularVel  = 20.0 ;
        
        veh.calc_swerve_steering_and_speed() ;
        veh.recommended_swerve_stering_and_speed() ;
                
        veh.print_results() ;  // print results of case 1     
        
        // Example Case 2: (translation case)
        // set the current vehicle angle
        veh.angle = Units.degreesToRadians(30.0 ) ;
        
        // set the desired velocity (linear and angular for the vehicle)
        veh.centerVel[0] = 1.1 * Math.cos(Units.degreesToRadians(60.0)) ;
        veh.centerVel[1] = 1.1 * Math.sin(Units.degreesToRadians(60.0)) ;
        veh.angularVel  = 0.0 ;
        
        veh.calc_swerve_steering_and_speed() ;
        veh.recommended_swerve_stering_and_speed() ;
        
        veh.print_results() ;  // print results of case 2 
        
        // Example Case 3: (recommend reverse wheel rotation based on current steering)
        // set the current vehicle angle
        veh.angle = Units.degreesToRadians(30.0 );
        
        // set the desired velocity (linear and angular for the vehicle)
        veh.centerVel[0] = -1.1 * Math.cos(Units.degreesToRadians(60.0)) ;
        veh.centerVel[1] = -1.1 * Math.sin(Units.degreesToRadians(60.0)) ;
        veh.angularVel  = -25.0 ;
        
        veh.calc_swerve_steering_and_speed() ;
        veh.recommended_swerve_stering_and_speed() ;
        
        veh.print_results() ;  // print results of case 3 
    }

}

