package liray.utils.other

interface Initializable {
    fun initialize() {}
}

fun initialize(vararg initializables: Initializable) {
    initializables.forEach {
        it.initialize()
    }
}