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
	// where TAG = 'vMajor.Minor,Micro'
	
        // or use the current HEAD of a branch e.g. branch:
        // implementation 'com.github.akapelrud:Rotary-Seekbar:master-SNAPSHOT'
        // implementation 'com.github.akapelrud:Rotary-Seekbar:develop-SNAPSHOT'
    }
    ```
- Step 3. Make sure that your app theme is based on one of the material design themes, e.g. `values/styles.xml`:

    ```
    <style name="AppTheme" parent="Theme.MaterialComponents.Light.DarkActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryVariant">@color/colorPrimaryVariant</item>
        <item name="colorOnPrimary">@color/colorOnPrimary</item>
        <item name="colorSecondary">@color/colorSecondary</item>
        <item name="colorSecondaryVariant">@color/colorSecondaryVariant</item>
        <item name="colorOnSecondary">@color/colorOnSecondary</item>
    </style>
    ```
- Step 4. Add the RotarySeekbar to your layout file, c.f. [the attribute definition](../master/RotarySeekbar/src/main/res/values/attrs.xml) and the demo app's layout file: [DEMO layout](../master/Examples/Palette/app/src/main/res/layout/activity_main.xml)
- Step 5. Implement the `RotarySeekbar.OnValueChangeListener` interface, and register using `setOnValueChangeListener()`.

    ```
    public interface OnValueChangedListener {
        void onValueChanged(RotarySeekbar sourceSeekbar, float value);
    }
    ```

## Example
The first image shows the demo application with 16 differently styled RotarySeekbars. All of these have been customized through the `.xml` layout file.

<img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app.png" width="50%" />

Pressing either of the widgets will enlarge the view of the component, making it easier to choose a value while still having the component visible.

<img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_01.png" width="23%" /> <img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_02.png" width="23%" /> <img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_03.png" width="23%" /> <img src="https://raw.githubusercontent.com/akapelrud/Rotary-Seekbar/master/screenshots/Screenshot_demo_app_04.png" width="23%" />
