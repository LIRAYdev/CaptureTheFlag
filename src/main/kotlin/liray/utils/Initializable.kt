package liray.utils

interface Initializable {
    fun initialize() {}
}

fun initialize(vararg initializables: Initializable) {
    initializables.forEach {
        it.initialize()
    }
}