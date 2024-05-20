import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestReflection {

    @XMLEntity.XMLName("persona")
    data class Person(
        val name: String,
        val age: Int,
        @XMLEntity.XMLIgnore val password: String,
        @XMLEntity.EntityXML val address: Address
    )


    data class Address(
        val street: String,
        val number: Int,
        val city: String
    )


    @Test
    fun createSimpleEntityThroughClass(){
        val address = Address("Main St", 123, "Springfield")
        val addressEntity = XMLEntity(address)
        assertEquals("Address city=\"Springfield\" number=\"123\" street=\"Main St\"", addressEntity.fullName)
    }

    @Test
    fun createComplexEntityThroughClass(){
        val address = Address("Main St", 123, "Springfield")
        val personClass = Person("John Doe", 30, "secret", address)
        val personEntity = XMLEntity(personClass)
        assertEquals("persona name=\"John Doe\" age=\"30\"", personEntity.fullName)

        val doc = XMLDocument(personEntity)
        assertEquals("persona\n" + "|-Address", doc.toTree())
        assertEquals(2, doc.getEntities { true }.size)


    }
}