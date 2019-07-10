package yuanchao.core.piechartview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import yuanchao.core.piechart.PieChartView
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val floatArray = floatArrayOf(10F, 0F,0F,0F,0F)
        findViewById<PieChartView>(R.id.pcv).adapter = object : PieChartView.PieChartAdapter {
            override fun getTotalValue(): Float {
                return floatArray.sum()
            }

            override fun getCount(): Int {
                return floatArray.count()
            }

            override fun getColor(position: Int): Int {
                return Color.rgb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255))
            }

            override fun getValue(position: Int): Float {
                return floatArray[position]
            }
        }
    }
}
