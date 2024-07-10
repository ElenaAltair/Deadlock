package ru.netology.deadlock

import kotlin.concurrent.thread

fun main() {
    val resourceA = Any()
    val resourceB = Any()

    val consumerA = Consumer("A")
    val consumerB = Consumer("B")

    val t1 = thread {
        consumerA.lockFirstAndTrySecond(resourceA, resourceB)
    }
    val t2 = thread {
        consumerB.lockFirstAndTrySecond(resourceB, resourceA)
    }

    t1.join() // функция main приостановила работу и ждёт пока завершатся потоки t1
    t2.join() // и t2

    println("main successfully finished")
}

// Ответ на вопрос «По какой причине не завершается работа функции main?»

// Synchronized устанавливает объект, монитор которого
// используется для защиты блока кода от параллельного
// исполнения разными потоками
// Поток перед входом в блок synchronized проверяет монитор
// объекта: если он свободен, то захватывает его, если нет —
// ждёт, пока монитор освободится

// В классе Consumer
// объект first заключён в функции lockFirstAndTrySecond в блок synchronized
// объект second заключён в функции lockSecond в блок synchronized
// Для первого потока объект first = resourceA, second = resourceB
// Для второго потока наоборот объект first = resourceB, second = resourceA
//
// У нас стартуют оба потока
// Если, например, поток А стартует первым и первым успеет выполнить свою работу и завершиться,
// до старта потока В и захвата им монитора объекта resourceB,
// и только потом уже запуститься поток B, то метод main завершиться успешно
//
// Но в нашем случае, когда стартуют оба потока, чаще всего происходит так:
// например первым вошёл в функцию lockFirstAndTrySecond поток A (и захватил монитор объекта resourceA),
// a за ним поток B (который захватывает монитор объекта resourceB),
// и он пытается запустить функцию lockSecond(second),
// но second для него - это объект resourceA,так как он заключён в блок synchronized, который уже захвачен потоком А,
//
// то поток B вынужден ждать пока поток А завершиться и освободит монитор объект resourceA,
// но поток А не может завершиться, так как ему для завершения нужно войти в функцию lockSecond с объектом resourceB,
// но монитор объекта resourceB захвачен потоком B, который ожидает когда освободиться монитор объекта resourceA, удерживаемый потоком А
//
// для успешного завершения метода main нам надо убрать блок synchronized с объекта либо в функции lockFirstAndTrySecond либо в функции lockSecond

class Consumer(private val name: String) {
    fun lockFirstAndTrySecond(first: Any, second: Any) {
        synchronized(first) {
            println("$name locked first, sleep and wait for second")
            Thread.sleep(1000)
            lockSecond(second)
        }
    }

    fun lockSecond(second: Any) {
        synchronized(second) {
            println("$name locked second")
        }
    }
}