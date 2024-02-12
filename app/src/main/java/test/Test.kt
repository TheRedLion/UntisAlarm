package test

import org.bytedream.untis4j.Session

fun main() {
    val session = Session.login(
        "KarenfCar",
        "Mytimetable1!",
        "nessa.webuntis.com",
        "gym-besidenstrasse"
    )
}