/*
Copyright (c) 2016 Robert Atkinson

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Robert Atkinson nor the names of his contributors may be used to
endorse or promote products derived from this software without specific prior
written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESSFOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity;

import ftc.vision.BlueResult;
import ftc.vision.FrameGrabber;
import ftc.vision.ImageProcessorResult;

import static android.R.attr.x;

/**
 * This file contains an minimal example of a Linear "OpMode". An OpMode is a 'program' that runs in either
 * the autonomous or the teleop period of an FTC match. The names of OpModes appear on the menu
 * of the FTC Driver Station. When an selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 * <p>
 * This particular OpMode just executes a basic Tank Drive Teleop for a PushBot
 * It includes all the skeletal structure that all linear OpModes contain.
 * <p>
 * Use Android Studios to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this opmode to the Driver Station OpMode list
 */

@Autonomous(name="Too", group="Autonomous")  // @Autonomous(...) is the other common choice
//@Disabled
public class BlueDetectOp extends LinearOpMode {
    Spitfire bot;
    @Override
    public void runOpMode() throws InterruptedException {
        bot = new Spitfire();
        bot.init(hardwareMap);
        FrameGrabber frameGrabber = FtcRobotControllerActivity.frameGrabber; //Get the frameGrabber
        FtcRobotControllerActivity.updateFrameGrabber(100, true, true);
        waitForStart();
    while(opModeIsActive()){
        frameGrabber.grabSingleFrame(); //Tell it to grab a frame
        while (!frameGrabber.isResultReady()) { //Wait for the result
            Thread.sleep(5); //sleep for 5 milliseconds
        }

        //Get the result
        FtcRobotControllerActivity.updateFrameGrabber(100, true, true);
        ImageProcessorResult imageProcessorResult = frameGrabber.getResult();

        BlueResult result = (BlueResult) imageProcessorResult.getResult();

        int blueX = result.getX();

        FtcRobotControllerActivity.updateFrameGrabber(100, true, false);
         imageProcessorResult = frameGrabber.getResult();

       result = (BlueResult) imageProcessorResult.getResult();

        int redX = result.getX();
        if(Math.abs(blueX) < Math.abs(redX)){
            telemetry.addData("color", "blue");
        }
        else if (Math.abs(redX) < Math.abs(blueX)){
            telemetry.addData("color", "red");
        }
        else{
            telemetry.addData("color", "none");
        }
        telemetry.update();



    }


    }
}