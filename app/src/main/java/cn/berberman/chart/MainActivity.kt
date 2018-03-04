package cn.berberman.chart

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import lecho.lib.hellocharts.gesture.ContainerScrollType
import lecho.lib.hellocharts.gesture.ZoomType
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener
import lecho.lib.hellocharts.model.*
import lecho.lib.hellocharts.view.LineChartView

class MainActivity : AppCompatActivity() {

	private val date = arrayOf("10-22", "11-22", "12-22", "1-22", "6-22", "5-23", "5-22", "6-22", "5-23", "5-22")//X轴的标注
	private val score = intArrayOf(50, 42, 90, 33, 10, 74, 22, 18, 79, 20)

	private lateinit var chartView: LineChartView
	private lateinit var helper: LineChartViewHelper
	private var bluetoothManager: BluetoothManager? = null
	private var bluetoothAdapter: BluetoothAdapter? = null
	private val searchDevice = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {

		}

	}
	private val intentFilter = IntentFilter().apply {
		addAction(BluetoothDevice.ACTION_FOUND)
		addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
		addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
		addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
		requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
		setContentView(R.layout.activity_main)
		bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
		bluetoothAdapter = bluetoothManager?.adapter
		registerReceiver(searchDevice, intentFilter)
		chartView = chart
		val values = Array(10) {
			ColumnData(date[it], score[it].toFloat(), it.toFloat())
		}
		helper = LineChartViewHelper(chartView, values.toMutableList())
		helper.refresh()
		chartView.onValueTouchListener = object : LineChartOnValueSelectListener {
			override fun onValueSelected(p0: Int, p1: Int, p2: PointValue) {
				Toast.makeText(this@MainActivity, "(${p2.x},${p2.y})", Toast.LENGTH_SHORT).show()
			}

			override fun onValueDeselected() {
			}
		}
	}

	override fun onResume() {
		super.onResume()
		helper.refresh()
	}
}


data class ColumnData(val name: String, val y: Float, val x: Float)

class LineChartViewHelper(private val view: LineChartView, val values: MutableList<ColumnData>) {
	private val xValues = arrayListOf<AxisValue>()
	private val pValues = arrayListOf<PointValue>()

	init {
		initChartView(generateData(generateXAxis(xValues), generateYAxis(), drawLine(pValues)))
	}

	private fun postValue() {
		xValues.clear()
		xValues.addAll(values.map { AxisValue(it.x).setLabel(it.name) })
		pValues.clear()
		pValues.addAll(values.map { PointValue(it.x, it.y) })
	}

	fun refresh() {
		postValue()
		view.lineChartData = generateData(generateXAxis(xValues), generateYAxis(), drawLine(pValues))
		view.invalidate()
	}

	private fun generateXAxis(value: List<AxisValue>) = Axis(value).apply {
		setHasTiltedLabels(true)
		textColor = R.color.primary_text
		textSize = 10
		maxLabelChars = 8
		setHasLines(true)
	}

	private fun generateYAxis() = Axis().apply {
		textSize = 10
	}

	private fun initChartView(data: LineChartData) {
		view.apply {
			isInteractive = true
			zoomType = ZoomType.HORIZONTAL_AND_VERTICAL
			maxZoom = 2.0F
			setContainerScrollEnabled(true, ContainerScrollType.HORIZONTAL)
			lineChartData = data
		}
	}

	private fun drawLine(points: List<PointValue>) = Line(points).apply {
		isCubic = true
		isFilled = false
		setHasLabels(true)
		setHasLines(true)
		color = R.color.colorAccent
		shape = ValueShape.CIRCLE
		setHasPoints(true)
	}

	private fun generateData(x: Axis, y: Axis, line: Line) = LineChartData(listOf(line)).apply {
		axisXBottom = x
		axisYLeft = y
	}
}