import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestXML {

    @Test
    fun testCreateEntity(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val c = XMLEntity("c", a)
        assertEquals(b.parent, a)
        assertEquals(a.childrens, listOf(b) + listOf(c))
        val doc = XMLDocument(a)
        //assertEquals(doc.addEntity("e",d.name),e)
        //assertEquals(doc.removeEntity("e"),d)
    }

    @Test
    fun testAddEntity(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val c = XMLEntity("c", a)

        val doc = XMLDocument(a)

        assertEquals("a\n" + "  |b\n" + "  |c\n", doc.toTree())
        val d = doc.addEntity("d", b.name)

        doc.addEntity("e", d!!.name)
        assertEquals("a\n" + "  |b\n" + "  |  |d\n" + "  |  |  |e\n" + "  |c", doc.toTree().trim())

    }

    @Test
    fun testRemoveEntity(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val c = XMLEntity("c", a)
        val d = XMLEntity("d", c)

        val doc = XMLDocument(a)

        doc.removeEntity(d.name)

        assertEquals("a\n" + "  |b\n" + "  |c\n", doc.toTree())
    }

    @Test
    fun testAttributes(){
        val a = XMLEntity("a")
        val b = XMLEntity("b", a)
        val b2 = XMLEntity("b", a)
        val c = XMLEntity("c", a)
        val d = XMLEntity("d", c)
        val doc = XMLDocument(a)

        assertEquals(null, b.getAttributes())
        doc.addAttribute(b.name,"código", "231")
        assertEquals(mapOf("código" to "231"), b.getAttributes())

        assertEquals(null, a.getAttributes())
        assertEquals(null, c.getAttributes())
        assertEquals(null, d.getAttributes())

        //adicionar um atributo a todas as entidades com um determinado nome

        doc.addToAllEntitiesAttribute("Cayde", "06"){entityName -> entityName == "b"}

        assertEquals(hashMapOf("Cayde" to "06"), b.getAttributes())
        assertEquals(hashMapOf("Cayde" to "06"), b2.getAttributes())

    }

    @Test
    fun testVisitors(){
        val root = XMLEntity("root")
        val child1 = XMLEntity("children", root)
        val child2 = XMLEntity("doggo", root)
        val child3 = XMLEntity("Jamija", child2)
        root.addAllAttributes(hashMapOf("ola" to "123", "bling" to "blong"))
        child3.addAttribute("Chitty Chitty", "Bang Bang")
        child1.entityPlainText = "I have lollipops!"

        val textXMLVisitor = XMLTextCollector()

        root.accept(textXMLVisitor)
        println(textXMLVisitor.getCollectedText())
    }
}