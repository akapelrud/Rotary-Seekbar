package no.kapelrud.rotary_seekbar_demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       
        /* It is possible to alter the values programmatically. The value will snap to valid
         * steps and bounds.
         */
        //rot01.value = 100.22f;
        //rot02.value = -12319.0f;
        //rot22.value = 47.5f;
    }
}
