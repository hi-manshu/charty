# Exporting Charts as PNG

Any Charty chart can be captured to an `ImageBitmap` and handed to the platform —
saved, downloaded, or shared. The capture is a real recording of the drawn chart,
so what you export is exactly what the user sees, including animations at the
moment of capture.

Two steps: **capture**, then **export**.

---

## Capture

```kotlin
import androidx.compose.ui.Modifier
import com.himanshoe.charty.export.capture
import com.himanshoe.charty.export.chartCapture
import com.himanshoe.charty.export.rememberChartCaptureController

val controller = rememberChartCaptureController()

LineChart(
    data = { revenue },
    modifier = Modifier.fillMaxWidth().height(300.dp).chartCapture(controller),
    color = ChartyColor.Solid(ChartyColors.Blue),
)
```

`Modifier.chartCapture(controller)` records the content it wraps into a graphics
layer and then draws that layer, so the chart renders exactly as it would without
the modifier while also being capturable. It changes nothing visually.

Read the recorded frame back from a coroutine:

```kotlin
val bitmap: ImageBitmap = controller.capture()
```

`capture()` is a `suspend` function. It reads the **most recently recorded frame**,
so call it after the chart has composed and drawn at least once — the layer is
empty before that.

Wrap only the chart if you want the chart alone, or wrap a surrounding `Column`
to include a title and legend in the exported image.

---

## Export

```kotlin
import com.himanshoe.charty.export.ChartExportResult
import com.himanshoe.charty.export.rememberChartExporter

val controller = rememberChartCaptureController()
val exporter = rememberChartExporter()
val scope = rememberCoroutineScope()

Button(
    onClick = {
        scope.launch {
            val result = exporter.export(bitmap = controller.capture(), fileName = "revenue")
            status =
                when (result) {
                    is ChartExportResult.Saved -> "Saved to ${result.location}"
                    ChartExportResult.Shared -> "Handed to the share sheet"
                    is ChartExportResult.Failed -> "Failed: ${result.reason}"
                }
        }
    },
) {
    Text(text = "Export PNG")
}
```

**Prefer `rememberChartExporter()` over calling `exportChartImage(...)` directly.**
The top-level `exportChartImage(bitmap, fileName)` is the same operation, but on
Android it needs an application `Context`, which only `rememberChartExporter()` can
capture while composing. Calling `exportChartImage` on Android before any
`rememberChartExporter()` has composed returns `ChartExportResult.Failed`.

### File names are normalised

Whatever you pass as `fileName` is sanitised before use: path separators and other
characters rejected by common file systems become underscores, surrounding
whitespace is dropped, the name is capped at 96 characters, a `.png` extension is
appended when missing, and a blank name falls back to `chart.png`. You can pass
`"revenue"`, `"revenue.png"`, or `"Q3 revenue / EMEA"` and get a valid file.

---

## `ChartExportResult`

```kotlin
sealed interface ChartExportResult {
    data class Saved(val location: String) : ChartExportResult
    data object Shared : ChartExportResult
    data class Failed(val reason: String) : ChartExportResult
}
```

Every branch is **terminal**. Exporting is a best-effort, platform-specific
operation — some platforms can only hand the image to a share sheet, others can
only write it to disk — so render a message rather than retrying.

| Branch | Meaning |
|---|---|
| `Saved(location)` | Written to storage. `location` is an absolute file path on file-backed platforms, or the download file name in the browser. |
| `Shared` | Handed to the platform share sheet; the user took over from there. |
| `Failed(reason)` | A human-readable explanation, suitable for showing in the UI. |

---

## Per-platform behaviour

This differs meaningfully by platform. Handle both `Saved` and `Shared` — you will
get different branches on different targets from the same call.

| Platform | What happens | Result |
|---|---|---|
| **Android** | **Saves** the PNG into the app's cache directory (`cacheDir/charty-exports/`) and returns the absolute path. **It does not share.** | `Saved(absolutePath)` |
| **iOS** | Presents the system share sheet (`UIActivityViewController`) for the image. | `Shared` |
| **JVM Desktop** | Writes the PNG into the user's `Downloads` folder, falling back to the home directory when that folder does not exist. | `Saved(absolutePath)` |
| **JS browser** | Triggers a browser download via a synthetic anchor with a `data:` URL. | `Saved(fileName)` |
| **WasmJs browser** | Same as JS: a browser download via a `data:` URL. | `Saved(fileName)` |

### The Android caveat

**On Android, export saves; it does not share.** This is not an oversight.

Handing a file to `Intent.ACTION_SEND` requires a `FileProvider` declared in the
consuming app's manifest, with its own authority and path configuration. A library
cannot declare one on your app's behalf, so Charty writes the PNG and returns the
path instead of pretending to share it.

If your app already owns a `FileProvider`, share the returned path yourself:

```kotlin
// Android-only code, in your app module.
val result = exporter.export(bitmap = controller.capture(), fileName = "revenue")
if (result is ChartExportResult.Saved) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(result.location))
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            },
            "Share chart",
        ),
    )
}
```

Because the file lands in `cacheDir`, Android may reclaim it under storage
pressure. Copy it somewhere durable if you need it to persist.

### iOS ignores the file name

`UIActivityViewController` names the shared image itself, and the destination app
decides the final file name. The `fileName` you pass is ignored on iOS.

---

## A complete example

```kotlin
@Composable
fun ExportableRevenueChart(revenue: List<LineData>) {
    val controller = rememberChartCaptureController()
    val exporter = rememberChartExporter()
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }

    Column {
        LineChart(
            data = { revenue },
            modifier = Modifier.fillMaxWidth().height(300.dp).chartCapture(controller),
            color = ChartyColor.Gradient(listOf(ChartyColors.Blue, ChartyColors.Teal)),
            lineConfig = LineChartConfig(interpolation = LineInterpolation.SMOOTH),
        )
        Button(
            onClick = {
                scope.launch {
                    status =
                        when (val result = exporter.export(bitmap = controller.capture(), fileName = "revenue")) {
                            is ChartExportResult.Saved -> "Saved to ${result.location}"
                            ChartExportResult.Shared -> "Shared"
                            is ChartExportResult.Failed -> "Failed: ${result.reason}"
                        }
                }
            },
        ) {
            Text(text = "Export PNG")
        }
        Text(text = status)
    }
}
```
