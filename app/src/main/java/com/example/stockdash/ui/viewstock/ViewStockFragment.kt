package com.example.stockdash.ui.viewstock

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.load
import com.addisonelliott.segmentedbutton.SegmentedButtonGroup
import com.example.stockdash.R
import com.example.stockdash.data.model.StockDetail
import com.example.stockdash.data.model.TimeSeriesData
import com.example.stockdash.databinding.FragmentViewStockBinding
import com.example.stockdash.ui.MainViewModel
import com.example.stockdash.util.Resource
import com.example.stockdash.util.resolveThemeColor
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class ViewStockFragment : Fragment() {
    private lateinit var binding: FragmentViewStockBinding
    private val viewModel: ViewStockModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private var intervalArg = "1D"
    private val intervalArgs = arrayOf("1D", "1W", "1M", "3M", "1Y", "5Y")
    private var price: Float = -1f
    private var priceOld: Float = -1f
    private var priceLow: Float = -1f
    private var priceHigh: Float = -1f

    companion object {
        const val ARG_SYMBOL = "symbolArg"

        fun newInstance(stock: String): ViewStockFragment {
            val fragment = ViewStockFragment()
            val args = Bundle()
            if (stock.isEmpty()) {
                Toast.makeText(fragment.requireContext(), "Empty Arguments in new ViewProductFragment", Toast.LENGTH_SHORT).show()
                return fragment
            }

            args.putString(ARG_SYMBOL, stock)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentViewStockBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDynamicChartHeight()
        observeChange()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setUiStateForProductDetail()
    }

    @SuppressLint("SetTextI18n")
    private fun setupView(stockDetail: StockDetail?) {

        if (stockDetail == null) {
            Toast.makeText(requireContext(), "NULL stockDetail Passed", Toast.LENGTH_SHORT).show()
            return
        }
        setupChartTouchInteraction()
        binding.apply {
            val logoUrl = "https://financialmodelingprep.com/image-stock/${stockDetail.symbol}.png"
            imageViewStockLogo.load(logoUrl) {
                crossfade(true)
            }

            segmentedButtonGroup.onPositionChangedListener =
                SegmentedButtonGroup.OnPositionChangedListener { position ->
                    if (intervalArg != intervalArgs[position]) {
                        intervalArg = intervalArgs[position]
                        viewModel.updateInterval(intervalArg)
                    }
                }

            textViewCompanyName.text = stockDetail.name

            textViewStockName.text = stockDetail.symbol + ", " + stockDetail.assetType
            textViewStockExchange.text = stockDetail.exchange?:""

            priceHigh = stockDetail.yearHigh?.toFloatOrNull()?:-1f
            priceLow = stockDetail.yearLow?.toFloatOrNull()?:-1f

            if (priceLow == -1f) return

            updatePrice()

            textViewAboutStockName.text = "About ${stockDetail.name}"
            textViewDesc.text = stockDetail.description

            if (stockDetail.industry == null) textViewIndustry.visibility = View.GONE
            else textViewIndustry.text = "Industry: ${stockDetail.industry}"
            if (stockDetail.sector == null) textViewSector.visibility = View.GONE
            else textViewSector.text = "Sector: ${stockDetail.sector}"

            textViewyearLowPrice.text = "$$priceLow"
            textViewYearHighPrice.text = "$$priceHigh"


            textViewMarketCap.text = makeBoldAfterLineBreak("Market Cap", formatNumber(stockDetail.marketCap))
            textViewPERatio.text = makeBoldAfterLineBreak("P/E Ratio", stockDetail.peRatio.toString())
            textViewBeta.text = makeBoldAfterLineBreak("Beta", stockDetail.beta.toString())
            textViewDividendYield.text = makeBoldAfterLineBreak("Dividend Yield", "${stockDetail.dividendYield}%")
            textViewProfitMargin.text = makeBoldAfterLineBreak("Profit Margin", "${stockDetail.profitMargin}%")
        }
    }

    private fun observeChange() {
        // Use viewLifecycleOwner.lifecycleScope for Fragment coroutines tied to view's lifecycle
        viewLifecycleOwner.lifecycleScope.launch { // VIEW Lifecycle scope. Stops when view gets destroyed

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) { // Start after View gets visible

                // Launch a coroutine for collecting stockDetailState
                launch { // Coroutine 1
                    viewModel.stockDetailState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                // TODO: Show loading for details
                            }
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    setupView(data)
                                } ?: run {
                                    Toast.makeText(requireContext(), "Detail Data NULL even on success", Toast.LENGTH_SHORT).show()
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(requireContext(), "DETAIL ERROR: ${resource.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // Launch another coroutine for collecting timeSeriesDataState
                launch { // Coroutine 2
                    viewModel.timeSeriesDataState.collect { resource ->
                        Log.d("ObserveChange", "TimeSeries State: $resource") // Add log
                        when (resource) {
                            is Resource.Loading -> {
                                binding.lineChart.visibility = View.INVISIBLE
                            }
                            is Resource.Success -> {
                                resource.data?.let { data ->
                                    Log.i("ObserveChange", "TIME_SERIES: Success time series data: $data")
                                    setupGraph(data)
                                } ?: run {
                                    Toast.makeText(requireContext(), "Chart Data NULL even on success", Toast.LENGTH_SHORT).show()
                                }
                            }
                            is Resource.Error -> {
                                Log.e("ObserveChange", "TIME_SERIES: Error - ${resource.message}")
                                Toast.makeText(requireContext(), "CHART ERROR: ${resource.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            // When the lifecycle goes below STARTED, the block inside repeatOnLifecycle is cancelled,
            // which cancels both inner launch blocks (and thus their collect operations).
            // When it returns to STARTED, the repeatOnLifecycle block re-executes, launching new coroutines for collection.
        }
    }

    private fun setupPriceChange(priceChange: Float, priceChangePercentage: Float) {
        binding.apply{
            if (priceChange < 0) textViewStockPriceChangePercent.apply{
                setTextColor(requireContext().resolveThemeColor(R.attr.stockPriceDownColor))
                setCompoundDrawables(null, null, AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_drop_down), null)
                compoundDrawables[2].setTint(requireContext().resolveThemeColor(R.attr.stockPriceDownColor))
            }
            else textViewStockPriceChangePercent.apply {
                setTextColor(requireContext().resolveThemeColor(R.attr.stockPriceUpColor))
                setCompoundDrawables(null, null, AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_drop_up), null)
                compoundDrawables[2].setTint(requireContext().resolveThemeColor(R.attr.stockPriceUpColor))
            }
            val changeAmountInString = String.Companion.format(Locale.getDefault(), "%+f", priceChange).trimEnd('0').trimEnd('.')
            textViewStockPriceChangePercent.text = String.Companion.format(Locale.getDefault(), "%s (%+.2f%%)", changeAmountInString , priceChangePercentage)
        }
    }

    private fun setupGraph(timeSeriesData: TimeSeriesData) {

        if (timeSeriesData.data.isEmpty()) {
            binding.lineChart.clear() // Clear previous data if any
            binding.lineChart.setNoDataTextColor(R.attr.BlackOnLight)
            binding.lineChart.setNoDataText("No Data available.") // Show a message
            binding.lineChart.visibility = View.VISIBLE
            binding.lineChart.invalidate() // Refresh the chart
            return
        }

        // 1. Create a list of Entry objects from your data
        val entries = ArrayList<Entry>()
        val dateLabels = ArrayList<String>() // To store date labels for X-axis

//        if (intervalArg == "1D") price = timeSeriesData.data.first().price
        val tillDateIdx = getStartDateIndex(timeSeriesData)
        priceOld = timeSeriesData.data[tillDateIdx].price
        price = timeSeriesData.data.first().price

        for (i in tillDateIdx downTo  0) {
            val entry = Entry(entries.size.toFloat(), timeSeriesData.data[i].price)
            entries.add(entry)
            dateLabels.add(timeSeriesData.data[i].date)
        }

        updatePrice()



        // 2. Create a LineDataSet with the entries
        val lineDataSet = LineDataSet(entries, "Stock Price") // Label for the dataset

        // --- Customize the LineDataSet (appearance of the line) ---
        val isPositive = price >= priceOld
        val graphColor = if (isPositive) {
            requireContext().resolveThemeColor(R.attr.stockPriceUpColor)
        }
        else {
            requireContext().resolveThemeColor(R.attr.stockPriceDownColor)
        }

        lineDataSet.color = graphColor
        lineDataSet.fillColor = graphColor
        lineDataSet.valueTextColor = requireContext().resolveThemeColor(R.attr.toolbarTextColor)
        lineDataSet.lineWidth = 2f
        lineDataSet.setDrawValues(false) // Hide Y values on each point on the line
        lineDataSet.setDrawCircles(false) // Hide circles on each data point
        lineDataSet.mode = LineDataSet.Mode.LINEAR // Makes the line smooth
        lineDataSet.setDrawFilled(true) // Enable filling below the line
        lineDataSet.fillAlpha = 40 // Opacity of the fill


        // 4. Create LineData object with the dataset
        val lineData = LineData(lineDataSet)

        // 5. Set data to the chart
        binding.lineChart.data = lineData

        // --- Customize the Chart (overall appearance) ---
        binding.lineChart.description.isEnabled = false // Hide the description label
        binding.lineChart.legend.isEnabled = false      // Hide the legend (if only one dataset)
        lineDataSet.isHighlightEnabled = true
        lineDataSet.highLightColor = graphColor

        // X-Axis Customization
        val xAxis = binding.lineChart.xAxis
        xAxis.textColor = requireContext().resolveThemeColor(R.attr.toolbarTextColor)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Minimum interval between labels
        xAxis.isGranularityEnabled = true
        xAxis.setLabelCount(3, true) // Limit labels to 5
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.setDrawGridLines(false) // Hide vertical grid lines


        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val index = value.toInt()
                return if (index in dateLabels.indices) formatDateLabel(dateLabels[index]) else ""
            }
        }

        // Y-Axis (Left) Customization
        val leftAxis = binding.lineChart.axisLeft
        leftAxis.setDrawGridLines(true) // Show horizontal grid lines
        leftAxis.gridColor = requireContext().resolveThemeColor(R.attr.BlackOnLight)
        leftAxis.textColor = requireContext().resolveThemeColor(R.attr.BlackOnLight)
        leftAxis.setLabelCount(4, true)

        // Y-Axis (Right) Customization
        val rightAxis = binding.lineChart.axisRight
        rightAxis.textColor = requireContext().resolveThemeColor(R.attr.BlackOnLight)
        rightAxis.setLabelCount(4, true)



        // Interaction
        binding.lineChart.setTouchEnabled(true)
        binding.lineChart.setPinchZoom(true)
        binding.lineChart.isDragEnabled = true
        binding.lineChart.setScaleEnabled(true)


        // 6. Refresh the chart
        binding.lineChart.visibility = View.VISIBLE
        binding.lineChart.invalidate()
        binding.lineChart.animateX(500) // Animate drawing the chart
    }

    private fun formatNumber(number: String?): String {
        return when {
            number == null -> ""
            number.length >= 12 -> String.Companion.format(Locale.getDefault(), "%.1fT$", number.toDouble() / 1_000_000_000_000.0)
            number.length >= 9 -> String.Companion.format(Locale.getDefault(), "%.1fB$", number.toDouble() / 1_000_000_000.0)
            number.length >= 6 -> String.Companion.format(Locale.getDefault(), "%.1fM$", number.toDouble() / 1_000_000.0)
            number.length >= 3 -> String.Companion.format(Locale.getDefault(), "%.1fK$", number.toDouble() / 1_000.0)
            else -> number
        }
    }

    private fun formatDateLabel(dateString: String): String {
        return try {
            // If it's a full datetime like "2023-10-27 16:00:00"
            if (dateString.length > 10) {
                val date = LocalDate.parse(dateString.substring(0, 10))
                "${dateString.substring(11, 16)} ${date.dayOfMonth} ${date.month}"
            }
            // If it's just a date like "2023-10-27"
            else if (dateString.length == 10) {
                val date = LocalDate.parse(dateString)
                val displayMonth = date.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                "${date.dayOfMonth} $displayMonth ${date.year.toString().substring(2)}"
            } else {
                dateString // Return as is if format is unknown
            }
        } catch (e: Exception) {
            e.printStackTrace()
            dateString
        }
    }

    private fun getStartDateIndex(data: TimeSeriesData): Int {
        val dateInString = data.data.first().date
        var today = LocalDate.parse(dateInString.substring(0, 10))
        today = when (intervalArg) {
            "1D" -> today.minusDays(1)
            "1W" -> today.minusWeeks(1)
            "1M" -> today.minusMonths(1)
            "3M" -> today.minusMonths(3)
            "1Y" -> today.minusYears(1)
            "5Y" -> today.minusYears(5)
            else -> today.minusDays(1)
        }
        val tillDate =  if (dateInString.length > 10) {
            "$today 23:59:59"
        } else {
            today.toString()
        }
        val result = data.data.indexOfFirst { it.date <= tillDate }
        return if (result == -1) data.data.lastIndex else result
    }

    private fun updatePrice() {
        if (price == -1f || priceLow == -1f || priceHigh == -1f || priceOld == -1f) return

        setPriceBias(((price-priceLow) / (priceHigh-priceLow)).coerceIn(0.0f, 1.0f))
        setPrice()
    }

    private fun setPriceBias(bias: Float) {
        binding.constraintLayoutViewProduct.post {
            val constraintSet = ConstraintSet()
            constraintSet.clone(binding.constraintLayoutCardView)
            constraintSet.setHorizontalBias(R.id.textViewBarPrice, bias)
            constraintSet.setHorizontalBias(R.id.imageViewPriceArrow, bias)
            constraintSet.applyTo(binding.constraintLayoutCardView)

            binding.textViewBarPrice.visibility = View.VISIBLE
            binding.imageViewPriceArrow.visibility = View.VISIBLE

            if (bias >= 0.5) {
                binding.textViewBarPrice.setTextColor(requireContext().resolveThemeColor(R.attr.stockPriceUpColor))
                binding.imageViewPriceArrow.imageTintList = ColorStateList.valueOf(requireContext().resolveThemeColor(
                    R.attr.stockPriceUpColor))
            }
            else {
                binding.textViewBarPrice.setTextColor(requireContext().resolveThemeColor(R.attr.stockPriceDownColor))
                binding.imageViewPriceArrow.imageTintList = ColorStateList.valueOf(requireContext().resolveThemeColor(
                    R.attr.stockPriceDownColor))
            }
        }
    }

    private fun setPrice() {
        binding.textViewStockPrice.text = resources.getString(R.string.price_before_dollar, price.toString())
        binding.textViewBarPrice.text = resources.getString(R.string.current_price, price.toString())
        if (price >= priceOld) {
            binding.textViewStockPrice.setTextColor(requireContext().resolveThemeColor(R.attr.stockPriceUpColor))
            binding.textViewStockPrice.setCompoundDrawables(null, null, AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_drop_up), null)
            binding
        } else {
            binding.textViewStockPrice.setTextColor(requireContext().resolveThemeColor(R.attr.stockPriceDownColor))
            binding.textViewStockPrice.setCompoundDrawables(null, null, AppCompatResources.getDrawable(requireContext(), R.drawable.arrow_drop_down), null)
        }

        setupPriceChange(price - priceOld, (price - priceOld) / priceOld * 100)
    }

    private fun makeBoldAfterLineBreak(label: String, value: String): SpannableString {
        val combined = "$label\n$value"
        val spannable = SpannableString(combined)
        val start = combined.indexOf('\n') + 1
        val end = combined.length
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupChartTouchInteraction() {
        binding.lineChart.setOnTouchListener(View.OnTouchListener { v, event ->
            // When the user touches the chart (ACTION_DOWN), request that the parent ScrollView does NOT intercept touch events.
            // This allows the chart to handle its own gestures (like dragging the marker).
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Tell the parent ScrollView to not intercept touch events.
                    binding.scrollViewViewProduct.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Allow the parent ScrollView to intercept touch events again once the gesture on the chart is finished.
                    binding.scrollViewViewProduct.requestDisallowInterceptTouchEvent(false)
                }
            }
            // Return false so that the chart's own touch listeners are still called.
            return@OnTouchListener false
        })
    }

    private fun setDynamicChartHeight() {
        // Get screen height
        val displayMetrics = DisplayMetrics()
        // For older APIs, use windowManager.defaultDisplay.getMetrics(displayMetrics)
        // For newer APIs (API 30+), it's better to get from WindowMetrics
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.systemBars()
            )
            displayMetrics.widthPixels = windowMetrics.bounds.width() - insets.left - insets.right
            displayMetrics.heightPixels = windowMetrics.bounds.height() - insets.top - insets.bottom
        } else {
            @Suppress("DEPRECATION")
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        val screenHeightPx = displayMetrics.heightPixels

        // Calculate desired chart height (e.g., 30% of screen height)
        val desiredChartHeightPx = (screenHeightPx * 0.30).toInt()

        // Get the CardView that contains the chart
        val chartCardView = binding.cardViewStockChart

        // Get its current LayoutParams
        val layoutParams = chartCardView.layoutParams

        // Set the new height
        layoutParams.height = desiredChartHeightPx

        // Apply the updated LayoutParams
        chartCardView.layoutParams = layoutParams
    }

}