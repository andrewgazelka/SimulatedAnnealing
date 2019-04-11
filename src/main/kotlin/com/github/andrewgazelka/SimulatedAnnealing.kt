package com.github.andrewgazelka

import kotlin.math.pow
import kotlin.random.Random

data class Point(val x: Double, val y: Double)

data class Swap(val index1: Int, val index2: Int, val temperature: Double)

data class SimulatedAnnealingResult(val base: List<Point>, val swaps: Sequence<Swap>)


val test get() = "aa"
fun simulatedAnnealing(): SimulatedAnnealingResult {
    val pointCount = 50
    val iterations = 10_000
    val alpha = 0.99

    val list = (1..pointCount).map {

        fun randNum() = if (Random.nextBoolean()) Random.nextDouble(0.8,1.0) else Random.nextDouble(0.0,0.2)
            Point(randNum(),randNum())
    }.toMutableList()

//    println("Previous dist: ${sqrt(list.pathDist2())}")
    var temp = 1.0

    val size = list.size
    var lastScore = Double.MAX_VALUE

    val first = list.toList()

    val second = sequence {
        repeat(iterations) {

            val i1 = Random.nextInt(size)
            val i2 = Random.nextInt(size)

            if(i1 == i2) return@repeat

            list.swap(i1, i2)
            val score = list.pathDist2()
            if (score < lastScore || Random.nextDouble() < acceptanceProbability(lastScore, score, temp)) {
                lastScore = score
                yield(Swap(i1, i2, temp))
            }
            else list.swap(i1, i2) // swap again because bad solution

            temp *= alpha
        }
    }

    return SimulatedAnnealingResult(first, second)

}

fun acceptanceProbability(old: Double, new: Double, temperature: Double): Double {
    return Math.E.pow((old - new) / temperature)
}

fun List<Point>.pathDist2(): Double {
    var dist2Sum = 0.0
    for (i in 0 until size - 1) {
        val p1 = get(i)
        val p2 = get(i + 1)
        dist2Sum += p1 dist2 p2
    }
    dist2Sum += last() dist2 first()
    return dist2Sum
}

infix fun Point.dist2(other: Point): Double {
    return (x - other.x).pow(2) + (y - other.y).pow(2)
}

fun <T> MutableList<T>.swap(i1: Int, i2: Int) {
    val temp = this[i1]
    this[i1] = this[i2]
    this[i2] = temp
}
