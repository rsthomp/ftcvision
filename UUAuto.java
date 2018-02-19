import ftc.vision.ImageProcessorResult;

@Autonomous(name = "UUAuto", group = "Autonomous")
//@Disabled
public class UUAuto extends LinearOpMode {

    /* Declare OpMode members. */
    Spitfire robot;
    boolean foundBall; // For computer vision
    private ElapsedTime runtime = new ElapsedTime();
    Toggle delaySeconds = new Toggle(); //To add delay
    Toggle mode = new Toggle(); // To select end position
    int autoMode;
    int delay;
    boolean lineFound = false; //Records whether the white line has been found


    public void runOpMode() throws InterruptedException {
        //Initializes the robot
        robot = new Spitfire();
        robot.init(hardwareMap);

        //FrameGrabber allows the robot to take pictures with the phone
        FrameGrabber frameGrabber = FtcRobotControllerActivity.frameGrabber;
        robot.gyro.calibrate();

        while (true) {
            //This is the menu for selecting autonomous options
            idle();
            if (gamepad1.a) break;
            delaySeconds.onPress(gamepad1.b);
            mode.onPress(gamepad1.y);

            //Set delay
            delay = (delaySeconds.count % 16) * 2;
            autoMode = mode.count % 6;

            //Set team color
            if (gamepad1.x) {
                robot.setBlueTeam(true);
            }
            if (gamepad1.b) {
                robot.setBlueTeam(false);
            }
            telemetry.addData("alliance", (robot.blueTeam ? "blue" : "red"));
            telemetry.addData("delay (B)", delay + " seconds");

            //Wait for gyro to calibrate
            telemetry.addData("gyro", robot.gyro.isCalibrating() ? "not ready" : "ready");
            telemetry.addData("ready (A)", "NO");

            //Set auto behavior
            if(autoMode == 0){
                telemetry.addData("Mode:", "Center Park");
            } else if (autoMode == 1){
                telemetry.addData("Mode:", "Ramp Park");
            } else if(autoMode == 2){
                telemetry.addData("Mode:", "Only Shoot");
            } else if (autoMode == 3){
                telemetry.addData("Mode:", "Shoot and Cap");
            } else if (autoMode == 4){
                telemetry.addData("Mode:", "Defense");
            }
            else{
                telemetry.addData("Mode:", "Nothing");
            }
            telemetry.update();

            //Positions whisker sensors
            if(robot.angleR.getVoltage() > 0){
                robot.potR.setPosition(.4);
            }
            else robot.potR.setPosition(.5);

            if(robot.angleL.getVoltage() < 4.95){
                robot.potL.setPosition(.6);
            }
            else robot.potL.setPosition(.518);

        }
        telemetry.addData("ready (A)","yes");
    /*    while (robot.gyro.isCalibrating()) {
            telemetry.addData("ready (A)","yes");
            telemetry.update();
            idle();
        }*/
        telemetry.update();
        waitForStart();

        runtime.reset();

        //Guarantee that the clutch is not engaged
        robot.shift.setPosition(0);

        robot.gyro.resetZAxisIntegrator();

        //The robot will wait here if we add a delay
        sleep(delay * 1000);

        //Score two balls

        robot.fcolor.enableLed(false);
        robot.fcolor.enableLed(true);
        robot.rcolor.enableLed(false);
        if (autoMode == 3) drive(0, .3, new DistanceStop(1300));
        robot.move(0,0);
        shoot();

        //Cap Ball Only Mode
        if(autoMode == 2) {
            sleep(5000);
            drive(-115, 0, new TimeStop(time, 1000));
            drive(-115, -.4, new DistanceStop(7000), new TimeStop(time, 8000));
            stop();
        }
        //Ramp Park Only Mode
        if(autoMode == 3) {
            //robot.setkP(-.006);
            //sleep(10000);

          //  drive(0, .5, new DistanceStop(3050));
            robot.move(0, 0);
            stop();
        }
        if(autoMode == 5){
            robot.servoAuto();
            drive(25, 0, new TimeStop(time, 750));
            drive(25, .3, new DistanceStop(800));
            robot.move(0, 0);
            drive(-30, .3, new DistanceStop(1200), new TimeStop(time, 3000));
            robot.move(0,0);
           // drive(0, -.3, new DistanceStop(1700));
            robot.move(0,0);
            robot.servoOut();
            robot.sweep.setPower(1);

            sleep(500);
            robot.sweep.setPower(0);

            sleep(500);

           // robot.tilt.setPosition(1);
            sleep(800);
           // robot.tilt.setPosition(.5);

            stop();

        }

       if(robot.blueTeam) {

           //Robot navigates to wall
           robot.setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
           robot.blueServo.setPosition(0);
           robot.redServo.setPosition(0);

           robot.move(0,0);
           //robot.setkP(-.002);

           potRPosition(2.1);
           drive(-36, .2, new DistanceStop(1200) );
           drive(-36, .5, new DistanceStop(3000));
           drive(-26, .35, new DistanceStop(3500), new touchStop(robot.angleR.getVoltage(),.15) );
           drive(-36, .45, new DistanceStop(3500), new touchStop(robot.angleR.getVoltage(),.15) );


           lineFound = false;

            //Robot aligns with wall
           TimeStop s = new TimeStop(time, 4000);
           while(!lineFound && !s.stop() && opModeIsActive()){
               telemetry.addData("fcolor", robot.fcolor.green());
               telemetry.update();
               s.update(time);
               if((robot.fcolor.green()>=6) && robot.fcolor.green() < 200) lineFound = true;
               if(robot.angleR.getVoltage() < 3.5) robot.move(.13, .13);
               else{
                   robot.move(.14, .145);
                   robot.potR.setPosition(.575);
               }

           }
           robot.potR.setPosition(.5);
           robot.move(0,0);
           robot.servoOut();



           //Robot follows wall, scores beacons
           TimeStop stop = new TimeStop(time,3500);
           while (!stop.stop()) {
               if (!opModeIsActive()) break;
               stop.update(time);
               robot.beaconScore();
               robot.move(-.12, -.10);

           }
           if(lineFound) {
               stop = new TimeStop(time, 1000);
           }
           else{
               stop = new TimeStop(time, 500);
           }
           while (!stop.stop()) {
               if (!opModeIsActive()) break;
               stop.update(time);
               robot.beaconScore();
               robot.move(-.26, -.23);

           }

           stop = new TimeStop(time,4000);
           while (!stop.stop()) {
               if (!opModeIsActive()) break;
               stop.update(time);
               robot.beaconScore();
               robot.move(-.14, -.10);

           }
           robot.move(0, 0);
           robot.servoOut();

           robot.gyro.resetZAxisIntegrator();


           if(autoMode == 0) {
               //Cap Ball and Center Park
               drive(100, .5, new DistanceStop(5000));
               robot.move(-.3, .3);
               sleep(1000);

           }
           else if(autoMode == 1) {
               //Ramp Park
               robot.setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
               drive(25, .3, new DistanceStop(1700));
               robot.servoInit();
               drive(-25, 0, new TimeStop(time, 850));
               drive(-5, -.35, new DistanceStop(3500));
           }

           if(autoMode == 4){
               //Defensive Mode
               robot.servoOut();
               drive(40, .25, new DistanceStop(1000));
               drive(40, .8, new DistanceStop(3400));
               robot.servoInit();
           }
           if(autoMode == 5){
               robot.servoOut();
               drive(22.5, .5, new DistanceStop(2400));
           }


           //Deploys sweeper wings
           robot.servoOut();
           robot.sweep.setPower(1);
          // robot.tilt.setPosition(0);
           robot.potR.setPosition(0);
           sleep(1000);
           robot.sweep.setPower(0);
          // robot.tilt.setPosition(.5);
           robot.potR.setPosition(.5);
           sleep(500);

           
           double heading = chase(frameGrabber, false);
           robot.sweep.setPower(1);
           drive(heading, .5, new DistanceStop(4500), new TimeStop(time, 3000));
           drive(heading, -.5, new DistanceStop(4000), new TimeStop(time, 3000));
           heading = chase(frameGrabber, true);
           drive(heading, -.3, new TimeStop(time, 1250));
           shoot(); 
        }
        else {
           robot.rcolor = robot.lcolor;
           //Robot navigates to wall
           robot.setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
           robot.blueServo.setPosition(0);
           robot.redServo.setPosition(0);
           robot.move(0,0);

           potLPosition(-1.7);
           drive(27, .2, new DistanceStop(1200) );
           drive(27, .5, new DistanceStop(3000));
           drive(17, .35, new DistanceStop(3500), new touchStop(robot.angleL.getVoltage(),.15) );
           drive(27, .45, new DistanceStop(3500), new touchStop(robot.angleL.getVoltage(),.15) );

           //Robot aligns self with the wall
         //  drive(22, .35, new DistanceStop(1000));
           TimeStop s = new TimeStop(time, 4500);
           while(!lineFound && !s.stop() &&opModeIsActive()){
               //telemetry.addData("color", robot.fcolor.green());
             //  telemetry.update();
               s.update(time);
               if((robot.fcolor.green()>=4) && robot.fcolor.green() < 200) lineFound = true;
               if(robot.angleL.getVoltage() > 4.95 - 1.7) robot.move(.13, .13);
               else{
                   robot.move(.15, .145);
                   robot.potL.setPosition(.525);
               }
           }

           robot.move(0,0);
           robot.servoOut();


           TimeStop stop = new TimeStop(time,4000);
           while (!stop.stop()) {
               if (!opModeIsActive()) break;
               stop.update(time);
               robot.beaconScore();
               robot.move(-.10, -.12);

           }


           //Robot follows the wall
           if(lineFound) {
               stop = new TimeStop(time, 1000);
           }
           else{
               stop = new TimeStop(time, 500);
           }
           while (!stop.stop()) {
               if (!opModeIsActive()) break;
               stop.update(time);
               robot.beaconScore();
               robot.move(-.23, -.25);

           }

           stop = new TimeStop(time,4000);
           while (!stop.stop()) {
               if (!opModeIsActive()) break;
               stop.update(time);
               robot.beaconScore();
               robot.move(-.09, -.095);

           }
           robot.move(0, 0);
           robot.servoOut();
           robot.potL.setPosition(.518);

           robot.gyro.resetZAxisIntegrator();
           if(autoMode == 0) {
               //Cap Ball and Center Park
               drive(-100, .5, new DistanceStop(5000));
           }
           else if(autoMode == 1) {
               //Ramp Park
               robot.setDriveMode(DcMotor.RunMode.RUN_USING_ENCODER);
               drive(-25, .3, new DistanceStop(1500));
               robot.servoInit();
               drive(25, 0, new TimeStop(time, 750));
               drive(5, -.35, new DistanceStop(3500));
           }

           if(autoMode == 4){
               //Defensive mode
               robot.servoOut();
               drive(-38, .25, new DistanceStop(1000));
               drive(-38, .8, new DistanceStop(3700));
               robot.servoInit();
              // drive(0, 0, new TimeStop(time, 1000));
             //  drive(0, .3, new DistanceStop(500));

           }
           if(autoMode == 5){
               robot.servoOut();
               drive(-22.5, .3, new DistanceStop(2400));
               robot.move(0,0);
           }
           
           double heading =- chase(frameGrabber, false);
           robot.sweep.setPower(1);
           drive(heading, .5, new DistanceStop(4500), new TimeStop(time, 3000));
           drive(heading, -.5, new DistanceStop(4500), new TimeStop(time, 3000));
           heading = chase(frameGrabber, true);
           drive(heading, -.3, new TimeStop(time, 1250));
           shoot();
           */

        //Deploys sweeper wings
        robot.servoOut();
        robot.sweep.setPower(1);
        // robot.tilt.setPosition(0);
        robot.potL.setPosition(1);
        sleep(1000);
        robot.sweep.setPower(0);
        // robot.tilt.setPosition(.5);
        robot.potL.setPosition(.5);
        sleep(500);
    }
    //For computer vision - this method allows the robot to find and retrieve a ball based on color
    //It's currently not being used
    public double chase (FrameGrabber frameGrabber, boolean shoot) throws InterruptedException{
        robot.gyro.resetZAxisIntegrator();
        idle();
        if(!shoot) {
            if (robot.blueTeam) FtcRobotControllerActivity.updateFrameGrabber(100, true, true);
            else FtcRobotControllerActivity.updateFrameGrabber(100, true, false);
        }
        else {
            if(robot.blueTeam) FtcRobotControllerActivity.updateFrameGrabber(50, false, true);
            else FtcRobotControllerActivity.updateFrameGrabber(50, false, false);
        }
        while(opModeIsActive()) {
            frameGrabber.grabSingleFrame(); //Tell it to grab a frame
            while (!frameGrabber.isResultReady()&&opModeIsActive()) { //Wait for the result
                Thread.sleep(35); //sleep for 35 milliseconds from 5
            }
            if(!opModeIsActive()) break;
            //Get the result
            ImageProcessorResult imageProcessorResult = frameGrabber.getResult();
            BlueResult result = (BlueResult) imageProcessorResult.getResult();
            int x = result.getX();
            if (!shoot){
                if((x < 95 && x > (robot.blueTeam ? 65 : 75))) {
                    robot.move(0, 0);
                    foundBall = true;
                    return robot.gyro.getIntegratedZValue();
                }
            else if (robot.gyro.getIntegratedZValue() >= 120 || robot.gyro.getIntegratedZValue() <= -120) {
                robot.move(0, 0);
                    foundBall = false;
                return 0;
            }
            }
        else{
                if((x < 90 && x > (robot.blueTeam? 60 : 70))) {
                    robot.move(0, 0);
                    return robot.gyro.getIntegratedZValue();
                }
                else if (robot.gyro.getIntegratedZValue() >= 200 || robot.gyro.getIntegratedZValue() <= -200) {
                    robot.move(0, 0);
                    return 100;
                }

        }
                if(robot.blueTeam) robot.move(-.45, .45);
                else robot.move(45, -.45);
        }
      return 0;
    }



    //Shoots two balls into the center vortex
    public void shoot() throws InterruptedException{
        //Spins up flywheel
        robot.servoOut();
        sleep(600);
        robot.pal.setMaxSpeed(5000);
        robot.par.setMaxSpeed(5000);
        robot.pal.setPower(1);
        robot.par.setPower(1);

        //Drives into position

        sleep(1000);
        //Shoots
        robot.load.setPosition(1);
        sleep(600);
        robot.load.setPosition(0);

        sleep(1000);
        robot.load.setPosition(1);
        sleep(600);

        //Powers down
        robot.clear.setPosition(0);
        robot.sweep.setPower(0);
        robot.load.setPosition(0);
        robot.pal.setPower(0);
        robot.par.setPower(0);
        robot.servoInit();
    }

    //This method allows the robot to drive on a given gyro heading
    public boolean drive(double targetAngle, double throttle, stopCondition... stops) throws InterruptedException {

        robot.startMove(time);

        //Records distance traveled using odometry
        int pastRPos = robot.rf.getCurrentPosition();
        int pastLPos = robot.lf.getCurrentPosition();
        double distanceShift = 0;
        while (stops.length > 0) {
            if (!opModeIsActive()) return false;
       //     telemetry.addData("enc", robot.angleR.getVoltage());
     //       telemetry.update();
            distanceShift =(Math.abs(robot.rf.getCurrentPosition() - pastRPos) + Math.abs(robot.lf.getCurrentPosition() - pastLPos));
            //Stops the robot if it's traveled far enough or if it times out
            for (stopCondition s : stops) {
                if (s.type().equals("distance")) {
                    s.update(distanceShift);
                }
                if (s.type().equals("time")) {
                    s.update(time);
                }
                if(s.type().equals("range")){
                   if(robot.blueTeam) s.update(robot.rdist.getVoltage());
                    else s.update((robot.rdist.getVoltage()));
                }
                if(s.type().equals("ods")){
                    s.update(robot.ods.getLightDetected());
                }
                if(s.type().equals("touch"))
                    if(robot.blueTeam)s.update(robot.angleR.getVoltage());
                    else s.update(robot.angleL.getVoltage());
                if(s.type().equals("angle"))
                    s.update(robot.gyroHeading());
                if (s.stop()) {
                    idle();
                    robot.move(0,0);
                    return false;
                }
                //Detects whether the line in front of the beacon has been crossed
                if(robot.fcolor.green()>=11) lineFound = true;

            }

            pastRPos = robot.rf.getCurrentPosition();
            pastLPos = robot.lf.getCurrentPosition();

            robot.moveHeading(targetAngle, throttle, time);
            idle();
        }
        return true;
    }



    private void potLPosition(double position){
        double rPos = robot.angleL.getVoltage() + position;
        if(position < 4.95){
            //    telemetry.addData("Moving", "true");
            //   telemetry.update();
            while(robot.angleL.getVoltage() > rPos){
                robot.potL.setPosition(.35);
            }
        }

        robot.potL.setPosition(.518);
        return;
    }

    private void potRPosition(double position){
        double rPos = robot.angleR.getVoltage() + position;
        if(position > 0){
            //    telemetry.addData("Moving", "true");
            //   telemetry.update();
            while(robot.angleR.getVoltage() < rPos){
                robot.potR.setPosition(.65);
            }
        }
        else if(position < 0){
            while(robot.angleR.getVoltage() > rPos){
                robot.potR.setPosition(.35);
            }
        }
        robot.potR.setPosition(.5);
        return;
    }

}