import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class MudawamaExtension @Inject constructor(objects: ObjectFactory) {
    val data = objects.newInstance(DataExtension::class.java)
    val presentation = objects.newInstance(PresentationExtension::class.java)

    fun data(configure: DataExtension.() -> Unit) {
        data.configure()
    }

    fun presentation(configure: PresentationExtension.() -> Unit) {
        presentation.configure()
    }
}

open class DataExtension {
    var useDataStore: Boolean = false
    var useTink: Boolean = false
    var useSerialization: Boolean = false
}

open class PresentationExtension {
    var useCompose: Boolean = true
}
