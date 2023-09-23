import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

enum class Language(val unit: String) {
    TR("kilometre"), EN("mile")
}

sealed class Step {
    abstract val distance: Int
}

data class Straight(override val distance: Int) : Step()
data class Left(override val distance: Int) : Step()
data class Right(override val distance: Int) : Step()
object Started : Step() {
    override val distance: Int = 0
}
object Finish : Step() {
    override val distance: Int = 0
}

data class RouteStep<T : Step>(val step: T, val nextStep: T?, val progress: Int)

infix fun Int.convertToMileKm(language: Language) = if (language == Language.TR) this * 0.001 else  this  * 0.000621371

suspend fun <T : Step> execute(route: List<T>) {

    println("Lütfen dili seçiniz\n 1.tr 2.en")
    val languageInput = readLine()
    val language = if (languageInput?.toInt() == 1)
        Language.TR
    else if (languageInput?.toInt() == 2)
        Language.EN
    else {
        println("Lütfen geçerli bir işlem giriniz!")
        return
    }
    val routeFlow = createRouteFlow(route)
    showRoute(routeFlow, language)

}

fun <T : Step> createRouteFlow(route: List<T>): Flow<RouteStep<T>> = flow {
    val totalDistance = route.sumOf { it.distance }
    var currentDistance = 0
    val routeIterator = route.iterator()
    var currentStep: T? = route.first()
    var nextStep: T? = if (routeIterator.hasNext()) routeIterator.next() else null


    while (currentStep != null) {
        currentDistance += currentStep.distance
        val progress = ((currentDistance.toDouble() / totalDistance.toDouble()) * 100).toInt()

        emit(RouteStep(currentStep, nextStep, progress = progress))

        currentStep = nextStep
        nextStep = if (routeIterator.hasNext()) routeIterator.next() else null

        delay(1000)
    }
}

suspend fun <T : Step> showRoute(routeFlow: Flow<RouteStep<T>>, language: Language) {
    routeFlow.collect { routeStep ->
        val unit = if (language == Language.TR) "kilometre" else "mile"
        val road = if (language == Language.TR) "yol" else "Road"
        val completed = if (language == Language.TR) "tamamlandı" else "completed"
        val nextStepAsString = if (language == Language.TR) "bir sonraki adım" else "next step"

        when (routeStep.step) {
            is Straight -> {
                val side = if (language == Language.TR) "düz" else "straight"
                val nextStepDescription =
                    if (routeStep.nextStep != null) "$nextStepAsString ${routeStep.nextStep.distance convertToMileKm language} $unit $side" else ""
                println(
                    "$road %${routeStep.progress} $completed. ${routeStep.step.distance convertToMileKm language} $unit $side $nextStepDescription"
                )
            }

            is Left -> {
                val side = if (language == Language.TR) "sol" else "left"
                val nextStepDescription =
                    if (routeStep.nextStep != null) "$nextStepAsString ${routeStep.nextStep.distance convertToMileKm language} $unit $side" else ""
                println(
                    "$road %${routeStep.progress} $completed. ${routeStep.step.distance convertToMileKm language} $unit $side $nextStepDescription"
                )
            }

            is Right -> {
                val side = if (language == Language.TR) "sağ" else "right"
                val nextStepDescription =
                    if (routeStep.nextStep != null) "$nextStepAsString ${routeStep.nextStep.distance convertToMileKm language} $unit $side" else ""
                println(
                    "$road %${routeStep.progress} $completed. ${routeStep.step.distance convertToMileKm language} $unit $side $nextStepDescription"
                )
            }
            is Started -> {
                if (language == Language.TR) println("Başladı") else println("Started")
            }
            is Finish -> {
                if (language == Language.TR) println("Bitti") else println("Finished")
            }
        }
    }
}

fun main() = runBlocking {
    val route: List<Step> = listOf(
        Started,
        Straight(100),
        Straight(150),
        Left(200),
        Left(250),
        Right(150),
        Straight(120),
        Straight(100),
        Finish
    )


    execute(route)
}
