package com.github.andrewgazelka.visualizer

import com.github.andrewgazelka.Point
import com.github.andrewgazelka.simulatedAnnealing
import javafx.animation.KeyValue
import javafx.scene.paint.Color
import javafx.scene.shape.LineTo
import javafx.scene.shape.MoveTo
import javafx.scene.shape.Path
import javafx.scene.shape.PathElement
import javafx.scene.text.Font
import tornadofx.*


class MyView : View() {

    private val scalar = 500.0
    private val simulatedAnnealing = simulatedAnnealing()
    private var pointPath = simulatedAnnealing.base.map { (x, y) ->
        Point(scalar * x, scalar * y)
    }.toMutableList()

    override val root = stackpane {
        group {
            pointPath.forEachIndexed { index, (x,y) ->
                val percent = index.toDouble()/pointPath.size
                circle {
                    centerX = x
                    centerY = y
                    radius = 5.0
                    fill = Color.hsb(0.0,percent,percent)
                }
            }

            val textField = text("a"){
                font = Font.font(40.0)
            }
            val path = path {
                paddingAll = 200.0
                val (firstX, firstY) = pointPath.first()
                moveTo(firstX, firstY)
                pointPath.drop(1).forEach {
                    lineTo(it.x, it.y)
                }

                val lineTo = lineTo(firstX, firstY)
                println("lt: $lineTo")
            }

            sequentialTransition {
                cycleCount = 10
                simulatedAnnealing.swaps.forEachIndexed { index, swap ->
                    timeline {
                        keyframe(200.millis) {
                            keyvalue(textField.textProperty(), "$index")
                            val (i1, i2) = swap

                            val (dest1, elements1) = path.elements(i1)
                            val (dest2, elements2) = path.elements(i2)

                            pointPath.swap(i1,i2)

                            if (i1 == i2 || elements1 == elements2) return@keyframe
                            println("dest1 = ${dest1}")
                            println("i1 = ${i1}")
                            println("elements1 = ${elements1}")
                            println("dest2 = ${dest2}")
                            println("elements2 = ${elements2}")
                            println("i2 = ${i2}")


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
