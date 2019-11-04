# Rotary Seekbar
Rotary Seekbar View for Android with Overlay zooming making it easy to adjust a rotary knob while actually seeing what you are doing.

**Please note**!  The repo has been renamed for searchability purposes, but the android library is still named RotaryKnob. This is subject to change in the future, definitely breaking the API.

The library can be used by adding a few lines too your project's build.gradle file:  
```
repositories {
    maven {
        url 'https://github.com/akapelrud/Rotary-Seekbar/raw/master/RotaryKnob/snapshots'
    }
}

dependencies {
    compile 'no.kapelrud:RotaryKnobLibrary:0.1.0:@aar'
}
```

## Example
First image shows three widget instances in a horizontal layout. One with opening facing left, one facing right, and with no opening. All aspecs of the rotary knob is customizable via xml; like the needle (size, color, length, etc.), the sectors (radial length, color, etc.) and ticks (thickness, length, color, radial size and position, etc.).

![example 1](https://raw.githubusercontent.com/akapelrud/RotaryKnob/master/screenshots/screenshot_01_cropped.png)

Pressing either of the widgets will enlarge the view of the component, making it easier to choose a value while still having the component visible.

![example 2](https://raw.githubusercontent.com/akapelrud/RotaryKnob/master/screenshots/screenshot_02_cropped.png)
![example 3](https://raw.githubusercontent.com/akapelrud/RotaryKnob/master/screenshots/screenshot_03_cropped.png)
![example 4](https://raw.githubusercontent.com/akapelrud/RotaryKnob/master/screenshots/screenshot_04_cropped.png)
