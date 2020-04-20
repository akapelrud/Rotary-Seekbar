# RotarySeekbar
RotarySeekbar (aka. a knob) is an interactive View for Android that upon touch enlarges the view (_overlay zooming_) thus making it easy to adjust the value of the knob while actually seeing what you are doing. The view responds to any touch within its' bounds, i.e. **not** relying on a precise touch of a tiny knob handle widget (c.f. [Fitt's law](https://en.wikipedia.org/wiki/Fitts%27s_law)). The rotation of the knob is based on amplifying the finger movement based on the distance from the center of the knob, giving the user both fine and coarse control of the value selection. The overlay zooming and movement amplification makes it possible to put compact rotary seekbars (e.g. 48dp x 48dp) in your gui, while still providing sufficient visual feedback and control granularity to the end user.

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
- Step 3. Make sure that you have defined the relevant material design colors in your app's `res/styles.xml` (at least `?attr/primaryColorVariant` is used in Release 1.0. Others might be added later on.):

    ```
    <item name="colorPrimaryVariant">@color/colorPrimaryVariant</item>
    ```
- Step 4. Add the RotarySeekbar to your layout file, c.f. [the attribute definition](../master/RotarySeekbar/src/main/res/values/attrs.xml) and the demo app's layout file: [DEMO layout](../master/Examples/Palette/app/src/main/res/layout/activity_main.xml)
- Step 5. Implement the `RotarySeekbar.OnValueChangeListener` interface, and register using `setValueChangeListener()`.

    ```
    public interface OnValueChangedListener {
        void onValueChanged(RotarySeekbar sourceSeekbar, float value);
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
The first image shows the demo application with 16 differently styled RotarySeekbars. All of these have been customized through the `.xml` layout file.

<img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app.png" width="50%" />

Pressing either of the widgets will enlarge the view of the component, making it easier to choose a value while still having the component visible.

<img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_01.png" width="23%" /> <img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_02.png" width="23%" /> <img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_03.png" width="23%" /> <img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_04.png" width="23%" />
