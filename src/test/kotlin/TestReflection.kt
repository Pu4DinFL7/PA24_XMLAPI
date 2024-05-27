import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestReflection {

    @XMLName("persona")
    data class Person(
        val name: String,
        val age: Int,
        @XMLIgnore val password: String,
        @EntityXML val address: Address
    )

    @XMLAdapter (DescendingOrder :: class)
    data class Address(
        val street: String,
        @XMLString (RickMorty::class)val number: Int,
        val city: String
    )

    data class Daniel(
        val nickname: String,
        @EntityXML
        val childrens:List<*>
    )

    data class Filhe(
        val nickname: String,
        val característica: String
    )

    class AddPercentage : StringEdit{
        override fun change(value: String):String {
            return "$value%"
        }
    }

    class RickMorty : StringEdit{
        override fun change(value: String): String {
            return "$value Mermaid"
        }
    }

    class DescendingOrder : EntityAdapter{
        override fun adapt(entity: XMLEntity): XMLEntity {
            println("-> " + entity.getAttributes())
            val attributes = entity.getAttributes().toList().sortedByDescending { it.first }
            println(" "+ attributes)
            entity.clearAttributes()
            attributes.forEach { (key,value) -> entity.addAttribute(key,value) }
            println("AUTISMMMMMMMMMMMM" + attributes)
            println("NAH NAH NAH NAH " + entity.getAttributes())
            return entity
        }
    }

    @Test
    fun testAdapter(){
    }
    @Test
    fun personalizeText(){
        val address = Address("Main St", 123, "Springfield")
        assertEquals("Address city=\"Springfield\" number=\"123 Mermaid\" street=\"Main St\"", XMLEntity(address).fullName)
    }

    @Test
    fun createEntityWithChildList(){
        val vasco = Filhe("Vasco do Araújo", "Chato")
        val flávio = Filhe("Fábio", "Gosta de dar com o casaco nas pessoas")
        val samurai = Daniel("Samurai", listOf(vasco, flávio))
        val doc = XMLDocument(XMLEntity(samurai))
        assertEquals("Daniel\n" +
                "├── Filhe\n" +
                "└── Filhe", doc.toTree())
    }

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