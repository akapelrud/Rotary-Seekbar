# RotarySeekbar
RotarySeekbar (aka. a knob) is an interactiv View for Android that upon touch enlarges the view (_overlay zooming_) thus making it easy to adjust the value of the knob while actually seeing what you are doing. The view responds to any touch within its' bounds, i.e. **not** relying on a precise touch of a tiny knob handle widget (c.f. [Fitt's law](https://en.wikipedia.org/wiki/Fitts%27s_law)). The rotation of the knob is based on amplifying the finger movement based on the distance from the center of the knob, giving the user both fine and coarse control of the value selection. The overlay zooming and movement amplification makes it possible to put compact rotary seekbars (e.g. 48dp x 48dp) in your gui, while still providing sufficient visual feedback and control granularity to the end user.

## Usage
 - Step 1. Add jitpack to your root build.gradle, at the end of the repositories list:
 
    ```
    allprojects {
        repositories {
            ...
	    maven { url 'https://jitpack.io' }
        }
    }
    ```
- Step 2. Add the dependency

    ```
    dependencies {
        // either choose a release (TAG) from github:
        implementation 'com.github.akapelrud:Rotary-Seekbar:TAG'
	// where TAG = 'vMajor.Minor,Micro', e.g. 'v0.2.1'
	
        // or use the current HEAD of e.g. the master branch:
        // implementation 'com.github.akapelrud:Rotary-Seekbar:master-SNAPSHOT'
    }
    ```

### 
<details>
    <summary>Usage for versions prior to v0.2.1 - [Click to show]</summary>
	
    The library can be used by adding a few lines too your project's build.gradle file:  
    ```
    repositories {
        maven {
            url 'https://github.com/akapelrud/Rotary-Seekbar/raw/master/RotarySeekbar/snapshots'
        }
    }
    dependencies {
        // 0.1.0 release
        //implementation 'no.kapelrud:RotaryKnobLibrary:0.1.0:@aar'
        // 0.2.0 release
        implementation 'no.kapelrud:RotarySeekbar:0.2.0:@aar'
    }
    ```
</details>

## Example
First image shows three widget instances in a horizontal layout. One with opening facing left, one facing right, and with no opening. All aspecs of the rotary knob is customizable via xml; like the needle (size, color, length, etc.), the sectors (radial length, color, etc.) and ticks (thickness, length, color, radial size and position, etc.).

![example 1](https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/screenshot_01_cropped.png)

Pressing either of the widgets will enlarge the view of the component, making it easier to choose a value while still having the component visible.

![example 2](https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/screenshot_02_cropped.png)
![example 3](https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/screenshot_03_cropped.png)
![example 4](https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/screenshot_04_cropped.png)
