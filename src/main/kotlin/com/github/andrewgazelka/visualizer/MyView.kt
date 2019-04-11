package com.github.andrewgazelka.visualizer

import com.github.andrewgazelka.Point
import com.github.andrewgazelka.pathDist2
import com.github.andrewgazelka.simulatedAnnealing
import javafx.animation.KeyValue
import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import tornadofx.*
import kotlin.math.sqrt


class MyView : View() {

    private val scalar = 500.0
    private val simulatedAnnealing = simulatedAnnealing()
    private var pointPath = simulatedAnnealing.base.map { (x, y) ->
        Point(scalar * x, scalar * y)
    }.toMutableList()

    override val root = stackpane {
        style {
            backgroundColor += Color.BLACK
        }
        group {

            val distance = text("Distance: ...") {
                style {
                    stroke = Color.WHITE
                }
            }

            val path = path {
                strokeWidth = 1.5
                style {
                    stroke = Color.web("272c33")
                }
                paddingAll = 200.0
                val (firstX, firstY) = pointPath.first()
                moveTo(firstX, firstY)
                pointPath.drop(1).forEach {
                    lineTo(it.x, it.y)
                }

                val lineTo = lineTo(firstX, firstY)
                println("lt: $lineTo")
            }

            pointPath.forEachIndexed { index, (x, y) ->
                val percent = index.toDouble() / pointPath.size
                circle {
                    centerX = x
                    centerY = y
                    radius = 5.0
                    fill = Color.hsb(0.0, percent, percent*2/3+.1)
                }
            }

            sequentialTransition {
                simulatedAnnealing.swaps.forEachIndexed { index, swap ->
                    timeline {
                        keyframe(150.millis) {
                            val (i1, i2) = swap

                            val (dest1, elements1) = path.elements(i1)
                            val (dest2, elements2) = path.elements(i2)

                            pointPath.swap(i1, i2)

                            keyvalue(distance.textProperty(), "Distance: %.2f".format(sqrt(pointPath.pathDist2())))

                            if (i1 == i2 || elements1 == elements2) return@keyframe

                            val keyValues =
                                elements1.flatMap { it.keyvalueTo(dest2) } + elements2.flatMap { it.keyvalueTo(dest1) }
//                            keyValues.forEach { println(it) }
                            println()
                            keyValues.forEach { this += it }
                        }
                    }
                }
            }
        }
    }

    fun PathElement.keyvalueTo(point: Point) = when (this) {
        is LineTo -> listOf(
            KeyValue(xProperty(), point.x),
            KeyValue(yProperty(), point.y)
        )
        is MoveTo -> listOf(
            KeyValue(xProperty(), point.x),
            KeyValue(yProperty(), point.y)
        )
        else -> throw IllegalArgumentException("not allowed")
    }

    data class ElementsResult(val point: Point, val elements: List<PathElement>)

    private fun Path.elements(index: Int): ElementsResult {

        val list = mutableListOf<PathElement>()
        val point = pointPath[index]
        with(elements) {
            if (index == 0) {
                val first = first() as? MoveTo ?: throw IllegalStateException("not move to")
                list.add(first)
                list.add(last())
            } else {
                val lineTo = get(index) as? LineTo ?: throw IllegalStateException("line to")
                list.add(lineTo)
            }
        }

        return ElementsResult(point, list)
    }

}
