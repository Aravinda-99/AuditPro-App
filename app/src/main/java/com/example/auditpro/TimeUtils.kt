import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

fun calculateRemainingTime(requestDate: LocalDate?, requestTime: LocalTime?): String {
    if (requestDate == null || requestTime == null) {
        return "Date or time not set"
    }

    val requestDateTime = LocalDateTime.of(requestDate, requestTime)
    val now = LocalDateTime.now()

    return if (requestDateTime.isAfter(now)) {
        val remainingSeconds = ChronoUnit.SECONDS.between(now, requestDateTime)
        val days = remainingSeconds / (3600 * 24)
        val hours = (remainingSeconds % (3600 * 24)) / 3600
        val minutes = (remainingSeconds % 3600) / 60
        val seconds = remainingSeconds % 60

        val daysPart = if (days > 0) "${days}d " else ""
        val hoursPart = if (hours > 0 || days > 0) "${hours}h " else ""
        val minutesPart = if (minutes > 0 || hours > 0 || days > 0) "${minutes}m " else ""
        val secondsPart = "${seconds}s"

        "$daysPart$hoursPart$minutesPart$secondsPart remaining".trim()
    } else {
        "Time has passed"
    }
}
